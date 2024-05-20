package com.example.myparty.Adapters


import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.DataClasses.FollowersDataClass
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.DataClasses.UsersSubsDataClass
import com.example.myparty.ProfileOrganizatorActivity
import com.example.myparty.R
import com.example.myparty.SupabaseConnection
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FollowersItemBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FollowersAdapter (private val userList: List<UserDataClass>, private val coroutineScope: CoroutineScope) : RecyclerView.Adapter<FollowersAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FollowersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding, coroutineScope)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: UserDataClass = userList[position]
        holder.bind(user)

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, ProfileOrganizatorActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

    class ViewHolder(private val itemBinding: FollowersItemBinding, private val coroutineScope: CoroutineScope) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(user: UserDataClass) { with(itemBinding)
            {
                name.text = user.Имя

                if(user.Верификация == false){
                    verify.visibility = View.INVISIBLE
                }

                var subscribe: Boolean = false

                var countFollowers: Int = 0

                coroutineScope.launch {
                    countFollowers = getFollowers(user.id!!)
                    textCountFollowers.text = countFollowers.toString() + " подписчиков"
                    textCountFollowers.visibility = View.VISIBLE
                }

                coroutineScope.launch {
                    subscribe = getFollowStatus(user.id!!, sb.auth.currentUserOrNull()?.id!!)
                    checkFollow(subscribe)
                    btnSubscribe.visibility = View.VISIBLE
                }

                btnSubscribe.setOnClickListener {
                    coroutineScope.launch {
                        try {
                            Log.e("Подписчик", subscribe.toString())
                            if (subscribe) {
                                if(subscribe != getFollowStatus(user.id!!, sb.auth.currentUserOrNull()?.id!!)){
                                    Toast.makeText(itemBinding.root.context, "Вы уже подписаны", Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    sb.from("Подписчики_пользователей").insert(
                                        UsersSubsDataClass(
                                            id_пользователя = user.id,
                                            id_подписчика = sb.auth.currentUserOrNull()?.id!!
                                        )
                                    )
                                }
                                subscribe =!subscribe
                                checkFollow(subscribe)

                                textCountFollowers.text = getFollowers(user.id!!).toString() + " подписчиков"
                            } else {
                                if(subscribe != getFollowStatus(user.id!!, sb.auth.currentUserOrNull()?.id!!)){
                                    Toast.makeText(itemBinding.root.context, "Вы уже отписались", Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    sb.from("Подписчики_пользователей").delete {
                                        filter {
                                            eq("id_пользователя", user.id)
                                            eq("id_подписчика", sb.auth.currentUserOrNull()!!.id)
                                        }
                                    }
                                }
                                subscribe =!subscribe
                                checkFollow(subscribe)

                                textCountFollowers.text = getFollowers(user.id!!).toString() + " подписчиков"
                            }
                        }catch (e: Exception){
                            Log.e("Ошибка добавления в избранное", e.message.toString())
                        }
                    }
                }
            }
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
            }.decodeList<PartyDataClass>().isEmpty()
        }

        fun checkFollow(status: Boolean) {
            if (!status) {
                itemBinding.btnSubscribe.text = "Отписаться"
                itemBinding.btnSubscribe.setBackgroundResource(R.drawable.button_secondary_color_selector)
//                        btnSubscribe.setTextColor(R.color.secondary_text_color)
            }
            else {
                itemBinding.btnSubscribe.text = "Подписаться"
                itemBinding.btnSubscribe.setBackgroundResource(R.drawable.button_main_color_selector)
//                    btnSubscribe.setTextColor(R.color.white)
            }
        }
    }
}