package com.example.myparty.StartActivities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.myparty.MainActivity
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.databinding.ActivityCreateProfileBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch


class CreateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateProfileBinding

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        val user = sharedPreferences.getString("TOKEN_USER", null)

        binding.textNick.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if(binding.textNick.text.toString().length > 15){
                    binding.containerNick.isCounterEnabled = true
                }
            }

        })

        binding.textName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if(binding.textName.text.toString().length > 20){
                    binding.containerName.isCounterEnabled = true
                }
            }

        })

        binding.textDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


            }

            override fun afterTextChanged(p0: Editable?) {
                if(binding.textDescription.text.toString().length > 150){
                    binding.containerDescription.isCounterEnabled = true
                }
            }

        })

        focusedListener(binding.containerNick, binding.textNick)
        focusedListener(binding.containerName, binding.textName)

        binding.btnCreateProfile.setOnClickListener {
            takeHelperText(binding.containerNick, binding.textNick)
            takeHelperText(binding.containerName, binding.textName)
            takeHelperText(binding.containerDescription, binding.textDescription)

            if(binding.containerNick.helperText == null && binding.containerName.helperText == null){

                lifecycleScope.launch {
                    try{
                        if (sb.postgrest["Пользователи"].select {
                                filter {
                                    eq("Ник", binding.textNick.text.toString())
                                }
                            }.decodeList<UserDataClass>().isNotEmpty()){
                            binding.containerNick.helperText = "Такой пользователь уже существует"
                        }
                        else{
                            val userAdd = UserDataClass(
                                Ник = binding.textNick.text.toString(),
                                Имя = binding.textName.text.toString(),
                                Описание = if(binding.textDescription.text.toString().isEmpty() ) null else binding.textDescription.text.toString()
                            )
                            sb.postgrest["Пользователи"].update(userAdd){
                                filter{
                                    eq("id", user!!)
                                }
                            }
                            val mainIntent = Intent(this@CreateProfileActivity, MainActivity::class.java)
                            startActivity(mainIntent)
                            finish()
                        }
                    }
                    catch(e: Throwable){
                        Log.e("Error create profile", e.toString())
                    }
                }
            }
        }
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
            binding.textDescription -> "описание"
            binding.textName -> "имя или название организации"
            else -> "короткое имя"
        }

        container.helperText = validText(container, editText.text.toString(), type)
    }

    private fun validText(container: TextInputLayout, text: String, type: String): String? {
        val maxLength = container.counterMaxLength
        if(text.length > maxLength){
            return "Слишком длинное $type"
        }
        if(type == "имя или название организации" || type == "короткое имя"){
            if(text.isEmpty()){
                return "Это обязательное поле"
            }
        }
        return null
    }
}