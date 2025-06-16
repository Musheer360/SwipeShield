package com.swipeshield

import android.app.Activity
import android.content.Context
import android.util.Log
import com.shreyaspatil.EasyUpiPayment.EasyUpiPayment
import com.shreyaspatil.EasyUpiPayment.listener.PaymentStatusListener
import com.shreyaspatil.EasyUpiPayment.model.TransactionDetails
import java.util.*

object UpiPaymentHelper {
    
    private const val TAG = "UpiPaymentHelper"
    
    /**
     * Initiates a UPI payment for a transaction
     * @param activity The activity context
     * @param transaction The transaction details
     * @param userUpiId The user's UPI ID (payee)
     * @param userName The user's name (optional)
     * @param listener Callback for payment status
     */
    fun initiatePayment(
        activity: Activity,
        transaction: Transaction,
        userUpiId: String,
        userName: String? = null,
        listener: PaymentStatusListener
    ) {
        try {
            val transactionId = "SS_${transaction.id.take(8)}_${System.currentTimeMillis()}"
            val description = "SwipeShield repayment for ${transaction.merchant} - Card ending ${transaction.cardLastFour}"
            
            val easyUpiPayment = EasyUpiPayment.Builder()
                .with(activity)
                // Set payee details (user's own UPI ID)
                .setPayeeVpa(userUpiId)
                .setPayeeName(userName ?: "SwipeShield User")
                // Set payment details
                .setTransactionId(transactionId)
                .setTransactionRefId(transactionId)
                .setDescription(description)
                .setAmount(String.format("%.2f", transaction.amount))
                // Build and start payment
                .build()
            
            // Set payment status listener
            easyUpiPayment.setPaymentStatusListener(listener)
            
            // Start payment - this will open UPI app chooser
            easyUpiPayment.startPayment()
            
            Log.d(TAG, "UPI payment initiated for amount: ${transaction.getFormattedAmount()}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating UPI payment", e)
            listener.onTransactionCancelled()
        }
    }
    
    /**
     * Validates if a UPI ID is in correct format
     * @param upiId The UPI ID to validate
     * @return true if valid, false otherwise
     */
    fun isValidUpiId(upiId: String): Boolean {
        // Basic UPI ID validation: should contain @ and have valid format
        val upiPattern = Regex("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$")
        return upiPattern.matches(upiId)
    }
    
    /**
     * Creates a simple payment status listener
     */
    fun createPaymentStatusListener(
        onSuccess: (TransactionDetails) -> Unit,
        onFailure: () -> Unit,
        onCancelled: () -> Unit
    ): PaymentStatusListener {
        return object : PaymentStatusListener {
            override fun onTransactionCompleted(transactionDetails: TransactionDetails) {
                Log.d(TAG, "Payment completed: ${transactionDetails.transactionId}")
                onSuccess(transactionDetails)
            }
            
            override fun onTransactionFailed() {
                Log.d(TAG, "Payment failed")
                onFailure()
            }
            
            override fun onTransactionCancelled() {
                Log.d(TAG, "Payment cancelled")
                onCancelled()
            }
        }
    }
}

