package com.ssfdre38.cpcli.android.client.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssfdre38.cpcli.android.client.R
import com.ssfdre38.cpcli.android.client.data.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: MutableList<ChatMessage> = mutableListOf()) : 
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {
    
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
    
    fun clearMessages() {
        val size = messages.size
        messages.clear()
        notifyItemRangeRemoved(0, size)
    }
    
    fun setMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromUser) TYPE_USER else TYPE_ASSISTANT
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = when (viewType) {
            TYPE_USER -> R.layout.item_chat_message_user
            else -> R.layout.item_chat_message_assistant
        }
        
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }
    
    override fun getItemCount(): Int = messages.size
    
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textMessage)
        private val timeText: TextView = itemView.findViewById(R.id.textTime)
        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        fun bind(message: ChatMessage) {
            messageText.text = message.content
            timeText.text = dateFormat.format(Date(message.timestamp))
        }
    }
    
    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_ASSISTANT = 2
    }
}