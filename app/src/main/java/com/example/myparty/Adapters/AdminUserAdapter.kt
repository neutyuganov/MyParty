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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.myparty.Admin.AdminUserActivity
import com.example.myparty.DataClasses.FollowersDataClass
import com.example.myparty.PartyActivity
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.PartyFavoriteDataClass
import com.example.myparty.DataClasses.UserDataClass
import com.example.myparty.DataClasses.UsersSubsDataClass
import com.example.myparty.ProfileOrganizator.ProfileOrganizatorActivity
import com.example.myparty.R
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.ItemFollowersBinding
import com.example.myparty.databinding.ItemPartyBinding
import com.example.myparty.databinding.ItemUsersBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class AdminUserAdapter (private val userList: List<UserDataClass>, private val coroutineScope: CoroutineScope) : RecyclerView.Adapter<AdminUserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemUsersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding, coroutineScope)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: UserDataClass = userList[position]
        holder.bind(user)

        // Переход на страницу организатора при нажатии на item
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, AdminUserActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

    class ViewHolder(private val itemBinding: ItemUsersBinding, private val coroutineScope: CoroutineScope) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(user: UserDataClass) {
            with(itemBinding)
            {
                // Вывод данных пользователя
                name.text = user.Имя
                nick.text  = "@"+user.Ник
                verify.isVisible = user.Верификация == true

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
            }
        }
    }
}