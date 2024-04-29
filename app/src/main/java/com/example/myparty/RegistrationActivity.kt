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

        emailFocusedListener()
        passwordFocusedListener()
        nameFocusedListener()

        binding.goReg.setOnClickListener{
            nameTakeHelperText()
            emailTakeHelperText()
            passwordTakeHelperText()
            if(validName() == null && validEmail() == null && validPassword() == null){
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
    private fun nameFocusedListener() {
        binding.textName.setOnFocusChangeListener{_, focused->
            if(!focused)
            {
                nameTakeHelperText()
            }
        }
    }

    private fun nameTakeHelperText(){
        binding.containerName.helperText = validName()
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
                emailTakeHelperText()
            }
        }
    }

    private fun emailTakeHelperText(){
        binding.containerEmail.helperText = validEmail()
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
                passwordTakeHelperText()
            }
        }
    }

    private fun passwordTakeHelperText(){
        binding.containerPassword.helperText = validPassword()
    }

    private fun validPassword(): String? {
        val password = binding.textPassword.text.toString()
        if(password.isEmpty()){
            return "Поле не должно быть пустым"
        }
        if(password.length < 6)
        {
            return "Пароль должен содержать минимум 6 символов"
        }
        /*if(!password.matches(".*[a-z, а-я].*".toRegex())){
            return "Пароль должен содержать буквы с нижним регистром"
        }
        if(!password.matches(".*[A-Z, А-Я].*".toRegex())){
            return "Пароль должен содержать буквы с верхним регистром"
        }
        if(!password.matches(".*[!@#\$&*].*".toRegex())){
            return "Пароль должен содержать специальные символы !@#\$&*"
        }*/
        return null
    }
}

