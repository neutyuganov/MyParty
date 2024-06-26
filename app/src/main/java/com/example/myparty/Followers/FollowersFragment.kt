package com.example.myparty.Followers

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.myparty.Adapters.ViewPagerAdapter
import com.example.myparty.databinding.FragmentFollowersBinding


class FollowersFragment : Fragment() {

    private lateinit var binding: FragmentFollowersBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        binding = FragmentFollowersBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun setupViewPager(viewPager: ViewPager) {

        val adapter = ViewPagerAdapter(childFragmentManager)

        adapter.addFragment(FollowersPageFragment(), "Подписчики")
        adapter.addFragment(FollowingPageFragment(), "Подписки")

        viewPager.adapter = adapter
    }
}