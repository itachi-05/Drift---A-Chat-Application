package com.example.driftdb

import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyAdapter(fragmentActivity: MainActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> { ChatsFragment() }
            1 -> { StatusFragment() }
            2 -> { CallFragment() }
            else -> {
                throw Resources.NotFoundException("Position not found")
            }
        }
    }
}