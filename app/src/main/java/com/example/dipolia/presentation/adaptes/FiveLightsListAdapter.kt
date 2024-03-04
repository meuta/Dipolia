package com.example.dipolia.presentation.adaptes

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.dipolia.databinding.ItemFiveLightsBinding
import com.example.dipolia.domain.entities.FiveLightsDomainEntity


class FiveLightsListAdapter : ListAdapter<FiveLightsDomainEntity, FiveLightsItemViewHolder>(FiveLightsItemDiffCallback()) {

    var onFiveLightsItemClickListener: ((FiveLightsDomainEntity) -> Unit)? = null
    var onFiveLightsItemLongClickListener: ((FiveLightsDomainEntity) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FiveLightsItemViewHolder {

        val binding = ItemFiveLightsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return FiveLightsItemViewHolder(binding)
    }


    override fun onBindViewHolder(holder: FiveLightsItemViewHolder, position: Int) {
//        Log.d("onBindViewHolder", "position")
        val dipolItem = getItem(position)

        val binding = holder.binding
//        Log.d("onBindViewHolder", binding.toString())

        binding.fiveLightsItem = dipolItem

        binding.root.setOnClickListener {
            onFiveLightsItemClickListener?.invoke(dipolItem)
        }
        binding.root.setOnLongClickListener {
            onFiveLightsItemLongClickListener?.invoke(dipolItem)
            true
        }

//        binding.etFiveLightsName.setText(dipolItem.lampName)
    }
}