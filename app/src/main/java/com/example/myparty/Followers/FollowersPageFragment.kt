package com.example.myparty.Followers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.Adapters.FollowersAdapter
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.R
import com.example.myparty.SupabaseConnection
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentFollowersPageBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import com.faltenreich.skeletonlayout.createSkeleton
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

//        binding.progressBar.visibility = View.VISIBLE

        skeleton = binding.recycler.applySkeleton(R.layout.followers_item, 3)

        skeleton.maskCornerRadius = 30f
        skeleton.showSkeleton()

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
                    val follower = UserDataClass(id = userId, Имя = userName, Верификация = userVerify)

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


//                binding.progressBar.visibility = View.GONE
                if(followers.isEmpty()){
                    binding.textView.visibility = View.VISIBLE
//                    binding.recycler.visibility = View.GONE
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
}