package com.ssfdre38.cpcli.android.client

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.ssfdre38.cpcli.android.client.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "About"
        }
        
        setupVersionInfo()
        setupTrademarks()
        setupButtons()
    }
    
    private fun setupVersionInfo() {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        binding.versionText.text = "Version ${packageInfo.versionName} (Build ${packageInfo.longVersionCode})"
        binding.appDescriptionText.text = "An Android client for connecting to GitHub Copilot CLI servers. " +
                "This application provides a mobile interface for interacting with AI-powered development tools."
    }
    
    private fun setupTrademarks() {
        binding.trademarksText.text = """
            TRADEMARK NOTICES:
            
            • GitHub® is a registered trademark of Microsoft Corporation
            • Copilot™ is a trademark of Microsoft Corporation  
            • Android™ is a trademark of Google LLC
            • Material Design™ is a trademark of Google LLC
            
            This application is not affiliated with, endorsed by, or sponsored by Microsoft Corporation, GitHub, Inc., or Google LLC.
            
            All trademarks and registered trademarks are the property of their respective owners.
        """.trimIndent()
    }
    
    private fun setupButtons() {
        binding.sourceCodeButton.setOnClickListener {
            openUrl("https://github.com/ssfdre38/CopilotAndroidClient")
        }
        
        binding.licenseButton.setOnClickListener {
            showLicense()
        }
        
        binding.reportIssueButton.setOnClickListener {
            openUrl("https://github.com/ssfdre38/CopilotAndroidClient/issues")
        }
    }
    
    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // Handle case where no browser is available
        }
    }
    
    private fun showLicense() {
        val licenseText = """
            MIT License
            
            Copyright (c) 2024 CopilotAndroidClient
            
            Permission is hereby granted, free of charge, to any person obtaining a copy
            of this software and associated documentation files (the "Software"), to deal
            in the Software without restriction, including without limitation the rights
            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
            copies of the Software, and to permit persons to whom the Software is
            furnished to do so, subject to the following conditions:
            
            The above copyright notice and this permission notice shall be included in all
            copies or substantial portions of the Software.
            
            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
            SOFTWARE.
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("MIT License")
            .setMessage(licenseText)
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}