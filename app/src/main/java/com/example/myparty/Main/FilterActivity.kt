package com.example.myparty.Main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.MainActivity
import com.example.myparty.Profile.EditPartyActivity
import com.example.myparty.Profile.ProfileFragment
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityFilterBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class FilterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilterBinding

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Создание переменной для SharedPreference
        sharedPreferences = this.getSharedPreferences("SHARED_PREFS_FILTER", Context.MODE_PRIVATE)

        val city = sharedPreferences.getString("FILTER_CITY", null)
        val time = sharedPreferences.getString("FILTER_TIME", null)
        val date = sharedPreferences.getString("FILTER_DATE", null)
        val priceStart = sharedPreferences.getString("FILTER_PRICE_START", null)
        val priceEnd = sharedPreferences.getString("FILTER_PRICE_END", null)

        if(city!= null) binding.textCity.setText(city)
        if(time!= null) binding.textTime.setText(time)
        if(date!= null){
            val dateArray = date.split("-")
            val year = dateArray[0].toInt()
            val month = dateArray[1].toInt()
            val day = dateArray[2].toInt()
            binding.textDate.setText(day.toString().padStart(2, '0') + "." + month.toString().padStart(2, '0') + "." + year.toString().padStart(2, '0'))
        }
        if(priceStart!= null) binding.textPriceOt.setText(priceStart)
        if(priceEnd!= null) binding.textPriceDo.setText(priceEnd)

        if(sharedPreferences.all.isEmpty()) {
            // Вывод текущей даты
            val dateText = LocalDate.now().dayOfMonth.toString().padStart(2, '0') + "." + LocalDate.now().monthValue.toString().padStart(2, '0') + "." + LocalDate.now().year.toString().padStart(2, '0')
            binding.textDate.setText(dateText)
        }

        // Загрузка результатов фильтрации
        loadParties()

        focusedListener(binding.textCity)
        focusedListenerEditText(binding.textPriceOt)
        focusedListenerEditText(binding.textPriceDo)
        focusedListener(binding.textDate)
        focusedListener(binding.textTime)

        binding.btnShowParties.setOnClickListener {
            if(binding.textDate.text.toString().isNotEmpty()) {
                takeHelperText(binding.containerDate, binding.textDate)
            }
            if(binding.textTime.text.toString().isNotEmpty()) {
                takeHelperText(binding.containerTime, binding.textTime)
            }

            if(binding.textPriceOt.text.toString().isNotEmpty() && binding.textPriceDo.text.toString().isNotEmpty()) {
                if(binding.textPriceOt.text.toString().toInt() > binding.textPriceDo.text.toString().toInt() || binding.textPriceDo.text.toString().toInt() < binding.textPriceOt.text.toString().toInt()){
                    binding.errorPrice.visibility = View.VISIBLE
                }
                else binding.errorPrice.visibility = View.GONE
            }

            if(binding.containerDate.helperText == null && binding.containerTime.helperText == null) {
                if(binding.textPriceOt.text.toString().isNotEmpty() && binding.textPriceDo.text.toString().isNotEmpty()) {
                    if (binding.textPriceOt.text.toString().toInt() < binding.textPriceDo.text.toString().toInt() || binding.textPriceDo.text.toString().toInt() > binding.textPriceOt.text.toString().toInt()
                    ) {
                        try{
                            loadParties()

                            // Сохранение города в SharedPreferences
                            if(binding.textCity.text.toString().isNotEmpty()) sharedPreferences.edit().putString("FILTER_CITY", binding.textCity.text.toString()).apply()
                            else sharedPreferences.edit().remove("FILTER_CITY").apply()

                            // Сохранение времени в SharedPreferences
                            if(binding.textTime.text.toString().isNotEmpty()) sharedPreferences.edit().putString("FILTER_TIME", binding.textTime.text.toString()).apply()
                            else sharedPreferences.edit().remove("FILTER_TIME").apply()

                            // Сохранение даты в SharedPreferences
                            if(binding.textDate.text.toString().isNotEmpty()){
                                val date = binding.textDate.text.toString().split('.')
                                val currentDate = LocalDate.of(date[2].toInt(), date[1].toInt(), date[0].toInt())
                                sharedPreferences.edit().putString("FILTER_DATE", currentDate.toString()).apply()
                            }
                            else sharedPreferences.edit().remove("FILTER_DATE").apply()

                            // Сохранение начальной цены в SharedPreferences
                            if(binding.textPriceOt.text.toString().isNotEmpty()) sharedPreferences.edit().putString("FILTER_PRICE_START", binding.textPriceOt.text.toString()).apply()
                            else sharedPreferences.edit().remove("FILTER_PRICE_START").apply()

                            // Сохранение конечной цены в SharedPreferences
                            if(binding.textPriceDo.text.toString().isNotEmpty()) sharedPreferences.edit().putString("FILTER_PRICE_END", binding.textPriceDo.text.toString()).apply()
                            else sharedPreferences.edit().remove("FILTER_PRICE_END").apply()

                            val intent = Intent(this, MainActivity::class.java)
                            val fragment = MainFragment()
                            intent.putExtra("FRAGMENT", fragment.javaClass.name)
                            startActivity(intent)
                            finish()
                        }
                        catch (e: Throwable){
                            Log.d("Error", e.toString())
                        }
                    }
                }
                else{
                    try{
                        loadParties()

                        // Сохранение города в SharedPreferences
                        if(binding.textCity.text.toString().isNotEmpty()) sharedPreferences.edit().putString("FILTER_CITY", binding.textCity.text.toString()).apply()
                        else sharedPreferences.edit().remove("FILTER_CITY").apply()

                        // Сохранение времени в SharedPreferences
                        if(binding.textTime.text.toString().isNotEmpty()) sharedPreferences.edit().putString("FILTER_TIME", binding.textTime.text.toString()).apply()
                        else sharedPreferences.edit().remove("FILTER_TIME").apply()

                        // Сохранение даты в SharedPreferences
                        if(binding.textDate.text.toString().isNotEmpty()){
                            val date = binding.textDate.text.toString().split('.')
                            val currentDate = LocalDate.of(date[2].toInt(), date[1].toInt(), date[0].toInt())
                            sharedPreferences.edit().putString("FILTER_DATE", currentDate.toString()).apply()
                        }
                        else sharedPreferences.edit().remove("FILTER_DATE").apply()

                        // Сохранение начальной цены в SharedPreferences
                        if(binding.textPriceOt.text.toString().isNotEmpty()) sharedPreferences.edit().putString("FILTER_PRICE_START", binding.textPriceOt.text.toString()).apply()
                        else sharedPreferences.edit().remove("FILTER_PRICE_START").apply()

                        // Сохранение конечной цены в SharedPreferences
                        if(binding.textPriceDo.text.toString().isNotEmpty()) sharedPreferences.edit().putString("FILTER_PRICE_END", binding.textPriceDo.text.toString()).apply()
                        else sharedPreferences.edit().remove("FILTER_PRICE_END").apply()

                        val intent = Intent(this, MainActivity::class.java)
                        val fragment = MainFragment()
                        intent.putExtra("FRAGMENT", fragment.javaClass.name)
                        startActivity(intent)
                        finish()
                    }
                    catch (e: Throwable){
                        Log.d("Error", e.toString())
                    }
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            // Удаление фильтров из SharedPreference
            sharedPreferences.edit().clear().apply()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnGoBack.setOnClickListener {
            finish()
        }

        binding.containerDate.setEndIconOnClickListener {
            val date = setDate()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val actualMonth = month + 1
                    val dateText = day.toString().padStart(2, '0') + "." + actualMonth.toString().padStart(2, '0') + "." + year.toString()
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

        var currentYear: Int
        var currentMonth: Int
        var currentDay: Int

        try{
            if (currentDate.isEmpty()) {
                currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                currentMonth = Calendar.getInstance().get(Calendar.MONTH)
                currentYear = Calendar.getInstance().get(Calendar.YEAR)
            }
            else {
                currentDay = currentDate.split('.')[0].toInt()
                currentMonth = currentDate.split('.')[1].toInt() - 1
                currentYear = currentDate.split('.')[2].toInt()
            }
        }
        catch (e: Exception){
            currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            currentYear = Calendar.getInstance().get(Calendar.YEAR)
        }
        return intArrayOf(currentYear, currentMonth, currentDay)
    }

    private fun setTime(): IntArray{
        val currentTime = binding.textTime.text.toString()

        var currentHour: Int
        var currentMinute: Int

        try{
            if (currentTime.isEmpty()) {
                currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
            }
            else {
                currentHour = currentTime.substringBefore(':').toInt()
                currentMinute = currentTime.substringAfter(':').toInt()
            }
        }
        catch (e: Exception){
            currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
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
                val countParties = filter().count()
                if (countParties > 0) {
                    binding.btnShowParties.text =
                        "Показать " + countParties + " вечеринок"
                    binding.btnShowParties.isEnabled = true
                }
                else binding.btnShowParties.text = "Вечеринки не найдены"
            }
            catch(e:Throwable){
                Log.e("Ошибка загрузки вечеринок", e.message.toString())
            }
        }
    }

    private fun takeHelperText(container: TextInputLayout, editText: TextInputEditText){
        val type = when (editText) {
            binding.textDate -> "даты"
            else -> "времени"
        }

        container.helperText = validText(editText.text.toString(), type)
    }

    private fun validText(text: String, type: String): String? {
        if(type == "даты"){
            if(text.split('.').size!= 3 || text.split('.')[0].toInt() > 31 || text.split('.')[1].toInt() > 12 || text.split('.')[0].length!= 2 || text.split('.')[1].length!= 2 || text.split('.')[2].length!= 4){
                return "Неверный формат $type"
            }
        }
        else {
            if(text.split(':').size!= 2 || text.split(':')[0].toInt() > 23 || text.split(':')[1].toInt() > 59 || text.split(':')[0].length!= 2 || text.split(':')[1].length!= 2){
                return "Неверный формат $type"
            }
        }
        if(text.isEmpty()){
            return "Это обязательное поле"
        }

        return null
    }

    suspend fun filter(): List<PartyDataClass> {
        val partiesResult = sb.from("Вечеринки")
            .select(Columns.raw("*, Возрастное_ограничение(Возраст), Пользователи(Имя, Верификация)")) {
            filter {
                neq("id_пользователя", sb.auth.currentUserOrNull()?.id.toString()) // Фитрация пользователя
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
        }.decodeList<PartyDataClass>()

        return partiesResult
    }
}