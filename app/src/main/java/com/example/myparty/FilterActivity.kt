package com.example.myparty

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityFilterBinding
import com.example.myparty.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

class FilterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilterBinding

    val parties = mutableListOf<PartyDataClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dateText = LocalDate.now().dayOfMonth.toString().padStart(2, '0') + "." + LocalDate.now().monthValue.toString().padStart(2, '0') + "." + LocalDate.now().year.toString().padStart(2, '0')
        binding.textDate.setText(dateText)

        val timeText = LocalTime.now().hour.toString().padStart(2, '0') + ":" + LocalTime.now().minute.toString().padStart(2, '0')
        binding.textTime.setText(timeText)

        loadParties()

        focusedListener(binding.textCity)
        focusedListenerEditText(binding.textPriceOt)
        focusedListenerEditText(binding.textPriceDo)
        focusedListener(binding.textDate)
        focusedListener(binding.textTime)

        binding.btnShowParties.setOnClickListener {
            finish()
        }

        binding.containerDate.setEndIconOnClickListener {
            val date = setDate()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val dateText = day.toString().padStart(2, '0') + "." + month.toString().padStart(2, '0') + "." + year.toString()
                    binding.textDate.setText(dateText)
                    loadParties()
                }, date[0], date[1], date[2]
            )
            datePickerDialog.show()
        }

        binding.containerTime.setEndIconOnClickListener {

            val time = setTime()

            val timePickerDialog = TimePickerDialog(
                this,
                { _, hour, minute ->
                    val timeText = hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')
                    binding.textTime.setText(timeText)
                    loadParties()
                }, time[0], time[1], true
            )
            timePickerDialog.show()
        }
    }

    private fun setDate(): IntArray{
        val currentDate = binding.textDate.text.toString()

        val currentYear: Int
        val currentMonth: Int
        val currentDay: Int

        if (currentDate.isEmpty()) {
            currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            currentYear = Calendar.getInstance().get(Calendar.YEAR)
        }
        else {
            currentDay = currentDate.split('.')[0].toInt()
            currentMonth = currentDate.split('.')[1].toInt()
            currentYear = currentDate.split('.')[2].toInt()
        }
        return intArrayOf(currentYear, currentMonth, currentDay)
    }

    private fun setTime(): IntArray{
        val currentTime = binding.textTime.text.toString()

        val currentHour: Int
        val currentMinute: Int

        if (currentTime.isEmpty()) {
            currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        }
        else {
            currentHour = currentTime.substringBefore(':').toInt()
            currentMinute = currentTime.substringAfter(':').toInt()
        }
        return intArrayOf(currentHour, currentMinute)
    }

    fun focusedListener(editText: TextInputEditText) {
        editText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                loadParties()
            }
        }
    }

    fun focusedListenerEditText(editText: EditText) {
        editText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                loadParties()
            }
        }
    }

    fun loadParties() {
        binding.btnShowParties.text = "Загрузка..."
        binding.btnShowParties.isEnabled = false
        lifecycleScope.launch {
            try{
                val countParties = filter().size
                if (countParties > 0) {
                    binding.btnShowParties.text =
                        "Показать " + countParties.toString() + " вечеринок"
                    binding.btnShowParties.isEnabled = true
                }
                else binding.btnShowParties.text =
                    "Вечеринки не найдены"
            }
            catch(e:Throwable){
                Log.e("воалвоаовыаовл", e.message.toString())
            }
        }
    }

    suspend fun filter(): MutableList<PartyDataClass> {
        val partiesResult = sb.from("Вечеринки")
            .select(Columns.raw("*, Возрастное_ограничение(Возраст), Пользователи(Имя, Верификация)")) {
            filter {
                // Фитрация разрешенных вечеринок
                eq("id_статуса_проверки", "3")
                // Фитрация города вечеринок
                if(binding.textCity.text.toString().isNotEmpty()) eq("Город", binding.textCity.text.toString())
                // Фитрация даты проведения вечеринок
                if(binding.textDate.text.toString().isNotEmpty()){
                    val date = binding.textDate.text.toString().split('.')
                    val currentDate = LocalDate.of(date[2].toInt(), date[1].toInt(), date[0].toInt())
                    gte("Дата", currentDate.toString())
                }
                // Фитрация времени проведения вечеринок
                if(binding.textTime.text.toString().isNotEmpty()) gte("Время", binding.textTime.text.toString())
                // Фитрация цены вечеринок при заполнении полей от и до
                if(binding.textPriceOt.text.toString().isNotEmpty() && binding.textPriceDo.text.toString().isNotEmpty()) {
                    gte("Цена", binding.textPriceOt.text.toString().toDouble())
                    and{
                        lte("Цена", binding.textPriceDo.text.toString().toDouble())
                    }
                }
                // Фитрация цены вечеринок при заполнении поля от
                if(binding.textPriceOt.text.toString().isNotEmpty()) gte("Цена", binding.textPriceOt.text.toString().toDouble())
                // Фитрация цены вечеринок при заполнении поля до
                if(binding.textPriceDo.text.toString().isNotEmpty()) lte("Цена", binding.textPriceDo.text.toString().toDouble())
            }
        }.data

        val jsonArrayParties = JSONArray(partiesResult)

        val partiesFavoritesResult = sb.from("Избранные_вечеринки").select() {
            filter {
                eq("id_пользователя", sb.auth.currentUserOrNull()?.id.toString())
            }
        }.data
        val jsonArrayFavorites = JSONArray(partiesFavoritesResult)

        parties.clear()

        for (i in 0 until jsonArrayParties.length()) {
            val jsonObject = jsonArrayParties.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val name = jsonObject.getString("Название")
//                    val slogan = jsonObject.getString("Слоган")
            val date = jsonObject.getString("Дата")
            val time = jsonObject.getString("Время")
//                    val city = jsonObject.getString("Город")
            val place = jsonObject.getString("Место")
//                    val description = jsonObject.getString("Описание")
            val price = jsonObject.getDouble("Цена")
            val ageObject = jsonObject.getJSONObject("Возрастное_ограничение")
            val age = ageObject.getInt("Возраст")
            val usersObject = jsonObject.getJSONObject("Пользователи")
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
                Имя = userName,
                Дата = date,
                Время = time,
                Место = place,
                Цена = price,
                Возраст = age,
                Верификация = userVerify,
                Избранное = favorite
            )
            parties.add(event)

        }
        Log.d("вечеринка", partiesResult.toString())
        return parties
    }
}