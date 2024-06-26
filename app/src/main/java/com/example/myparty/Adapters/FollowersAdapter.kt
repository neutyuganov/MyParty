package com.example.myparty.Adapters


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.DataClasses.FollowersDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.DataClasses.UsersSubsDataClass
import com.example.myparty.ProfileOrganizator.ProfileOrganizatorActivity
import com.example.myparty.R
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ItemFollowersBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream


class FollowersAdapter (private val userList: List<UserDataClass>, private val coroutineScope: CoroutineScope) : RecyclerView.Adapter<FollowersAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemFollowersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding, coroutineScope)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: UserDataClass = userList[position]
        holder.bind(user)

        // Переход на страницу организатора при нажатии на item
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, ProfileOrganizatorActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

    class ViewHolder(private val itemBinding: ItemFollowersBinding, private val coroutineScope: CoroutineScope) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(user: UserDataClass) { with(itemBinding)
            {
                // Получение данных об авторизованном пользователе
                val currentUserId = sb.auth.currentUserOrNull()?.id!!
                val userId = user.id!!

                // Получение данных о пользователе
                name.text = user.Имя

                if(user.Верификация == false){
                    verify.visibility = View.INVISIBLE
                }

                coroutineScope.launch {
                    itemBinding.textCountFollowers.text = getFollowers(userId).toString()+ " подписчиков"
                }


                var subscribe = user.Статус_подписки!!
                checkFollowStatus(subscribe)

                coroutineScope.launch {
                    if(user.Фото != "null") {
                        image.scaleType = ImageView.ScaleType.CENTER_CROP
                        image.setImageDrawable(null)

                        val bucket = sb.storage["images"]
                        val bytes = bucket.downloadPublic(user.Фото.toString())
                        val is1: InputStream = ByteArrayInputStream(bytes)
                        val bmp: Bitmap = BitmapFactory.decodeStream(is1)
                        val dr = BitmapDrawable(itemBinding.root.context.resources, bmp)
                        image.setImageDrawable(dr)
                    }
                    progressBarImage.visibility = View.GONE
                }

                // Обработка нажатия на кнопку подписаться/отписаться
                btnSubscribe.setOnClickListener {
                    coroutineScope.launch {
                        try {
                            btnSubscribe.isEnabled = false
                            btnSubscribe.text = "Загрузка..."
                            // Получение статуса подписки пользователя
                            val isSubscribed = getFollowStatus(userId, currentUserId)

                            // Проверка наличия подписки
                            if (subscribe) {
                                // Проверка достоверности статуса подписки пользователя
                                if (subscribe == isSubscribed) {
                                    sb.from("Подписчики_пользователей").insert(
                                        UsersSubsDataClass(
                                            id_пользователя = userId,
                                            id_подписчика = currentUserId
                                        )
                                    )
                                } else {
                                    Toast.makeText(itemBinding.root.context, "Вы уже подписаны", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (subscribe == isSubscribed) {
                                    sb.from("Подписчики_пользователей").delete {
                                        filter {
                                            eq("id_пользователя", userId)
                                            eq("id_подписчика", currentUserId)
                                        }
                                    }
                                } else {
                                    Toast.makeText(itemBinding.root.context, "Вы уже отписались", Toast.LENGTH_SHORT).show()
                                }
                            }
                            subscribe = !subscribe

                            // Изменение индикации кнопки
                            textCountFollowers.text = getFollowers(userId).toString() + " подписчиков"
                            checkFollowStatus(subscribe)
                        } catch (e: Exception) {
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
            }.decodeList<FollowersDataClass>().isEmpty()
        }

        fun checkFollowStatus(status: Boolean) {
            with(itemBinding) {
                btnSubscribe.isEnabled = true

                if (!status) {
                    btnSubscribe.text = "Отписаться"
                    btnSubscribe.setBackgroundResource(R.drawable.button_secondary_color_selector)
                    itemBinding.btnSubscribe.setTextColor(itemView.context.getColor(R.color.secondary_text_color))
                } else {
                    btnSubscribe.text = "Подписаться"
                    btnSubscribe.setBackgroundResource(R.drawable.button_main_color_selector)
                    btnSubscribe.setTextColor(itemView.context.getColor(R.color.white))
                }
            }
        }

    }
}