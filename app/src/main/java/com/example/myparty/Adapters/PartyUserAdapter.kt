package com.example.myparty.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.databinding.UsersPartyItemBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PartyUserAdapter (private val partyList: List<PartyDataClass>) : RecyclerView.Adapter<PartyUserAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = UsersPartyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun getItemCount(): Int = partyList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val party: PartyDataClass = partyList[position]
        holder.bind(party)
    }

    class ViewHolder(private val itemBinding: UsersPartyItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(party: PartyDataClass) { with(itemBinding) {
            name.text = party.Название
            val ageFormat = party.Возраст

            age.text = "+$ageFormat"

            val priceFormat = party.Цена
            if(priceFormat?.rem(1) == 0.0){
                val priceInt = priceFormat.toInt()
                price.text = "от $priceInt ₽"
            }
            else{
                price.text = "от $priceFormat ₽"
            }

            if(party.Статус_проверки == "На проверке"){
                status.visibility = View.VISIBLE
            }

            val inputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val localDate = LocalDate.parse(party.Дата, inputFormatterDate)
            val outputFormatterDate = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
            val formattedDate = localDate.format(outputFormatterDate)

            if(localDate.isBefore(LocalDate.now())){
                buttonRe.visibility = View.GONE
                name.alpha = 0.82f
                age.alpha = 0.82f
                price.alpha = 0.82f
                date.alpha = 0.82f
                place.alpha = 0.82f
                image.alpha = 0.82f
                background.alpha = 0.82f
            }

            val inputFormatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
            val localTime = LocalTime.parse(party.Время, inputFormatterTime)
            val outputFormatterTime = DateTimeFormatter.ofPattern("HH:mm")
            val formattedTime = localTime.format(outputFormatterTime)
            date.text = "$formattedDate  $formattedTime"

            place.text = party.Место

            if(party.Статус_проверки == "Заблокировано"){
                buttonRe.visibility = View.GONE
                name.alpha = 0.7f
                age.alpha = 0.70f
                price.alpha = 0.70f
                date.alpha = 0.70f
                place.alpha = 0.70f
                image.alpha = 0.70f
                background.alpha = 0.70f
            }
        }
        }
    }

}