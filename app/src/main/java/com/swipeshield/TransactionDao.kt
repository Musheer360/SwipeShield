package com.swipeshield

import androidx.room.*
import androidx.lifecycle.LiveData
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val merchant: String,
    val cardLastFour: String,
    val timestamp: Long,
    val isPaid: Boolean,
    val rawSmsText: String
) {
    fun toTransaction(): Transaction {
        return Transaction(
            id = id,
            amount = amount,
            merchant = merchant,
            cardLastFour = cardLastFour,
            timestamp = Date(timestamp),
            isPaid = isPaid,
            rawSmsText = rawSmsText
        )
    }
    
    companion object {
        fun fromTransaction(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                amount = transaction.amount,
                merchant = transaction.merchant,
                cardLastFour = transaction.cardLastFour,
                timestamp = transaction.timestamp.time,
                isPaid = transaction.isPaid,
                rawSmsText = transaction.rawSmsText
            )
        }
    }
}

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): LiveData<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): LiveData<List<TransactionEntity>>
    
    @Query("SELECT COUNT(*) FROM transactions WHERE isPaid = 1")
    fun getPaidCount(): LiveData<Int>
    
    @Query("SELECT COUNT(*) FROM transactions WHERE isPaid = 0")
    fun getUnpaidCount(): LiveData<Int>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)
    
    @Update
    suspend fun update(transaction: TransactionEntity)
    
    @Delete
    suspend fun delete(transaction: TransactionEntity)
    
    @Query("UPDATE transactions SET isPaid = 1 WHERE id = :transactionId")
    suspend fun markAsPaid(transactionId: String)
}

// Extension function to insert Transaction directly
suspend fun TransactionDao.insert(transaction: Transaction) {
    insert(TransactionEntity.fromTransaction(transaction))
}

