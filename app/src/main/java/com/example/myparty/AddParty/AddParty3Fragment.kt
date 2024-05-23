package com.example.myparty.AddParty

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myparty.R
import com.example.myparty.databinding.FragmentAddParty3Binding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class AddParty3Fragment() : Fragment() {
    private lateinit var binding: FragmentAddParty3Binding

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddParty3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Создание переменной для SharedPreference
        sharedPreferences = requireActivity().getSharedPreferences("SHARED_PREFS_ADD_PARTY", Context.MODE_PRIVATE)

        if(sharedPreferences.contains("ADD_PARTY_DATE")){
            binding.textDate.setText(sharedPreferences.getString("ADD_PARTY_DATE", null))
            binding.textTime.setText(sharedPreferences.getString("ADD_PARTY_TIME", null))
        }

        focusedListener(binding.containerDate, binding.textDate)
        focusedListener(binding.containerTime, binding.textTime)

        binding.btnGoNext.setOnClickListener {
            takeHelperText(binding.containerDate, binding.textDate)
            takeHelperText(binding.containerTime, binding.textTime)

            if(binding.containerDate.helperText == null && binding.containerTime.helperText == null){
                sharedPreferences.edit().putString("ADD_PARTY_DATE", binding.textDate.text.toString()).apply()
                sharedPreferences.edit().putString("ADD_PARTY_TIME", binding.textTime.text.toString()).apply()

                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame, AddParty4Fragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }

        binding.containerDate.setEndIconOnClickListener {
            val date = setDate()

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val actualMonth = month + 1
                    val dateText = day.toString().padStart(2, '0') + "." + actualMonth.toString().padStart(2, '0') + "." + year.toString()
                    binding.textDate.setText(dateText)
                    takeHelperText(binding.containerDate, binding.textDate)
                }, date[0], date[1], date[2]
            )
            datePickerDialog.show()
        }

        binding.containerTime.setEndIconOnClickListener {
            val time = setTime()

            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    val timeText = hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')
                    binding.textTime.setText(timeText)
                    takeHelperText(binding.containerTime, binding.textTime)
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
            currentMonth = currentDate.split('.')[1].toInt() - 1
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

    private fun focusedListener(container: TextInputLayout, editText: TextInputEditText) {
        editText.setOnFocusChangeListener{_, focused->
            if(!focused)
            {
                takeHelperText(container, editText)
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
            if(LocalDate.parse(text, DateTimeFormatter.ofPattern("dd.MM.uuuu")).isBefore(LocalDate.now())){
                return "Хотите окунуться в прошлое?"
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
}