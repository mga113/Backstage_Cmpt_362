package com.group_12.backstage.MyAccount

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.group_12.backstage.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
            // Very simple signed-out state
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
                val email = firebaseUser?.email ?: "User"

                val receiveNotifications =
                    snapshot?.getBoolean("receiveNotifications") ?: false
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
                            welcomeBrand = email,
                            showSignIn = false
                        )
                    )

                    // Notifications section
                    add(SettingsItem.SectionTitle(title = "Notifications"))
                    add(
                        SettingsItem.Chevron(
                            id = "my_notifications",
                            title = "My Notifications",
                            icon = R.drawable.ic_mail
                        )
                    )
                    add(
                        SettingsItem.Switch(
                            id = "receive_notifications",
                            title = "Receive Notifications?",
                            checked = receiveNotifications,
                            icon = R.drawable.ic_notifications
                        )
                    )

                    // Location section
                    add(
                        SettingsItem.SectionTitle(
                            title = "Location Settings",
                            badge = "NEW!"
                        )
                    )
                    add(
                        SettingsItem.ValueRow(
                            id = "my_location",
                            title = "My Location",
                            value = myLocation,
                            icon = R.drawable.ic_location,
                            showEdit = true
                        )
                    )
                    add(
                        SettingsItem.ValueRow(
                            id = "my_country",
                            title = "My Country",
                            value = myCountry,
                            icon = R.drawable.ic_flag,
                            showEdit = true
                        )
                    )
                    add(
                        SettingsItem.Switch(
                            id = "location_content",
                            title = "Location Based Content",
                            checked = locationContent,
                            icon = R.drawable.ic_send
                        )
                    )

                    // ❌ Preferences section removed completely

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
