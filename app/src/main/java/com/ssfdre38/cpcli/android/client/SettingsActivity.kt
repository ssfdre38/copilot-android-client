package com.ssfdre38.cpcli.android.client

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {

    private lateinit var buttonServerManagement: MaterialButton
    private lateinit var buttonHelp: MaterialButton
    private lateinit var buttonAbout: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        buttonServerManagement = findViewById(R.id.buttonServerManagement)
        buttonHelp = findViewById(R.id.buttonHelp)
        buttonAbout = findViewById(R.id.buttonAbout)
    }

    private fun setupListeners() {
        buttonServerManagement.setOnClickListener {
            // TODO: Open server management
        }

        buttonHelp.setOnClickListener {
            // TODO: Open help
        }

        buttonAbout.setOnClickListener {
            // TODO: Open about
        }
    }
}