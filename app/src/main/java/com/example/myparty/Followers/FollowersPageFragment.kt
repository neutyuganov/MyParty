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
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.R
import com.example.myparty.SupabaseConnection
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentFollowersPageBinding
import com.example.myparty.databinding.FragmentMainBinding
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

        val currentUserId = SupabaseConnection.Singleton.sb.auth.currentUserOrNull()?.id.toString()

        binding.progressBar.visibility = View.VISIBLE

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

                binding.recycler.adapter = FollowersAdapter(followers, coroutineScope)
            }
            catch (e: Throwable){
                Log.e("Ошибка получения данных", e.message.toString())
            }
            finally{
                binding.progressBar.visibility = View.GONE
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
}