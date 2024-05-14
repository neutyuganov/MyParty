package com.example.myparty.DataClasses

import kotlinx.serialization.Serializable

@Serializable
data class UserDataClass(val id: String? = "", val Имя: String? = "", val Ник: String? = "", val Описание: String? = "", val Верификация: Boolean? = false, val Почта: String? = "", val Статус_проверки: String? = "", val Роль: String? = "", val Комментарий: String? = "")
