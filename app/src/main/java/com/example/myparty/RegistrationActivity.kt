package com.example.myparty

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myparty.databinding.ActivityRegistrationBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.lang.Exception

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    private lateinit var sharedpreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedpreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        val sb = SupabaseConnection.Singleton.sb

        Log.e("USERCURRENT1", SupabaseConnection.Singleton.sb.auth.currentUserOrNull().toString())

        focusedListener(binding.containerName, binding.textName)
        focusedListener(binding.containerEmail, binding.textEmail)
        focusedListener(binding.containerPassword, binding.textPassword)

        binding.goReg.setOnClickListener{
            takeHelperText(binding.containerName, binding.textName)
            takeHelperText(binding.containerEmail, binding.textEmail)
            takeHelperText(binding.containerPassword, binding.textPassword)
            if(validText(binding.textName.text.toString(), 1) == null && validText(binding.textEmail.text.toString(), 2) == null && validText(binding.textPassword.text.toString(), 3) == null){
                lifecycleScope.launch {
                    try{
                    val users = sb.from("Пользователи").select{
                        filter {
                            eq("Почта", binding.textEmail.text.toString())
                        }
                    }.decodeList<UserDataClass>().count()

                    if(users == 0){
                        sb.auth.signUpWith(Email) {
                            email = binding.textEmail.text.toString()
                            password = binding.textPassword.text.toString()
                        }

                        sharedpreferences.edit().putString("TOKEN_USER", sb.auth.currentAccessTokenOrNull()).apply()

                        val user = sb.auth.currentUserOrNull()
                        val userAdd = UserDataClass(id = user?.id.toString(), Имя = binding.textName.text.toString(), Почта = user?.email.toString())
                        sb.postgrest["Пользователи"].insert(userAdd)

                        val myIntent = Intent(this@RegistrationActivity, MainActivity::class.java)
                        startActivity(myIntent)
                        finish()
                    }
                    else {
                        binding.containerEmail.helperText = "Пользователь с таким email уже есть"
                        binding.textEmail.requestFocus()
                    }

                    }
                    catch (e: Exception){
                        Log.e("ERROR", e.message.toString())
                    }
                }
            }
        }

        binding.toLogIn.setOnClickListener{
            val myIntent = Intent(this@RegistrationActivity, LoginActivity::class.java)
            startActivity(myIntent)
            finish()
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
            binding.textEmail -> 2
            binding.textPassword -> 3
            else -> 0
        }

        container.helperText = validText(editText.text.toString(), type)
    }

    private fun validText(text: String, type: Int): String? {
        if(text.isEmpty()){
            return "Поле не должно быть пустым"
        }
        if(type == 2){
            if(!Patterns.EMAIL_ADDRESS.matcher(text).matches())
            {
                return "Неверный формат почты"
            }
        }
        if(type == 3){
            if(text.length < 6)
            {
                return "Пароль должен содержать минимум 6 символов"
            }
            /*if(!text.matches(".*[a-z, а-я].*".toRegex())){
                return "Пароль должен содержать буквы с нижним регистром"
            }
            if(!text.matches(".*[A-Z, А-Я].*".toRegex())){
                return "Пароль должен содержать буквы с верхним регистром"
            }
            if(!text.matches(".*[!@#\$&*].*".toRegex())){
                return "Пароль должен содержать специальные символы !@#\$&*"
            }*/
        }
        return null
    }
}

