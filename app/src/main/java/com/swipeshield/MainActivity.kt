package com.swipeshield

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.swipeshield.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private var transactionReceiver: BroadcastReceiver? = null
    
    // Permission launcher for notification permission (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, create notification channel
            NotificationHelper.createNotificationChannel(this)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if onboarding is needed
        if (OnboardingActivity.shouldShowOnboarding(this)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        requestPermissions()
        registerTransactionReceiver()
        
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }
    
    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            // Handle repay button click
            openPaymentActivity(transaction)
        }
        
        binding.rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = transactionAdapter
        }
    }
    
    private fun setupObservers() {
        val database = TransactionDatabase.getInstance(this)
        
        // Observe recent transactions
        database.transactionDao().getRecentTransactions(5).observe(this) { transactions ->
            if (transactions.isEmpty()) {
                binding.tvNoTransactions.visibility = android.view.View.VISIBLE
                binding.rvRecentTransactions.visibility = android.view.View.GONE
            } else {
                binding.tvNoTransactions.visibility = android.view.View.GONE
                binding.rvRecentTransactions.visibility = android.view.View.VISIBLE
                transactionAdapter.submitList(transactions.map { it.toTransaction() })
            }
        }
        
        // Observe paid count
        database.transactionDao().getPaidCount().observe(this) { count ->
            binding.tvPaidCount.text = count.toString()
        }
        
        // Observe unpaid count
        database.transactionDao().getUnpaidCount().observe(this) { count ->
            binding.tvUnpaidCount.text = count.toString()
        }
        
        // Display user's UPI ID
        val prefs = getSharedPreferences("SwipeShield", Context.MODE_PRIVATE)
        val upiId = prefs.getString("user_upi_id", "Not set")
        binding.tvUpiId.text = upiId
    }
    
    private fun setupClickListeners() {
        binding.btnViewAll.setOnClickListener {
            // Open transaction history activity
            startActivity(Intent(this, TransactionHistoryActivity::class.java))
        }
    }
    
    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        
        // SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECEIVE_SMS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_SMS)
        }
        
        // Notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (permissions.isNotEmpty()) {
            Dexter.withContext(this)
                .withPermissions(permissions)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            // All permissions granted
                        } else {
                            // Some permissions denied
                            showPermissionDialog()
                        }
                    }
                    
                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest>,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                })
                .check()
        }
    }
    
    private fun showPermissionDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.sms_permission_message))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun registerTransactionReceiver() {
        transactionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == SmsReceiver.ACTION_TRANSACTION_DETECTED) {
                    // Transaction detected, UI will be updated automatically through observers
                }
            }
        }
        
        val filter = IntentFilter(SmsReceiver.ACTION_TRANSACTION_DETECTED)
        registerReceiver(transactionReceiver, filter)
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
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        transactionReceiver?.let {
            unregisterReceiver(it)
        }
    }
}

