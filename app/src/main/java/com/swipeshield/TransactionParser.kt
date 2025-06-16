package com.swipeshield

import java.util.Date
import java.util.UUID
import java.util.regex.Pattern

object TransactionParser {
    
    // Common patterns for credit card transaction SMS
    private val patterns = listOf(
        // Pattern 1: Amount spent at merchant using card ending XXXX
        Pattern.compile(
            "(?i).*(?:spent|debited|charged).*?(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{2})?).*?(?:at|on)\\s+([^\\s]+).*?(?:card|ending).*?(\\d{4}).*",
            Pattern.DOTALL
        ),
        
        // Pattern 2: Transaction of amount at merchant
        Pattern.compile(
            "(?i).*transaction.*?(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{2})?).*?(?:at|on)\\s+([^\\s]+).*?(?:card|ending).*?(\\d{4}).*",
            Pattern.DOTALL
        ),
        
        // Pattern 3: Purchase of amount using card
        Pattern.compile(
            "(?i).*purchase.*?(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{2})?).*?(?:at|on)\\s+([^\\s]+).*?(?:card|ending).*?(\\d{4}).*",
            Pattern.DOTALL
        ),
        
        // Pattern 4: Generic amount pattern with merchant and card
        Pattern.compile(
            "(?i).*(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{2})?).*?([a-zA-Z0-9\\s]+).*?(\\d{4}).*",
            Pattern.DOTALL
        )
    )
    
    // Keywords that indicate credit card transactions
    private val creditCardKeywords = listOf(
        "credit card", "card", "spent", "purchase", "transaction", 
        "debited", "charged", "payment", "pos", "online"
    )
    
    // Bank sender patterns
    private val bankSenders = listOf(
        "HDFC", "ICICI", "SBI", "AXIS", "KOTAK", "CITI", "AMEX", 
        "INDUS", "YES", "PNB", "BOB", "CANARA", "UNION", "FEDERAL"
    )
    
    fun parseTransaction(smsBody: String, sender: String): Transaction? {
        // Check if SMS is from a bank
        if (!isBankSms(sender)) {
            return null
        }
        
        // Check if SMS contains credit card keywords
        if (!containsCreditCardKeywords(smsBody)) {
            return null
        }
        
        // Try to extract transaction details using patterns
        for (pattern in patterns) {
            val matcher = pattern.matcher(smsBody)
            if (matcher.find()) {
                try {
                    val amountStr = matcher.group(1)?.replace(",", "") ?: continue
                    val amount = amountStr.toDoubleOrNull() ?: continue
                    
                    val merchant = extractMerchant(matcher.group(2) ?: "", smsBody)
                    val cardLastFour = matcher.group(3) ?: ""
                    
                    // Validate extracted data
                    if (amount > 0 && merchant.isNotBlank() && cardLastFour.length == 4) {
                        return Transaction(
                            id = UUID.randomUUID().toString(),
                            amount = amount,
                            merchant = merchant.trim(),
                            cardLastFour = cardLastFour,
                            timestamp = Date(),
                            isPaid = false,
                            rawSmsText = smsBody
                        )
                    }
                } catch (e: Exception) {
                    // Continue to next pattern if parsing fails
                    continue
                }
            }
        }
        
        return null
    }
    
    private fun isBankSms(sender: String): Boolean {
        return bankSenders.any { bank -> 
            sender.uppercase().contains(bank) 
        }
    }
    
    private fun containsCreditCardKeywords(smsBody: String): Boolean {
        val lowerBody = smsBody.lowercase()
        return creditCardKeywords.any { keyword -> 
            lowerBody.contains(keyword) 
        }
    }
    
    private fun extractMerchant(rawMerchant: String, fullSms: String): String {
        // Clean up merchant name
        var merchant = rawMerchant.trim()
        
        // Remove common prefixes/suffixes
        merchant = merchant.replace(Regex("(?i)\\b(at|on|from|to)\\b"), "")
        merchant = merchant.replace(Regex("[^a-zA-Z0-9\\s]"), "")
        merchant = merchant.trim()
        
        // If merchant is too short or generic, try to extract from full SMS
        if (merchant.length < 3 || merchant.matches(Regex("\\d+"))) {
            // Look for merchant patterns in full SMS
            val merchantPattern = Pattern.compile("(?i)(?:at|on)\\s+([a-zA-Z][a-zA-Z0-9\\s]{2,20})(?:\\s|,|\\.|$)")
            val matcher = merchantPattern.matcher(fullSms)
            if (matcher.find()) {
                merchant = matcher.group(1)?.trim() ?: merchant
            }
        }
        
        // Capitalize first letter of each word
        return merchant.split(" ").joinToString(" ") { word ->
            if (word.isNotBlank()) {
                word.lowercase().replaceFirstChar { it.uppercase() }
            } else {
                word
            }
        }
    }
}

