package com.example.myparty

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityPartyBinding
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class PartyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val partyId = intent.getIntExtra("PARTY_ID", 0)
        var party: PartyDataClass? = null

        try{
            lifecycleScope.launch {
                party = sb.from("Вечеринки").select{
                    filter{
                        eq("id", partyId)
                    }
                }.decodeSingle()
            }
        }
        catch(e: Exception){
            Log.e("Ошибка при получении данных вечеринки", e.message.toString())
        }
    }
}