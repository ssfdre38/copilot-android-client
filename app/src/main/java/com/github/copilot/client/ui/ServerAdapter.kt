package com.github.copilot.client.ui

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.copilot.client.R
import com.github.copilot.client.databinding.ItemServerBinding
import com.github.copilot.client.model.ServerConfig

class ServerAdapter(
    private val onServerClick: (ServerConfig) -> Unit,
    private val onServerDelete: (ServerConfig) -> Unit,
    private val onSetDefault: (ServerConfig) -> Unit
) : ListAdapter<ServerConfig, ServerAdapter.ServerViewHolder>(ServerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val binding = ItemServerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServerViewHolder(binding, onServerClick, onServerDelete, onSetDefault)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ServerViewHolder(
        private val binding: ItemServerBinding,
        private val onServerClick: (ServerConfig) -> Unit,
        private val onServerDelete: (ServerConfig) -> Unit,
        private val onSetDefault: (ServerConfig) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(server: ServerConfig) {
            binding.apply {
                textServerName.text = server.name
                textServerUrl.text = server.url
                
                // Show last used time
                val lastUsedText = DateUtils.getRelativeTimeSpanString(
                    server.lastUsed.time,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
                textLastUsed.text = "Last used: $lastUsedText"
                
                // Show default badge
                chipDefault.visibility = if (server.isDefault) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                
                // Set server icon based on URL protocol
                val iconRes = when {
                    server.url.startsWith("wss://") -> R.drawable.ic_secure_server
                    server.url.startsWith("ws://") -> R.drawable.ic_server
                    else -> R.drawable.ic_server
                }
                imageServerIcon.setImageResource(iconRes)

                root.setOnClickListener {
                    onServerClick(server)
                }

                buttonMoreOptions.setOnClickListener { view ->
                    val popup = PopupMenu(view.context, view)
                    popup.menuInflater.inflate(R.menu.server_options, popup.menu)
                    
                    // Hide "Set as Default" if already default
                    popup.menu.findItem(R.id.action_set_default)?.isVisible = !server.isDefault
                    
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.action_edit -> {
                                onServerClick(server)
                                true
                            }
                            R.id.action_set_default -> {
                                onSetDefault(server)
                                true
                            }
                            R.id.action_delete -> {
                                onServerDelete(server)
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
            }
        }
    }

    private class ServerDiffCallback : DiffUtil.ItemCallback<ServerConfig>() {
        override fun areItemsTheSame(oldItem: ServerConfig, newItem: ServerConfig): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ServerConfig, newItem: ServerConfig): Boolean {
            return oldItem == newItem
        }
    }
}