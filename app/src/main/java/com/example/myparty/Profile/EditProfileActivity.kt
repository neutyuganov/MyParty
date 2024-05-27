package com.example.myparty.Profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.Main.MainFragment
import com.example.myparty.MainActivity
import com.example.myparty.R
import com.example.myparty.databinding.ActivityEditProfileBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID


class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private lateinit var user: UserDataClass

    var image: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = View.VISIBLE
        binding.content.visibility = View.GONE
        binding.btnSave.visibility = View.INVISIBLE

        binding.btnGoBack.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            try{
                user = sb.from("Пользователи").select{
                    filter {
                        eq("id", sb.auth.currentUserOrNull()?.id.toString())
                    }
                }.decodeSingle()

                binding.textNick.setText(user.Ник)
                binding.textName.setText(user.Имя)
                binding.textDescription.setText(user.Описание)

                if(user.Фото != null) {
                    binding.imageUser.scaleType = ImageView.ScaleType.CENTER_CROP
                    binding.imageUser.setImageDrawable(null)

                    val bucket = sb.storage["images"]
                    image = bucket.downloadPublic(user.Фото.toString())
                    val is1: InputStream = ByteArrayInputStream(image)
                    val bmp: Bitmap = BitmapFactory.decodeStream(is1)
                    val dr = BitmapDrawable(resources, bmp)
                    binding.imageUser.setImageDrawable(dr)

                    binding.btnDelete.visibility = View.VISIBLE
                }
                binding.progressBarImage.visibility = View.GONE
            }
            catch (e: Throwable){
                Log.e("Error", e.toString())
            }
            finally {
                binding.progressBar.visibility = View.GONE
                binding.content.visibility = View.VISIBLE
                binding.btnSave.visibility = View.VISIBLE
            }
        }

        binding.textNick.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if(binding.textNick.text.toString().length > 10){
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
                if(binding.textName.text.toString().length > 10){
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
                if(binding.textDescription.text.toString().length > 150){
                    binding.containerDescription.isCounterEnabled = true
                }
            }

        })

        focusedListener(binding.containerNick, binding.textNick)
        focusedListener(binding.containerName, binding.textName)
        focusedListener(binding.containerDescription, binding.textDescription)

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

        binding.btnSave.setOnClickListener {
            takeHelperText(binding.containerNick, binding.textNick)
            takeHelperText(binding.containerName, binding.textName)
            takeHelperText(binding.containerDescription, binding.textDescription)

            if(binding.containerNick.helperText == null && binding.containerName.helperText == null && binding.containerDescription.helperText == null){

                binding.btnSave.isEnabled = false
                binding.content.alpha = 0.62f
                binding.progressBar.visibility = View.VISIBLE
                binding.btnGoBack.visibility = View.INVISIBLE

                try{
                    lifecycleScope.launch {
                        if (sb.postgrest["Пользователи"].select {
                                filter {
                                    eq("Ник", binding.textNick.text.toString())
                                }
                            }.decodeList<UserDataClass>().isNotEmpty() && binding.textNick.text.toString() != user.Ник){
                            binding.containerNick.helperText = "Такой пользователь уже существует"
                            binding.content.alpha = 1f
                            binding.progressBar.visibility = View.GONE
                        }
                        else{
                            var uuid: String? = null
                            try{
                                val imageDB = sb.postgrest["Пользователи"].select {
                                    filter {
                                        eq("id", sb.auth.currentUserOrNull()?.id.toString())
                                    }
                                }.decodeSingle<UserDataClass>().Фото

                                Log.e("imageDB", imageDB.toString())
                                Log.e("image", image.toString())

                                if(imageDB != null){
                                    if(image == null){
                                        uuid = null
                                        val bucket = sb.storage["images"]
                                        bucket.delete(imageDB)
                                    }
                                    else{
                                        uuid = imageDB
                                        val bucket = sb.storage.from("images")
                                        bucket.upload(uuid, image!!, upsert = true)
                                    }
                                }
                                else{
                                    if(image != null) {
                                        uuid = UUID.randomUUID().toString()
                                        val bucket = sb.storage.from("images")
                                        bucket.upload(uuid, image!!, upsert = true)
                                    }
                                    else{
                                        uuid = null
                                    }
                                }
                            }
                            catch (e: Throwable){
                                Log.e("Error!!!", e.toString())
                            }


                            /*if(image!= null){
                                if(imageDB.isEmpty()){
                                    uuid = UUID.randomUUID().toString()
                                    val bucket = sb.storage.from("images")
                                    bucket.upload(uuid!!, image!!, upsert = false)
                                }
                                else{
                                    uuid = imageDB
                                    val bucket = sb.storage.from("images")
                                    bucket.upload(uuid!!, image!!, upsert = true)
                                }
                            }
                            else{
                                uuid = null
                                if(imageDB.isNotEmpty()){
                                    val bucket = sb.storage.from("images")
                                    bucket.delete(imageDB)
                                }
                            }*/

                            val userAdd = UserDataClass(Ник = binding.textNick.text.toString(), Имя = binding.textName.text.toString(), Описание = if(binding.textDescription.text.toString().isEmpty()) null else binding.textDescription.text.toString(), id_статуса_проверки = 1, Фото = uuid)
                            sb.postgrest["Пользователи"].update(userAdd){
                                filter{
                                    eq("id", user.id.toString())
                                }
                            }
                            val intent = Intent(this@EditProfileActivity, MainActivity::class.java)
                            val fragment = ProfileFragment()
                            intent.putExtra("FRAGMENT", fragment.javaClass.name)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
                catch(e: Exception){
                    Log.e("Error create profile", e.toString())
                }
            }
        }
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
            else -> "ник"
        }

        container.helperText = validText(container, editText.text.toString(), type)
    }

    private fun validText(container: TextInputLayout, text: String, type: String): String? {
        val maxLength = container.counterMaxLength
        if(text.length > maxLength){
            return if(type == "ник"){
                "Слишком длинный $type"
            } else{
                "Слишком длинное $type"
            }
        }
        if(type == "имя или название организации" || type == "ник"){
            if(text.isEmpty()){
                return "Это обязательное поле"
            }
        }
        return null
    }
}