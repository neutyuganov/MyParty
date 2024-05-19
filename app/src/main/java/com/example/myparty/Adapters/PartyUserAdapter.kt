package com.example.myparty.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.EditPartyActivity
import com.example.myparty.PartyActivity
import com.example.myparty.ProfileOrganizatorActivity
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.UsersPartyItemBinding
import io.github.jan.supabase.gotrue.auth
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

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, PartyActivity::class.java)
            intent.putExtra("PARTY_ID", party.id)
            it.context.startActivity(intent)
        }
    }

    class ViewHolder(private val itemBinding: UsersPartyItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(party: PartyDataClass) { with(itemBinding)
            {
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

                buttonRe.setOnClickListener {
                    val intent = Intent(it.context, EditPartyActivity::class.java)
                    intent.putExtra("PARTY_ID", party.id)
                    it.context.startActivity(intent)
                }

                val inputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val localDate = LocalDate.parse(party.Дата, inputFormatterDate)
                val outputFormatterDate = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
                val formattedDate = localDate.format(outputFormatterDate)

                if(party.id_пользователя != sb.auth.currentUserOrNull()?.id){
                    buttonRe.visibility = View.GONE
                }
                if(localDate.isBefore(LocalDate.now()) || party.Статус_проверки == "Заблокировано" ){
                    content.alpha = 0.82f
                    buttonRe.visibility = View.GONE
                }


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