package com.pkmkcub.spectragrow.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.pkmkcub.spectragrow.model.Plant
import com.pkmkcub.spectragrow.databinding.ItemPlantBinding

class PlantAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Plant, PlantAdapter.MyViewHolder>(DIFF_CALLBACK) {

    private var originalPlantList: List<Plant> = emptyList()

    fun setOriginalPlantList(plantList: List<Plant>) {
        originalPlantList = plantList
    }

    fun filterPlants(text: String) {
        val filteredList = if (text.isBlank()) {
            originalPlantList
        } else {
            originalPlantList.filter { plant ->
                plant.name.contains(text, ignoreCase = true)
            }
        }
        submitList(filteredList)
    }


    interface OnItemClickListener {
        fun onItemClick(item: Plant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemPlantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val plant = getItem(position)
        holder.bind(plant)
    }

    inner class MyViewHolder(private val binding: ItemPlantBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val plant = getItem(position)
                listener.onItemClick(plant)
            }
        }

        fun bind(item: Plant) {
            binding.apply {
                titleId.text = item.name
                titleLt.text = item.nama_lt
                itemThumbnail.load(item.photo_url)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Plant>() {
            override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean {
                return oldItem == newItem
            }
        }
    }
}
