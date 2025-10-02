package com.github.copilot.client.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.copilot.client.databinding.ItemHelpBinding
import com.github.copilot.client.model.HelpItem

class HelpAdapter(
    private val onItemClick: (HelpItem) -> Unit
) : ListAdapter<HelpItem, HelpAdapter.HelpViewHolder>(HelpDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val binding = ItemHelpBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HelpViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HelpViewHolder(
        private val binding: ItemHelpBinding,
        private val onItemClick: (HelpItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(helpItem: HelpItem) {
            binding.apply {
                textTitle.text = helpItem.title
                chipCategory.text = helpItem.category
                
                // Show content preview (first few lines)
                val contentPreview = helpItem.content
                    .replace("#", "")
                    .trim()
                    .split("\n")
                    .take(3)
                    .joinToString(" ")
                    .take(150) + if (helpItem.content.length > 150) "..." else ""
                
                textContent.text = contentPreview

                root.setOnClickListener {
                    onItemClick(helpItem)
                }
            }
        }
    }

    private class HelpDiffCallback : DiffUtil.ItemCallback<HelpItem>() {
        override fun areItemsTheSame(oldItem: HelpItem, newItem: HelpItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HelpItem, newItem: HelpItem): Boolean {
            return oldItem == newItem
        }
    }
}