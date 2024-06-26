package com.example.myparty.Main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myparty.Adapters.PartyAdapter
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.DataClasses.PartyFavoriteDataClass
import com.example.myparty.R
import com.example.myparty.SkeletonClass
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentMainBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate;
import org.json.JSONArray
import java.util.Locale


class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    private lateinit var skeleton: Skeleton

    private lateinit var sharedPreferences: SharedPreferences

    private var jsonArrayParties = JSONArray()
    private var jsonArrayFavorites = JSONArray()

    private var partiesWithSearch: List<PartyDataClass>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val parties = mutableListOf<PartyDataClass>()

        skeleton = binding.recycler.applySkeleton(R.layout.item_party_skeleton, 6)

        SkeletonClass().skeletonShow(skeleton, resources)

        sharedPreferences = requireActivity().getSharedPreferences("SHARED_PREFS_FILTER", Context.MODE_PRIVATE)
        val city = sharedPreferences.getString("FILTER_CITY", null)
        val time = sharedPreferences.getString("FILTER_TIME", null)
        val date = sharedPreferences.getString("FILTER_DATE", null)
        val priceStart = sharedPreferences.getString("FILTER_PRICE_START", null)
        val priceEnd = sharedPreferences.getString("FILTER_PRICE_END", null)

        if(city != null) binding.buttonPlace.text = city
        else binding.buttonPlace.text = "Россия"

        lifecycleScope.launch {
            getParties( parties,null, city, time, date, priceStart, priceEnd)
        }

        binding.swipe.setOnRefreshListener {
            binding.textView.visibility = View.INVISIBLE
            binding.recycler.visibility = View.VISIBLE

            SkeletonClass().skeletonShow(skeleton, resources)

            lifecycleScope.launch {
                if(binding.searchView.query != null)  {
                    getParties(parties, binding.searchView.query.toString(), city, time, date, priceStart, priceEnd)
                }
                else {
                    getParties(parties, null, city, time, date, priceStart, priceEnd)
                }
                binding.swipe.isRefreshing = false
            }
        }

        binding.buttonFilter.setOnClickListener {
            val mainIntent = Intent(context, FilterActivity::class.java)
            startActivity(mainIntent)
        }

        var searchPartiesBefore = mutableListOf<PartyDataClass>()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                try {
                    if (text != null) {
                        val searchParties = mutableListOf<PartyDataClass>()
                        for (i in parties) {
                            if (i.Название!!.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) searchParties.add(i)
                        }
                        if (searchParties != searchPartiesBefore)  {
                            val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
                            val partyAdapter = PartyAdapter(searchParties, coroutineScope, true)
                            binding.recycler.adapter = partyAdapter
                        }

                        searchPartiesBefore = searchParties
                    }
                } catch (e: Exception) {
                    Log.e("SEARCH_FILTER", e.toString())
                }
                return true
            }
        })
    }

    suspend fun getParties(parties: MutableList<PartyDataClass>,name_party:String?, city: String?, time: String?, date: String?, priceStart: String?, priceEnd: String?)  {
        try {
            parties.clear()
            if (sharedPreferences.all.isNotEmpty()) {
                binding.buttonFilter.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.filter_enabled), null, null, null);

                val partiesResult = sb.from("Вечеринки")
                    .select(Columns.raw("*, Возрастное_ограничение(Возраст), Пользователи(Имя, Верификация)")) {
                        filter {
                            neq("id_пользователя", sb.auth.currentUserOrNull()?.id.toString()) // Фитрация пользователя
                            // Фитрация разрешенных вечеринок
                            eq("id_статуса_проверки", "3")
                            // Фитрация города вечеринок
                            if(city != null) eq("Город", city.toString())
                            // Фитрация даты проведения вечеринок
                            if(date != null){
                                val dateSplit = date.split('-')
                                val currentDate = LocalDate.of(dateSplit[0].toInt(), dateSplit[1].toInt(), dateSplit[2].toInt())
                                gte("Дата", currentDate.toString())
                            }
                            // Фитрация времени проведения вечеринок
                            if(time != null) gte("Время", time)
                            // Фитрация цены вечеринок при заполнении полей от и до
                            if(priceStart != null && priceEnd != null) {
                                gte("Цена", priceStart.toDouble())
                                and{
                                    lte("Цена", priceEnd.toDouble())
                                }
                            }
                            // Фитрация цены вечеринок при заполнении поля от
                            if(priceStart != null) gte("Цена", priceStart.toDouble())
                            // Фитрация цены вечеринок при заполнении поля до
                            if(priceEnd != null) lte("Цена", priceEnd.toDouble())
                        }
                    }.data

                jsonArrayParties = JSONArray(partiesResult)

                Log.d("SHARED_PREFS_FILTER", jsonArrayParties.toString())
            }
            else {
                val partiesResult = sb.from("Вечеринки")
                    .select(Columns.raw("*, Возрастное_ограничение(Возраст), Пользователи(Имя, Верификация)")) {
                        filter {
                            neq("id_пользователя", sb.auth.currentUserOrNull()?.id.toString())
                            gte("Дата", LocalDate.now())
                            eq("id_статуса_проверки", 3)
                        }
                    }.data

                jsonArrayParties = JSONArray(partiesResult)

                Log.d("SHARED_PREFS_FILTER", jsonArrayParties.toString())
            }

            val partiesFavoritesResult = sb.from("Избранные_вечеринки").select() {
                filter {
                    eq("id_пользователя", sb.auth.currentUserOrNull()?.id.toString())
                }
            }.data

            jsonArrayFavorites = JSONArray(partiesFavoritesResult)

            for (i in 0 until jsonArrayParties.length()) {
                val jsonObject = jsonArrayParties.getJSONObject(i)
                val id = jsonObject.getInt("id")
                val name = jsonObject.getString("Название")
                val date = jsonObject.getString("Дата")
                val time = jsonObject.getString("Время")
                val place = jsonObject.getString("Место")
                val price = jsonObject.getDouble("Цена")
                val userId = jsonObject.getString("id_пользователя")
                val image = jsonObject.getString("Фото")
                val ageObject = jsonObject.getJSONObject("Возрастное_ограничение")
                val age = ageObject.getInt("Возраст")
                val usersObject = jsonObject.getJSONObject("Пользователи")
                val userName = usersObject.getString("Имя")
                val userVerify = usersObject.getBoolean("Верификация")
                var favorite = false
                for (j in 0 until jsonArrayFavorites.length()) {
                    val jsonObjectFavorites = jsonArrayFavorites.getJSONObject(j)
                    if (jsonObjectFavorites.getInt("id_вечеринки") == id) {
                        favorite = true
                    }
                }

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
                    Избранное = favorite,
                    Фото = image
                )
                parties.add(event)

                // Фильтрация по тексту в поиске
                if(name_party!= null){
                    partiesWithSearch = parties.filter {
                        it.Название!!.contains(name_party, true)
                    }
                }
                else partiesWithSearch  = parties

                Log.d("SHARED_PREFS_FILTER", event.toString())
            }

            Log.d("SHARED_PREFS_FILTER", parties.toString())

            val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
            val partyAdapter = PartyAdapter(partiesWithSearch!!, coroutineScope, false)
            binding.recycler.adapter = partyAdapter
        } catch (e: Throwable) {
            Log.e("Ошибка получения данных вечеринки", e.toString())
        } finally {
            if (parties.isEmpty()) {
                binding.textView.visibility = View.VISIBLE
                binding.recycler.visibility = View.GONE
            }
        }
    }
}

