package com.example.myparty.AddParty
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.myparty.R
import com.example.myparty.databinding.FragmentAddParty1Binding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream


class AddParty1Fragment : Fragment() {

    private lateinit var binding: FragmentAddParty1Binding

    private lateinit var sharedPreferences: SharedPreferences

    var image: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddParty1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Создание переменной для SharedPreference
        sharedPreferences = requireActivity().getSharedPreferences("SHARED_PREFS_ADD_PARTY", Context.MODE_PRIVATE)

        if(sharedPreferences.contains("ADD_PARTY_NAME")){
            try{
                binding.textName.setText(sharedPreferences.getString("ADD_PARTY_NAME", null))

                image = sharedPreferences.getString("ADD_PARTY_IMAGE", null)

                val b1: ByteArray = Base64.decode(image, Base64.DEFAULT)
                val bitmap1 = BitmapFactory.decodeByteArray(b1, 0, b1.size)
                binding.btnAddImage.setImageBitmap(bitmap1)

                binding.btnAddImage.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.btnDelete.visibility = View.VISIBLE
            }
            catch (e: Throwable){
                Log.d("AddParty1Fragment", "onViewCreated: ${e.message}")
            }


        }

        binding.textName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if(binding.textName.text.toString().length > 15){
                    binding.containerName.isCounterEnabled = true
                }
            }
        })

        binding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        binding.btnDelete.setOnClickListener {
            binding.btnAddImage.scaleType = ImageView.ScaleType.CENTER
            binding.btnAddImage.setImageResource(R.drawable.plus)
            image = null

            binding.btnDelete.visibility = View.GONE
        }

        binding.btnGoNext.setOnClickListener {
            takeHelperText(binding.containerName, binding.textName)

            if(binding.containerName.helperText == null && image != null){
                sharedPreferences.edit().putString("ADD_PARTY_NAME", binding.textName.text.toString()).apply()
                // Сохраняем bitmap в SharedPreferences
                sharedPreferences.edit().putString("ADD_PARTY_IMAGE", image.toString()).apply()

                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame, AddParty2Fragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == 100 && resultCode == RESULT_OK) {

                try{
                    val selectedImage = data?.data

                    // Получаем bitmap из URI
                    val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImage)
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val b = baos.toByteArray()
                    image = Base64.encodeToString(b, Base64.DEFAULT)

                    val b1: ByteArray = Base64.decode(image, Base64.DEFAULT)
                    val bitmap1 = BitmapFactory.decodeByteArray(b1, 0, b1.size)
                    binding.btnAddImage.setImageBitmap(bitmap1)


                    binding.btnAddImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    binding.btnDelete.visibility = View.VISIBLE
                }
                catch (e: Exception){
                    Log.d("AddParty1Fragment", "onActivityResult: ${e.message}")
                }

            }
        }
    }

    private fun takeHelperText(container: TextInputLayout, editText: TextInputEditText){
        container.helperText = validText(container, editText.text.toString())
    }

    private fun validText(container: TextInputLayout, text: String): String? {
        val maxLength = container.counterMaxLength
        if(text.length > maxLength){
            return "Слишком длинное название"
        }
        if(text.length < 3){
            return "Слишком короткое название"
        }
        if(text.isEmpty()){
            return "Это обязательное поле"
        }
        return null
    }
