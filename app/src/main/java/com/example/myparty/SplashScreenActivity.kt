package com.example.myparty

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.example.myparty.SupabaseConnection.Singleton.sb
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var sharedpreferences: SharedPreferences

    private var tokenUser : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedpreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        tokenUser = sharedpreferences.getString("TOKEN_USER", null)

        Handler(Looper.getMainLooper()).postDelayed({

            if (isUserAuthenticated()) {
                try{
                    lifecycleScope.launch {

                        Log.e("USERCURRENT", sb.auth.currentUserOrNull().toString())
                        // Проверяем, авторизован ли пользователь
                        withTimeout(5000L) {
                            // Проверяем, авторизован ли пользователь
                            val users = sb.from("Пользователи").select{
                                filter {
                                    eq("id",tokenUser.toString())
                                }
                            }.decodeSingle<UserDataClass>()

                            if (users.Ник == null) {
                                val mainIntent = Intent(this@SplashScreenActivity, CreateProfileActivity::class.java)
                                startActivity(mainIntent)
                                finish()
                            }
                            else {
                                val mainIntent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                                startActivity(mainIntent)
                                finish()
                            }
                        }
                    }
                }
                catch (e: Exception) {
                    Log.e("Error SplashScreen", e.toString())
                }

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
