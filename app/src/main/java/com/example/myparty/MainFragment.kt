package com.example.myparty

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myparty.databinding.FragmentMainBinding
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class MainFragment : Fragment(R.layout.fragment_main) {

    private var _binding:FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        _binding =FragmentMainBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sb = SupabaseConnection().sb

        lifecycleScope.launch {
        try{
                val parties = mutableListOf<PartyDataClass>()
                val partiesResult = sb.from("Вечеринки").select()
                parties.addAll(partiesResult.decodeList())
                val partyAdapter = PartyAdapter(parties)
                binding.recycler.adapter = partyAdapter
        }
        catch (e: Exception){
            Log.e("ERROR", e.message.toString())
        }
        }

    }
}