package com.example.dipolia.presentation.adaptes

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.dipolia.databinding.ItemDipolBinding
import com.example.dipolia.domain.entities.DipolDomainEntity


class DipolListAdapter : ListAdapter<DipolDomainEntity, DipolItemViewHolder>(DipolItemDiffCallback()) {

    var onDipolItemClickListener: ((DipolDomainEntity) -> Unit)? = null
    var onDipolItemLongClickListener: ((DipolDomainEntity) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DipolItemViewHolder {

        val binding = ItemDipolBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return DipolItemViewHolder(binding)
    }


    override fun onBindViewHolder(holder: DipolItemViewHolder, position: Int) {
        Log.d("onBindViewHolder", "position")
        val dipolItem = getItem(position)

        val binding = holder.binding
        Log.d("onBindViewHolder", binding.toString())

        binding.dipolItem = dipolItem

        binding.root.setOnClickListener {
            onDipolItemClickListener?.invoke(dipolItem)
        }
        binding.root.setOnLongClickListener {
            onDipolItemLongClickListener?.invoke(dipolItem)
            true
        }
    }
}