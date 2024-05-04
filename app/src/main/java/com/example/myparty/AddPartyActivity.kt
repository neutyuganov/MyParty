package com.example.myparty

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.myparty.databinding.ActivityAddPartyBinding
import com.example.myparty.databinding.ActivityMainBinding

class AddPartyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPartyBinding

    private lateinit var currentfragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPartyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(AddParty1Fragment())
        }

        binding.btnGoNext.setOnClickListener {
            when(currentfragment) {
                is AddParty1Fragment -> loadFragment(AddParty2Fragment())
                is AddParty2Fragment -> loadFragment(AddParty3Fragment())
                is AddParty3Fragment -> loadFragment(AddParty4Fragment())
                is AddParty4Fragment -> {
                    loadFragment(AddParty5Fragment())
                    binding.btnGoNext.text = "Завершить"
                }
            }
        }

        binding.btnGoBack.setOnClickListener {
            when(currentfragment) {
                is AddParty5Fragment -> {
                    loadFragment(AddParty4Fragment())
                    binding.btnGoNext.text = "Продолжить"
                }
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

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        currentfragment = fragment
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
    }
}