package com.swipeshield

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.swipeshield.databinding.ActivityTransactionHistoryBinding
import kotlinx.coroutines.launch

class TransactionHistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var transactionAdapter: TransactionAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.transaction_history)
    }
    
    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            // Handle repay button click
            openPaymentActivity(transaction)
        }
        
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@TransactionHistoryActivity)
            adapter = transactionAdapter
        }
    }
    
    private fun setupObservers() {
        val database = TransactionDatabase.getInstance(this)
        
        // Observe all transactions
        database.transactionDao().getAllTransactions().observe(this) { transactions ->
            if (transactions.isEmpty()) {
                binding.tvNoTransactions.visibility = android.view.View.VISIBLE
                binding.rvTransactions.visibility = android.view.View.GONE
            } else {
                binding.tvNoTransactions.visibility = android.view.View.GONE
                binding.rvTransactions.visibility = android.view.View.VISIBLE
                transactionAdapter.submitList(transactions.map { it.toTransaction() })
            }
        }
    }
    
    private fun openPaymentActivity(transaction: Transaction) {
        val intent = PaymentActivity.createIntent(
            this,
            transaction.id,
            transaction.amount,
            transaction.merchant,
            transaction.cardLastFour
        )
        startActivity(intent)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

