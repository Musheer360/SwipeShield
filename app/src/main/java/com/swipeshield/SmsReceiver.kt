package com.swipeshield

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "SmsReceiver"
        const val ACTION_TRANSACTION_DETECTED = "com.swipeshield.TRANSACTION_DETECTED"
        const val EXTRA_TRANSACTION = "transaction"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }
        
        try {
            // Check if SMS detection is enabled
            val prefs = context.getSharedPreferences("SwipeShield", Context.MODE_PRIVATE)
            val smsDetectionEnabled = prefs.getBoolean("sms_detection_enabled", true)
            
            if (!smsDetectionEnabled) {
                Log.d(TAG, "SMS detection is disabled")
                return
            }
            
            // Extract SMS messages
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            for (smsMessage in messages) {
                val sender = smsMessage.originatingAddress ?: ""
                val messageBody = smsMessage.messageBody ?: ""
                
                Log.d(TAG, "Received SMS from: $sender")
                Log.d(TAG, "Message body: $messageBody")
                
                // Parse the SMS for transaction details
                val transaction = TransactionParser.parseTransaction(messageBody, sender)
                
                if (transaction != null) {
                    Log.d(TAG, "Transaction detected: ${transaction.getFormattedAmount()} at ${transaction.merchant}")
                    
                    // Store transaction in local database
                    TransactionDatabase.getInstance(context).transactionDao().insert(transaction)
                    
                    // Broadcast transaction detected event
                    val transactionIntent = Intent(ACTION_TRANSACTION_DETECTED).apply {
                        putExtra(EXTRA_TRANSACTION, transaction)
                    }
                    context.sendBroadcast(transactionIntent)
                    
                    // Show notification
                    NotificationHelper.showTransactionNotification(context, transaction)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing SMS", e)
        }
    }
}

