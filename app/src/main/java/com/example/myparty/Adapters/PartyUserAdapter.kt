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

            val inputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val localDate = LocalDate.parse(party.Дата, inputFormatterDate)
            val outputFormatterDate = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
            val formattedDate = localDate.format(outputFormatterDate)

            if(localDate.isBefore(LocalDate.now())) buttonRe.visibility = View.GONE

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