package com.example.myparty

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.UserData
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityPartyBinding
import com.example.myparty.databinding.ActivityProfileOrganizatorBinding
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class ProfileOrganizatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileOrganizatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileOrganizatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("USER_ID")
        var user: UserDataClass? = null

        try{
            lifecycleScope.launch {
                user = sb.from("Пользователи").select{
                    filter{
                        eq("id", userId!!)
                    }
                }.decodeSingle()
            }
            Toast.makeText(applicationContext, user!!.Имя.toString(), Toast.LENGTH_SHORT).show()
        }
        catch(e: Exception){
            Log.e("Ошибка при получении данных вечеринки", e.message.toString())
        }
    }
}