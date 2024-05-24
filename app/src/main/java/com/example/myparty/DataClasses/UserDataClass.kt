package com.example.myparty.DataClasses

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlinx.serialization.Serializable

@Serializable
data class UserDataClass(val id: String? = "", val Имя: String? = "", val Ник: String? = "", val Описание: String? = "", val Верификация: Boolean? = false, val Почта: String? = "", val Статус_проверки: String? = "", val Роль: String? = "", val Комментарий: String? = "", val id_статуса_проверки: Int? = 0, val id_роли: Int? = 0, val Фото: String? = null, val Количество_подписчиков: Int? = 0, val Статус_подписки: Boolean? = false)
