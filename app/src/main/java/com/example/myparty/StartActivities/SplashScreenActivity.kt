package com.example.myparty.StartActivities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myparty.AdminActivity
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.MainActivity
import com.example.myparty.R
import com.example.myparty.SupabaseConnection
import com.example.myparty.SupabaseConnection.Singleton.sb
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONArray

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private var user : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        user = sharedPreferences.getString("TOKEN_USER", null)

        Handler(Looper.getMainLooper()).postDelayed({

            if (isUserAuthenticated()) {
                lifecycleScope.launch {
                    // Проверяем, авторизован ли пользователь
                    withTimeout(5000L) {

                        try {
                            // Проверяем, авторизован ли пользователь
                            val users = SupabaseConnection.Singleton.sb.from("Пользователи").select(
                                Columns.raw("*, Роли(Название), Статусы_проверки(Название)")){
                                filter {
                                    eq("id",user!!)
                                }
                            }.data

                            val jsonArrayUser = JSONArray(users).getJSONObject(0)

                            val jsonArrayStatus = jsonArrayUser.getJSONObject("Статусы_проверки")
                            val status = jsonArrayStatus.getString("Название")
                            val jsonArrayRoles = jsonArrayUser.getJSONObject("Роли")
                            val role = jsonArrayRoles.getString("Название")
                            val comment = jsonArrayUser.getString("Комментарий")

                            val userData = UserDataClass(Статус_проверки = status, Роль = role, Комментарий = comment)
                            Log.e("USERS", userData.Роль.toString())

                            if(userData.Роль == "Пользователь") {
                                if(userData.Статус_проверки == "Заблокировано") {
                                    val dialog = AlertDialog.Builder(this@SplashScreenActivity)
                                        .setMessage(userData.Комментарий)
                                        .setTitle("Профиль заблокирован")
                                        .setPositiveButton("ОК") { _, _ ->
                                            // Действие при нажатии на кнопку "ОК"
                                            // Переходим на экран входа
                                            val mainIntent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
                                            startActivity(mainIntent)
                                            finish()

                                            sharedPreferences.edit().putString("TOKEN_USER", null).apply()
                                        }
                                        .setCancelable(false) // Позволяет закрыть диалог, нажав "Назад" или коснувшись вне диалога
                                        .create()

                                    dialog.setCanceledOnTouchOutside(false) // Закрывает диалог, когда пользователь касается вне диалога
                                    dialog.show()

                                }
                                else if (userData.Ник == null) {
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
                            else if(userData.Роль == "Администратор") {
                                val mainIntent = Intent(this@SplashScreenActivity, AdminActivity::class.java)
                                startActivity(mainIntent)
                                finish()
                            }

                        }
                        catch(e: Throwable) {
                            Log.e("ERROR_splash", e.toString())
                        }
                    }
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
        return user!= null
    }
}
