package com.example.myparty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myparty.databinding.ActivityLoginBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sb = SupabaseConnection().sb

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
                    }
                    catch (e: Exception){

                        Toast.makeText(this@LoginActivity, "Что-то пошло не так", Toast.LENGTH_SHORT).show()
                        Log.e("ERROR", e.message.toString())
                    }
                    val myIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(myIntent)
                    finish()
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
        return null
    }
}