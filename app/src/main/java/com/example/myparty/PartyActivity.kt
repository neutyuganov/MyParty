package com.example.myparty

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
import com.example.myparty.ProfileOrganizator.ProfileOrganizatorActivity
import com.example.myparty.SupabaseConnection.Singleton.sb
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

class PartyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartyBinding

    var partyId: Int = 0

    lateinit var party: PartyDataClass

    var favorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получение данных об авторизованном пользователе
        val currentUserId = sb.auth.currentUserOrNull()?.id!!

        // Получение переданной id вечеринки
        partyId = intent.getIntExtra("PARTY_ID", 0)

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
                        btnRe.visibility = View.VISIBLE
                        btnBuy.visibility = View.GONE
                        star.visibility = View.GONE
                        if(localDate < LocalDate.now()) {
                            btnRe.visibility = View.GONE
                        }
                        if(party.Статус_проверки == "На проверке") {
                            btnRe.visibility = View.VISIBLE
                            status.visibility = View.VISIBLE
                            status.text = "На проверке"
                            status.setTextColor(this@PartyActivity.getColor(R.color.yellow))
                        }
                        else if(party.Статус_проверки == "Заблокировано") {
                            btnRe.visibility = View.VISIBLE
                            status.visibility = View.VISIBLE
                            comment.visibility = View.VISIBLE
                            status.text = "Заблокировано"
                            status.setTextColor(this@PartyActivity.getColor(R.color.red))
                            comment.text = party.Комментарий
                            comment.setTextColor(this@PartyActivity.getColor(R.color.red))
                        }

                    }
                    else {
                        if(localDate < LocalDate.now()) {
                            btnBuy.visibility = View.GONE
                        }
                        else {
                            val priceFormat = party.Цена
                            if(priceFormat == 0.0){
                                btnBuy.text = "Бесплатно"
                            }
                            if(priceFormat?.rem(1) == 0.0){
                                val priceInt = priceFormat.toInt()
                                btnBuy.text = "Купить проход за $priceInt ₽"
                            }
                            else{
                                btnBuy.text = "Купить проход за $priceFormat ₽"
                            }
                        }
                    }

                    btnBuy.setOnClickListener {
                        Toast.makeText(this@PartyActivity, "Вы получили проход", Toast.LENGTH_SHORT).show()
                    }

                    name.text = party.Название

                    slogan.text = party.Слоган

                    place.text = party.Город + ", " + party.Место

                    description.text = party.Описание

                    userName.text = party.Имя

                    age.text = "+" + party.Возраст

                    if (!party.Верификация!!) verify.visibility = View.GONE

                    favorite = party.Избранное!!
                    updateFavorite(favorite)

                    content.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE


                    if(party.Фото != null) {
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


                }
            }
            catch (e: Throwable){
                Log.e("Error", e.toString())
            }
        }

        binding.apply {

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
                intent.putExtra("PARTY_ID", partyId)
                startActivity(intent)
            }

            binding.star.setOnClickListener {
                lifecycleScope.launch {
                    try {
                        // После нажатия на кнопку добавления в избранное делаем ее неактивной, на время проведения операции
                        star.isEnabled = false

                        // Получение данных о статусе наличия в избранном у пользователя
                        val isFavorite = getFavoriteStatus(currentUserId, partyId)
                        // Проверка наличия в избранном
                        if (!favorite) {
                            // Проверка достоверности статуса наличия в избранном у пользователя
                            if(favorite == isFavorite){
                                sb.from("Избранные_вечеринки").insert(
                                    PartyFavoriteDataClass(
                                        id_пользователя = sb.auth.currentUserOrNull()?.id!!,
                                        id_вечеринки = party.id
                                    )
                                )
                            }
                            else{
                                Toast.makeText(this@PartyActivity, "Вечеринка уже в избранном", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Проверка достоверности статуса наличия в избранном у пользователя
                            if(favorite == isFavorite){
                                sb.from("Избранные_вечеринки").delete {
                                    filter {
                                        eq("id_пользователя", sb.auth.currentUserOrNull()?.id!!)
                                        eq("id_вечеринки", partyId)
                                    }
                                }
                            }
                            else{
                                Toast.makeText(this@PartyActivity, "Вечеринка уже не в избранном", Toast.LENGTH_SHORT).show()
                            }
                        }
                        favorite = !favorite
                        updateFavorite(favorite)

                    }catch (e: Exception){
                        Log.e("Ошибка добавления в избранное", e.message.toString())
                    }
                }
            }
        }
    }

    suspend fun loadParty(): PartyDataClass {
        val partiesResult = sb.from("Вечеринки").select(Columns.raw("*, Возрастное_ограничение(Возраст), Пользователи(Имя, Верификация), Статусы_проверки(Название)")){
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
        val comment = jsonObjectParty.getString("Комментарий")
        val image = jsonObjectParty.getString("Фото")
        val ageObject = jsonObjectParty.getJSONObject("Возрастное_ограничение")
        val age = ageObject.getInt("Возраст")
        val usersObject = jsonObjectParty.getJSONObject("Пользователи")
        val userName = usersObject.getString("Имя")
        val userVerify = usersObject.getBoolean("Верификация")
        val statusObject = jsonObjectParty.getJSONObject("Статусы_проверки")
        val status = statusObject.getString("Название")
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
            Избранное = favorite,
            Статус_проверки = status,
            Комментарий = comment,
            Фото = image
        )

        return event
    }

    fun updateFavorite(favorite: Boolean){
        binding.apply {
            if(favorite){
                star.setImageResource(R.drawable.star_big)
            }
            else{
                star.setImageResource(R.drawable.empty_star_big)
            }
            star.isEnabled = true
        }
    }

    suspend fun getFavoriteStatus(userId: String, partyId: Int): Boolean  = withContext(
        Dispatchers.IO) {
        sb.from("Избранные_вечеринки").select{
            filter {
                eq("id_пользователя", userId)
                eq("id_вечеринки", partyId)
            }
        }.decodeList<PartyDataClass>().isNotEmpty()
    }
}