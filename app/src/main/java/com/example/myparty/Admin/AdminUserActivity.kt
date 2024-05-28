package com.example.myparty.Admin

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.R
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityAdminUserBinding
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.ByteArrayInputStream
import java.io.InputStream


class AdminUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUserBinding

    var userId: String = ""

    lateinit var user: UserDataClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.content.visibility = View.GONE
        binding.btnCorrect.visibility = View.GONE
        binding.btnBan.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        binding.btnGoBack.setOnClickListener {
            finish()
        }

        binding.btnBan.setOnClickListener  {
            val dialog  = Dialog(this@AdminUserActivity)
            dialog.setContentView(R.layout.dialog_item_edittext)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val title  = dialog.findViewById<TextView>(R.id.title)
            val editText  = dialog.findViewById<EditText>(R.id.textBun)
            val btnCancel  = dialog.findViewById<Button>(R.id.btnCancel)
            val btnBun = dialog.findViewById<Button>(R.id.btnBun)

            title.text = "Блокировка пользователя"

            btnCancel.setOnClickListener  {
                dialog.dismiss()
            }

            btnBun.setOnClickListener {
                val reason = editText.text.toString().trim()
                if (reason.isNotEmpty()) {
                    btnCancel.isEnabled = false
                    btnBun.isEnabled  = false
                    editText.isEnabled = false
                    btnBun.text  =  "Загрузка..."
                    lifecycleScope.launch {
                        try {
                            sb.from("Пользователи").update(
                                PartyDataClass(
                                    id_статуса_проверки = 2,
                                    Комментарий = reason
                                )
                            ) {
                                filter {
                                    eq("id", userId)
                                }
                            }

                            val intent = Intent(this@AdminUserActivity, AdminActivity::class.java)
                            startActivity(intent)
                            finishAffinity()

                        } catch (e: Throwable) {
                            Log.e("Error", e.toString())
                            Toast.makeText(this@AdminUserActivity, "Что-то пошло не так", Toast.LENGTH_SHORT).show()
                            btnCancel.isEnabled = true
                            btnBun.isEnabled  = true
                            editText.isEnabled = true
                            btnBun.text  =  "Заблокировать"
                        }
                    }
                }
                else  {
                    Toast.makeText(this@AdminUserActivity, "Введите причину", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
        }

        binding.btnCorrect.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.content.alpha = 0.62f
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            lifecycleScope.launch {
                try{
                    sb.from("Пользователи").update(PartyDataClass(id_статуса_проверки = 3, Комментарий  =  null))  {
                        filter {
                            eq("id", userId)
                        }
                    }

                    val intent = Intent(this@AdminUserActivity, AdminActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
                catch (e: Throwable){
                    Log.e("Error", e.toString())
                    Toast.makeText(this@AdminUserActivity, "Что-то пошло не так", Toast.LENGTH_SHORT).show()

                    binding.progressBar.visibility = View.GONE
                    binding.content.alpha = 1f

                }
            }
        }

        // Получение переданной id вечеринки
        userId = intent.getStringExtra("USER_ID").toString()

        // Заполнение окна данными пользователя
        lifecycleScope.launch {
            try{
                user = loadUser()
                binding.apply {

                    name.text = user.Имя
                    nick.text = "@"+user.Ник
                    verify.isVisible = user.Верификация == true

                    if(user.Описание!= "null")  {
                        description.text = user.Описание
                    }
                    else  {
                        description.visibility = View.GONE
                        containerDescription.text = "Описание отсутствует"
                    }

                    content.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE

                    if(user.Фото != "null") {
                        progressBarImage.visibility = View.VISIBLE
                        image.scaleType = ImageView.ScaleType.CENTER_CROP
                        image.setImageDrawable(null)

                        val bucket = sb.storage["images"]
                        val bytes = bucket.downloadPublic(user.Фото.toString())
                        val is1: InputStream = ByteArrayInputStream(bytes)
                        val bmp: Bitmap = BitmapFactory.decodeStream(is1)
                        val dr = BitmapDrawable(resources, bmp)
                        image.setImageDrawable(dr)
                    }
                    progressBarImage.visibility = View.GONE
                    binding.btnCorrect.visibility = View.VISIBLE
                    binding.btnBan.visibility = View.VISIBLE

                }
            }
            catch (e: Throwable){
                Log.e("Error", e.toString())
            }
        }

    }

    suspend fun loadUser(): UserDataClass {
        val usersResult = sb.from("Пользователи").select {
            filter {
                eq("id", userId)
            }
        }.data

        val jsonObject = JSONArray(usersResult).getJSONObject(0)

        val id = jsonObject.getString("id")
        val name = jsonObject.getString("Имя")
        val nick = jsonObject.getString("Ник")
        val verify = jsonObject.getBoolean("Верификация")
        val description = jsonObject.getString("Описание")
        val image = jsonObject.getString("Фото")

        return UserDataClass(
            id = id,
            Имя = name,
            Ник = nick,
            Верификация  = verify,
            Описание = description,
            Фото = image
        )
    }
}