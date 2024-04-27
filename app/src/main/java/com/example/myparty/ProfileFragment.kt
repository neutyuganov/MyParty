package com.example.myparty

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myparty.databinding.FragmentMainBinding
import com.example.myparty.databinding.FragmentProfileBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import java.time.LocalDate


class ProfileFragment : Fragment() {

    private var _binding:FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        _binding = FragmentProfileBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sb = SupabaseConnection.Singleton.sb

        val user = sb.auth.currentUserOrNull()

        lifecycleScope.launch{
            try{
                val users = sb.from("Пользователи").select{
                    filter {
                        eq("id", user?.id.toString())
                    }
                }.decodeSingle<UserDataClass>()

                binding.nameUser.text = users.Имя
                binding.nickUser.text = "@" + users.Ник
                binding.descriptionUser.text = users.Описание
                if(users.Верификация == true){
                    binding.verifyUser.visibility = View.VISIBLE
                }
                else{
                    binding.verifyUser.visibility = View.GONE
                }
            }
            catch (e: Exception){
                Log.e("Error", "Error: ${e.message}")
            }
        }

        lifecycleScope.launch {
            val followersCount = sb.from("Подписчики_пользователей").select{
                filter {
                    eq("id_пользователя", user?.id.toString())
                }
            }.decodeList<FollowersDataClass>().count()

            binding.countFollower.text = followersCount.toString()
        }

        lifecycleScope.launch {
            val followingCount = sb.from("Подписчики_пользователей").select{
                filter {
                    eq("id_подписчика", user?.id.toString())
                }
            }.decodeList<FollowersDataClass>().count()

            binding.countFollowing.text = followingCount.toString()
        }

        lifecycleScope.launch {
            val partyCount = sb.from("Вечеринки").select{
                filter {
                    eq("id_пользователя", user?.id.toString())
                }
            }.decodeList<FollowersDataClass>().count()

            binding.countParty.text = partyCount.toString()
        }
    }

}