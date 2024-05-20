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
import com.example.myparty.databinding.FragmentFollowersBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FollowersFragment : Fragment() {

    private lateinit var binding: FragmentFollowersBinding

    private lateinit var sharedPreferences: SharedPreferences

    private var user: String? = null

    var userData: UserDataClass? = null
    var followersCount = 0
    var followingCount = 0
    var partyCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        binding = FragmentFollowersBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        user = sharedPreferences.getString("TOKEN_USER", null)

//        binding.content.visibility = View.INVISIBLE

        lifecycleScope.launch {
            try{
                followersCount = getFollowers()
                followingCount = getFollowing()

                loadUserData()

//                binding.content.visibility = View.VISIBLE
//                binding.progressBar.visibility = View.GONE
            }
            catch(e:Throwable){
                Log.e("Ошибка получения данных FollowersFragment", e.message.toString())
            }
        }

        setupViewPager(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)


    }

    private fun setupViewPager(viewPager: ViewPager) {

        val adapter = ViewPagerAdapter(childFragmentManager)

        adapter.addFragment(ActualPartyFragment(user!!), "Подписчики")
        adapter.addFragment(BeforePartyFragment(user!!), "Подписки")

        lifecycleScope.launch {
            try{
                if(sb.from("Вечеринки").select {
                        filter {
                            eq("id_пользователя", user!!)
                            eq("id_статуса_проверки", "2")
                        }
                    }.decodeList<PartyDataClass>().isNotEmpty()) adapter.addFragment(BanPartyFragment(), "Заблокированные")
                viewPager.adapter = adapter
            }
            catch(e:Throwable){
                Log.e("ProfileFragment заполнение ViewPager", e.message.toString())
            }

        }
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

    }

}