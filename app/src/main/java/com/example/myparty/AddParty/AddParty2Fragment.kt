package com.example.myparty.AddParty

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myparty.databinding.FragmentAddParty2Binding

class AddParty2Fragment() : Fragment() {

    private lateinit var binding: FragmentAddParty2Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddParty2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textSlogan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun afterTextChanged(p0: Editable?) {
                if(binding.textSlogan.text.toString().length > 70) {
                    binding.containerSlogan.isCounterEnabled = true
                }
            }

        })

    }
}