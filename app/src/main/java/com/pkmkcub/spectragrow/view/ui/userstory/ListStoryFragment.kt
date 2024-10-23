package com.pkmkcub.spectragrow.view.ui.userstory

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.databinding.FragmentListStoryBinding
import com.pkmkcub.spectragrow.model.Story
import com.pkmkcub.spectragrow.view.adapter.StoryAdapter

class ListStoryFragment : Fragment(), StoryAdapter.OnItemClickListener {

    private lateinit var binding: FragmentListStoryBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storyAdapter: StoryAdapter
    private var storyList = mutableListOf<Story>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListStoryBinding.inflate(inflater, container, false)
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
        storyAdapter = StoryAdapter(this)
        binding.rvMain.apply {
            adapter = storyAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun fetchAllPlantData() {
        firestore.collection("stories")
            .get()
            .addOnSuccessListener { result ->
                storyList.clear()
                for (document in result) {
                    val story = document.toObject(Story::class.java)
                    storyList.add(story)
                }
                storyAdapter.submitList(storyList)
            }
            .addOnFailureListener { exception ->
                Log.e("Plant Item", "Error fetching plants", exception)
            }
    }

    private fun setupUi() {
        binding.apply {
            newStoryBtn.setOnClickListener {
                findNavController().navigate(R.id.action_listStory_to_addStory)
            }
        }
    }

    override fun onItemClick(item: Story) {
        val action = ListStoryFragmentDirections.actionListStoryToDetailStory(item)
        findNavController().navigate(action)
    }
}