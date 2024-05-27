package com.example.myparty.Admin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.example.myparty.Adapters.ViewPagerAdapter
import com.example.myparty.Followers.FollowersPageFragment
import com.example.myparty.StartActivities.LoginActivity
import com.example.myparty.SupabaseConnection
import com.example.myparty.databinding.ActivityAdminBinding
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch


class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        setupViewPager(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.btnLogOut.setOnClickListener {
            binding.btnLogOut.isEnabled = false
            binding.content.alpha = 0.62f
            binding.progressBar.visibility = View.VISIBLE
            sharedPreferences.edit().putString("TOKEN_USER", null).apply()
            lifecycleScope.launch {
                SupabaseConnection.Singleton.sb.auth.signOut()
                val myIntent = Intent(this@AdminActivity, LoginActivity::class.java)
                startActivity(myIntent)
                finishAffinity()
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {

        val adapter = ViewPagerAdapter(supportFragmentManager)

        adapter.addFragment(AdminPartiesFragment(), "Вечеринки")
        adapter.addFragment(AdminUsersFragment(), "Пользователи")

        viewPager.adapter = adapter
    }
}