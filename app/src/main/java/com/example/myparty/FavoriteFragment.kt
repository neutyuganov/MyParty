package com.example.myparty

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myparty.Adapters.PartyAdapter
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.databinding.FragmentFavoriteBinding
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

class FavoriteFragment : Fragment() {

    private lateinit var binding: FragmentFavoriteBinding

    private lateinit var skeleton: Skeleton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        skeleton = binding.recycler.applySkeleton(R.layout.item_party_skeleton, 6)

        SkeletonClass().skeletonShow(skeleton, resources)

        val parties = mutableListOf<PartyDataClass>()

        lifecycleScope.launch {
            try {
                val partiesResult = SupabaseConnection.Singleton.sb.from("Вечеринки")
                    .select(Columns.raw("*, Возрастное_ограничение(Возраст), Пользователи(Имя, Верификация)")) {
                        filter {
                            eq("id_статуса_проверки", 3)
                        }
                    }.data
                val jsonArrayParties = JSONArray(partiesResult)

                val partiesFavoritesResult = SupabaseConnection.Singleton.sb.from("Избранные_вечеринки").select() {
                    filter {
                        eq("id_пользователя", SupabaseConnection.Singleton.sb.auth.currentUserOrNull()?.id.toString())
                    }
                }.data
                val jsonArrayFavorites = JSONArray(partiesFavoritesResult)

                for (i in 0 until jsonArrayParties.length()) {
                    val jsonObject = jsonArrayParties.getJSONObject(i)
                    val id = jsonObject.getInt("id")
                    val name = jsonObject.getString("Название")
                    val date = jsonObject.getString("Дата")
                    val time = jsonObject.getString("Время")
                    val place = jsonObject.getString("Место")
                    val price = jsonObject.getDouble("Цена")
                    val userId = jsonObject.getString("id_пользователя")
                    val ageObject = jsonObject.getJSONObject("Возрастное_ограничение")
                    val age = ageObject.getInt("Возраст")
                    val usersObject = jsonObject.getJSONObject("Пользователи")
                    val userName = usersObject.getString("Имя")
                    val userVerify = usersObject.getBoolean("Верификация")
                    for (j in 0 until jsonArrayFavorites.length()) {
                        val jsonObjectFavorites = jsonArrayFavorites.getJSONObject(j)
                        if (jsonObjectFavorites.getInt("id_вечеринки") == id) {
                            val favorite = true
                            val event = PartyDataClass(
                                id = id,
                                Название = name,
                                id_пользователя = userId,
                                Имя = userName,
                                Дата = date,
                                Время = time,
                                Место = place,
                                Цена = price,
                                Возраст = age,
                                Верификация = userVerify,
                                Избранное = favorite
                            )
                            parties.add(event)
                        }
                    }
                }

                val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
                val partyAdapter = PartyAdapter(parties, coroutineScope)
                binding.recycler.adapter = partyAdapter

            } catch (e: Throwable) {
                Log.e("Ошибка получения данных вечеринки", e.message.toString())
            } finally {
                if(parties.isEmpty()){
                    binding.textView.visibility = View.VISIBLE
                    binding.recycler.visibility = View.GONE
                }
            }
        }
    }


}