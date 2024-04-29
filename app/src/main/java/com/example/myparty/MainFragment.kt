package com.example.myparty

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentMainBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import java.time.LocalDate;
import org.json.JSONArray


class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.VISIBLE

        Log.e("USERCURRENT", sb.auth.currentUserOrNull().toString())

        lifecycleScope.launch {
            try{
                val parties = mutableListOf<PartyDataClass>()
                val partiesResult = sb.from("Вечеринки").select(Columns.raw("*, Возрастное_ограничение(Возраст), Пользователи(Имя, Верификация)")){
                    filter {
                        gte("Дата", LocalDate.now())
                    }
                } .data
                val jsonArray = JSONArray(partiesResult)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val id = jsonObject.getInt("id")
                    val name = jsonObject.getString("Название")
//                    val slogan = jsonObject.getString("Слоган")
                    val date = jsonObject.getString("Дата")
                    val time = jsonObject.getString("Время")
//                    val city = jsonObject.getString("Город")
                    val place = jsonObject.getString("Место")
//                    val description = jsonObject.getString("Описание")
                    val price = jsonObject.getDouble("Цена")
                    val ageObject = jsonObject.getJSONObject("Возрастное_ограничение")
                    val age = ageObject.getInt("Возраст")
                    val usersObject = jsonObject.getJSONObject("Пользователи")
                    val userName = usersObject.getString("Имя")
                    val userVerify = usersObject.getBoolean("Верификация")
                    val event = PartyDataClass(id = id, Название = name, Имя = userName, Дата = date, Время = time, Место = place, Цена = price, Возраст = age, Верификация = userVerify)
                    parties.add(event)
                }

                val partyAdapter = PartyAdapter(parties)
                binding.recycler.adapter = partyAdapter
            }

            catch (e: Exception){
                    Log.e("Ошибка получения данных вечеринки", e.message.toString())
            }

            finally{
                    binding.progressBar.visibility = View.GONE
            }
        }
    }


}

