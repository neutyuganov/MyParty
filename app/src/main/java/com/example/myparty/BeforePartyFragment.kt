package com.example.myparty

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentActualPartyBinding
import com.example.myparty.databinding.FragmentBeforePartyBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate


class BeforePartyFragment : Fragment() {
    private lateinit var binding: FragmentBeforePartyBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBeforePartyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.VISIBLE

        Log.e("USERCURRENT", sb.auth.currentUserOrNull().toString())

        lifecycleScope.launch {
            try{
                val parties = mutableListOf<PartyDataClass>()
                val partiesResult = sb.from("Вечеринки").select(Columns.raw("*, Возрастное_ограничение(Возраст)")){
                    filter {
                        lt("Дата", LocalDate.now())
                        eq("id_пользователя", sb.auth.currentUserOrNull()?.id.toString())
                    }
                } .data
                val jsonArray = JSONArray(partiesResult)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val id = jsonObject.getInt("id")
                    val name = jsonObject.getString("Название")
                    val date = jsonObject.getString("Дата")
                    val time = jsonObject.getString("Время")
                    val place = jsonObject.getString("Место")
                    val price = jsonObject.getDouble("Цена")
                    val ageObject = jsonObject.getJSONObject("Возрастное_ограничение")
                    val age = ageObject.getInt("Возраст")
                    val event = PartyDataClass(id = id, Название = name, Дата = date, Время = time, Место = place, Цена = price, Возраст = age)
                    parties.add(event)
                }

                val partyAdapter = PartyUserAdapter(parties)
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