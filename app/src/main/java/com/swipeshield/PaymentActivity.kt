package com.swipeshield

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shreyaspatil.EasyUpiPayment.listener.PaymentStatusListener
import com.shreyaspatil.EasyUpiPayment.model.TransactionDetails
import com.swipeshield.databinding.ActivityPaymentBinding
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPaymentBinding
    private var transactionId: String? = null
    private var amount: Double = 0.0
    private var merchant: String = ""
    private var cardLastFour: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get transaction details from intent
        extractIntentData()
        
        // Set up UI
        setupUI()
        
        // Set up click listeners
        setupClickListeners()
        
        // Auto-initiate payment if user wants immediate payment
        val autoStart = intent.getBooleanExtra("auto_start_payment", false)
        if (autoStart) {
            initiateUpiPayment()
        }
    }
    
    private fun extractIntentData() {
        transactionId = intent.getStringExtra("transaction_id")
        amount = intent.getDoubleExtra("amount", 0.0)
        merchant = intent.getStringExtra("merchant") ?: ""
        cardLastFour = intent.getStringExtra("card_last_four") ?: ""
    }
    
    private fun setupUI() {
        // Set transaction alert text
        binding.tvTransactionAlert.text = getString(R.string.you_spent, "₹${String.format("%.2f", amount)}")
        
        // Set payment instruction
        binding.tvPaymentInstruction.text = "Tap 'Pay Now' to open your UPI app and complete the payment of ₹${String.format("%.2f", amount)}"
        
        // Set transaction details
        binding.tvMerchantDetail.text = getString(R.string.merchant, merchant)
        binding.tvCardDetail.text = getString(R.string.card_ending, cardLastFour)
        binding.tvTimeDetail.text = getString(R.string.transaction_time, getCurrentTime())
        
        // Set payment amount
        binding.tvPaymentAmount.text = getString(R.string.amount, "₹${String.format("%.2f", amount)}")
        
        // Get and display user's UPI ID
        val prefs = getSharedPreferences("SwipeShield", Context.MODE_PRIVATE)
        val userUpiId = prefs.getString("user_upi_id", "") ?: ""
        binding.tvPaymentTo.text = getString(R.string.payment_to, userUpiId)
    }
    
    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }
        
        binding.btnPayNow.setOnClickListener {
            initiateUpiPayment()
        }
    }
    
    private fun initiateUpiPayment() {
        val prefs = getSharedPreferences("SwipeShield", Context.MODE_PRIVATE)
        val userUpiId = prefs.getString("user_upi_id", "") ?: ""
        val userName = prefs.getString("user_name", null)
        
        if (userUpiId.isEmpty()) {
            showError("UPI ID not set. Please set your UPI ID in settings.")
            return
        }
        
        if (!UpiPaymentHelper.isValidUpiId(userUpiId)) {
            showError("Invalid UPI ID. Please update your UPI ID in settings.")
            return
        }
        
        // Create transaction object
        val transaction = Transaction(
            id = transactionId ?: "",
            amount = amount,
            merchant = merchant,
            cardLastFour = cardLastFour
        )
        
        // Create payment status listener
        val paymentListener = UpiPaymentHelper.createPaymentStatusListener(
            onSuccess = { transactionDetails ->
                handlePaymentSuccess(transactionDetails)
            },
            onFailure = {
                handlePaymentFailure()
            },
            onCancelled = {
                handlePaymentCancelled()
            }
        )
        
        // Initiate UPI payment
        UpiPaymentHelper.initiatePayment(
            activity = this,
            transaction = transaction,
            userUpiId = userUpiId,
            userName = userName,
            listener = paymentListener
        )
    }
    
    private fun handlePaymentSuccess(transactionDetails: TransactionDetails) {
        Toast.makeText(this, "Payment successful! Transaction ID: ${transactionDetails.transactionId}", Toast.LENGTH_LONG).show()
        
        // Mark transaction as paid
        transactionId?.let { id ->
            lifecycleScope.launch {
                try {
                    TransactionDatabase.getInstance(this@PaymentActivity)
                        .transactionDao()
                        .markAsPaid(id)
                    
                    // Cancel the notification
                    NotificationHelper.cancelNotification(this@PaymentActivity, id)
                } catch (e: Exception) {
                    // Handle error silently
                }
            }
        }
        
        // Close activity after successful payment
        finish()
    }
    
    private fun handlePaymentFailure() {
        Toast.makeText(this, "Payment failed. Please try again.", Toast.LENGTH_LONG).show()
    }
    
    private fun handlePaymentCancelled() {
        Toast.makeText(this, "Payment cancelled.", Toast.LENGTH_SHORT).show()
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        binding.tvPaymentInstruction.text = "Error: $message"
    }
    
    private fun getCurrentTime(): String {
        val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }
    
    companion object {
        fun createIntent(
            context: Context,
            transactionId: String,
            amount: Double,
            merchant: String,
            cardLastFour: String,
            autoStartPayment: Boolean = false
        ): Intent {
            return Intent(context, PaymentActivity::class.java).apply {
                putExtra("transaction_id", transactionId)
                putExtra("amount", amount)
                putExtra("merchant", merchant)
                putExtra("card_last_four", cardLastFour)
                putExtra("auto_start_payment", autoStartPayment)
            }
        }
    }
}

