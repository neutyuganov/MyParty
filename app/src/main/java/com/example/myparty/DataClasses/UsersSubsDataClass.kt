package com.example.myparty.DataClasses

import kotlinx.serialization.Serializable

@Serializable
data class UsersSubsDataClass(val id: Int? = 0, val id_пользователя: String? = "", val id_подписчика: String? = "")
