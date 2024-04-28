package com.example.myparty

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.myparty.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Восстановление фрагмента, если он был сохранен
        if (savedInstanceState != null) {
            currentFragment?.let { fragment ->
                loadFragment(fragment)
            }
        } else {
            loadFragment(MainFragment())
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(MainFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Метод для загрузки фрагмента и сохранения его состояния
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        currentFragment = fragment
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
    }

    // Метод для сохранения состояния активности
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("current_fragment", currentFragment?.javaClass?.name)
    }

    // Метод для восстановления состояния активности
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val fragmentName = savedInstanceState.getString("current_fragment") ?: return
        try {
            val fragmentClass = Class.forName(fragmentName) as Class<out Fragment>
            val fragmentInstance = fragmentClass.newInstance()
            loadFragment(fragmentInstance)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}