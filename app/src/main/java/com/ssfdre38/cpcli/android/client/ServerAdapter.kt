package com.ssfdre38.cpcli.android.client

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ssfdre38.cpcli.android.client.data.ServerConfig

class ServerAdapter(
    private val onServerAction: (ServerConfig, Action) -> Unit
) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {
    
    private val servers = mutableListOf<ServerConfig>()
    
    enum class Action {
        EDIT, DELETE, SET_DEFAULT
    }
    
    fun updateServers(newServers: List<ServerConfig>) {
        servers.clear()
        servers.addAll(newServers)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        holder.bind(servers[position])
    }
    
    override fun getItemCount(): Int = servers.size
    
    inner class ServerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textServerName: TextView = itemView.findViewById(R.id.textServerName)
        private val textServerUrl: TextView = itemView.findViewById(R.id.textServerUrl)
        private val textDefaultIndicator: TextView = itemView.findViewById(R.id.textDefaultIndicator)
        private val buttonEdit: MaterialButton = itemView.findViewById(R.id.buttonEdit)
        private val buttonDelete: MaterialButton = itemView.findViewById(R.id.buttonDelete)
        private val buttonSetDefault: MaterialButton = itemView.findViewById(R.id.buttonSetDefault)
        
        fun bind(server: ServerConfig) {
            textServerName.text = server.name
            textServerUrl.text = "${server.url}:${server.port}"
            
            // Show default indicator
            textDefaultIndicator.visibility = if (server.isDefault) View.VISIBLE else View.GONE
            
            // Update set default button
            buttonSetDefault.text = if (server.isDefault) "Default" else "Set Default"
            buttonSetDefault.isEnabled = !server.isDefault
            
            // Set click listeners
            buttonEdit.setOnClickListener {
                onServerAction(server, Action.EDIT)
            }
            
            buttonDelete.setOnClickListener {
                onServerAction(server, Action.DELETE)
            }
            
            buttonSetDefault.setOnClickListener {
                onServerAction(server, Action.SET_DEFAULT)
            }
        }
    }
}