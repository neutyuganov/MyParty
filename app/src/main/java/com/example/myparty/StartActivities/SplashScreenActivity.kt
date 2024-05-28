package com.example.myparty.StartActivities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myparty.Admin.AdminActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        val user = sharedPreferences.getString("TOKEN_USER", null)

        Log.e("USER", user.toString())

        Handler(Looper.getMainLooper()).postDelayed({
            // Проверяем, авторизован ли пользователь
            if (user!= null) {
                lifecycleScope.launch {
                    withTimeout(5000L) {
                        try {
                            // Проверяем, авторизован ли пользователь
                            val users = sb.from("Пользователи").select(
                                Columns.raw("*, Роли(Название), Статусы_проверки(Название)")){
                                filter {
                                    eq("id",user!!)
                                }
                            }.decodeSingle<UserDataClass>()

                            if(users.id_роли == 1) {
                                if(users.id_статуса_проверки == 2) {
                                    val dialog  = Dialog(this@SplashScreenActivity)
                                    dialog.setContentView(R.layout.dialog_item)
                                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                    dialog.setCancelable(false)

                                    val textComment  = dialog.findViewById<TextView>(R.id.comment)
                                    val btnOk = dialog.findViewById<Button>(R.id.btnOk)

                                    textComment.setText("Причина: " + users.Комментарий)

                                    btnOk.setOnClickListener  {
                                        dialog.dismiss()
                                        sharedPreferences.edit().putString("TOKEN_USER", null).apply()
                                        val mainIntent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
                                        startActivity(mainIntent)
                                        finish()
                                    }

                                    dialog.setCanceledOnTouchOutside(false)

                                    dialog.show()
                                }
                                else if (users.Ник == null) {
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
                            else if(users.id_роли == 2) {
                                val mainIntent = Intent(this@SplashScreenActivity, AdminActivity::class.java)
                                startActivity(mainIntent)
                                finish()
                            }
                        }
                        catch(e: Throwable) {

                            Log.e("ERROR", e.toString())
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
}
