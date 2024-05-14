package com.example.myparty.Adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.PartyActivity
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.PartyFavoriteDataClass
import com.example.myparty.R
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.MainRecyclerViewItemBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class PartyAdapter(private val partyList: List<PartyDataClass>, private val coroutineScope: CoroutineScope) : RecyclerView.Adapter<PartyAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = MainRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding, coroutineScope)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val party: PartyDataClass = partyList[position]
        holder.bind(party)

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, PartyActivity::class.java)
            intent.putExtra("PARTY_ID", party.id)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = partyList.size

    class ViewHolder(private val itemBinding: MainRecyclerViewItemBinding, private val coroutineScope: CoroutineScope) : RecyclerView.ViewHolder(itemBinding.root) {
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

            if(party.Избранное == true){
                star.setImageResource(R.drawable.star)
            }

            try {
                star.setOnClickListener {
                    if (party.Избранное!!) {
                        party.Избранное = false
                        itemBinding.star.setImageResource(R.drawable.empty_star)
                        coroutineScope.launch {
                            sb.from("Избранные_вечеринки").delete {
                                filter {
                                    eq("id_пользователя", sb.auth.currentUserOrNull()?.id!!)
                                    eq("id_вечеринки", party.id!!)
                                }
                            }
                        }
                    } else {
                        party.Избранное = true
                        itemBinding.star.setImageResource(R.drawable.star)
                        coroutineScope.launch {
                            sb.from("Избранные_вечеринки").insert(
                                PartyFavoriteDataClass(
                                    id_пользователя = sb.auth.currentUserOrNull()?.id!!,
                                    id_вечеринки = party.id!!
                                )
                            )
                        }
                    }
                }
            }catch (e: Exception){
                Log.e("Ошибка добавления в избранное", e.message.toString())
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