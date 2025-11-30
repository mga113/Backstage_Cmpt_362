package com.group_12.backstage.MyAccount

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.group_12.backstage.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class MyAccountViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _items = MutableStateFlow<List<SettingsItem>>(emptyList())
    val items = _items.asStateFlow()

    init {
        refreshAuthStatus()
    }

    fun refreshAuthStatus() {
        val user = auth.currentUser
        if (user == null) {
            _items.value = listOf(
                SettingsItem.Header(
                    welcomeBrand = "Backstage",
                    showSignIn = true
                )
            )
        } else {
            listenToUserSettings(user.uid)
        }
    }

    private fun listenToUserSettings(uid: String) {
        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                val firebaseUser = auth.currentUser
                val displayName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                val greetingName = displayName ?: firebaseUser?.email ?: "User"
                val profileImageUrl = firebaseUser?.photoUrl?.toString()

                val receiveNotifications =
                    snapshot?.getBoolean("receiveNotifications") ?: false
                //TODO: we want below information to be retrieved from user's current location, if they give access
                val myLocation =
                    snapshot?.getString("myLocation") ?: "Vancouver, BC"
                val myCountry =
                    snapshot?.getString("myCountry") ?: "Canada"

                val locationContent =
                    snapshot?.getBoolean("locationBasedContent") ?: false

                _items.value = buildList {
                    // Header
                    add(
                        SettingsItem.Header(
                            welcomeBrand = greetingName,
                            showSignIn = false,
                            profileImageUrl = profileImageUrl
                        )
                    )

                    // Notifications section
                    add(SettingsItem.SectionTitle(title = "Notifications"))

                    add(
                        SettingsItem.Switch(
                            id = "receive_notifications",
                            title = "Receive Notifications?",
                            checked = receiveNotifications,
                            icon = R.drawable.ic_notifications
                        )
                    )

                    // Location toggle button (cityAndState and country are the fields that we will extract from user's current location and save in db)
                    add(
                        SettingsItem.SectionTitle(
                            title = "Location Settings",
                            badge = "NEW!"
                        )
                    )

                    add(
                        SettingsItem.Switch(
                            id = "location_content",
                            title = "Location Based Content",
                            checked = locationContent,
                            icon = R.drawable.ic_location
                        )
                    )

                    // Account section
                    add(SettingsItem.SectionTitle(title = "Account"))
                    add(
                        SettingsItem.Chevron(
                            id = "sign_out",
                            title = "Sign Out",
                            icon = R.drawable.ic_arrow_back
                        )
                    )
                }
            }
    }

    // ---  FUNCTIONS FOR HANDLING THE PROFILE IMAGE for uploading ---
    private val _uploadProgress = MutableStateFlow(false)
    val uploadProgress = _uploadProgress.asStateFlow()
    private var tempImageUri: Uri? = null

    fun createTempImageUri(context: Context): Uri? {
        val file = File.createTempFile("temp_profile_image", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        val authority = "com.group_12.backstage.provider" // Use your app's package name
        tempImageUri = FileProvider.getUriForFile(context, authority, file)
        return tempImageUri
    }

    fun uploadProfileImage(uri: Uri) {
        _uploadProgress.value = true
        val user = auth.currentUser ?: run { _uploadProgress.value = false; return }
        // Firebase Storage library is separate from Auth and Firestore
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")

        storageRef.putFile(uri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                updateUserProfileUrl(downloadUri)
            }
        }.addOnFailureListener {
            _uploadProgress.value = false
        }
    }

    private fun updateUserProfileUrl(uri: Uri) {
        val user = auth.currentUser!!
        val profileUpdates = userProfileChangeRequest { photoUri = uri }

        user.updateProfile(profileUpdates).addOnCompleteListener {
            if (it.isSuccessful) {
                db.collection("users").document(user.uid)
                    .update("profileImageUrl", uri.toString())
                    .addOnCompleteListener { firestoreUpdateTask ->
                        _uploadProgress.value = false
                        // After everything is saved, refresh the data to update the UI.
                        // This will re-fetch the user and their new photoUrl.
                        if (firestoreUpdateTask.isSuccessful) {
                            refreshAuthStatus()
                        }
                    }
            }
            else {
                _uploadProgress.value = false
            }
        }
    }

    // Called from MyAccountFragment when a switch is toggled
    fun updateToggle(id: String, enabled: Boolean) {
        // Update local list so UI responds instantly
        _items.value = _items.value.map {
            if (it is SettingsItem.Switch && it.id == id) it.copy(checked = enabled) else it
        }

        // Persist to Firestore
        val uid = auth.currentUser?.uid ?: return
        val field = when (id) {
            "receive_notifications" -> "receiveNotifications"
            "location_content" -> "locationBasedContent"
            else -> null
        } ?: return

        db.collection("users").document(uid).update(field, enabled)
    }

    // Used by the edit dialog to get the current text
    fun getCurrentValue(id: String): String? {
        return _items.value
            .firstOrNull { it is SettingsItem.ValueRow && it.id == id }
            ?.let { (it as SettingsItem.ValueRow).value }
    }

    // Called from MyAccountFragment when user saves new value
    fun updateValueRow(id: String, newValue: String) {
        // Update local list
        _items.value = _items.value.map {
            if (it is SettingsItem.ValueRow && it.id == id) it.copy(value = newValue) else it
        }

        // Persist to Firestore
        val uid = auth.currentUser?.uid ?: return
        val field = when (id) {
            "my_location" -> "myLocation"
            "my_country" -> "myCountry"
            else -> null
        } ?: return

        db.collection("users").document(uid).update(field, newValue)
    }
}