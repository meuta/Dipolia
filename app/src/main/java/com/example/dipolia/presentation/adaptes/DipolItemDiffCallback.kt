package com.example.dipolia.presentation.adaptes

import androidx.recyclerview.widget.DiffUtil
import com.example.dipolia.domain.DipolDomainEntity

class DipolItemDiffCallback: DiffUtil.ItemCallback<DipolDomainEntity>() {
    override fun areItemsTheSame(oldItem: DipolDomainEntity, newItem: DipolDomainEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: DipolDomainEntity,
        newItem: DipolDomainEntity
    ): Boolean {
        return oldItem == newItem
    }
}