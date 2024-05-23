package com.example.myparty.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.Profile.EditPartyActivity
import com.example.myparty.PartyActivity
import com.example.myparty.R
import com.example.myparty.databinding.ItemCurrentUserPartyBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PartyUserAdapter (private val partyList: List<PartyDataClass>) : RecyclerView.Adapter<PartyUserAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemCurrentUserPartyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
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

    class ViewHolder(private val itemBinding: ItemCurrentUserPartyBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(party: PartyDataClass) { with(itemBinding)
            {
                // Форматирование даты
                val inputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val partyDate = LocalDate.parse(party.Дата, inputFormatterDate)
                val outputFormatterDate = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
                val formattedDate = partyDate.format(outputFormatterDate)

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

                // Обработка нажатия на кнопку изменения вечеринки
                buttonRe.setOnClickListener {
                    val intent = Intent(it.context, EditPartyActivity::class.java)
                    intent.putExtra("PARTY_ID", party.id)
                    it.context.startActivity(intent)
                }
            }
        }
    }

}