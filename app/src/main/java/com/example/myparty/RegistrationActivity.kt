package com.example.myparty

import android.R.attr.password
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myparty.databinding.ActivityRegistrationBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emailFocusedListener()
        passwordFocusedListener()
        nameFocusedListener()
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
            return "Поле имени не должно быть пустым"
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
            return "Поле почты не должно быть пустым"
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

