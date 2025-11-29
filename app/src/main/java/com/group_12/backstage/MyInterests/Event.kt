package com.group_12.backstage.MyInterests

data class Event(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val status: String = "interested", // "interested" or "going"
    val ticketUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
