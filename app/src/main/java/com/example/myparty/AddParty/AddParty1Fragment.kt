package com.example.myparty.AddParty
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myparty.R
import com.example.myparty.databinding.FragmentAddParty1Binding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class AddParty1Fragment : Fragment() {

    private lateinit var binding: FragmentAddParty1Binding

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddParty1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Создание переменной для SharedPreference
        sharedPreferences = requireActivity().getSharedPreferences("SHARED_PREFS_ADD_PARTY", Context.MODE_PRIVATE)

        if(sharedPreferences.contains("ADD_PARTY_NAME")){
            binding.textName.setText(sharedPreferences.getString("ADD_PARTY_NAME", null))
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

        binding.btnGoNext.setOnClickListener {
            takeHelperText(binding.containerName, binding.textName)

            if(binding.containerName.helperText == null){
                sharedPreferences.edit().putString("ADD_PARTY_NAME", binding.textName.text.toString()).apply()

                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame, AddParty2Fragment())
                transaction.addToBackStack(null)
                transaction.commit()
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
}