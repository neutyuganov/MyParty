package com.example.myparty

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myparty.databinding.FragmentAddParty3Binding
import java.util.Calendar


class AddParty3Fragment() : Fragment() {
    private lateinit var binding: FragmentAddParty3Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddParty3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.containerDate.setEndIconOnClickListener {

            val date = setDate()

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val dateText = day.toString().padStart(2, '0') + "." + month.toString().padStart(2, '0') + "." + year.toString()
                    binding.textDate.setText(dateText)
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
}