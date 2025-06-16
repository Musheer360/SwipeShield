package com.swipeshield

import java.util.Date

data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val merchant: String = "",
    val cardLastFour: String = "",
    val timestamp: Date = Date(),
    val isPaid: Boolean = false,
    val rawSmsText: String = ""
) {
    fun getFormattedAmount(): String {
        return "₹${String.format("%.2f", amount)}"
    }
    
    fun getFormattedTime(): String {
        val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        return formatter.format(timestamp)
    }
    
    fun getFormattedDate(): String {
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return formatter.format(timestamp)
    }
}

