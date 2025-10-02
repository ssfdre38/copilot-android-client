package com.ssfdre38.cpcli.android.client.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssfdre38.cpcli.android.client.databinding.ItemQuickActionBinding
import com.ssfdre38.cpcli.android.client.model.QuickAction

class QuickActionAdapter(
    private val onActionClick: (QuickAction) -> Unit
) : ListAdapter<QuickAction, QuickActionAdapter.QuickActionViewHolder>(QuickActionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickActionViewHolder {
        val binding = ItemQuickActionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QuickActionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: QuickActionViewHolder, position: Int) {
        holder.bind(getItem(position), onActionClick)
    }
    
    class QuickActionViewHolder(private val binding: ItemQuickActionBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(action: QuickAction, onActionClick: (QuickAction) -> Unit) {
            binding.apply {
                textViewActionTitle.text = "${action.icon} ${action.title}"
                
                root.setOnClickListener {
                    onActionClick(action)
                }
            }
        }
    }
    
    class QuickActionDiffCallback : DiffUtil.ItemCallback<QuickAction>() {
        override fun areItemsTheSame(oldItem: QuickAction, newItem: QuickAction): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: QuickAction, newItem: QuickAction): Boolean {
            return oldItem == newItem
        }
    }
}