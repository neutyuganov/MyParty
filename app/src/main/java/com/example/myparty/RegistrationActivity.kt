package com.example.myparty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myparty.databinding.ActivityRegistrationBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.supabaseJson
import kotlinx.coroutines.launch
import java.lang.Exception


class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    private lateinit var sbConnection: SupabaseConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sb = sbConnection.sb

        emailFocusedListener()
        passwordFocusedListener()
        nameFocusedListener()

        binding.goReg.setOnClickListener{
            if(validName() == null && validEmail() == null && validPassword() == null){
                lifecycleScope.launch {
                    try{
                        sb.auth.signUpWith(Email) {
                            email = binding.textEmail.text.toString()
                            password = binding.textPassword.text.toString()
                        }

                        val userId = sb.auth.currentUserOrNull()?.id
                        val userAdd = UserDataClass(id = userId.toString(), Имя = binding.textName.text.toString())
                        sb.postgrest["Пользователи"].insert(userAdd)

                        val myIntent = Intent(this@RegistrationActivity, MainActivity::class.java)
                        startActivity(myIntent)
                        finish()
                    }
                    catch (e: Exception){
                        Log.e("ERROR", e.message.toString())
                    }
                }


            }
        }
    }
    private fun nameFocusedListener() {
        binding.textName.setOnFocusChangeListener{_, focused->
            if(!focused)
            {
                binding.containerName.helperText = validName()
            }
        }
    }

    private fun validName(): String? {
        val name = binding.textName.text.toString()
        if(name.isEmpty()){
            return "Поле не должно быть пустым"
        }
        return null
    }

    private fun emailFocusedListener() {
        binding.textEmail.setOnFocusChangeListener{_, focused->
            if(!focused)
            {
                binding.containerEmail.helperText = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val email = binding.textEmail.text.toString()
        if(email.isEmpty()){
            return "Поле не должно быть пустым"
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            return "Неверный формат почты"
        }
        return null
    }

    private fun passwordFocusedListener() {
        binding.textPassword.setOnFocusChangeListener{_, focused->
            if(!focused)
            {
                binding.containerPassword.helperText = validPassword()
            }
        }
    }

    private fun validPassword(): String? {
        val password = binding.textPassword.text.toString()
        if(password.isEmpty()){
            return "Поле пароля не должно быть пустым"
        }
        if(password.length < 6)
        {
            return "Пароль должен содержать минимум 6 символов"
        }
        if(!password.matches(".*[a-z, а-я].*".toRegex())){
            return "Пароль должен содержать буквы с нижним регистром"
        }
        if(!password.matches(".*[A-Z, А-Я].*".toRegex())){
            return "Пароль должен содержать буквы с верхним регистром"
        }
        if(!password.matches(".*[!@#\$&*].*".toRegex())){
            return "Пароль должен содержать специальные символы !@#\$&*"
        }
        return null
    }
}

