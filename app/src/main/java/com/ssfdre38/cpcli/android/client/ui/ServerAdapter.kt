package com.ssfdre38.cpcli.android.client.ui

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.ssfdre38.cpcli.android.client.data.ServerConfig
import java.text.SimpleDateFormat
import java.util.*

class ServerAdapter(
    private var servers: MutableList<ServerConfig>,
    private val onServerClick: (ServerConfig) -> Unit,
    private val onServerEdit: (ServerConfig) -> Unit,
    private val onServerDelete: (ServerConfig) -> Unit
) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {
    
    private var activeServerId: String? = null
    private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    class ServerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainLayout: LinearLayout = itemView as LinearLayout
        val nameText: TextView
        val urlText: TextView
        val statusText: TextView
        val lastConnectedText: TextView
        val actionLayout: LinearLayout
        val selectButton: Button
        val editButton: Button
        val deleteButton: Button
        
        init {
            // Create the view hierarchy programmatically
            mainLayout.apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 15, 20, 15)
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 5, 0, 5)
                }
            }
            
            nameText = TextView(itemView.context).apply {
                textSize = 16f
                setTextColor(Color.BLACK)
            }
            
            urlText = TextView(itemView.context).apply {
                textSize = 14f
                setTextColor(Color.GRAY)
            }
            
            statusText = TextView(itemView.context).apply {
                textSize = 12f
                setPadding(0, 5, 0, 0)
            }
            
            lastConnectedText = TextView(itemView.context).apply {
                textSize = 11f
                setTextColor(Color.GRAY)
                setPadding(0, 5, 0, 10)
            }
            
            actionLayout = LinearLayout(itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            
            selectButton = Button(itemView.context).apply {
                text = "Select"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(0, 0, 5, 0)
                }
            }
            
            editButton = Button(itemView.context).apply {
                text = "Edit"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(5, 0, 5, 0)
                }
            }
            
            deleteButton = Button(itemView.context).apply {
                text = "Delete"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(5, 0, 0, 0)
                }
            }
            
            actionLayout.addView(selectButton)
            actionLayout.addView(editButton)
            actionLayout.addView(deleteButton)
            
            mainLayout.addView(nameText)
            mainLayout.addView(urlText)
            mainLayout.addView(statusText)
            mainLayout.addView(lastConnectedText)
            mainLayout.addView(actionLayout)
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val layout = LinearLayout(parent.context)
        return ServerViewHolder(layout)
    }
    
    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = servers[position]
        val isActive = server.id == activeServerId
        
        holder.nameText.text = server.getDisplayName()
        holder.urlText.text = server.getFullUrl()
        
        // Status
        if (isActive) {
            holder.statusText.text = "🟢 Active Server"
            holder.statusText.setTextColor(Color.parseColor("#4CAF50"))
            holder.mainLayout.setBackgroundColor(Color.parseColor("#E8F5E8"))
            holder.selectButton.text = "Active"
            holder.selectButton.isEnabled = false
        } else {
            holder.statusText.text = "⚪ Available"
            holder.statusText.setTextColor(Color.GRAY)
            holder.mainLayout.setBackgroundColor(Color.parseColor("#F5F5F5"))
            holder.selectButton.text = "Select"
            holder.selectButton.isEnabled = true
        }
        
        // Last connected
        if (server.lastConnected > 0) {
            holder.lastConnectedText.text = "Last connected: ${dateFormat.format(Date(server.lastConnected))}"
            holder.lastConnectedText.visibility = View.VISIBLE
        } else {
            holder.lastConnectedText.text = "Never connected"
            holder.lastConnectedText.visibility = View.VISIBLE
        }
        
        // Click listeners
        holder.selectButton.setOnClickListener {
            onServerClick(server)
        }
        
        holder.editButton.setOnClickListener {
            onServerEdit(server)
        }
        
        holder.deleteButton.setOnClickListener {
            onServerDelete(server)
        }
    }
    
    override fun getItemCount(): Int = servers.size
    
    fun updateServers(newServers: List<ServerConfig>) {
        val oldSize = servers.size
        servers.clear()
        servers.addAll(newServers)
        
        // Notify of data change based on the specific changes
        if (newServers.size < oldSize) {
            // A server was deleted
            notifyDataSetChanged()
        } else if (newServers.size > oldSize) {
            // A server was added
            notifyItemInserted(newServers.size - 1)
        } else {
            // Servers were modified
            notifyDataSetChanged()
        }
    }
    
    fun setActiveServer(serverId: String?) {
        activeServerId = serverId
        notifyDataSetChanged()
    }
}