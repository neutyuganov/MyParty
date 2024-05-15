package com.example.myparty.StartActivities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.myparty.AdminActivity
import com.example.myparty.MainActivity
import com.example.myparty.SupabaseConnection
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var sharedPreferences: SharedPreferences

    private var user : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sb = SupabaseConnection.Singleton.sb

        sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

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

                        sharedPreferences.edit().putString("TOKEN_USER", sb.auth.currentUserOrNull()?.id.toString()).apply()

                        user = sharedPreferences.getString("TOKEN_USER", null)

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
                                    val dialog = AlertDialog.Builder(this@LoginActivity)
                                        .setMessage(userData.Комментарий)
                                        .setTitle("Профиль заблокирован")
                                        .setPositiveButton("ОК") { _, _ ->
                                            // Действие при нажатии на кнопку "ОК"
                                            // Переходим на экран входа

                                            sharedPreferences.edit().putString("TOKEN_USER", null).apply()
                                        }
                                        .setCancelable(true) // Позволяет закрыть диалог, нажав "Назад" или коснувшись вне диалога
                                        .create()

                                    dialog.setCanceledOnTouchOutside(true) // Закрывает диалог, когда пользователь касается вне диалога
                                    dialog.show()

                                }
                                else if (userData.Ник == null) {
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
                            else if(userData.Роль == "Администратор") {
                                val mainIntent = Intent(this@LoginActivity, AdminActivity::class.java)
                                startActivity(mainIntent)
                                finish()
                            }

                        }
                        catch(e: Throwable) {
                            Log.e("ERROR_splash", e.toString())
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