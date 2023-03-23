package com.example.dipolia.presentation.adaptes

import androidx.recyclerview.widget.DiffUtil
import com.example.dipolia.domain.entities.DipolDomainEntity

class DipolItemDiffCallback: DiffUtil.ItemCallback<DipolDomainEntity>() {
    override fun areItemsTheSame(oldItem: DipolDomainEntity, newItem: DipolDomainEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: DipolDomainEntity,
        newItem: DipolDomainEntity
    ): Boolean {

        val oldItemCopy = oldItem.copy(lastConnection = newItem.lastConnection)
        return oldItemCopy == newItem
    }

}