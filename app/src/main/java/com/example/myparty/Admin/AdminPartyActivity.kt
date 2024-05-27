package com.example.myparty.Admin

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.PartyFavoriteDataClass
import com.example.myparty.Profile.EditPartyActivity
import com.example.myparty.Profile.ProfileFragment
import com.example.myparty.ProfileOrganizator.ProfileOrganizatorActivity
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityAdminPartyBinding
import com.example.myparty.databinding.ActivityPartyBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AdminPartyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPartyBinding

    var partyId: Int = 0

    lateinit var party: PartyDataClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPartyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.content.visibility = View.GONE

        binding.btnCorrect.visibility = View.GONE
        binding.btnBan.visibility = View.GONE

        binding.btnGoBack.setOnClickListener {
            finish()
        }

        binding.btnCorrect.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.content.alpha = 0.62f
            binding.btnCorrect.visibility = View.GONE
            binding.btnBan.visibility = View.GONE
            binding.btnGoBack.visibility = View.GONE

            lifecycleScope.launch {
                try{
                    sb.from("Вечеринки").update(PartyDataClass(id_статуса_проверки = 2)) {
                        filter {
                            eq("id", partyId)
                        }
                    }

                    val intent = Intent(this@AdminPartyActivity, AdminActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
                catch (e: Throwable){
                    Log.e("Error", e.toString())
                    Toast.makeText(this@AdminPartyActivity, "Что-то пошло не так", Toast.LENGTH_SHORT).show()

                    binding.progressBar.visibility = View.GONE
                    binding.content.alpha = 1f
                    binding.btnCorrect.visibility = View.VISIBLE
                    binding.btnBan.visibility = View.VISIBLE
                    binding.btnGoBack.visibility = View.VISIBLE
                }
            }
        }

        // Получение переданной id вечеринки
        partyId = intent.getIntExtra("PARTY_ID", 0)

        lifecycleScope.launch {
            try{
                party = loadParty()
                binding.apply {
                    val formattedDate: String
                    // Форматирование даты
                    val inputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val partyDate = LocalDate.parse(party.Дата, inputFormatterDate)
                    formattedDate =
                        if(partyDate.year == LocalDate.now().year){
                            val outputFormatterDate = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
                            partyDate.format(outputFormatterDate)
                        } else{
                            val outputFormatterDate = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
                            partyDate.format(outputFormatterDate)
                        }

                    val inputFormatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
                    val localTime = LocalTime.parse(party.Время, inputFormatterTime)
                    val outputFormatterTime = DateTimeFormatter.ofPattern("HH:mm")
                    val formattedTime = localTime.format(outputFormatterTime)
                    date.text = "$formattedDate  $formattedTime"

                    name.text = party.Название

                    slogan.text = party.Слоган

                    place.text = party.Город + ", " + party.Место

                    description.text = party.Описание

                    content.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE

                    if(party.Фото != "null") {
                        progressBarImage.visibility = View.VISIBLE
                        image.scaleType = ImageView.ScaleType.CENTER_CROP
                        image.setImageDrawable(null)

                        val bucket = sb.storage["images"]
                        val bytes = bucket.downloadPublic(party.Фото.toString())
                        val is1: InputStream = ByteArrayInputStream(bytes)
                        val bmp: Bitmap = BitmapFactory.decodeStream(is1)
                        val dr = BitmapDrawable(resources, bmp)
                        image.setImageDrawable(dr)
                    }
                    progressBarImage.visibility = View.GONE
                    binding.btnCorrect.visibility = View.VISIBLE
                    binding.btnBan.visibility = View.VISIBLE
                }
            }
            catch (e: Throwable){
                Log.e("Error", e.toString())
            }
        }
    }

    suspend fun loadParty(): PartyDataClass {
        val partiesResult = sb.from("Вечеринки").select{
            filter{
                eq("id", partyId)
            }
        }.data

        val jsonObjectParty = JSONArray(partiesResult).getJSONObject(0)

        val id = jsonObjectParty.getInt("id")
        val name = jsonObjectParty.getString("Название")
        val slogan = jsonObjectParty.getString("Слоган")
        val date = jsonObjectParty.getString("Дата")
        val time = jsonObjectParty.getString("Время")
        val city = jsonObjectParty.getString("Город")
        val place = jsonObjectParty.getString("Место")
        val description = jsonObjectParty.getString("Описание")
        val price = jsonObjectParty.getDouble("Цена")
        val image = jsonObjectParty.getString("Фото")


        val event = PartyDataClass(
            id = id,
            Название = name,
            Слоган = slogan,
            Дата = date,
            Время = time,
            Город = city,
            Место = place,
            Описание = description,
            Цена = price,
            Фото = image
        )

        return event
    }
}