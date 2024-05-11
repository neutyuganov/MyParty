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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
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

        focusedListener(binding.containerEmail, binding.textEmail)
        focusedListener(binding.containerPassword, binding.textPassword)

        binding.goLogIn.setOnClickListener{
            takeHelperText(binding.containerEmail, binding.textEmail)
            takeHelperText(binding.containerPassword, binding.textPassword)
            binding.textError.isVisible = false
            if(validText(binding.textEmail.text.toString(), 1) == null && validText(binding.textPassword.text.toString(), 0) == null){
                lifecycleScope.launch {
                    try{
                        sb.auth.signInWith(Email){
                            email = binding.textEmail.text.toString()
                            password = binding.textPassword.text.toString()
                        }

                        sharedpreferences.edit().putString("TOKEN_USER", sb.auth.currentUserOrNull()?.id.toString()).apply()

                        val user = SupabaseConnection.Singleton.sb.auth.currentUserOrNull()?.id

                        val users = SupabaseConnection.Singleton.sb.from("Пользователи").select{
                            filter {
                                eq("id",user.toString())
                            }
                        }.decodeSingle<UserDataClass>()

                        if (users.Ник == null) {
                            val mainIntent = Intent(this@LoginActivity, CreateProfileActivity::class.java)
                            startActivity(mainIntent)
                            finish()
                        }
                        else {
                            val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(mainIntent)
                            finish()
                        }

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
            binding.textEmail -> 1
            else -> 0
        }

        container.helperText = validText(editText.text.toString(), type)
    }

    private fun validText(text: String, type: Int): String? {
        if(text.isEmpty()){
            return "Поле не должно быть пустым"
        }
        if(type == 1){
            if(!Patterns.EMAIL_ADDRESS.matcher(text).matches())
            {
                return "Неверный формат почты"
            }
        }
        return null
    }
}