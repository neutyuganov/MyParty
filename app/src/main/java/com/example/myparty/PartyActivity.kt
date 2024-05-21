package com.example.myparty

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.PartyFavoriteDataClass
import com.example.myparty.Profile.EditPartyActivity
import com.example.myparty.ProfileOrganizator.ProfileOrganizatorActivity
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityPartyBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PartyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartyBinding

    var partyId: Int = 0

    lateinit var party: PartyDataClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        partyId = intent.getIntExtra("PARTY_ID", 0)

        binding.content.visibility = View.GONE

        binding.btnGoBack.setOnClickListener {
            finish()
        }

        binding.userName.setOnClickListener {
            if(party.id_пользователя != sb.auth.currentUserOrNull()?.id!!) {
                val intent = Intent(it.context, ProfileOrganizatorActivity::class.java)
                intent.putExtra("USER_ID", party.id_пользователя)
                it.context.startActivity(intent)
            }
        }

        binding.btnRe.setOnClickListener {
            val intent = Intent(this@PartyActivity, EditPartyActivity::class.java)
            intent.putExtra("PARTY_ID", party.id)
            startActivity(intent)
        }

        binding.favorite.setOnClickListener {
            if (party.Избранное!!) {
                party.Избранное = false
                binding.favorite.setImageResource(R.drawable.empty_star_big)
                lifecycleScope.launch {
                    try{
                        sb.from("Избранные_вечеринки").delete {
                            filter {
                                eq("id_пользователя", sb.auth.currentUserOrNull()?.id!!)
                                eq("id_вечеринки", party.id!!)
                            }
                        }
                    } catch (e: Throwable) {
                        Log.e("Exception", e.toString())
                    }
                }
            }
            else {
                party.Избранное = true
                binding.favorite.setImageResource(R.drawable.star_big)

                lifecycleScope.launch {
                    try {
                        sb.from("Избранные_вечеринки").insert(
                            PartyFavoriteDataClass(
                                id_пользователя = sb.auth.currentUserOrNull()?.id!!,
                                id_вечеринки = party.id!!
                            )
                        )
                    } catch (e: Throwable) {
                        Log.e("Exception", e.toString())
                    }
                }
            }
        }

        lifecycleScope.launch {
            try{
                party = loadParty()
                binding.apply {
                    val inputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val localDate = LocalDate.parse(party.Дата, inputFormatterDate)
                    val outputFormatterDate = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
                    val formattedDate = localDate.format(outputFormatterDate)

                    val inputFormatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
                    val localTime = LocalTime.parse(party.Время, inputFormatterTime)
                    val outputFormatterTime = DateTimeFormatter.ofPattern("HH:mm")
                    val formattedTime = localTime.format(outputFormatterTime)
                    date.text = "$formattedDate  $formattedTime"

                    if(party.id_пользователя == sb.auth.currentUserOrNull()?.id!!) {
                        binding.btnRe.visibility = View.VISIBLE
                        binding.btnBuy.visibility = View.GONE
                        binding.favorite.visibility = View.GONE
                        if(localDate < LocalDate.now()) {
                            binding.btnRe.visibility = View.GONE
                        }
                    }
                    else {
                        if(localDate < LocalDate.now()) {
                            btnBuy.visibility = View.GONE
                        }
                        else {
                            val priceFormat = party.Цена
                            if(priceFormat?.rem(1) == 0.0){
                                val priceInt = priceFormat.toInt()
                                btnBuy.text = "Купить проход от $priceInt ₽"
                            }
                            else{
                                btnBuy.text = "Купить проход от $priceFormat ₽"
                            }
                        }
                    }

                    btnBuy.setOnClickListener {
                        Toast.makeText(this@PartyActivity, "Вы купили проход", Toast.LENGTH_SHORT).show()
                    }

                    name.text = party.Название

                    slogan.text = party.Слоган

                    place.text = party.Город + ", " + party.Место

                    description.text = party.Описание

                    userName.text = party.Имя

                    age.text = "+" + party.Возраст

                    if (!party.Верификация!!) verify.visibility = View.GONE

                    if(party.Избранное == true) {
                        favorite.setImageResource(R.drawable.star_big)
                    }
                    else favorite.setImageResource(R.drawable.empty_star_big)

                    content.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            }
            catch (e: Throwable){
                Log.e("Error", e.toString())
            }
        }
    }

    suspend fun loadParty(): PartyDataClass {
        val partiesResult = sb.from("Вечеринки").select(Columns.raw("*, Возрастное_ограничение(Возраст), Пользователи(Имя, Верификация)")){
            filter{
               eq("id", partyId)
            }
        }.data
        val jsonObjectParty = JSONArray(partiesResult).getJSONObject(0)

        val partiesFavoritesResult = sb.from("Избранные_вечеринки").select() {
            filter {
                eq("id_пользователя", sb.auth.currentUserOrNull()?.id.toString())
            }
        }.data
        val jsonArrayFavorites = JSONArray(partiesFavoritesResult)

        val id = jsonObjectParty.getInt("id")
        val name = jsonObjectParty.getString("Название")
        val slogan = jsonObjectParty.getString("Слоган")
        val date = jsonObjectParty.getString("Дата")
        val time = jsonObjectParty.getString("Время")
        val city = jsonObjectParty.getString("Город")
        val place = jsonObjectParty.getString("Место")
        val description = jsonObjectParty.getString("Описание")
        val price = jsonObjectParty.getDouble("Цена")
        val userId = jsonObjectParty.getString("id_пользователя")
        val ageObject = jsonObjectParty.getJSONObject("Возрастное_ограничение")
        val age = ageObject.getInt("Возраст")
        val usersObject = jsonObjectParty.getJSONObject("Пользователи")
        val userName = usersObject.getString("Имя")
        val userVerify = usersObject.getBoolean("Верификация")
        var favorite = false
        for (j in 0 until jsonArrayFavorites.length()) {
            val jsonObjectFavorites = jsonArrayFavorites.getJSONObject(j)
            if (jsonObjectFavorites.getInt("id_вечеринки") == id) {
                favorite = true
            }
        }

        val event = PartyDataClass(
            id = id,
            Название = name,
            Слоган = slogan,
            id_пользователя = userId,
            Имя = userName,
            Дата = date,
            Время = time,
            Город = city,
            Место = place,
            Описание = description,
            Цена = price,
            Возраст = age,
            Верификация = userVerify,
            Избранное = favorite
        )

        return event
    }
}