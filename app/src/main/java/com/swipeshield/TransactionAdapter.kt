package com.swipeshield

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swipeshield.databinding.ItemTransactionBinding

class TransactionAdapter(
    private val onRepayClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(transaction: Transaction) {
            binding.apply {
                // Set transaction details
                tvAmount.text = transaction.getFormattedAmount()
                tvMerchant.text = transaction.merchant
                tvCardNumber.text = "**** ${transaction.cardLastFour}"
                tvTime.text = transaction.getFormattedTime()
                
                // Set status
                if (transaction.isPaid) {
                    tvStatus.text = root.context.getString(R.string.paid)
                    tvStatus.setTextColor(root.context.getColor(R.color.success_green))
                    btnRepay.visibility = android.view.View.GONE
                } else {
                    tvStatus.text = root.context.getString(R.string.unpaid)
                    tvStatus.setTextColor(root.context.getColor(R.color.warning_orange))
                    btnRepay.visibility = android.view.View.VISIBLE
                }
                
                // Set repay button click listener
                btnRepay.setOnClickListener {
                    onRepayClick(transaction)
                }
                
                // Set card click listener to open QR code
                root.setOnClickListener {
                    if (!transaction.isPaid) {
                        onRepayClick(transaction)
                    }
                }
            }
        }
    }
    
    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}

