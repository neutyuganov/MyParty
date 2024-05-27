package com.example.myparty.StartActivities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityRegistrationBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch


class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    private lateinit var sharedpreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedpreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        val spanText = SpannableString("С пользовательским соглашением ознакомлен(а)")

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // При клике на слово "правилами" запустите вторую активность
                val intent = Intent(this@RegistrationActivity, RulesActivity::class.java)
                startActivity(intent)
            }
        }
        spanText.setSpan(clickableSpan, 2, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Установите текст и кликабельность на определенном слове
        binding.checkBoxRules.setText(spanText)
        binding.checkBoxRules.setMovementMethod(LinkMovementMethod.getInstance())
        binding.checkBoxRules.setHighlightColor(Color.TRANSPARENT)

        // Проверка введенных данных
        focusedListener(binding.containerPasswordRepeat, binding.textPasswordRepeat)
        focusedListener(binding.containerEmail, binding.textEmail)
        focusedListener(binding.containerPassword, binding.textPassword)

        binding.goReg.setOnClickListener{
            // Проверка введенных данных
            takeHelperText(binding.containerPasswordRepeat, binding.textPasswordRepeat)
            takeHelperText(binding.containerEmail, binding.textEmail)
            takeHelperText(binding.containerPassword, binding.textPassword)

            // Проверка введенных данных
            if(validText(binding.textPasswordRepeat, 0) == null && validText(binding.textEmail, 2) == null && validText(binding.textPassword, 3) == null){

                if(!binding.checkBoxRules.isChecked){
                    binding.textError.visibility = View.VISIBLE
                }
                else{
                    binding.textError.visibility = View.GONE
                    // Создание корутина для взаимодействия с базой даных
                    lifecycleScope.launch {
                        try{
                            // Проверка на существование пользователя с введенным email
                            val users = sb.from("Пользователи").select{
                                filter {
                                    eq("Почта", binding.textEmail.text.toString().trim())
                                }
                            }.decodeList<UserDataClass>().count()

                            // Если пользователя с таким email не существует, то создается новый пользователь
                            if(users == 0){
                                sb.auth.signUpWith(Email) {
                                    email = binding.textEmail.text.toString().trim()
                                    password = binding.textPassword.text.toString().trim()
                                }

                                // Сохранение id пользователя в SharedPreference
                                sharedpreferences.edit().putString("TOKEN_USER", sb.auth.currentUserOrNull()?.id.toString()).apply()

                                val user = sb.auth.currentUserOrNull()

                                // Добавление данных пользователя в таблицу Пользователи
                                val userAdd = UserDataClass(id = user?.id.toString().trim(), id_статуса_проверки = 1, id_роли = 1,  Почта = user?.email.toString().trim())
                                sb.postgrest["Пользователи"].insert(userAdd)

                                Log.e("create profile", userAdd.toString())

                                // Переход на экран создания профиля пользователя
                                val myIntent = Intent(this@RegistrationActivity, CreateProfileActivity::class.java)
                                startActivity(myIntent)
                                finish()
                            }
                            // Если пользователь с таким email уже существует, то выводит подсказку
                            else {
                                binding.containerEmail.helperText = "Пользователь с таким email уже есть"
                                binding.textEmail.requestFocus()
                            }

                        }
                        catch (e: Exception){
                            Log.e("ERROR", e.message.toString())
                        }
                    }
                }
            }
        }

        // Переход на экран логина
        binding.toLogIn.setOnClickListener{
            val myIntent = Intent(this@RegistrationActivity, LoginActivity::class.java)
            startActivity(myIntent)
            finish()
        }
    }

    // Функция для проверки правильности введенных данных
    private fun focusedListener(container: TextInputLayout, editText: TextInputEditText) {
        editText.setOnFocusChangeListener{_, focused->
            if(!focused)
            {
                takeHelperText(container, editText)
            }
        }
    }

    // Функция для вывода подсказки
    private fun takeHelperText(container: TextInputLayout, editText: TextInputEditText){
        val type = when (editText) {
            binding.textEmail -> 2
            binding.textPassword -> 3
            else -> 0
        }

        container.helperText = validText(editText, type)
    }

    // Функция для возвращения текста подсказки
    private fun validText(editText: TextInputEditText, type: Int): String? {
        val text = editText.text.toString().trim()

        if(text.isEmpty()){
            return "Поле не должно быть пустым"
        }
        if(type == 0){
            if(text != binding.textPassword.text?.toString()){
                return "Пароли не совпадают"
            }
        }
        if(type == 2){
            if(!Patterns.EMAIL_ADDRESS.matcher(text).matches())
            {
                return "Неверный формат почты"
            }
        }
        if(type == 3){
            if(text.length < 6)
            {
                return "Пароль должен содержать минимум 6 символов"
            }
            if(!text.matches(".*[a-z, а-я].*".toRegex())){
                return "Пароль должен содержать буквы с нижним регистром"
            }
            if(!text.matches(".*[A-Z, А-Я].*".toRegex())){
                return "Пароль должен содержать буквы с верхним регистром"
            }
            if(!text.matches(".*[!@#\$&*].*".toRegex())){
                return "Пароль должен содержать специальные символы !@#\$&*"
            }
        }
        return null
    }
}

