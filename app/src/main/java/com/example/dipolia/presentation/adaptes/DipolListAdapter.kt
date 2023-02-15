package com.example.dipolia.presentation.adaptes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import com.example.dipolia.R
import com.example.dipolia.databinding.ItemDipolSelectedBinding
import com.example.dipolia.databinding.ItemDipolUnselectedBinding
import com.example.dipolia.domain.DipolDomainEntity


class DipolListAdapter : ListAdapter<DipolDomainEntity, DipolItemViewHolder>(DipolItemDiffCallback()) {

    var onDipolItemClickListener: ((DipolDomainEntity) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DipolItemViewHolder {
        val layout = when (viewType){
            VIEW_TYPE_SELECTED -> R.layout.item_dipol_selected
            VIEW_TYPE_UNSELECTED -> R.layout.item_dipol_unselected
            else -> throw RuntimeException("Unknown viewType: $viewType")
        }
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            layout,
            parent,
            false
        )
        return DipolItemViewHolder(binding)
    }


    override fun onBindViewHolder(holder: DipolItemViewHolder, position: Int) {

        val dipolItem = getItem(position)

        val binding = holder.binding

        binding.root.setOnClickListener {
            onDipolItemClickListener?.invoke(dipolItem)
        }

        when (binding){
            is ItemDipolSelectedBinding -> {
                binding.dipolItem = dipolItem
            }
            is ItemDipolUnselectedBinding -> {
                binding.dipolItem = dipolItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).selected) VIEW_TYPE_SELECTED else VIEW_TYPE_UNSELECTED
    }


    companion object {
        const val VIEW_TYPE_SELECTED = 1
        const val VIEW_TYPE_UNSELECTED = 0

        const val MAX_POOL_SIZE = 15
    }
}