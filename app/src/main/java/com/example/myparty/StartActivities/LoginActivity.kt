package com.example.myparty.StartActivities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.myparty.Admin.AdminActivity
import com.example.myparty.MainActivity
import com.example.myparty.SupabaseConnection
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.R
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
            if(validText(binding.textEmail.text.toString().trim(), 1) == null && validText(binding.textPassword.text.toString().trim(), 0) == null){
                lifecycleScope.launch {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.content.alpha = 0.62f
                    binding.toReg.isEnabled = false
                    binding.goLogIn.isEnabled = false
                    try{
                        sb.auth.signInWith(Email){
                            email = binding.textEmail.text.toString().trim()
                            password = binding.textPassword.text.toString().trim()
                        }

                        sharedPreferences.edit().putString("TOKEN_USER", sb.auth.currentUserOrNull()?.id.toString()).apply()

                        user = sharedPreferences.getString("TOKEN_USER", null)

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
                                    val dialog  = Dialog(this@LoginActivity)
                                    dialog.setContentView(R.layout.dialog_item)
                                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                    dialog.setCancelable(false)

                                    val textComment  = dialog.findViewById<TextView>(R.id.comment)
                                    val btnOk = dialog.findViewById<Button>(R.id.btnOk)

                                    textComment.setText("Причина: " + users.Комментарий)

                                    btnOk.setOnClickListener  {
                                        dialog.dismiss()
                                        sharedPreferences.edit().putString("TOKEN_USER", null).apply()
                                        binding.progressBar.visibility = View.GONE
                                        binding.content.alpha = 1f
                                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    }

                                    dialog.setCanceledOnTouchOutside(false)

                                    dialog.show()
                                }
                                else if (users.Ник == null) {
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
                            else if(users.id_роли == 2) {
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
                            binding.progressBar.visibility = View.GONE
                            binding.content.alpha = 1f
                            binding.toReg.isEnabled = true
                            binding.goLogIn.isEnabled = true
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

        container.helperText = validText(editText.text.toString().trim(), type)
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