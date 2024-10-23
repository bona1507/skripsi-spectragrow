package com.pkmkcub.spectragrow.view.ui.detailsitem

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pkmkcub.spectragrow.databinding.FragmentDetailsItemBinding

class DetailsItemFragment : Fragment() {

    private lateinit var binding: FragmentDetailsItemBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.apply {
            cvPh.setOnClickListener {
                accordionToggle(phContent, llPh)
            }
            cvNatrium.setOnClickListener {
                accordionToggle(natriumContent, llNatrium)
            }
            cvFosfat.setOnClickListener {
                accordionToggle(fosfatContent, llFosfat)
            }
            cvKalium.setOnClickListener {
                accordionToggle(kaliumContent, llKalium)
            }
            cvKa.setOnClickListener {
                accordionToggle(kaContent, llKa)
            }
            cvTt.setOnClickListener {
                accordionToggle(ttContent, llTt)
            }
            cvSkl.setOnClickListener {
                accordionToggle(sklContent, llSkl)
            }
            cvTemp.setOnClickListener {
                accordionToggle(tempContent, llTemp)
            }
            cvHigh.setOnClickListener {
                accordionToggle(highContent, llHigh)
            }
        }
    }

    private fun accordionToggle(accordionItem: View, layout: ViewGroup) {
        TransitionManager.beginDelayedTransition(layout, AutoTransition())

        if (accordionItem.visibility == View.VISIBLE) {
            accordionItem.visibility = View.GONE
        } else {
            accordionItem.visibility = View.VISIBLE
        }
    }
}