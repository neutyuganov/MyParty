package com.example.myparty.Followers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myparty.Adapters.FollowersAdapter
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentFollowingPageBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class FollowingPageFragment : Fragment() {

    private lateinit var binding: FragmentFollowingPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFollowingPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = sb.auth.currentUserOrNull()?.id.toString()

        Log.e("USERCURRENT", currentUserId)

        val following = mutableListOf<UserDataClass>()

        lifecycleScope.launch {
            try{
                val followersResult = sb.from("Подписчики_пользователей").select{
                    filter {
                        eq("id_подписчика", currentUserId)
                    }
                }.data

                val jsonArray = JSONArray(followersResult)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val userId = jsonObject.getString("id_пользователя")
                    val userData = getUserData(userId)
                    val userName = userData.Имя
                    val userVerify = userData.Верификация ?: false
                    val follower = UserDataClass(id = userId, Имя = userName, Верификация = userVerify)

                    following.add(follower)
                }

                Log.e("Подписчик_вывод", following.toString())

                val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

                binding.recycler.adapter = FollowersAdapter(following, coroutineScope)
            }
            catch (e: Throwable){
                Log.e("Ошибка получения данных", e.message.toString())
            }
            finally{
                if(following.isEmpty()){
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