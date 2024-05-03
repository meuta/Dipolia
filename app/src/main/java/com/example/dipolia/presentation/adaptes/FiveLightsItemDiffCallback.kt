package com.example.dipolia.presentation.adaptes

import androidx.recyclerview.widget.DiffUtil
import com.example.dipolia.domain.entities.FiveLightsDomainEntity

class FiveLightsItemDiffCallback: DiffUtil.ItemCallback<FiveLightsDomainEntity>() {
    override fun areItemsTheSame(oldItem: FiveLightsDomainEntity, newItem: FiveLightsDomainEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: FiveLightsDomainEntity,
        newItem: FiveLightsDomainEntity
    ): Boolean {

        val oldItemCopy = oldItem.copy(lastConnection = newItem.lastConnection)
        return oldItemCopy == newItem
    }

}