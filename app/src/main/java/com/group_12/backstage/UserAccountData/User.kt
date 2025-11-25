package com.group_12.backstage.UserAccountData

data class User(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val bio: String = "",
    val receiveNotifications: Boolean = false,
    val myLocation: String = "",
    val myCountry: String = "",
    val locationBasedContent: Boolean = false
)