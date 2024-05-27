package com.example.myparty.Profile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.MainActivity
import com.example.myparty.R
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityEditPartyBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class EditPartyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPartyBinding

    var partyId: Int = 0

    var image: ByteArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPartyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получение переданной id вечеринки
        partyId = intent.getIntExtra("PARTY_ID", 0)

        binding.progressBar.visibility = View.VISIBLE
        binding.content.visibility = View.GONE
        binding.btnSave.visibility = View.INVISIBLE

        focusedListener(binding.containerName, binding.textName)
        focusedListener(binding.containerDate, binding.textDate)
        focusedListener(binding.containerTime, binding.textTime)
        focusedListener(binding.containerDescription, binding.textDescription)
        focusedListener(binding.containerSlogan, binding.textSlogan)
        focusedListener(binding.containerCity, binding.textCity)
        focusedListener(binding.containerPlace, binding.textPlace)


        binding.btnGoBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            takeHelperText(binding.containerName, binding.textName)
            takeHelperText(binding.containerDate, binding.textDate)
            takeHelperText(binding.containerTime, binding.textTime)
            takeHelperText(binding.containerDescription, binding.textDescription)
            takeHelperText(binding.containerSlogan, binding.textSlogan)
            takeHelperText(binding.containerCity, binding.textCity)
            takeHelperText(binding.containerPlace, binding.textPlace)
            takeHelperText(binding.containerPrice, binding.textPrice)

            Log.e("TAG", image.toString())

            if(binding.containerName.helperText == null && binding.containerDate.helperText == null && binding.containerTime.helperText == null && binding.containerDescription.helperText == null && binding.containerSlogan.helperText == null && binding.containerCity.helperText == null && binding.containerPlace.helperText == null && binding.containerPrice.helperText == null && image!= null){

                binding.btnSave.isEnabled = false
                binding.content.alpha = 0.62f
                binding.progressBar.visibility = View.VISIBLE
                binding.btnGoBack.visibility = View.INVISIBLE

                lifecycleScope.launch {
                    try{
                        val imageId = sb.postgrest["Вечеринки"].select {
                            filter {
                                eq("id", partyId)
                            }
                        }.decodeSingle<PartyDataClass>().Фото

                        val bucket = sb.storage.from("images")
                        bucket.upload(imageId.toString(), image!!, upsert = true)

                        val date = binding.textDate.text.toString().split('.')
                        val currentDate = LocalDate.of(date[2].toInt(), date[1].toInt(), date[0].toInt())

                        val partyData = PartyDataClass(Название = binding.textName.text.toString(), Дата = currentDate.toString(), Время = binding.textTime.text.toString(), Описание = binding.textDescription.text.toString(), Слоган = binding.textSlogan.text.toString(), Город = binding.textCity.text.toString(), Место = binding.textPlace.text.toString(), id_статуса_проверки = 1)
                        sb.postgrest["Вечеринки"].update(partyData){
                            filter{
                                eq("id", partyId)
                            }
                        }
                        val intent = Intent(this@EditPartyActivity, MainActivity::class.java)
                        val fragment = ProfileFragment()
                        intent.putExtra("FRAGMENT", fragment.javaClass.name)
                        startActivity(intent)
                        finish()
                    }
                    catch (e: Throwable){
                        Log.e("TAG", e.toString())
                        binding.btnSave.isEnabled = true
                        binding.content.alpha = 1f
                        binding.progressBar.visibility = View.GONE
                        binding.btnGoBack.visibility = View.VISIBLE
                        Toast.makeText(this@EditPartyActivity, "Что-то пошло не так", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }

        lifecycleScope.launch {
            try {
                val party = loadParty(partyId)
                binding.apply {
                    val inputFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val localDate = LocalDate.parse(party.Дата, inputFormatterDate)
                    val outputFormatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    val formattedDate = localDate.format(outputFormatterDate)
                    textDate.setText(formattedDate)

                    val inputFormatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
                    val localTime = LocalTime.parse(party.Время, inputFormatterTime)
                    val outputFormatterTime = DateTimeFormatter.ofPattern("HH:mm")
                    val formattedTime = localTime.format(outputFormatterTime)
                    textTime.setText(formattedTime)

                    textName.setText(party.Название)

                    textSlogan.setText(party.Слоган)

                    textCity.setText(party.Город)

                    textPlace.setText(party.Место)

                    // Преобразование цены
                    val priceFormat = party.Цена
                    if(priceFormat?.rem(1) == 0.0){
                        val priceInt = priceFormat.toInt()
                        textPrice.setText(priceInt.toString())
                    }
                    else{
                        textPrice.setText(priceFormat.toString())
                    }


                    textDescription.setText(party.Описание)

                    btnAddImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    btnAddImage.setImageDrawable(null)

                    val bucket = sb.storage["images"]
                    image = bucket.downloadPublic(party.Фото.toString())
                    val is1: InputStream = ByteArrayInputStream(image)
                    val bmp: Bitmap = BitmapFactory.decodeStream(is1)
                    val dr = BitmapDrawable(resources, bmp)
                    btnAddImage.setImageDrawable(dr)

                    binding.btnDelete.visibility = View.VISIBLE


                    btnSave.visibility = View.VISIBLE
                    content.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            }
            catch (e: Throwable) {
                Log.e("EditPartyActivity", e.toString())
            }
        }

        binding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        binding.btnDelete.setOnClickListener {
            binding.btnAddImage.scaleType = ImageView.ScaleType.CENTER
            binding.btnAddImage.setImageResource(R.drawable.plus)
            image = null

            binding.btnDelete.visibility = View.GONE
        }

        binding.textName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if (binding.textName.text.toString().length > 15) {
                    binding.containerName.isCounterEnabled = true
                }
            }
        })

        binding.textSlogan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if (binding.textSlogan.text.toString().length > 70) {
                    binding.containerSlogan.isCounterEnabled = true
                }
            }

        })

        binding.textDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if (binding.textDescription.text.toString().length > 700) {
                    binding.containerDescription.isCounterEnabled = true
                }
            }

        })

        binding.containerDate.setEndIconOnClickListener {
            val date = setDate(binding.textDate.text.toString())

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val actualMonth = month + 1
                    val dateText = day.toString().padStart(2, '0') + "." + actualMonth.toString()
                        .padStart(2, '0') + "." + year.toString()
                    binding.textDate.setText(dateText)
                    takeHelperText(binding.containerDate, binding.textDate)
                }, date[0], date[1], date[2]
            )
            datePickerDialog.show()
        }

        binding.containerTime.setEndIconOnClickListener {
            val time = setTime(binding.textTime.text.toString())

            val timePickerDialog = TimePickerDialog(
                this,
                { _, hour, minute ->
                    val timeText =
                        hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')
                    binding.textTime.setText(timeText)
                    takeHelperText(binding.containerTime, binding.textTime)
                }, time[0], time[1], true
            )
            timePickerDialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {

            try {
                val selectedImage = data?.data

                // Получаем bitmap из URI
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                image = baos.toByteArray()

                binding.btnAddImage.setImageBitmap(bitmap)

                binding.btnAddImage.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.btnDelete.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.d("AddParty1Fragment", "onActivityResult: ${e.message}")
            }
        }
    }

    private fun setDate(currentDate: String): IntArray {

        var currentYear: Int
        var currentMonth: Int
        var currentDay: Int

        try {
            if (currentDate.isEmpty()) {
                currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                currentMonth = Calendar.getInstance().get(Calendar.MONTH)
                currentYear = Calendar.getInstance().get(Calendar.YEAR)
            } else {
                currentDay = currentDate.split('.')[0].toInt()
                currentMonth = currentDate.split('.')[1].toInt() - 1
                currentYear = currentDate.split('.')[2].toInt()
            }
        } catch (e: Exception) {
            currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            currentYear = Calendar.getInstance().get(Calendar.YEAR)
        }

        return intArrayOf(currentYear, currentMonth, currentDay)
    }

    private fun setTime(currentTime: String): IntArray {
        var currentHour: Int
        var currentMinute: Int

        try {
            if (currentTime.isEmpty()) {
                currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
            } else {
                currentHour = currentTime.substringBefore(':').toInt()
                currentMinute = currentTime.substringAfter(':').toInt()
            }
        } catch (e: Exception) {
            currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        }

        return intArrayOf(currentHour, currentMinute)
    }

    private fun focusedListener(container: TextInputLayout, editText: TextInputEditText) {
        editText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                takeHelperText(container, editText)
            }
        }
    }

    private fun takeHelperText(container: TextInputLayout, editText: TextInputEditText) {
        val type = when (editText) {
            binding.textName -> "название"
            binding.textDescription -> "описание"
            binding.textSlogan -> "слоган"
            binding.textDate -> "даты"
            binding.textTime -> "времени"
            else -> "цены"
        }

        container.helperText = validText(container, editText.text.toString(), type)
    }

    private fun validText(container: TextInputLayout, text: String, type: String): String? {
        val maxLength = container.counterMaxLength
        if (type == "даты") {
            if (text.split('.').size != 3 || text.split('.')[0].toInt() > 31 || text.split('.')[1].toInt() > 12 || text.split(
                    '.'
                )[0].length != 2 || text.split('.')[1].length != 2 || text.split('.')[2].length != 4
            ) {
                return "Неверный формат $type"
            }
            if (LocalDate.parse(text, DateTimeFormatter.ofPattern("dd.MM.uuuu"))
                    .isBefore(LocalDate.now())
            ) {
                return "Хотите окунуться в прошлое?"
            }
        }
        else if(type == "времени") {
            if(text.split(':').size != 2 || text.split(':')[0].toInt() > 23 || text.split(':')[1].toInt() > 59 || text.split(':')[0].length != 2 || text.split(':')[1].length != 2) {
                return "Неверный формат $type"
            }
        }
        else if(type == "название") {
            if(text.length > maxLength){
                return "Слишком длинное $type"
            }
            if(text.length < 3){
                return "Слишком короткое $type"
            }
        }
        else if(type == "описание"){
            if(text.length > maxLength){
                return "Слишком длинное $type"
            }
        }
        else if(type == "слоган"){
            if(text.length > maxLength){
                return "Слишком длинный $type"
            }
        }
        else if(type == "цены"){
            if(text.contains(".")){
                if(text.split(".")[1].length > 2){
                    return "Неверный формат $type"
                }
            }
        }
        if (text.isEmpty()) {
            return "Это обязательное поле"
        }

        return null
    }

    suspend fun loadParty(partyId: Int): PartyDataClass {
        // Загрузка данных о вечеринке
        val partiesResult = sb.from("Вечеринки").select(Columns.raw("*")) {
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

        val event = PartyDataClass(
            id = id,
            Название = name,
            Слоган = slogan,
            Описание = description,
            Дата = date,
            Время = time,
            Город = city,
            Место = place,
            Цена = price,
            Фото = image
        )

        return event
    }
}
