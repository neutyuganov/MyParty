package com.example.myparty.StartActivities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myparty.AdminActivity
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.MainActivity
import com.example.myparty.SupabaseConnection
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
            // Проверяем, авторизован ли пользователь
            if (isUserAuthenticated()) {
                lifecycleScope.launch {
                    withTimeout(5000L) {
                        try {
                            // Получаем данные пользователя
                            val users = SupabaseConnection.Singleton.sb.from("Пользователи").select(
                                Columns.raw("*, Роли(Название), Статусы_проверки(Название)")){
                                filter {
                                    eq("id",user!!)
                                }
                            }.data

                            // Обрабатываем полученные данные пользователя
                            val jsonArrayUser = JSONArray(users).getJSONObject(0)

                            val jsonArrayStatus = jsonArrayUser.getJSONObject("Статусы_проверки")
                            val status = jsonArrayStatus.getString("Название")
                            val jsonArrayRoles = jsonArrayUser.getJSONObject("Роли")
                            val role = jsonArrayRoles.getString("Название")
                            val comment = jsonArrayUser.getString("Комментарий")

                            // Добавляем данные пользоателя в UserDataClass
                            val userData = UserDataClass(Статус_проверки = status, Роль = role, Комментарий = comment)
                            Log.e("USERS", userData.Роль.toString())

                            // Проверка роли пользователя
                            if(userData.Роль == "Пользователь") {
                                // Если роль - Пользователь
                                if(userData.Статус_проверки == "Заблокировано") {
                                    // Если пользователь заблокирован - показываем диалог с сообщением
                                    val dialog = AlertDialog.Builder(this@SplashScreenActivity)
                                        .setMessage(userData.Комментарий)
                                        .setTitle("Профиль заблокирован")
                                        .setPositiveButton("ОК") { _, _ ->
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
                                // Проверка наличия информации о пользователе
                                else if (userData.Ник == null) {
                                    // Если информации о пользователе нет - переходим на экран создания профиля
                                    val mainIntent = Intent(this@SplashScreenActivity, CreateProfileActivity::class.java)
                                    startActivity(mainIntent)
                                    finish()
                                }
                                else {
                                    // Если пользователь авторизован и имеет информацию о себе - переходим на главный экран
                                    val mainIntent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                                    startActivity(mainIntent)
                                    finish()
                                }
                            }
                            else if(userData.Роль == "Администратор") {
                                // Если роль - Администратор
                                val mainIntent = Intent(this@SplashScreenActivity, AdminActivity::class.java)
                                startActivity(mainIntent)
                                finish()
                            }
                        }
                        catch(e: Throwable) {
                            Toast.makeText(this@SplashScreenActivity, "Проверьте подключение к интернету", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }

            } else {
                // Если пользователь не авторизован - переходим на экран входа
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
                finish()
            }
        }, 2000) // Запускаем таймер
    }

    // Метод проверяем, авторизован ли пользователь
    private fun isUserAuthenticated(): Boolean {
        return user!= null
    }
}
