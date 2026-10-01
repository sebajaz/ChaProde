package com.chaprode.mobile.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val rol: String,
    val createdAt: String? = null
)
