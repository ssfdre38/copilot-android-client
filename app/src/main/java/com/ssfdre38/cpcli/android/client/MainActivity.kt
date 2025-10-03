package com.ssfdre38.cpcli.android.client

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var editTextServerUrl: TextInputEditText
    private lateinit var buttonConnect: MaterialButton
    private lateinit var buttonStartChat: MaterialButton
    private lateinit var buttonSettings: MaterialButton
    private lateinit var textConnectionStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        editTextServerUrl = findViewById(R.id.editTextServerUrl)
        buttonConnect = findViewById(R.id.buttonConnect)
        buttonStartChat = findViewById(R.id.buttonStartChat)
        buttonSettings = findViewById(R.id.buttonSettings)
        textConnectionStatus = findViewById(R.id.textConnectionStatus)
    }

    private fun setupListeners() {
        buttonConnect.setOnClickListener {
            val serverUrl = editTextServerUrl.text.toString()
            if (serverUrl.isNotBlank()) {
                // Simulate connection
                textConnectionStatus.text = getString(R.string.connected)
                buttonStartChat.isEnabled = true
                Toast.makeText(this, "Connected to $serverUrl", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a server URL", Toast.LENGTH_SHORT).show()
            }
        }

        buttonStartChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}