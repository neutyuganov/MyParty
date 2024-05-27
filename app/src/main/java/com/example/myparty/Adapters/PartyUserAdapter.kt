package com.example.myparty.Adapters

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.Profile.EditPartyActivity
import com.example.myparty.PartyActivity
import com.example.myparty.R
import com.example.myparty.SupabaseConnection
import com.example.myparty.databinding.ItemCurrentUserPartyBinding
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PartyUserAdapter (private val partyList: List<PartyDataClass>, private val coroutineScope: CoroutineScope) : RecyclerView.Adapter<PartyUserAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemCurrentUserPartyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding, coroutineScope)
    }

    override fun getItemCount(): Int = partyList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val party: PartyDataClass = partyList[position]
        holder.bind(party)

        // Обработка нажатия на item
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, PartyActivity::class.java)
            intent.putExtra("PARTY_ID", party.id)
            it.context.startActivity(intent)
        }
    }

    class ViewHolder(private val itemBinding: ItemCurrentUserPartyBinding, private val coroutineScope: CoroutineScope) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(party: PartyDataClass) { with(itemBinding)
            {
                // Форматирование даты
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

                // Проверка времени на вечеринку
                if(partyDate.isBefore(LocalDate.now())){
                    // Если дата вечеринки меньше текущей даты, то затемняем item и скрываем кнопку изменения вечеринки
                    content.alpha = 0.62f
                    buttonRe.visibility = View.GONE
                }

                // Проверка статуса проверки вечеринки
                if(party.Статус_проверки == "Заблокировано" ){
                    // Если вечеринка заблокирована, то затемняем item
                    content.alpha = 0.62f
                    status.visibility = View.VISIBLE
                    status.text = "Заблокировано"
                    status.setTextColor(itemBinding.root.context.getColor(R.color.red))
                    buttonRe.visibility = View.VISIBLE
                }
                else if(party.Статус_проверки == "На проверке"){
                    // Если вечеринка на проверке, то выводим статус На проверке
                    status.visibility = View.VISIBLE
                    buttonRe.visibility = View.VISIBLE
                }

                // Вывод информации о вечеринке
                name.text = party.Название

                val ageFormat = party.Возраст
                age.text = "+$ageFormat"

                place.text = party.Место

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

                        val bucket = SupabaseConnection.Singleton.sb.storage["images"]
                        val bytes = bucket.downloadPublic(party.Фото.toString())
                        val is1: InputStream = ByteArrayInputStream(bytes)
                        val bmp: Bitmap = BitmapFactory.decodeStream(is1)
                        val dr = BitmapDrawable(itemBinding.root.context.resources, bmp)
                        image.setImageDrawable(dr)
                    }
                    progressBarImage.visibility = View.GONE
                }

                // Обработка нажатия на кнопку изменения вечеринки
                buttonRe.setOnClickListener {
                    val intent = Intent(it.context, EditPartyActivity::class.java)
                    intent.putExtra("PARTY_ID", party.id)
                    it.context.startActivity(intent)
                }

                status.setOnClickListener {
                    if(party.Статус_проверки == "На проверке"){
                        val balloon = Balloon.Builder(it.context)
                            .setWidth(BalloonSizeSpec.WRAP)
                            .setHeight(BalloonSizeSpec.WRAP)
                            .setText("Подождите, пока модераторы\nзавершат проверку")
                            .setTextColorResource(R.color.main_text_color)
                            .setTextSize(12f)
                            .setMarginHorizontal(10)
                            .setTextTypeface(it.context.resources.getFont(R.font.rubik_medium))
                            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                            .setArrowSize(7)
                            .setPaddingVertical(4)
                            .setPaddingHorizontal(8)
                            .setCornerRadius(10f)
                            .setBackgroundColorResource(R.color.stroke_color)
                            .setBalloonAnimation(BalloonAnimation.FADE)
                            .build()

                        coroutineScope.launch {
                            balloon.showAlignTop(status)
                        }
                    }
                    else{
                        val balloon = Balloon.Builder(it.context)
                            .setWidth(BalloonSizeSpec.WRAP)
                            .setHeight(BalloonSizeSpec.WRAP)
                            .setText("Причина: " + party.Комментарий.toString())
                            .setTextColorResource(R.color.main_text_color)
                            .setTextSize(12f)
                            .setMarginHorizontal(10)
                            .setTextTypeface(it.context.resources.getFont(R.font.rubik_medium))
                            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                            .setArrowSize(7)
                            .setPaddingVertical(4)
                            .setPaddingHorizontal(8)
                            .setCornerRadius(10f)
                            .setBackgroundColorResource(R.color.stroke_color)
                            .setBalloonAnimation(BalloonAnimation.FADE)
                            .build()

                        coroutineScope.launch {
                            balloon.showAlignTop(status)
                        }
                    }
                }
            }
        }
    }

}