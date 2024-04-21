package com.example.myparty

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.databinding.MainRecyclerViewItemBinding
import io.github.jan.supabase.postgrest.from


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
        fun bind(party: PartyDataClass) {

            itemBinding.name.text = party.Название
            itemBinding.userName.text = party.id_пользователя
        }
    }
}