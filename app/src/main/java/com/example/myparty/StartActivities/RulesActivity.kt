package com.example.myparty.StartActivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myparty.R
import com.example.myparty.databinding.ActivityRegistrationBinding
import com.example.myparty.databinding.ActivityRulesBinding

class RulesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRulesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGoBack.setOnClickListener {
            finish()
        }
    }
}