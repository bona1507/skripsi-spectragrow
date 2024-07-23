package com.pkmkcub.spectragrow.view.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.model.Plant
import com.pkmkcub.spectragrow.databinding.FragmentHomeBinding
import com.pkmkcub.spectragrow.view.adapter.PlantAdapter
import com.pkmkcub.spectragrow.view.ui.auth.AuthViewModel

class HomeFragment : Fragment(), PlantAdapter.OnItemClickListener {

    private lateinit var binding: FragmentHomeBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var plantAdapter: PlantAdapter
    private lateinit var firestore: FirebaseFirestore
    private var plantList = mutableListOf<Plant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance("plant")
        setupRecyclerView()
        fetchAllPlantData()
        setupUi()
    }

    private fun setupRecyclerView() {
        plantAdapter = PlantAdapter(this)
        binding.rvSearch.apply {
            adapter = plantAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun fetchAllPlantData() {
        firestore.collection("plant")
            .get()
            .addOnSuccessListener { result ->
                plantList.clear()
                for (document in result) {
                    val plant = document.toObject(Plant::class.java)
                    plantList.add(plant)
                }
                plantAdapter.setOriginalPlantList(plantList)
                plantAdapter.submitList(plantList)
            }
            .addOnFailureListener { exception ->
                Log.e("Plant Item", "Error fetching plants", exception)
            }
    }

    private fun setupUi() {
        binding.apply {
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.maps_btn -> {
                        findNavController().navigate(R.id.action_home_to_maps)
                        true
                    }
                    R.id.logout_btn -> {
                        logoutUser()
                        true
                    }
                    R.id.add_story_btn -> {
                        findNavController().navigate(R.id.action_home_to_listStory)
                        true
                    }
                    else -> false
                }
            }
            etSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    plantAdapter.filterPlants(editable.toString())
                }

                override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            titleId.text = getString(R.string.welcome_username, authViewModel.getDisplayNameUser())
        }
    }

    private fun logoutUser() {
        authViewModel.logout { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.action_home_to_onboarding)
            } else {
                // Handle logout failure if needed
            }
        }
    }

    override fun onItemClick(item: Plant) {
        binding.apply {
            itemTitleId.text = item.name
            itemTitleLt.text = item.nama_lt
            fertilizerDetail.text = item.fertilizer
            phValue.text = item.pH.toString()
            fosfatValue.text = item.fosfat
            natriumValue.text = item.natrium
            kaliumValue.text = item.kalium
            boValue.text = item.bahan_organik
            sklValue.text = item.skl
            ttValue.text = item.tekstur_tanah
            kaValue.text = item.kadar_air.toString()
            itemThumbnail.load(item.photo_url)
        }
    }
}
