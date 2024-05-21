package com.example.myparty.Followers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myparty.Adapters.FollowersAdapter
import com.example.myparty.DataClasses.FollowersDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.R
import com.example.myparty.SkeletonClass
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentFollowersPageBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class FollowersPageFragment : Fragment() {

    private lateinit var binding: FragmentFollowersPageBinding

    private lateinit var skeleton: Skeleton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFollowersPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = sb.auth.currentUserOrNull()?.id.toString()

        skeleton = binding.recycler.applySkeleton(R.layout.item_followers_skeleton, 3)

        SkeletonClass().skeletonShow(skeleton, resources)

        Log.e("USERCURRENT", currentUserId)

        val followers = mutableListOf<UserDataClass>()

        lifecycleScope.launch {
            try{
                val followersResult = sb.from("Подписчики_пользователей").select{
                    filter {
                        eq("id_пользователя", currentUserId)
                    }
                }.data

                val jsonArray = JSONArray(followersResult)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val userId = jsonObject.getString("id_подписчика")
                    val userData = getUserData(userId)
                    val userName = userData.Имя
                    val userVerify = userData.Верификация ?: false
                    val countFollowers = getFollowers(userId)
                    val isFollow = getFollowStatus(userId, currentUserId)
                    val follower = UserDataClass(id = userId, Имя = userName, Верификация = userVerify, Количество_подписчиков = countFollowers, Статус_подписки = isFollow)

                    followers.add(follower)
                }

                Log.e("Подписчик_вывод", followers.toString())

                val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

                skeleton.showOriginal()
                binding.recycler.adapter = FollowersAdapter(followers, coroutineScope)

            }
            catch (e: Throwable){
                Log.e("Ошибка получения данных", e.message.toString())
            }
            finally{
                if(followers.isEmpty()){
                    binding.textView.visibility = View.VISIBLE
                    binding.recycler.visibility = View.GONE
                }
            }
        }
    }

    suspend fun getUserData(userId: String): UserDataClass = withContext(Dispatchers.IO) {
        sb.from("Пользователи").select{
            filter {
                eq("id", userId)
            }
        }.decodeSingle()
    }

    suspend fun getFollowers(userId: String): Int  = withContext(Dispatchers.IO) {
        sb.from("Подписчики_пользователей").select{
            filter {
                eq("id_пользователя", userId)
            }
        }.decodeList<FollowersDataClass>().count()
    }

    suspend fun getFollowStatus(userId: String, followerId: String): Boolean  = withContext(
        Dispatchers.IO) {
        sb.from("Подписчики_пользователей").select{
            filter {
                eq("id_пользователя", userId)
                eq("id_подписчика", followerId)
            }
        }.decodeList<FollowersDataClass>().isEmpty()
    }
}