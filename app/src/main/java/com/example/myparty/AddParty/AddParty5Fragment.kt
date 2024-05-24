package com.example.myparty.AddParty

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.Main.MainFragment
import com.example.myparty.MainActivity
import com.example.myparty.Profile.ProfileFragment
import com.example.myparty.R
import com.example.myparty.SupabaseConnection
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentAddParty5Binding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddParty5Fragment() : Fragment() {
    private lateinit var binding: FragmentAddParty5Binding

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddParty5Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Создание переменной для SharedPreference
        sharedPreferences = requireActivity().getSharedPreferences("SHARED_PREFS_ADD_PARTY", Context.MODE_PRIVATE)

        binding.btnGoNext.setOnClickListener {
            takeHelperText(binding.containerPrice, binding.textPrice)

            if(binding.containerPrice.helperText == null){

                binding.btnGoNext.isEnabled = false
                binding.btnGoNext.text = "Загрузка..."
                binding.progressBar.visibility = View.VISIBLE
                binding.content.alpha = 0.62f

                val name = sharedPreferences.getString("ADD_PARTY_NAME", null)

                val description = sharedPreferences.getString("ADD_PARTY_DESCRIPTION", null)
                val slogan = sharedPreferences.getString("ADD_PARTY_SLOGAN", null)

                val city = sharedPreferences.getString("ADD_PARTY_CITY", null)
                val place = sharedPreferences.getString("ADD_PARTY_PLACE", null)

                val date = sharedPreferences.getString("ADD_PARTY_DATE", null).toString().split('.')
                val currentDate = LocalDate.of(date[2].toInt(), date[1].toInt(), date[0].toInt()).toString()
                val time = sharedPreferences.getString("ADD_PARTY_TIME", null)

                val price = binding.textPrice.text.toString().toDouble()

                val userId = sb.auth.currentUserOrNull()?.id.toString()

                lifecycleScope.launch {
                    try{
                        // Добавление данных Вечеринки в таблицу Вечеринки
                        val partyAdd = PartyDataClass(
                            Название = name,
                            Описание = description,
                            Слоган = slogan,
                            Город = city, Место = place,
                            Дата = currentDate, Время = time,
                            Цена = price,
                            id_статуса_проверки = 1,
                            id_пользователя =  userId,
                            id_возрастного_ограничения = 4
                        )

                        sb.postgrest["Вечеринки"].insert(partyAdd)

                        sharedPreferences.edit().clear().apply()

                        val intent = Intent(requireContext(), MainActivity::class.java)
                        val fragment = ProfileFragment()
                        intent.putExtra("FRAGMENT", fragment.javaClass.name)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    catch (e: Throwable){
                        Log.e("Ошибка при добавлении данных в таблицу", e.message.toString())
                    }
                }
            }
        }

    }

    private fun takeHelperText(container: TextInputLayout, editText: TextInputEditText){
        container.helperText = validText(editText.text.toString())
    }

    private fun validText(text: String): String? {
        if(text.isEmpty()){
            return "Это обязательное поле"
        }
        if(text.contains(".")){
            if(text.split(".")[1].length > 2){
                return "Неверный формат цены"
            }
        }

        return null
    }
}