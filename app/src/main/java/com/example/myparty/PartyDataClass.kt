package com.example.myparty

import kotlinx.serialization.Serializable

@Serializable
data class PartyDataClass(val id: Int? = 0, val Название: String? = "", val Слоган: String? = "", val Дата: String? = "", val Время: String? = "", val Город: String? = "", val Место: String? = "", val Описание: String? = "", val Цена: Double? = 0.0, val id_возрастного_ограничения: Int? = 0, val id_пользователя: String? = "")
