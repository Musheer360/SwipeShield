package com.swipeshield

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    
    private const val CHANNEL_ID = "transaction_alerts"
    private const val CHANNEL_NAME = "Transaction Alerts"
    private const val CHANNEL_DESCRIPTION = "Notifications for detected credit card transactions"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showTransactionNotification(context: Context, transaction: Transaction) {
        // Create intent to open payment activity
        val paymentIntent = Intent(context, PaymentActivity::class.java).apply {
            putExtra("transaction_id", transaction.id)
            putExtra("amount", transaction.amount)
            putExtra("merchant", transaction.merchant)
            putExtra("card_last_four", transaction.cardLastFour)
            putExtra("auto_start_payment", true) // Auto-start payment when opened from notification
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            transaction.id.hashCode(),
            paymentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(context.getString(R.string.transaction_detected))
            .setContentText(context.getString(R.string.you_spent, transaction.getFormattedAmount()))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${context.getString(R.string.you_spent, transaction.getFormattedAmount())} at ${transaction.merchant}. ${context.getString(R.string.tap_to_repay)}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_upi_payment,
                context.getString(R.string.repay_now),
                pendingIntent
            )
            .setColor(context.getColor(R.color.md_theme_light_primary))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        // Show notification
        try {
            NotificationManagerCompat.from(context).notify(
                transaction.id.hashCode(),
                notification
            )
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
            android.util.Log.e("NotificationHelper", "Failed to show notification: ${e.message}")
        }
    }
    
    fun cancelNotification(context: Context, transactionId: String) {
        NotificationManagerCompat.from(context).cancel(transactionId.hashCode())
    }
}

