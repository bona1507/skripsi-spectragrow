package com.pkmkcub.spectragrow.view.ui.userstory

import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import coil.load
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.databinding.FragmentDetailStoryBinding
import java.util.Locale

class DetailStoryFragment : Fragment() {

    private lateinit var binding: FragmentDetailStoryBinding
    private val args: DetailStoryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() {
        val story = args.story
        binding.apply {
            ivThumbnail.load(story.photo_url)
            tvTitle.text = story.title
            tvDetail.text = story.content
            tvTimestamp.text = getLocationName(story.lat, story.lon)
        }
    }

    private fun getLocationName(lat: Double, lon: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                address.getAddressLine(0)
            } else {
                getString(R.string.unknown_location)
            }
        } catch (e: Exception) {
            getString(R.string.unknown_location)
        }
    }
}
