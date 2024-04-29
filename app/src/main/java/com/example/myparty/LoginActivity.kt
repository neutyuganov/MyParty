package com.example.myparty

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.myparty.databinding.ActivityLoginBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var sharedpreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sb = SupabaseConnection.Singleton.sb

        sharedpreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        emailFocusedListener()
        passwordFocusedListener()

        binding.goLogIn.setOnClickListener{
            emailTakeHelperText()
            passwordTakeHelperText()
            if(validEmail() == null && validPassword() == null){
                lifecycleScope.launch {
                    try{
                        sb.auth.signInWith(Email){
                            email = binding.textEmail.text.toString()
                            password = binding.textPassword.text.toString()
                        }

                        sharedpreferences.edit().putString("TOKEN_USER", sb.auth.currentAccessTokenOrNull()).apply()

                        val myIntent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(myIntent)
                        finish()
                    }
                    catch (e: Exception){
                        Log.e("ERROR", e.message.toString())
                        if(e.message.toString().startsWith("invalid_grant (Invalid login credentials)") ){
                            binding.textError.isVisible = true
                        }
                    }
                }
            }
        }

        binding.toReg.setOnClickListener{
            val myIntent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            startActivity(myIntent)
            finish()
        }
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
        binding.textError.isVisible = false
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
        binding.textError.isVisible = false
    }

    private fun validPassword(): String? {
        val password = binding.textPassword.text.toString()
        if(password.isEmpty()){
            return "Поле не должно быть пустым"
        }
        return null
    }
}