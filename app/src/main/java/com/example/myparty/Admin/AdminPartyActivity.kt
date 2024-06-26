package com.example.myparty.Admin

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.R
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityAdminPartyBinding
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
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
        binding.progressBar.visibility = View.VISIBLE

        binding.btnGoBack.setOnClickListener {
            finish()
        }

        binding.btnBan.setOnClickListener  {

            // Создание диалогового окна
            val dialog  = Dialog(this@AdminPartyActivity)
            dialog.setContentView(R.layout.dialog_item_edittext)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val editText  = dialog.findViewById<EditText>(R.id.textBun)
            val btnCancel  = dialog.findViewById<Button>(R.id.btnCancel)
            val btnBun = dialog.findViewById<Button>(R.id.btnBun)

            // Закрытие окна при нажатии кнопки "Отмена"
            btnCancel.setOnClickListener  {
                dialog.dismiss()
            }

            btnBun.setOnClickListener {
                val reason = editText.text.toString().trim()
                // Проврека на наличии текста причины блокировки
                if (reason.isNotEmpty()) {

                    // Блокировка интерфейса на время обработки запроса
                    btnCancel.isEnabled = false
                    btnBun.isEnabled  = false
                    editText.isEnabled = false
                    btnBun.text  =  "Загрузка..."
                    lifecycleScope.launch {
                        try {

                            // Обновление данных вечеринки
                            sb.from("Вечеринки").update(
                                PartyDataClass(
                                    id_статуса_проверки = 2,
                                    Комментарий = reason
                                )
                            ) {
                                filter {
                                    eq("id", partyId)
                                }
                            }

                            val intent = Intent(this@AdminPartyActivity, AdminActivity::class.java)
                            startActivity(intent)
                            finishAffinity()

                        } catch (e: Throwable) {
                            Log.e("Error", e.toString())
                            Toast.makeText(this@AdminPartyActivity, "Что-то пошло не так", Toast.LENGTH_SHORT).show()

                            // Разблокировка интерфейса
                            btnCancel.isEnabled = true
                            btnBun.isEnabled  = true
                            editText.isEnabled = true
                            btnBun.text  =  "Заблокировать"
                        }
                    }
                }
                else  {
                    Toast.makeText(this@AdminPartyActivity, "Введите причину", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
        }

        binding.btnCorrect.setOnClickListener {

            // Установка загрузки и неактивности элементов
            binding.progressBar.visibility = View.VISIBLE
            binding.content.alpha = 0.62f
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            lifecycleScope.launch {
                try{

                    // Обновление данных вечеринки
                    sb.from("Вечеринки").update(PartyDataClass(id_статуса_проверки = 3, Комментарий  =  null))  {
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

                    // Отключение загрузки и переход в активное состоянеие элементов
                    binding.progressBar.visibility = View.GONE
                    binding.content.alpha = 1f
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
        val partiesResult = sb.from("Вечеринки").select {
            filter {
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


        return PartyDataClass(
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
    }
}