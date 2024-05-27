package com.rm.loginappcompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val name: String? = null,
    val email: String? = null,
    val picture: String? = null,
    val mongoAccessId: String? = null
)
