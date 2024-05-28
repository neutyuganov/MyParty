package com.example.myparty.AddParty

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myparty.R
import com.example.myparty.databinding.ActivityAddPartyBinding

class AddPartyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPartyBinding

    private lateinit var currentfragment: Fragment

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPartyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Создание переменной для SharedPreference
        sharedPreferences = this.getSharedPreferences("SHARED_PREFS_ADD_PARTY", Context.MODE_PRIVATE)

        if(sharedPreferences.all.isNotEmpty()) {
            val dialog  = Dialog(this@AddPartyActivity)
            dialog.setContentView(R.layout.dialog_item_add_party)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)

            val btnAgree = dialog.findViewById<Button>(R.id.btnAgree)
            val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

            btnAgree.setOnClickListener  {
                dialog.dismiss()
                if(sharedPreferences.contains("ADD_PARTY_CITY")){
                    loadFragment(AddParty4Fragment())
                }
                else if(sharedPreferences.contains("ADD_PARTY_DATE")){
                    loadFragment(AddParty3Fragment())
                }
                else if(sharedPreferences.contains("ADD_PARTY_SLOGAN")){
                    loadFragment(AddParty2Fragment())
                }
                else if(sharedPreferences.contains("ADD_PARTY_NAME")){
                    loadFragment(AddParty1Fragment())
                }
            }

            btnCancel.setOnClickListener  {
                dialog.dismiss()
                loadFragment(AddParty1Fragment())
                sharedPreferences.edit().clear().apply()
            }

            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }

        if (savedInstanceState == null) {
            loadFragment(AddParty1Fragment())
        }

        binding.btnGoBack.setOnClickListener {
            val fragmentName = supportFragmentManager.findFragmentById(R.id.frame)?.javaClass?.name
            val fragmentClass = Class.forName(fragmentName!!).newInstance() as Fragment
            when(fragmentClass) {
                is AddParty5Fragment -> loadFragment(AddParty4Fragment())
                is AddParty4Fragment -> loadFragment(AddParty3Fragment())
                is AddParty3Fragment -> loadFragment(AddParty2Fragment())
                is AddParty2Fragment -> loadFragment(AddParty1Fragment())
                is AddParty1Fragment -> finish()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.run {
            putString("fragment", currentfragment.javaClass.name)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val fragmentName = savedInstanceState.getString("fragment")
        val fragmentClass = Class.forName(fragmentName!!).newInstance() as Fragment
        loadFragment(fragmentClass)

    }

    fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        currentfragment = fragment
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
    }
}