package com.example.myparty

import kotlinx.serialization.Serializable

@Serializable
data class PartyFavoriteDataClass(val id: Int? = 0, val id_пользователя: String? = "", val id_вечеринки: Int? = 0)
