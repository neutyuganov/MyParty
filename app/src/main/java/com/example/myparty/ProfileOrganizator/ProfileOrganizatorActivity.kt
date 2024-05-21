package com.example.myparty.ProfileOrganizator

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.example.myparty.DataClasses.FollowersDataClass
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.DataClasses.UsersSubsDataClass
import com.example.myparty.Adapters.ViewPagerAdapter
import com.example.myparty.R
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ActivityProfileOrganizatorBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.createSkeleton
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream


class ProfileOrganizatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileOrganizatorBinding

    private var userId: String? = null

    var userData: UserDataClass? = null
    var followersCount = 0
    var followingCount = 0
    var partyCount = 0
    var subsribe = false

    var fragmentActual = ProfileOrganizatorActualPartyFragment()
    var fragmentBefore = ProfileOrganizatorBeforePartyFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileOrganizatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("USER_ID")

        binding.content.visibility = View.INVISIBLE

        lifecycleScope.launch {
            try{
                userData = getUserData()
                followersCount = getFollowers()
                followingCount = getFollowing()
                partyCount = getParty()
                subsribe = getSub()
                loadUserData()

                setupViewPager(binding.viewPager)
                binding.tabLayout.setupWithViewPager(binding.viewPager)

                binding.content.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
            catch(e:Throwable){
                Log.e("ProfileFragment", e.message.toString())
            }
        }

        binding.btnGoBack.setOnClickListener {
            finish()
        }

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

        val args = Bundle()

        args.putString("userId", userId!!) // Записываем аргумент в Bundle

        fragmentActual.arguments = args
        fragmentBefore.arguments = args

        adapter.addFragment(fragmentActual, "Активные")
        adapter.addFragment(fragmentBefore, "Прошедшие")
        viewPager.adapter = adapter
    }

    suspend fun getUserData(): UserDataClass = withContext(Dispatchers.IO){
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

    suspend fun loadUserData() {
        binding.nameUser.text = userData?.Имя
        binding.nickUser.text = "@" + userData?.Ник
        if(userData?.Описание.isNullOrEmpty()) binding.descriptionUser.visibility = View.GONE else binding.descriptionUser.text = userData?.Описание
        binding.verifyUser.visibility = if (userData?.Верификация == true) View.VISIBLE else View.GONE

        val bucket = sb.storage["images"]
        val bytes = bucket.downloadPublic(userData?.Фото.toString())
        val is1: InputStream = ByteArrayInputStream(bytes)
        val bmp: Bitmap = BitmapFactory.decodeStream(is1)
        val dr = BitmapDrawable(resources, bmp)
        binding.imageUser.setImageDrawable(dr)

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