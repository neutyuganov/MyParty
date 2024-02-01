package com.example.myparty

import android.R.attr.value
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btReg: Button = findViewById(R.id.toReg)

        btReg.setOnClickListener{
            val myIntent = Intent(this, RegistrationActivity::class.java)
            startActivity(myIntent)
        }
    }
}