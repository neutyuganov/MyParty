package com.example.myparty.Profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.example.myparty.Adapters.ViewPagerAdapter
import com.example.myparty.DataClasses.FollowersDataClass
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.StartActivities.LoginActivity
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.R
import com.example.myparty.databinding.FragmentProfileBinding
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream


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

        binding.progressBar.visibility = View.VISIBLE
        binding.content.visibility = View.GONE
        binding.btnLogOut.visibility = View.INVISIBLE

        lifecycleScope.launch {
            try{
                userData = getUserData()
                followersCount = getFollowers()
                followingCount = getFollowing()
                partyCount = getParty()

                loadUserData()

                binding.content.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE

                if(userData?.Фото != null) {
                    binding.imageUser.scaleType = ImageView.ScaleType.CENTER_CROP
                    binding.imageUser.setImageDrawable(null)

                    val bucket = sb.storage["images"]
                    val bytes = bucket.downloadPublic(userData?.Фото.toString())
                    val is1: InputStream = ByteArrayInputStream(bytes)
                    val bmp: Bitmap = BitmapFactory.decodeStream(is1)
                    val dr = BitmapDrawable(resources, bmp)
                    binding.imageUser.setImageDrawable(dr)
                }

                binding.progressBarImage.visibility = View.GONE

                setupViewPager(binding.viewPager)
                binding.tabLayout.setupWithViewPager(binding.viewPager)

                binding.btnLogOut.visibility = View.VISIBLE
            }
            catch(e:Throwable){
                Log.e("ProfileFragment вывод данных", e.message.toString())
            }
        }

        val balloon = Balloon.Builder(requireContext())
            .setWidth(BalloonSizeSpec.WRAP)
            .setHeight(BalloonSizeSpec.WRAP)
            .setTextColorResource(R.color.main_text_color)
            .setTextSize(12f)
            .setMarginHorizontal(10)
            .setMarginBottom(5)
            .setTextTypeface(resources.getFont(R.font.rubik_medium))
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(7)
            .setPaddingVertical(4)
            .setPaddingHorizontal(8)
            .setCornerRadius(10f)
            .setBackgroundColorResource(R.color.stroke_color)
            .setBalloonAnimation(BalloonAnimation.FADE)

        binding.verifyUser.setOnClickListener {

            balloon.setText("Подтвержденная организация")

            lifecycleScope.launch {
                balloon.build().showAlignTop(binding.verifyUser)
            }
        }

        binding.nickUser.setOnClickListener {

            if(userData?.id_статуса_проверки == 1) balloon.setText("Профиль на проверке")
            else balloon.setText("Профиль подтвержен")

            lifecycleScope.launch {
                balloon.build().showAlignTop(binding.nickUser)
            }
        }

        binding.btnGoEditProfile.setOnClickListener {
            val myIntent = Intent(context, EditProfileActivity::class.java)
            startActivity(myIntent)
        }

        binding.btnLogOut.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.content.alpha = 0.62f
            requireActivity(). window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            val sharedPreferencesFilter = requireActivity().getSharedPreferences("SHARED_PREFS_FILTER", Context.MODE_PRIVATE)
            val sharedPreferencesAddParty = requireActivity().getSharedPreferences("SHARED_PREFS_ADD_PARTY", Context.MODE_PRIVATE)

            sharedPreferences.edit().clear().apply()
            sharedPreferencesFilter.edit().clear().apply()
            sharedPreferencesAddParty.edit().clear().apply()

            lifecycleScope.launch {
                sb.auth.signOut()
                val myIntent = Intent(context, LoginActivity::class.java)
                startActivity(myIntent)
                requireActivity().finishAffinity()
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {

        val adapter = ViewPagerAdapter(childFragmentManager)

        adapter.addFragment(ActualPartyFragment(), "Активные")
        adapter.addFragment(BeforePartyFragment(), "Прошедшие")

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

    suspend fun loadUserData() {
        binding.nameUser.text = userData?.Имя
        binding.nickUser.text = "@" + userData?.Ник
        if(userData?.id_статуса_проверки == 1) binding.nickUser.setTextColor(requireActivity().getColor(R.color.yellow))
        if(userData?.Описание.isNullOrEmpty()) binding.descriptionUser.visibility = View.GONE else binding.descriptionUser.text = userData?.Описание
        binding.verifyUser.visibility = if (userData?.Верификация == true) View.VISIBLE else View.GONE
        binding.countFollower.text = followersCount.toString()
        binding.countFollowing.text = followingCount.toString()
        binding.countParty.text = partyCount.toString()
    }

}