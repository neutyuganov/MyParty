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
import com.example.myparty.Admin.AdminPartyActivity
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


class AdminPartyAdapter(private val partyList: List<PartyDataClass>, private val coroutineScope: CoroutineScope) : RecyclerView.Adapter<AdminPartyAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemPartyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding, coroutineScope)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val party: PartyDataClass = partyList[position]
        holder.bind(party)

        // Переход на карточку вечернки при нажатии на item
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, AdminPartyActivity::class.java)
            intent.putExtra("PARTY_ID", party.id)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = partyList.size

    class ViewHolder(private val itemBinding: ItemPartyBinding, private val coroutineScope: CoroutineScope) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(party: PartyDataClass) { with(itemBinding) {

            // Форматирование даты
            val formattedDate: String
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


            userInfoContainer.visibility = View.GONE
            star.visibility = View.GONE
            age.visibility = View.GONE

            // Вывод информации о вечеринке
            name.text = party.Название

            place.text = party.Место

            val ageFormat = party.Возраст
            age.text = "+$ageFormat"

            // Преобразование цены
            val priceFormat = party.Цена
            if(priceFormat == 0.0){
                price.text = "Бесплатно"
            }
            else if(priceFormat?.rem(1) == 0.0){
                val priceInt = priceFormat.toInt()
                price.text = "$priceInt ₽"
            }
            else{
                price.text = "$priceFormat ₽"
            }

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
            }
        }
    }
}