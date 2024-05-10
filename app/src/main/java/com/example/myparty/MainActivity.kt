package com.example.myparty

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.myparty.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mainFragment: Fragment = MainFragment()
    private var profileFragment: Fragment = ProfileFragment()
    private lateinit var currentFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(savedInstanceState == null){
            loadFragment(mainFragment)

//            currentFragment = mainFragment
//            supportFragmentManager.beginTransaction().apply {
//                add(R.id.frame, mainFragment, "MainFragment")
//                add(R.id.frame, profileFragment, "ProfileFragment").hide(profileFragment)
//            }.commit()
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(mainFragment)
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(profileFragment)
                    true
                }
                R.id.nav_add_party -> {
                    val myIntent = Intent(this, AddPartyActivity::class.java)
                    startActivity(myIntent)
                    false
                }
                else -> false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putString("fragment", currentFragment.javaClass.name)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val fragmentName = savedInstanceState.getString("fragment")
        val fragmentClass = Class.forName(fragmentName!!).newInstance() as Fragment

        loadFragment(fragmentClass)
    }

    // Метод для загрузки фрагмента и сохранения его состояния
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
//        transaction.hide(currentFragment).show(fragment).commit()
        currentFragment = fragment
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
    }
}