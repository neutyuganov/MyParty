package com.example.myparty.Admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myparty.Adapters.AdminPartyAdapter
import com.example.myparty.Adapters.AdminUserAdapter
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.R
import com.example.myparty.SkeletonClass
import com.example.myparty.SupabaseConnection
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentAdminUsersBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray


class AdminUsersFragment : Fragment() {

    private lateinit var binding: FragmentAdminUsersBinding

    private val users = mutableListOf<UserDataClass>()

    private lateinit var skeleton: Skeleton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        binding = FragmentAdminUsersBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        skeleton = binding.recycler.applySkeleton(R.layout.item_users_skeleton, 10)

        SkeletonClass().skeletonShow(skeleton, resources)

        lifecycleScope.launch {
            try{
                val usersResult = sb.from("Пользователи").select {
                    filter {
                        eq("id_статуса_проверки", 1)
                    }
                }.data

                val jsonArrayUsers = JSONArray(usersResult)

                for (i in 0 until jsonArrayUsers.length()) {
                    val jsonObject = jsonArrayUsers.getJSONObject(i)
                    val id = jsonObject.getString("id")
                    val name = jsonObject.getString("Имя")
                    val nick = jsonObject.getString("Ник")
                    val verify = jsonObject.getBoolean("Верификация")
                    val description= jsonObject.getString("Описание")
                    val image = jsonObject.getString("Фото")

                    val user = UserDataClass(
                        id = id,
                        Имя = name,
                        Верификация   = verify,
                        Ник = nick,
                        Описание  = description,
                        Фото  = image
                    )

                    users.add(user)
                }

                val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
                val partyAdapter = AdminUserAdapter(users, coroutineScope)
                binding.recycler.adapter = partyAdapter
            }
            catch (e: Throwable){
                Log.d("AdminPartiesFragment", e.toString())
                Toast.makeText(context, "Что-то пошло не так", Toast.LENGTH_LONG).show()
            }
            finally {
                if (users.isEmpty()) {
                    binding.textView.visibility = View.VISIBLE
                    binding.recycler.visibility = View.GONE
                }
            }
        }
    }
}