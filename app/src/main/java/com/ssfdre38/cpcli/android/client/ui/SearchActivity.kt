package com.ssfdre38.cpcli.android.client.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssfdre38.cpcli.android.client.R
import com.ssfdre38.cpcli.android.client.data.ChatMessage
import com.ssfdre38.cpcli.android.client.data.StorageManager
import java.text.SimpleDateFormat
import java.util.*

data class SearchResult(
    val message: ChatMessage,
    val serverName: String,
    val highlightedText: String
)

class SearchActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var noResultsTextView: TextView
    private lateinit var searchAdapter: SearchResultsAdapter
    private lateinit var storageManager: StorageManager
    
    private val searchResults = mutableListOf<SearchResult>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Search Chat History"
        
        initializeViews()
        setupSearch()
        storageManager = StorageManager(this)
    }
    
    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
        noResultsTextView = findViewById(R.id.noResultsTextView)
        
        searchAdapter = SearchResultsAdapter(searchResults) { searchResult ->
            // Return result to calling activity
            val intent = Intent().apply {
                putExtra("server_name", searchResult.serverName)
                putExtra("message_text", searchResult.message.content)
                putExtra("timestamp", searchResult.message.timestamp)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchResultsRecyclerView.adapter = searchAdapter
    }
    
    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim()
                if (!query.isNullOrEmpty() && query.length >= 2) {
                    performSearch(query)
                } else {
                    clearResults()
                }
            }
        })
    }
    
    private fun performSearch(query: String) {
        Thread {
            val results = searchInDatabase(query)
            runOnUiThread {
                displayResults(results)
            }
        }.start()
    }
    
    private fun searchInDatabase(query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val servers = storageManager.getAllServers()
        
        for (server in servers) {
            val messages = storageManager.getChatHistory(server.id)
            for (message in messages) {
                if (message.content.contains(query, ignoreCase = true)) {
                    val highlightedText = highlightSearchTerm(message.content, query)
                    results.add(SearchResult(message, server.name, highlightedText))
                }
            }
        }
        
        // Sort by timestamp (most recent first)
        return results.sortedByDescending { it.message.timestamp }
    }
    
    private fun highlightSearchTerm(text: String, searchTerm: String): String {
        val regex = Regex(searchTerm, RegexOption.IGNORE_CASE)
        return regex.replace(text) { matchResult ->
            "**${matchResult.value}**"  // Use markdown-style highlighting
        }
    }
    
    private fun displayResults(results: List<SearchResult>) {
        searchResults.clear()
        searchResults.addAll(results)
        searchAdapter.notifyDataSetChanged()
        
        if (results.isEmpty()) {
            searchResultsRecyclerView.visibility = RecyclerView.GONE
            noResultsTextView.visibility = TextView.VISIBLE
        } else {
            searchResultsRecyclerView.visibility = RecyclerView.VISIBLE
            noResultsTextView.visibility = TextView.GONE
        }
    }
    
    private fun clearResults() {
        searchResults.clear()
        searchAdapter.notifyDataSetChanged()
        searchResultsRecyclerView.visibility = RecyclerView.GONE
        noResultsTextView.visibility = TextView.GONE
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

class SearchResultsAdapter(
    private val searchResults: List<SearchResult>,
    private val onResultClick: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    class SearchResultViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val serverNameTextView: TextView = itemView.findViewById(R.id.serverNameTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val result = searchResults[position]
        
        holder.serverNameTextView.text = result.serverName
        holder.messageTextView.text = result.highlightedText
        holder.timestampTextView.text = dateFormat.format(Date(result.message.timestamp))
        holder.senderTextView.text = if (result.message.isFromUser) "You" else "Copilot"
        
        holder.itemView.setOnClickListener {
            onResultClick(result)
        }
    }
    
    override fun getItemCount(): Int = searchResults.size
}