package com.example.myparty

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.UserData
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.example.myparty.DataClasses.FollowersDataClass
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.PartyFavoriteDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.DataClasses.UsersSubsDataClass
import com.example.myparty.Profile.ActualPartyFragment
import com.example.myparty.Profile.BanPartyFragment
import com.example.myparty.Profile.BeforePartyFragment
import com.example.myparty.Profile.EditProfileActivity
import com.example.myparty.Profile.ViewPagerAdapter
import com.example.myparty.StartActivities.LoginActivity
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityPartyBinding
import com.example.myparty.databinding.ActivityProfileOrganizatorBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileOrganizatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileOrganizatorBinding

    private var userId: String? = null

    var userData: UserDataClass? = null
    var followersCount = 0
    var followingCount = 0
    var partyCount = 0
    var subsribe = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileOrganizatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("USER_ID")

        lifecycleScope.launch {
            try{
                userData = getUserData()
                followersCount = getFollowers()
                followingCount = getFollowing()
                partyCount = getParty()
                subsribe = getSub()
                loadUserData()
            }
            catch(e:Throwable){
                Log.e("ProfileFragment", e.message.toString())
            }
        }

        setupViewPager(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.btnSubscribe.setOnClickListener {
            try {
                if (subsribe) {
                    subsribe = false
                    binding.btnSubscribe.text = "Отписаться"
                    binding.btnSubscribe.background = resources.getDrawable(R.drawable.button_secondary_color_selector)
                    binding.btnSubscribe.setTextColor(resources.getColor(R.color.secondary_text_color))
                    lifecycleScope.launch {
                        sb.from("Подписчики_пользователей").insert(
                            UsersSubsDataClass(
                                id_пользователя = userId!!,
                                id_подписчика = sb.auth.currentUserOrNull()?.id!!
                            )
                        )
                        followersCount = getFollowers()
                        binding.countFollower.text = followersCount.toString()
                    }
                } else {
                    subsribe = true
                    binding.btnSubscribe.text = "Подписаться"
                    binding.btnSubscribe.background = resources.getDrawable(R.drawable.button_main_color_selector)
                    binding.btnSubscribe.setTextColor(resources.getColor(R.color.white))
                    lifecycleScope.launch {
                        sb.from("Подписчики_пользователей").delete {
                            filter {
                                eq("id_пользователя", userId!!)
                                eq("id_подписчика", sb.auth.currentUserOrNull()!!.id)
                            }
                        }
                        followersCount = getFollowers()
                        binding.countFollower.text = followersCount.toString()
                    }
                }
            }catch (e: Exception){
                Log.e("Ошибка добавления в избранное", e.message.toString())
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {

        val adapter = ViewPagerAdapter(supportFragmentManager)

        adapter.addFragment(ActualPartyFragment(userId!!), "Активные")
        adapter.addFragment(BeforePartyFragment(userId!!), "Прошедшие")
        viewPager.adapter = adapter
    }

    suspend fun getUserData(): UserDataClass = withContext(Dispatchers.IO) {
        sb.from("Пользователи").select {
            filter {
                eq("id", userId!!)
            }
        }.decodeSingle()
    }

    suspend fun getFollowers(): Int  = withContext(Dispatchers.IO) {
        sb.from("Подписчики_пользователей").select{
            filter {
                eq("id_пользователя", userId!!)
            }
        }.decodeList<FollowersDataClass>().count()
    }

    suspend fun getFollowing(): Int  = withContext(Dispatchers.IO) {
        sb.from("Подписчики_пользователей").select{
            filter {
                eq("id_подписчика", userId!!)
            }
        }.decodeList<FollowersDataClass>().count()
    }

    suspend fun getParty(): Int  = withContext(Dispatchers.IO) {
        sb.from("Вечеринки").select{
            filter {
                eq("id_пользователя", userId!!)
                eq("id_статуса_проверки", "3")
            }
        }.decodeList<PartyDataClass>().count()
    }

    suspend fun getSub(): Boolean  = withContext(Dispatchers.IO) {
        sb.from("Подписчики_пользователей").select{
            filter {
                eq("id_пользователя", userId!!)
                eq("id_подписчика", sb.auth.currentUserOrNull()!!.id)
            }
        }.decodeList<PartyDataClass>().isEmpty()
    }

    fun loadUserData() {
        binding.nameUser.text = userData?.Имя
        binding.nickUser.text = "@" + userData?.Ник
        if(userData?.Описание.isNullOrEmpty()) binding.descriptionUser.visibility = View.GONE else binding.descriptionUser.text = userData?.Описание
        binding.verifyUser.visibility = if (userData?.Верификация == true) View.VISIBLE else View.GONE
        binding.countFollower.text = followersCount.toString()
        binding.countFollowing.text = followingCount.toString()
        binding.countParty.text = partyCount.toString()
        if (subsribe == false) {
            binding.btnSubscribe.text = "Отписаться"
            binding.btnSubscribe.background = resources.getDrawable(R.drawable.button_secondary_color_selector)
            binding.btnSubscribe.setTextColor(resources.getColor(R.color.secondary_text_color))
        }
        else {
            binding.btnSubscribe.text = "Подписаться"
            binding.btnSubscribe.background = resources.getDrawable(R.drawable.button_main_color_selector)
            binding.btnSubscribe.setTextColor(resources.getColor(R.color.white))
        }

    }
}