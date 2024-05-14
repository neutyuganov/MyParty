package com.example.myparty

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
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentProfileBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    val user = sb.auth.currentUserOrNull()

    private lateinit var sharedpreferences: SharedPreferences

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

        sharedpreferences = requireActivity().getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        binding.btnLogOut.setOnClickListener {
            sharedpreferences.edit().putString("TOKEN_USER", null).apply()
            lifecycleScope.launch {
                sb.auth.signOut()
                val myIntent = Intent(context, LoginActivity::class.java)
                startActivity(myIntent)
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(ActualPartyFragment(), "Активные")
        adapter.addFragment(BeforePartyFragment(), "Прошедшие")
        viewPager.adapter = adapter
    }

    suspend fun getUserData(): UserDataClass = withContext(Dispatchers.IO) {
        sb.from("Пользователи").select {
            filter {
                eq("id", user?.id.toString())
            }
        }.decodeSingle()
    }

    suspend fun getFollowers(): Int  = withContext(Dispatchers.IO) {
        sb.from("Подписчики_пользователей").select{
            filter {
                eq("id_пользователя", user?.id.toString())
            }
        }.decodeList<FollowersDataClass>().count()
    }

    suspend fun getFollowing(): Int  = withContext(Dispatchers.IO) {
        sb.from("Подписчики_пользователей").select{
            filter {
                eq("id_подписчика", user?.id.toString())
            }
        }.decodeList<FollowersDataClass>().count()
    }

    suspend fun getParty(): Int  = withContext(Dispatchers.IO) {
        sb.from("Вечеринки").select{
            filter {
                eq("id_пользователя", user?.id.toString())
            }
        }.decodeList<PartyDataClass>().count()
    }

    fun loadUserData() {
        binding.nameUser.text = userData?.Имя
        binding.nickUser.text = "@" + userData?.Ник
        binding.descriptionUser.text = userData?.Описание
        binding.verifyUser.visibility = if (userData?.Верификация == true) View.VISIBLE else View.GONE
        binding.countFollower.text = followersCount.toString()
        binding.countFollowing.text = followingCount.toString()
        binding.countParty.text = partyCount.toString()
    }

}