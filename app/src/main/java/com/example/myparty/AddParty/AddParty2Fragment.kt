package com.example.myparty.AddParty

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myparty.R
import com.example.myparty.databinding.FragmentAddParty2Binding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddParty2Fragment() : Fragment() {

    private lateinit var binding: FragmentAddParty2Binding

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddParty2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Создание переменной для SharedPreference
        sharedPreferences = requireActivity().getSharedPreferences("SHARED_PREFS_ADD_PARTY", Context.MODE_PRIVATE)

        if(sharedPreferences.contains("ADD_PARTY_SLOGAN")){
            binding.textSlogan.setText(sharedPreferences.getString("ADD_PARTY_SLOGAN", null))
            binding.textDescription.setText(sharedPreferences.getString("ADD_PARTY_DESCRIPTION", null))
        }

        focusedListener(binding.containerDescription, binding.textDescription)
        focusedListener(binding.containerSlogan, binding.textSlogan)

        binding.textSlogan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if(binding.textSlogan.text.toString().length > 70) {
                    binding.containerSlogan.isCounterEnabled = true
                }
            }

        })

        binding.textDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if(binding.textDescription.text.toString().length > 700) {
                    binding.containerDescription.isCounterEnabled = true
                }
            }

        })

        binding.btnGoNext.setOnClickListener {
            takeHelperText(binding.containerDescription, binding.textDescription)
            takeHelperText(binding.containerSlogan, binding.textSlogan)

            if(binding.containerDescription.helperText == null && binding.containerSlogan.helperText == null){
                sharedPreferences.edit().putString("ADD_PARTY_SLOGAN", binding.textSlogan.text.toString()).apply()
                sharedPreferences.edit().putString("ADD_PARTY_DESCRIPTION", binding.textDescription.text.toString()).apply()

                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame, AddParty3Fragment())
                transaction.addToBackStack(null)
                transaction.commit()
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
            else -> "слоган"
        }

        container.helperText = validText(container, editText.text.toString(), type)
    }

    private fun validText(container: TextInputLayout, text: String, type: String): String? {
        val maxLength = container.counterMaxLength
        if(text.length > maxLength){
            return if(type == "слоган"){
                "Слишком длинный $type"
            } else{
                "Слишком длинное $type"
            }
        }
        if(text.isEmpty()){
            return "Это обязательное поле"
        }

        return null
    }
}