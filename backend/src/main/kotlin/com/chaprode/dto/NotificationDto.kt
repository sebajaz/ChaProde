package com.chaprode.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenRequest(
    val fcmToken: String,
    val plataforma: String = "ANDROID"
)

@Serializable
data class NotificationAlertDto(
    val id: String,
    val titulo: String,
    val mensaje: String,
    val tipo: String, // "REMINDER", "POINTS", "LEAGUE"
    val leido: Boolean = false,
    val createdAt: String
)
