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
import com.example.myparty.Adapters.PartyAdapter
import com.example.myparty.DataClasses.PartyDataClass
import com.example.myparty.R
import com.example.myparty.SkeletonClass
import com.example.myparty.SupabaseConnection
import com.example.myparty.SupabaseConnection.Singleton.sb
import com.example.myparty.databinding.FragmentAdminPartiesBinding
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


class AdminPartiesFragment : Fragment() {

    private lateinit var binding: FragmentAdminPartiesBinding

    private lateinit var skeleton: Skeleton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        binding = FragmentAdminPartiesBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        skeleton = binding.recycler.applySkeleton(R.layout.item_party_skeleton, 6)

        SkeletonClass().skeletonShow(skeleton, resources)

        lifecycleScope.launch {
            getParties()
        }

        binding.swipe.setOnRefreshListener {
            binding.textView.visibility = View.INVISIBLE
            binding.recycler.visibility = View.VISIBLE

            SkeletonClass().skeletonShow(skeleton, resources)

            lifecycleScope.launch {
                getParties()
                binding.swipe.isRefreshing = false
            }
        }
    }

    suspend fun getParties(){
        val parties = mutableListOf<PartyDataClass>()
        try{
            val partiesResult = sb.from("Вечеринки").select {
                filter {
                    eq("id_статуса_проверки", 1)
                }
            }.data

            val jsonArrayParties = JSONArray(partiesResult)

            for (i in 0 until jsonArrayParties.length()) {
                val jsonObject = jsonArrayParties.getJSONObject(i)
                val id = jsonObject.getInt("id")
                val name = jsonObject.getString("Название")
                val date = jsonObject.getString("Дата")
                val time = jsonObject.getString("Время")
                val place = jsonObject.getString("Место")
                val price = jsonObject.getDouble("Цена")
                val image = jsonObject.getString("Фото")

                val event = PartyDataClass(
                    id = id,
                    Название = name,
                    Дата = date,
                    Время = time,
                    Место = place,
                    Цена = price,
                    Фото = image
                )

                Log.d("AdminPartiesFragment", event.id.toString())
                parties.add(event)
            }

            val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
            val partyAdapter = AdminPartyAdapter(parties, coroutineScope)
            binding.recycler.adapter = partyAdapter
        }
        catch (e: Throwable){
            Log.d("AdminPartiesFragment", e.toString())
            Toast.makeText(context, "Что-то пошло не так", Toast.LENGTH_LONG).show()
        }
        finally {
            if (parties.isEmpty()) {
                binding.textView.visibility = View.VISIBLE
                binding.recycler.visibility = View.INVISIBLE
            }
        }
    }
}