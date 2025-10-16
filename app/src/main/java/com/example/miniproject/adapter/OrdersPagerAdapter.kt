package com.example.miniproject.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.miniproject.fragment.ActiveOrdersFragment
import com.example.miniproject.fragment.CompletedOrdersFragment

class OrdersPagerAdapter(
    fragment: Fragment,
    private val userRole: String
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ActiveOrdersFragment()
            1 -> CompletedOrdersFragment()
            else -> ActiveOrdersFragment()
        }
    }
}