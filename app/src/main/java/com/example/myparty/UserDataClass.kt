package com.example.myparty

import kotlinx.serialization.Serializable

@Serializable
data class UserDataClass(val id: String? = "", val Имя: String? = "", val Ник: String? = "", val Описание: String? = "", val Верификация: Boolean? = false, val Почта: String? = "")
