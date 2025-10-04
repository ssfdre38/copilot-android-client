package com.ssfdre38.cpcli.android.client

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.ssfdre38.cpcli.android.client.utils.ThemeManager

class AboutActivity : AppCompatActivity() {

    private lateinit var textVersion: TextView
    private lateinit var textGitHubTrademark: TextView
    private lateinit var textMicrosoftTrademark: TextView
    private lateinit var textDisclaimer: TextView
    private lateinit var buttonViewSource: MaterialButton
    private lateinit var buttonReportIssue: MaterialButton
    private lateinit var buttonLicense: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before calling super.onCreate to prevent flicker
        ThemeManager.applyActivityTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initViews()
        setupContent()
        setupListeners()
        
        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "About"
    }

    private fun initViews() {
        textVersion = findViewById(R.id.textVersion)
        textGitHubTrademark = findViewById(R.id.textGitHubTrademark)
        textMicrosoftTrademark = findViewById(R.id.textMicrosoftTrademark)
        textDisclaimer = findViewById(R.id.textDisclaimer)
        buttonViewSource = findViewById(R.id.buttonViewSource)
        buttonReportIssue = findViewById(R.id.buttonReportIssue)
        buttonLicense = findViewById(R.id.buttonLicense)
    }
    
    private fun setupContent() {
        // Set version info from app build config
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName ?: "Unknown"
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
            textVersion.text = "Version $versionName ($versionCode)"
        } catch (e: Exception) {
            textVersion.text = "Version information unavailable"
        }
        
        // Set trademark notices
        textGitHubTrademark.text = getString(R.string.github_trademark)
        textMicrosoftTrademark.text = getString(R.string.microsoft_trademark)
        textDisclaimer.text = getString(R.string.disclaimer)
    }

    private fun setupListeners() {
        buttonViewSource.setOnClickListener {
            openUrl("https://github.com/ssfdre38/copilot-android-client")
        }
        
        buttonReportIssue.setOnClickListener {
            openUrl("https://github.com/ssfdre38/copilot-android-client/issues")
        }
        
        buttonLicense.setOnClickListener {
            openUrl("https://github.com/ssfdre38/copilot-android-client/blob/main/LICENSE")
        }
    }
    
    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // Handle case where no browser is available
            android.widget.Toast.makeText(this, "Unable to open link", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}