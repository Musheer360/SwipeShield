package com.swipeshield

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.swipeshield.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupClickListeners()
    }
    
    private fun setupUI() {
        // Set up text watcher for UPI ID validation
        binding.etUpiId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val upiId = s?.toString()?.trim() ?: ""
                val isValid = UpiPaymentHelper.isValidUpiId(upiId)
                
                binding.btnGetStarted.isEnabled = isValid
                
                if (upiId.isNotEmpty() && !isValid) {
                    binding.tilUpiId.error = getString(R.string.invalid_upi_id)
                } else {
                    binding.tilUpiId.error = null
                }
            }
        })
    }
    
    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            val upiId = binding.etUpiId.text?.toString()?.trim() ?: ""
            
            if (UpiPaymentHelper.isValidUpiId(upiId)) {
                saveUpiId(upiId)
                navigateToMain()
            } else {
                binding.tilUpiId.error = getString(R.string.invalid_upi_id)
            }
        }
    }
    
    private fun saveUpiId(upiId: String) {
        val prefs = getSharedPreferences("SwipeShield", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("user_upi_id", upiId)
            putBoolean("onboarding_completed", true)
            putBoolean("sms_detection_enabled", true)
            apply()
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    companion object {
        fun shouldShowOnboarding(context: Context): Boolean {
            val prefs = context.getSharedPreferences("SwipeShield", Context.MODE_PRIVATE)
            return !prefs.getBoolean("onboarding_completed", false)
        }
    }
}

