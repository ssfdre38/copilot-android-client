package com.ssfdre38.cpcli.android.client

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssfdre38.cpcli.android.client.databinding.ActivityHelpBinding
import com.ssfdre38.cpcli.android.client.model.HelpItem
import com.ssfdre38.cpcli.android.client.ui.HelpAdapter
import com.ssfdre38.cpcli.android.client.utils.HelpContentProvider

class HelpActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHelpBinding
    private lateinit var helpAdapter: HelpAdapter
    private var allHelpItems: List<HelpItem> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupActionBar()
        setupRecyclerView()
        loadHelpContent()
        setupSearch()
    }
    
    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Help & Documentation"
        }
    }
    
    private fun setupRecyclerView() {
        helpAdapter = HelpAdapter { helpItem ->
            // Handle help item click - show detail view
            showHelpDetail(helpItem)
        }
        
        binding.recyclerViewHelp.apply {
            layoutManager = LinearLayoutManager(this@HelpActivity)
            adapter = helpAdapter
        }
    }
    
    private fun loadHelpContent() {
        allHelpItems = HelpContentProvider.getAllHelpItems()
        helpAdapter.submitList(allHelpItems)
        
        // Setup category filter
        val categories = HelpContentProvider.getCategories()
        // TODO: Add category filter UI if needed
    }
    
    private fun setupSearch() {
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                filterHelpItems(query)
            }
        })
    }
    
    private fun filterHelpItems(query: String) {
        val filteredItems = if (query.isEmpty()) {
            allHelpItems
        } else {
            HelpContentProvider.searchHelpItems(query)
        }
        helpAdapter.submitList(filteredItems)
    }
    
    private fun showHelpDetail(helpItem: HelpItem) {
        val intent = HelpDetailActivity.createIntent(this, helpItem)
        startActivity(intent)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}