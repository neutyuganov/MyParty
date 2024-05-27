package com.example.myparty.StartActivities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.example.myparty.MainActivity
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.R
import com.example.myparty.databinding.ActivityCreateProfileBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID


class CreateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateProfileBinding

    private lateinit var sharedPreferences: SharedPreferences

    var image: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        val user = sharedPreferences.getString("TOKEN_USER", null)

        binding.textNick.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if(binding.textNick.text.toString().trim().length > 10){
                    binding.containerNick.isCounterEnabled = true
                }
            }

        })

        binding.textName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if(binding.textName.text.toString().trim().length > 10){
                    binding.containerName.isCounterEnabled = true
                }
            }

        })

        binding.textDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


            }

            override fun afterTextChanged(p0: Editable?) {
                if(binding.textDescription.text.toString().trim().length > 150){
                    binding.containerDescription.isCounterEnabled = true
                }
            }

        })

        binding.imageUser.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        binding.btnDelete.setOnClickListener {
            binding.imageUser.scaleType = ImageView.ScaleType.CENTER
            binding.imageUser.setImageResource(R.drawable.plus)
            image = null

            binding.btnDelete.visibility = View.GONE
        }

        focusedListener(binding.containerNick, binding.textNick)
        focusedListener(binding.containerName, binding.textName)

        binding.btnCreateProfile.setOnClickListener {
            takeHelperText(binding.containerNick, binding.textNick)
            takeHelperText(binding.containerName, binding.textName)
            takeHelperText(binding.containerDescription, binding.textDescription)

            if(binding.containerNick.helperText == null && binding.containerName.helperText == null){

                binding.btnCreateProfile.isEnabled = false
                binding.content.alpha = 0.62f
                binding.progressBar.visibility = View.VISIBLE

                lifecycleScope.launch {
                    try{
                        if (sb.postgrest["Пользователи"].select {
                                filter {
                                    eq("Ник", binding.textNick.text.toString().trim())
                                }
                            }.decodeList<UserDataClass>().isNotEmpty()){
                            binding.containerNick.helperText = "Такой пользователь уже существует"

                            binding.btnCreateProfile.isEnabled = true
                            binding.content.alpha = 1f
                            binding.progressBar.visibility = View.GONE
                        }
                        else{
                            var uuid: String? = null

                            if(image!= null){
                                uuid = UUID.randomUUID().toString()
                                val bucket = sb.storage.from("images")
                                bucket.upload(uuid, image!!, upsert = false)
                            }
                            else{
                                uuid = null
                            }

                            val userAdd = UserDataClass(
                                Ник = binding.textNick.text.toString().trim(),
                                Имя = binding.textName.text.toString().trim(),
                                Описание = if(binding.textDescription.text.toString().trim().isEmpty() ) null else binding.textDescription.text.toString().trim(),
                                id_статуса_проверки = 1,
                                Верификация = false,
                                Фото = uuid
                            )
                            sb.postgrest["Пользователи"].update(userAdd){
                                filter{
                                    eq("id", user!!)
                                }
                            }
                            val mainIntent = Intent(this@CreateProfileActivity, MainActivity::class.java)
                            startActivity(mainIntent)
                            finish()
                        }
                    }
                    catch(e: Throwable){
                        Log.e("Error create profile", e.toString())
                    }
                }
            }
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
            binding.textDescription -> "описание"
            binding.textName -> "имя или название организации"
            else -> "короткое имя"
        }

        container.helperText = validText(container, editText.text.toString().trim(), type)
    }

    private fun validText(container: TextInputLayout, text: String, type: String): String? {
        val maxLength = container.counterMaxLength
        if(text.length > maxLength){
            return "Слишком длинное $type"
        }
        if(type == "имя или название организации" || type == "короткое имя"){
            if(text.isEmpty()){
                return "Это обязательное поле"
            }
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {

            try{
                val selectedImage = data?.data

                // Получаем bitmap из URI
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                image = baos.toByteArray()

                binding.imageUser.setImageBitmap(bitmap)

                binding.imageUser.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.btnDelete.visibility = View.VISIBLE
            }
            catch (e: Exception){
                Log.d("AddParty1Fragment", "onActivityResult: ${e.message}")
            }

        }
    }
}