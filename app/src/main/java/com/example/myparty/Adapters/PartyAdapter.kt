package com.example.myparty.Adapters

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.PartyActivity
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.PartyFavoriteDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.ProfileOrganizator.ProfileOrganizatorActivity
import com.example.myparty.R
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ItemPartyBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class PartyAdapter(private val partyList: List<PartyDataClass>, private val coroutineScope: CoroutineScope) : RecyclerView.Adapter<PartyAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemPartyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding, coroutineScope)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val party: PartyDataClass = partyList[position]
        holder.bind(party)


        // Переход на карточку вечернки при нажатии на item
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, PartyActivity::class.java)
            intent.putExtra("PARTY_ID", party.id)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = partyList.size

    class ViewHolder(private val itemBinding: ItemPartyBinding, private val coroutineScope: CoroutineScope) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(party: PartyDataClass) { with(itemBinding) {
            // Получение данных об авторизованном пользователе
            val currentUserId = sb.auth.currentUserOrNull()?.id!!
            val partyId = party.id!!

            val formattedDate: String
            // Форматирование даты
            val inputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val partyDate = LocalDate.parse(party.Дата, inputFormatterDate)
            formattedDate =
                if(partyDate.year == LocalDate.now().year){
                val outputFormatterDate = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
                partyDate.format(outputFormatterDate)
            } else{
                val outputFormatterDate = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
                partyDate.format(outputFormatterDate)
            }

            // Форматирование времени
            val inputFormatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
            val partyTime = LocalTime.parse(party.Время, inputFormatterTime)
            val outputFormatterTime = DateTimeFormatter.ofPattern("HH:mm")
            val formattedTime = partyTime.format(outputFormatterTime)
            date.text = "$formattedDate  $formattedTime"

            // Проверка наличия текста в поле Имя
            if(party.Имя == "") {
                // Если поле Имя пустое, то значит item выводится на экран профиля, где Имя не указывается
                userInfoContainer.visibility = View.GONE
            }

            // Сверка авторизованного пользователя и текущего пользователя в списке
            if(party.id_пользователя == currentUserId) {
                // Если авторизованный пользователь и текущий пользователь в списке совпадают, то убрать кнопку добавления в избранное
                star.visibility = View.GONE
            }

            // Проверка времени на вечеринку
            if(partyDate.isBefore(LocalDate.now())){
                // Если дата вечеринки меньше текущей даты, то затемняем item
                content.alpha = 0.62f
            }

            // Вывод информации о вечеринке
            name.text = party.Название

            userName.text = party.Имя

            verify.isVisible = party.Верификация == true

            place.text = party.Место

            val ageFormat = party.Возраст
            age.text = "+$ageFormat"

            // Преобразование цены
            val priceFormat = party.Цена!!
            if(priceFormat == 0.0 || priceFormat <= 0.001){
                price.text = "Бесплатно"
            }
            else if(priceFormat?.rem(1) == 0.0){
                val priceInt = priceFormat.toInt()
                price.text = "$priceInt ₽"
            }
            else{
                price.text = "$priceFormat ₽"
            }

            // Проверка наличия избранного
            var favorite = party.Избранное!!
            updateFavorite(favorite)

            Log.d("party", party.Фото.toString())

            coroutineScope.launch {
                if(party.Фото != "null") {
                    image.scaleType = ImageView.ScaleType.CENTER_CROP
                    image.setImageDrawable(null)

                    val bucket = sb.storage["images"]
                    val bytes = bucket.downloadPublic(party.Фото.toString())
                    val is1: InputStream = ByteArrayInputStream(bytes)
                    val bmp: Bitmap = BitmapFactory.decodeStream(is1)
                    val dr = BitmapDrawable(itemBinding.root.context.resources, bmp)
                    image.setImageDrawable(dr)
                }
                progressBarImage.visibility = View.GONE
            }

            star.setOnClickListener {

                coroutineScope.launch {
                    try {
                        // После нажатия на кнопку добавления в избранное делаем ее неактивной, на время проведения операции
                        star.isEnabled = false
                        star.visibility = View.INVISIBLE
                        progressBarFavorite.visibility = View.VISIBLE
                        // Получение данных о статусе наличия в избранном у пользователя
                        val isFavorite = getFavoriteStatus(currentUserId, partyId)
                        // Проверка наличия в избранном
                        if (!favorite) {
                            // Проверка достоверности статуса наличия в избранном у пользователя
                            if(favorite == isFavorite){
                                sb.from("Избранные_вечеринки").insert(
                                    PartyFavoriteDataClass(
                                        id_пользователя = sb.auth.currentUserOrNull()?.id!!,
                                        id_вечеринки = party.id
                                    )
                                )
                            }
                            else{
                                Toast.makeText(itemBinding.root.context, "Вечеринка уже в избранном", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Проверка достоверности статуса наличия в избранном у пользователя
                            if(favorite == isFavorite){
                                sb.from("Избранные_вечеринки").delete {
                                    filter {
                                        eq("id_пользователя", sb.auth.currentUserOrNull()?.id!!)
                                        eq("id_вечеринки", party.id)
                                    }
                                }
                            }
                            else{
                                Toast.makeText(itemBinding.root.context, "Вечеринка уже не в избранном", Toast.LENGTH_SHORT).show()
                            }
                        }
                        favorite = !favorite
                        updateFavorite(favorite)

                        star.visibility = View.VISIBLE
                        progressBarFavorite.visibility = View.GONE

                    }catch (e: Exception){
                        Log.e("Ошибка добавления в избранное", e.message.toString())
                        Toast.makeText(it.context, "Что-то пошло не так", Toast.LENGTH_SHORT).show()

                        star.isEnabled = true
                        star.visibility = View.VISIBLE
                        progressBarFavorite.visibility = View.GONE
                    }
                }
            }

            userName.setOnClickListener {
                if(party.id_пользователя != sb.auth.currentUserOrNull()?.id!!) {
                    val intent = Intent(it.context, ProfileOrganizatorActivity::class.java)
                    intent.putExtra("USER_ID", party.id_пользователя)
                    it.context.startActivity(intent)
                }
            }

            }
        }
        suspend fun getFavoriteStatus(userId: String, partyId: Int): Boolean  = withContext(
            Dispatchers.IO) {
            sb.from("Избранные_вечеринки").select{
                filter {
                    eq("id_пользователя", userId)
                    eq("id_вечеринки", partyId)
                }
            }.decodeList<PartyDataClass>().isNotEmpty()
        }

        fun updateFavorite(favorite: Boolean){
            if(favorite){
                itemBinding.star.setImageResource(R.drawable.star)
            }
            else{
                itemBinding.star.setImageResource(R.drawable.empty_star)
            }
            itemBinding.star.isEnabled = true
        }
    }

}