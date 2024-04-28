package com.example.myparty

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.databinding.MainRecyclerViewItemBinding
import io.github.jan.supabase.postgrest.from
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class PartyAdapter(private val partyList: List<PartyDataClass>) : RecyclerView.Adapter<PartyAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = MainRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val party: PartyDataClass = partyList[position]
        holder.bind(party)
    }

    override fun getItemCount(): Int = partyList.size

    class ViewHolder(private val itemBinding: MainRecyclerViewItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(party: PartyDataClass) { with(itemBinding) {
                name.text = party.Название
                userName.text = party.Имя
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

                verify.isVisible = party.Верификация == true

                val inputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val localDate = LocalDate.parse(party.Дата, inputFormatterDate)
                val outputFormatterDate = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
                val formattedDate = localDate.format(outputFormatterDate)

                val inputFormatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
                val localTime = LocalTime.parse(party.Время, inputFormatterTime)
                val outputFormatterTime = DateTimeFormatter.ofPattern("HH:mm")
                val formattedTime = localTime.format(outputFormatterTime)
                date.text = "$formattedDate  $formattedTime"

                place.text = party.Место
            }
        }
    }
}