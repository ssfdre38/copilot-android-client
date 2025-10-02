package com.ssfdre38.cpcli.android.client.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssfdre38.cpcli.android.client.R
import com.ssfdre38.cpcli.android.client.databinding.ItemChatMessageBinding
import com.ssfdre38.cpcli.android.client.model.ChatMessage
import com.ssfdre38.cpcli.android.client.model.MessageSender
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ChatViewHolder(private val binding: ItemChatMessageBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        fun bind(message: ChatMessage) {
            binding.apply {
                textViewMessage.text = message.content
                textViewSender.text = when (message.sender) {
                    MessageSender.USER -> "You"
                    MessageSender.COPILOT -> "Copilot"
                }
                textViewTimestamp.text = timeFormat.format(message.timestamp)
                
                // Style messages differently based on sender
                val backgroundColor = when (message.sender) {
                    MessageSender.USER -> ContextCompat.getColor(root.context, R.color.user_message_bg)
                    MessageSender.COPILOT -> ContextCompat.getColor(root.context, R.color.copilot_message_bg)
                }
                
                cardMessage.setCardBackgroundColor(backgroundColor)
                
                // Show typing indicator
                if (message.isTyping) {
                    textViewMessage.text = "Copilot is thinking..."
                    textViewMessage.alpha = 0.7f
                } else {
                    textViewMessage.alpha = 1.0f
                }
            }
        }
    }
    
    class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}