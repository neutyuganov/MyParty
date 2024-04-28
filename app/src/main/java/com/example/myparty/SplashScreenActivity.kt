package com.example.myparty

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import io.github.jan.supabase.gotrue.auth

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var sharedpreferences: SharedPreferences

    private var tokenUser : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedpreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        tokenUser = sharedpreferences.getString("TOKEN_USER", null)

        Handler(Looper.getMainLooper()).postDelayed({
            // Проверяем, авторизован ли пользователь
            if (isUserAuthenticated()) {
                // Переходим на главный экран
                val mainIntent = Intent(this, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            } else {
                // Переходим на экран входа
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
                finish()
            }
        }, 2000) // Запускаем таймер
    }

    private fun isUserAuthenticated(): Boolean {
        return tokenUser!= null
    }
}
