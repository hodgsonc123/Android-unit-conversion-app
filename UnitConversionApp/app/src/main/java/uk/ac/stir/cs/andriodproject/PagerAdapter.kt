package uk.ac.stir.cs.andriodproject

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * class handles the swithcing of fragments based on which tab has been selected
 * **/
internal class PagerAdapter(fragmentManager: FragmentManager?, private val mNumOfTabs:
                                Int) : FragmentStatePagerAdapter(fragmentManager!!,
                                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SelectionFragment()
            1 -> ConversionFragment()
            2 -> AddConversionFragment()
            3 -> RemoveConversionFragment()
            else -> Fragment()
        }
    }
    override fun getCount(): Int {
        return mNumOfTabs
    }

}