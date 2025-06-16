# SwipeShield - Credit Card Debt Prevention App

SwipeShield is an Android app that helps users avoid falling into credit card debt by prompting immediate repayment of every credit card transaction through UPI payments.

## 🎯 Purpose

Help users stay ahead of their credit card bills by:
- Detecting credit card transactions in real-time via SMS
- Prompting immediate repayment through UPI
- Sending money to the user's own UPI ID (like a "Repayment Vault")

## 📱 Core Features

### Real-time Transaction Detection
- Monitors SMS inbox for credit card transaction notifications
- Parses transaction details (amount, merchant, card last 4 digits)
- Supports multiple bank SMS formats

### Instant UPI Repayment
- Shows immediate notification when transaction is detected
- "Tap to repay now" button opens UPI app directly
- Pre-fills payment amount and user's UPI ID
- Works with all UPI apps (Google Pay, PhonePe, Paytm, etc.)

### Clean Material You UI
- Modern Material Design 3 theming
- Dynamic color support
- Dark/Light mode through system settings
- Minimalist, clutter-free interface

### Smart Features
- Transaction history with paid/unpaid status
- Settings to update UPI ID and toggle SMS detection
- Local data storage (no backend required)
- Privacy-focused (everything happens on-device)

## 🛠 Technical Implementation

### Architecture
- **Language**: Kotlin
- **UI**: Material Design 3 with View Binding
- **Database**: Room (SQLite)
- **UPI Integration**: EasyUpiPayment library
- **SMS Detection**: BroadcastReceiver
- **Notifications**: NotificationCompat

### Key Components
- `MainActivity`: Dashboard showing recent transactions and repayment status
- `PaymentActivity`: Direct UPI payment integration
- `OnboardingActivity`: Initial UPI ID setup
- `SettingsActivity`: App configuration
- `TransactionParser`: Smart SMS parsing for multiple bank formats
- `UpiPaymentHelper`: Direct UPI app integration

### Permissions Required
- `RECEIVE_SMS`: To detect credit card transaction SMS
- `READ_SMS`: To parse transaction details
- `POST_NOTIFICATIONS`: To show transaction alerts

## 🚀 How It Works

1. **Setup**: User enters their UPI ID during onboarding
2. **Detection**: App monitors SMS for credit card transactions
3. **Parsing**: Extracts amount, merchant, and card details
4. **Notification**: Shows instant alert with "Tap to repay" button
5. **Payment**: Opens UPI app with pre-filled payment to user's own UPI ID
6. **Tracking**: Marks transaction as paid and updates history

## 📋 Installation

1. Clone the repository
2. Open in Android Studio
3. Build and install on Android device (API 24+)
4. Grant SMS and notification permissions
5. Enter your UPI ID during onboarding

## 🔧 Configuration

### Supported Banks
The app supports SMS formats from major Indian banks including:
- HDFC Bank
- ICICI Bank
- State Bank of India (SBI)
- Axis Bank
- Kotak Mahindra Bank
- And many more...

### UPI Apps Supported
Works with all UPI-enabled apps:
- Google Pay
- PhonePe
- Paytm
- BHIM UPI
- Bank UPI apps
- Any other UPI app

## 🎨 UI/UX Features

- **Material You**: Dynamic theming with system color extraction
- **Responsive Design**: Works on all screen sizes
- **Accessibility**: Proper content descriptions and touch targets
- **Animations**: Smooth transitions and feedback
- **Dark Mode**: Automatic system theme support

## 🔒 Privacy & Security

- **Local Storage**: All data stored locally on device
- **No Backend**: No server communication required
- **Permission Control**: Users can toggle SMS detection
- **Secure**: No sensitive data transmitted

## 📊 Transaction Management

- **Real-time Status**: Live updates of paid/unpaid transactions
- **History View**: Complete transaction history
- **Smart Parsing**: Handles various SMS formats automatically
- **Status Tracking**: Visual indicators for payment status

## 🛡 Error Handling

- Graceful handling of invalid UPI IDs
- Fallback for unsupported SMS formats
- Network error handling for UPI payments
- User-friendly error messages

## 🔄 Future Enhancements

- Support for more bank SMS formats
- Spending analytics and insights
- Budget tracking integration
- Automated payment scheduling
- Multi-language support

## 📱 System Requirements

- Android 7.0 (API level 24) or higher
- SMS permission for transaction detection
- UPI app installed for payments
- Internet connection for UPI transactions

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- EasyUpiPayment library for seamless UPI integration
- Material Design team for excellent design guidelines
- Android development community for best practices

---

**SwipeShield** - Stay ahead of your credit card bills! 🛡️💳

