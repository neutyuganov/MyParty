package com.example.myparty.ProfileOrganizator

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myparty.Adapters.PartyAdapter
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.R
import com.example.myparty.SkeletonClass
import com.example.myparty.SupabaseConnection
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentActualPartyBinding
import com.example.myparty.databinding.FragmentProfileOrganizatorBeforePartyBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate


class ProfileOrganizatorBeforePartyFragment : Fragment() {

    private lateinit var binding: FragmentProfileOrganizatorBeforePartyBinding

    lateinit var userId: String

    private lateinit var skeleton: Skeleton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileOrganizatorBeforePartyBinding.inflate(inflater, container, false)

        userId = arguments?.getString("userId").toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        skeleton = binding.recycler.applySkeleton(R.layout.item_party_skeleton, 3)

        SkeletonClass().skeletonShow(skeleton, resources)

        lifecycleScope.launch {
            getParties(userId)
        }

        binding.swipe.setOnRefreshListener {
            binding.textView.visibility = View.INVISIBLE
            binding.recycler.visibility = View.VISIBLE

            SkeletonClass().skeletonShow(skeleton, resources)

            lifecycleScope.launch {
                getParties(userId)
                binding.swipe.isRefreshing = false
            }
        }
    }

    suspend fun getParties(userId: String){
        val parties = mutableListOf<PartyDataClass>()
        try{
            val partiesResult = sb.from("Вечеринки").select(Columns.raw("*, Возрастное_ограничение(Возраст), Статусы_проверки(Название)")){
                filter {
                    lt("Дата", LocalDate.now())
                    eq("id_пользователя", userId)
                    neq("id_статуса_проверки", "2")
                }
            } .data

            val jsonArray = JSONArray(partiesResult)

            val partiesFavoritesResult = sb.from("Избранные_вечеринки").select() {
                filter {
                    eq("id_пользователя", sb.auth.currentUserOrNull()?.id.toString())
                }
            }.data

            val jsonArrayFavorites = JSONArray(partiesFavoritesResult)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.getInt("id")
                val userId = jsonObject.getString("id_пользователя")
                val name = jsonObject.getString("Название")
                val date = jsonObject.getString("Дата")
                val time = jsonObject.getString("Время")
                val place = jsonObject.getString("Место")
                val price = jsonObject.getDouble("Цена")
                val ageObject = jsonObject.getJSONObject("Возрастное_ограничение")
                val image = jsonObject.getString("Фото")
                val age = ageObject.getInt("Возраст")
                val statusObject = jsonObject.getJSONObject("Статусы_проверки")
                val status = statusObject.getString("Название")
                var favorite = false
                for (j in 0 until jsonArrayFavorites.length()) {
                    val jsonObjectFavorites = jsonArrayFavorites.getJSONObject(j)
                    if (jsonObjectFavorites.getInt("id_вечеринки") == id) {
                        favorite = true
                    }
                }
                val event = PartyDataClass(id = id, Название = name, id_пользователя = userId, Дата = date, Время = time, Место = place, Цена = price, Возраст = age, Статус_проверки = status, Фото = image, Избранное = favorite)
                parties.add(event)
            }

            val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
            val partyAdapter = PartyAdapter(parties, coroutineScope)
            binding.recycler.adapter = partyAdapter
        }
        catch (e: Throwable){
            Log.e("Ошибка получения данных вечеринки", e.message.toString())
        }
        finally{
            if(parties.isEmpty()){
                binding.textView.visibility = View.VISIBLE
                binding.recycler.visibility = View.GONE
            }
        }
    }
}