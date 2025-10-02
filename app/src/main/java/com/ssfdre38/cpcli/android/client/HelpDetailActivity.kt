package com.ssfdre38.cpcli.android.client

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ssfdre38.cpcli.android.client.databinding.ActivityHelpDetailBinding
import com.ssfdre38.cpcli.android.client.model.HelpItem
import io.noties.markwon.Markwon

class HelpDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHelpDetailBinding
    private lateinit var helpItem: HelpItem
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        loadHelpItem()
        setupActionBar()
        displayContent()
    }
    
    private fun loadHelpItem() {
        val helpId = intent.getStringExtra(EXTRA_HELP_ID) ?: ""
        val title = intent.getStringExtra(EXTRA_HELP_TITLE) ?: ""
        val content = intent.getStringExtra(EXTRA_HELP_CONTENT) ?: ""
        val category = intent.getStringExtra(EXTRA_HELP_CATEGORY) ?: ""
        
        helpItem = HelpItem(
            id = helpId,
            title = title,
            content = content,
            category = category
        )
    }
    
    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = helpItem.title
            subtitle = helpItem.category
        }
    }
    
    private fun displayContent() {
        binding.apply {
            textTitle.text = helpItem.title
            textCategory.text = helpItem.category
            
            // Use Markwon for markdown rendering if available, otherwise plain text
            try {
                val markwon = Markwon.create(this@HelpDetailActivity)
                markwon.setMarkdown(textContent, helpItem.content)
            } catch (e: Exception) {
                // Fallback to plain text
                textContent.text = helpItem.content
            }
            
            textContent.movementMethod = ScrollingMovementMethod()
        }
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
    
    companion object {
        private const val EXTRA_HELP_ID = "help_id"
        private const val EXTRA_HELP_TITLE = "help_title"
        private const val EXTRA_HELP_CONTENT = "help_content"
        private const val EXTRA_HELP_CATEGORY = "help_category"
        
        fun createIntent(context: Context, helpItem: HelpItem): Intent {
            return Intent(context, HelpDetailActivity::class.java).apply {
                putExtra(EXTRA_HELP_ID, helpItem.id)
                putExtra(EXTRA_HELP_TITLE, helpItem.title)
                putExtra(EXTRA_HELP_CONTENT, helpItem.content)
                putExtra(EXTRA_HELP_CATEGORY, helpItem.category)
            }
        }
    }
}