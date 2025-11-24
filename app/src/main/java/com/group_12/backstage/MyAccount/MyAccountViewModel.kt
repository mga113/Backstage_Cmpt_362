package com.group_12.backstage.MyAccount

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.group_12.backstage.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MyAccountViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<SettingsItem>>(emptyList())
    val items = _items.asStateFlow()

    init {
        refreshAuthStatus()
    }

    fun refreshAuthStatus() {
        val user = FirebaseAuth.getInstance().currentUser
        _items.value = buildList(user)
    }

    fun updateToggle(id: String, enabled: Boolean) {
        val updated = _items.value.map {
            if (it is SettingsItem.Switch && it.id == id) it.copy(checked = enabled) else it
        }
        _items.value = updated
    }

    private fun buildList(user: com.google.firebase.auth.FirebaseUser?): List<SettingsItem> = buildList {
        // HEADER LOGIC
        if (user == null) {
            add(SettingsItem.Header(welcomeBrand = "Backstage", showSignIn = true))
        } else {
            val name = user.email ?: "User"
            add(SettingsItem.Header(welcomeBrand = name, showSignIn = false))
        }

        add(SettingsItem.SectionTitle(title = "Notifications"))
        add(SettingsItem.Chevron(id = "my_notifications", title = "My Notifications", icon = R.drawable.ic_mail))
        add(SettingsItem.Switch(id = "receive_notifications", title = "Receive Notifications?", checked = false, icon = R.drawable.ic_notifications))

        add(SettingsItem.SectionTitle(title = "Location Settings", badge = "NEW!"))
        add(SettingsItem.ValueRow(id = "my_location", title = "My Location", value = "Vancouver, BC", icon = R.drawable.ic_location, showEdit = true))
        add(SettingsItem.ValueRow(id = "my_country", title = "My Country", value = "Canada", icon = R.drawable.ic_flag, showEdit = true))
        add(SettingsItem.Switch(id = "location_content", title = "Location Based Content", checked = false, icon = R.drawable.ic_send))

        add(SettingsItem.SectionTitle(title = "Preferences"))
        add(SettingsItem.Chevron(id = "my_favourites", title = "My Favourites", icon = R.drawable.ic_favorite))
        add(SettingsItem.Chevron(id = "saved_payments", title = "Saved Payment Methods", icon = R.drawable.ic_payment))
        
        // SIGN OUT ITEM (Only if logged in)
        if (user != null) {
             add(SettingsItem.SectionTitle(title = "Account"))
             add(SettingsItem.Chevron(id = "sign_out", title = "Sign Out", icon = R.drawable.ic_arrow_back))
        }
    }
}
