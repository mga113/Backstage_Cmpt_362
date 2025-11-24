package com.group_12.backstage.MyAccount

interface MyAccountNavigator {
    fun onSignInClicked()
    fun onSignOutClicked() // New method
    fun onChevronClicked(id: String)
    fun onSwitchChanged(id: String, enabled: Boolean)
    fun onEditClicked(id: String)
}
