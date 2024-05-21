package com.example.myparty.Profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myparty.Adapters.PartyAdapter
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.Adapters.PartyUserAdapter
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentActualPartyBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate


class ActualPartyFragment(userId : String) : Fragment() {

    var userId = userId

    private lateinit var binding: FragmentActualPartyBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentActualPartyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.VISIBLE

        Log.e("USERCURRENT", userId)

        val parties = mutableListOf<PartyDataClass>()

        lifecycleScope.launch {
            try{
                val partiesResult = sb.from("Вечеринки").select(Columns.raw("*, Возрастное_ограничение(Возраст), Статусы_проверки(Название)")){
                    filter {
                        gte("Дата", LocalDate.now())
                        eq("id_пользователя", userId)
                        neq("id_статуса_проверки", "2")
                    }
                } .data

                val jsonArray = JSONArray(partiesResult)

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
                    val age = ageObject.getInt("Возраст")
                    val statusObject = jsonObject.getJSONObject("Статусы_проверки")
                    val status = statusObject.getString("Название")
                    val event = PartyDataClass(id = id, Название = name, id_пользователя = userId, Дата = date, Время = time, Место = place, Цена = price, Возраст = age, Статус_проверки = status)
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
                binding.progressBar.visibility = View.GONE
                if(parties.isEmpty()){
                    binding.textView.visibility = View.VISIBLE
                    binding.recycler.visibility = View.GONE
                }
            }
        }

    }
}