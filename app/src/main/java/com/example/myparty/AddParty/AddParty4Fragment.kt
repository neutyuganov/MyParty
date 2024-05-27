package com.example.myparty.AddParty

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myparty.R
import com.example.myparty.databinding.FragmentAddParty4Binding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddParty4Fragment() : Fragment() {
    private lateinit var binding: FragmentAddParty4Binding

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddParty4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Создание переменной для SharedPreference
        sharedPreferences = requireActivity().getSharedPreferences("SHARED_PREFS_ADD_PARTY", Context.MODE_PRIVATE)

        if(sharedPreferences.contains("ADD_PARTY_CITY")){
            binding.textCity.setText(sharedPreferences.getString("ADD_PARTY_CITY", null))
            binding.textPlace.setText(sharedPreferences.getString("ADD_PARTY_PLACE", null))
        }

        focusedListener(binding.containerCity, binding.textCity)
        focusedListener(binding.containerPlace, binding.textPlace)

        binding.btnGoNext.setOnClickListener {
            takeHelperText(binding.containerCity, binding.textCity)
            takeHelperText(binding.containerPlace, binding.textPlace)

            if(binding.containerCity.helperText == null && binding.containerPlace.helperText == null){
                sharedPreferences.edit().putString("ADD_PARTY_CITY", binding.textCity.text.toString().trim()).apply()
                sharedPreferences.edit().putString("ADD_PARTY_PLACE", binding.textPlace.text.toString().trim()).apply()

                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame, AddParty5Fragment())
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
        container.helperText = validText(editText.text.toString().trim())
    }

    private fun validText(text: String): String? {
        if(text.isEmpty()){
            return "Это обязательное поле"
        }
        return null
    }
}