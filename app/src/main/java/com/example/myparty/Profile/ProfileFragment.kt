package com.example.myparty.Profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.example.myparty.DataClasses.FollowersDataClass
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.StartActivities.LoginActivity
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.databinding.FragmentProfileBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var sharedPreferences: SharedPreferences

    private var user: String? = null

    var userData: UserDataClass? = null
    var followersCount = 0
    var followingCount = 0
    var partyCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        binding = FragmentProfileBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        user = sharedPreferences.getString("TOKEN_USER", null)

        lifecycleScope.launch {
            try{
                userData = getUserData()
                followersCount = getFollowers()
                followingCount = getFollowing()
                partyCount = getParty()
                loadUserData()
            }
            catch(e:Throwable){
                Log.e("ProfileFragment", e.message.toString())
            }
        }

        setupViewPager(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.btnGoEditProfile.setOnClickListener {
            val myIntent = Intent(context, EditProfileActivity::class.java)
            startActivity(myIntent)
        }

        binding.btnLogOut.setOnClickListener {
            sharedPreferences.edit().putString("TOKEN_USER", null).apply()
            lifecycleScope.launch {
                sb.auth.signOut()
                val myIntent = Intent(context, LoginActivity::class.java)
                startActivity(myIntent)
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {

        val adapter = ViewPagerAdapter(childFragmentManager)

        adapter.addFragment(ActualPartyFragment(user!!), "Активные")
        adapter.addFragment(BeforePartyFragment(user!!), "Прошедшие")

        lifecycleScope.launch {
            try{
                if(sb.from("Вечеринки").select {
                        filter {
                            eq("id_пользователя", user!!)
                            eq("id_статуса_проверки", "2")
                        }
                    }.decodeList<PartyDataClass>().isNotEmpty()) adapter.addFragment(BanPartyFragment(), "Заблокированные")
            }
            catch(e:Throwable){
                Log.e("ProfileFragment", e.message.toString())
            }
            viewPager.adapter = adapter
        }
        viewPager.adapter = adapter
    }

    suspend fun getUserData(): UserDataClass = withContext(Dispatchers.IO) {
        sb.from("Пользователи").select {
            filter {
                eq("id", user!!)
            }
        }.decodeSingle()
    }

    suspend fun getFollowers(): Int  = withContext(Dispatchers.IO) {
        sb.from("Подписчики_пользователей").select{
            filter {
                eq("id_пользователя", user!!)
            }
        }.decodeList<FollowersDataClass>().count()
    }

    suspend fun getFollowing(): Int  = withContext(Dispatchers.IO) {
        sb.from("Подписчики_пользователей").select{
            filter {
                eq("id_подписчика", user!!)
            }
        }.decodeList<FollowersDataClass>().count()
    }

    suspend fun getParty(): Int  = withContext(Dispatchers.IO) {
        sb.from("Вечеринки").select{
            filter {
                eq("id_пользователя", user!!)
                eq("id_статуса_проверки", "3")
            }
        }.decodeList<PartyDataClass>().count()
    }

    fun loadUserData() {
        binding.nameUser.text = userData?.Имя
        binding.nickUser.text = "@" + userData?.Ник
        if(userData?.Описание.isNullOrEmpty()) binding.descriptionUser.visibility = View.GONE else binding.descriptionUser.text = userData?.Описание
        binding.verifyUser.visibility = if (userData?.Верификация == true) View.VISIBLE else View.GONE
        binding.countFollower.text = followersCount.toString()
        binding.countFollowing.text = followingCount.toString()
        binding.countParty.text = partyCount.toString()
    }

}