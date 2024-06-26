package com.example.myparty.Profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
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
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.regex.Pattern


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
        binding.verifyTg.visibility = View.INVISIBLE

        val spanText = SpannableString("Свяжитесь с нами в Telegram, для получения статуса верефицированной организации")

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val telegram = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/m_p_support"))
                telegram.setPackage("org.telegram.messenger")
                startActivity(telegram)
            }
        }
        spanText.setSpan(clickableSpan, 19, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Установите текст и кликабельность на определенном слове
        binding.verifyTg.setText(spanText)
        binding.verifyTg.setMovementMethod(LinkMovementMethod.getInstance())
        binding.verifyTg.setHighlightColor(Color.TRANSPARENT)

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
                binding.verifyTg.visibility = View.VISIBLE
            }
        }

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

        focusedListener(binding.containerNick, binding.textNick)
        focusedListener(binding.containerName, binding.textName)
        focusedListener(binding.containerDescription, binding.textDescription)

        // Загрузка изображения из галереи при нажатии на кнопку
        binding.imageUser.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
            binding.imageUser.isEnabled = false
        }

        // Удаление изображения при нажатии на кнопку
        binding.btnDelete.setOnClickListener {
            binding.imageUser.scaleType = ImageView.ScaleType.CENTER
            binding.imageUser.setImageResource(R.drawable.plus)
            image = null

            binding.btnDelete.visibility = View.GONE
        }

        val balloon = Balloon.Builder(this)
            .setWidth(BalloonSizeSpec.WRAP)
            .setHeight(BalloonSizeSpec.WRAP)
            .setTextColorResource(R.color.main_text_color)
            .setTextSize(12f)
            .setMarginRight(10)
            .setMarginLeft(10)
            .setTextTypeface(resources.getFont(R.font.rubik_medium))
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(7)
            .setPaddingVertical(4)
            .setPaddingHorizontal(8)
            .setCornerRadius(10f)
            .setBackgroundColorResource(R.color.stroke_color)
            .setBalloonAnimation(BalloonAnimation.FADE)

        binding.infoDescription.setOnClickListener() {
            balloon.setText("Информация о пользователе,\nнапример о себе или о своей организации")

            lifecycleScope.launch {
                balloon.build().showAlignBottom(binding.infoDescription)
            }
        }

        binding.infoNick.setOnClickListener {
            balloon.setText("Узнаваемый идентификатор пользователя,\nнапример @my_party")

            lifecycleScope.launch {
                balloon.build().showAlignTop(binding.infoNick)
            }
        }

        binding.btnSave.setOnClickListener {
            takeHelperText(binding.containerNick, binding.textNick)
            takeHelperText(binding.containerName, binding.textName)
            takeHelperText(binding.containerDescription, binding.textDescription)

            if(binding.containerNick.helperText == null && binding.containerName.helperText == null && binding.containerDescription.helperText == null){

                binding.progressBar.visibility = View.VISIBLE
                binding.content.alpha = 0.62f
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                try{
                    lifecycleScope.launch {

                        // Проверка на наличие дубликатов ника
                        if (sb.postgrest["Пользователи"].select {
                                filter {
                                    eq("Ник", binding.textNick.text.toString().trim())
                                }
                            }.decodeList<UserDataClass>().isNotEmpty() && binding.textNick.text.toString().trim() != user.Ник){
                            binding.containerNick.helperText = "Такой пользователь уже существует"

                            binding.progressBar.visibility = View.GONE
                            binding.content.alpha = 1f
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                        else{
                            var uuid: String? = null
                            try{

                                // Проверка наличия фото пользователя
                                val imageDB = sb.postgrest["Пользователи"].select {
                                    filter {
                                        eq("id", sb.auth.currentUserOrNull()?.id.toString())
                                    }
                                }.decodeSingle<UserDataClass>().Фото

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

                            // Обновление данных пользователя
                            val userAdd = UserDataClass(Ник = binding.textNick.text.toString().trim().toLowerCase(), Имя = binding.textName.text.toString().trim(), Описание = if(binding.textDescription.text.toString().trim().isEmpty()) null else binding.textDescription.text.toString().trim(), id_статуса_проверки = 1, Фото = uuid)
                            sb.postgrest["Пользователи"].update(userAdd){
                                filter{
                                    eq("id", user.id.toString())
                                }
                            }
                            val intent = Intent(this@EditProfileActivity, MainActivity::class.java)
                            val fragment = ProfileFragment()
                            intent.putExtra("FRAGMENT", fragment.javaClass.name)
                            startActivity(intent)
                            finishAffinity()
                        }
                    }
                }
                catch(e: Exception){
                    Log.e("Error create profile", e.toString())

                    binding.progressBar.visibility = View.GONE
                    binding.content.alpha = 1f
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        }
    }

    // Метод определяющий обработку полученной картинки из галереи
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            try{
                val selectedImage = data?.data

                // Получаем bitmap из URI
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

                // Переформатирование картинки в массив байтов
                image = baos.toByteArray()

                // Присваивание выбранной картинки из галереии в imageUser
                binding.imageUser.setImageBitmap(bitmap)

                // Изменение интерфейса при загрузке картинки
                binding.imageUser.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.btnDelete.visibility = View.VISIBLE
                binding.imageUser.isEnabled = true
            }
            catch (e: Exception){
                Log.d("Ошибка загрузки изображения", "onActivityResult: ${e.message}")
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

        container.helperText = validText(container, editText.text.toString().trim(), type)
    }

    private fun validText(container: TextInputLayout, text: String, type: String): String? {
        val maxLength = container.counterMaxLength
        if(type == "ник"){
            if(!Pattern.compile("^[a-zA-Z1-9]+$").toRegex().matches(text)) {
                return "Можно использовать только латинские буквы"
            }
        }
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