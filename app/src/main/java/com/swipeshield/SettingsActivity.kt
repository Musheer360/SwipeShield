package com.swipeshield

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.swipeshield.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        loadSettings()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }
    
    private fun loadSettings() {
        val prefs = getSharedPreferences("SwipeShield", Context.MODE_PRIVATE)
        
        // Load current UPI ID
        val currentUpiId = prefs.getString("user_upi_id", "") ?: ""
        binding.etNewUpiId.setText(currentUpiId)
        
        // Load SMS detection setting
        val smsDetectionEnabled = prefs.getBoolean("sms_detection_enabled", true)
        binding.switchSmsDetection.isChecked = smsDetectionEnabled
    }
    
    private fun setupClickListeners() {
        binding.btnUpdateUpiId.setOnClickListener {
            updateUpiId()
        }
        
        binding.switchSmsDetection.setOnCheckedChangeListener { _, isChecked ->
            updateSmsDetectionSetting(isChecked)
        }
        
        binding.btnViewHistory.setOnClickListener {
            startActivity(android.content.Intent(this, TransactionHistoryActivity::class.java))
        }
    }
    
    private fun updateUpiId() {
        val newUpiId = binding.etNewUpiId.text?.toString()?.trim() ?: ""
        
        if (newUpiId.isEmpty()) {
            binding.tilNewUpiId.error = "UPI ID cannot be empty"
            return
        }
        
        if (!UpiPaymentHelper.isValidUpiId(newUpiId)) {
            binding.tilNewUpiId.error = getString(R.string.invalid_upi_id)
            return
        }
        
        // Save new UPI ID
        val prefs = getSharedPreferences("SwipeShield", Context.MODE_PRIVATE)
        prefs.edit().putString("user_upi_id", newUpiId).apply()
        
        binding.tilNewUpiId.error = null
        Toast.makeText(this, getString(R.string.upi_id_updated), Toast.LENGTH_SHORT).show()
    }
    
    private fun updateSmsDetectionSetting(enabled: Boolean) {
        val prefs = getSharedPreferences("SwipeShield", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("sms_detection_enabled", enabled).apply()
        
        val message = if (enabled) {
            "SMS detection enabled"
        } else {
            "SMS detection disabled"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

