# Alpaca Trader Pro - Android Trading Application

A modern, feature-rich Android application for day trading using the Alpaca API. Built with Jetpack Compose, Material3, and MVVM architecture.

## Features

### Core Functionality
- **Live Price Streaming**: Real-time WebSocket connection to Alpaca's market data
- **2x Leverage Trading**: Quick buy/sell with automatic 2x leverage calculation
- **Daily High/Low Tracking**: Automatic logging of daily price extremes with timestamps
- **Time Window Suggestions**: AI-powered analysis of historical data to suggest optimal buy/sell windows
- **Trade Confirmations**: Built-in safety with confirmation dialogs for all trades

### UI/UX
- **Material3 Design**: Modern, sleek interface with deep blue/green finance color scheme
- **Heavy Animations**: Lottie animations for celebrations, loading states, and transitions
- **Pull-to-Refresh**: Intuitive gesture to refresh market data
- **Dark Mode Optimized**: Designed for comfortable trading in any lighting condition
- **Responsive Layout**: Adaptive UI elements with smooth transitions

### Data Management
- **Room Database**: Local storage of daily trading logs
- **CSV Export**: Share trading history via any compatible app
- **Secure Storage**: Encrypted SharedPreferences for API credentials
- **Search & Filter**: Quick access to historical trading data

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: 
  - OkHttp for WebSocket connections
  - Retrofit for REST API calls
- **Database**: Room for local data persistence
- **Animations**: Lottie-Compose for rich animations
- **Security**: EncryptedSharedPreferences for sensitive data
- **Async Operations**: Kotlin Coroutines and Flow
- **Push Notifications**: Firebase Cloud Messaging
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Setup Instructions

### Prerequisites
1. Android Studio Hedgehog (2023.1.1) or later
2. JDK 17 or later
3. Alpaca Trading Account (live trading - no paper trading support)
4. Alpaca API Key and Secret

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/austinmhill88/Vanta.git
   cd Vanta
   ```

2. **Configure Firebase**
   - Create a Firebase project at https://console.firebase.google.com
   - Add an Android app with package name `com.alpaca.traderpro`
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Firebase Cloud Messaging in your Firebase console

3. **Update Lottie Animations (Optional)**
   - Replace placeholder animation files in `app/src/main/res/raw/` with actual Lottie animations
   - Download free animations from https://lottiefiles.com or create custom ones
   - Required files:
     - `loading_animation.json` - Splash screen animation
     - `celebration_confetti.json` - Profit celebration
     - `celebration_big_win.json` - Big win celebration (>1% profit)
     - `success_checkmark.json` - Settings save success

4. **Build the Project**
   ```bash
   ./gradlew build
   ```

5. **Run on Device/Emulator**
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

## First Time Setup

1. **Launch the App**
   - On first launch, you'll see a risk disclaimer
   - Read and accept the disclaimer

2. **Enter API Credentials**
   - Input your Alpaca API Key
   - Input your Alpaca API Secret
   - These are stored securely using EncryptedSharedPreferences

3. **Start Trading**
   - Navigate to the Home screen
   - Enter a stock symbol (e.g., AAPL, TSLA)
   - Watch live prices update in real-time
   - Use Buy All (2x) or Sell All buttons to trade

## Usage Guide

### Home Screen
- **Symbol Input**: Change the stock symbol you want to track
- **Current Price**: Large display showing real-time price with animations
- **High/Low Cards**: Today's high and low prices with timestamps
- **Buy All (2x)**: Purchases maximum shares with 2x leverage
- **Sell All**: Sells all shares of the current symbol
- **Pull-to-Refresh**: Swipe down to reconnect WebSocket and refresh data

### Logs Screen
- **View History**: Browse all daily trading logs
- **Search**: Filter logs by symbol or date
- **Export CSV**: Share logs via email, cloud storage, etc.
- **Expand Details**: Tap any log to see full details
- **Delete**: Remove individual log entries

### Settings Screen
- **API Credentials**: Update your Alpaca API key/secret
- **Time Windows**: Set manual buy/sell time windows
- **Suggest Windows**: Analyze last 30 days to suggest optimal trading times
- **Notifications**: Enable/disable trading window notifications
- **Save**: Persist all settings with celebration animation

## Security Considerations

⚠️ **IMPORTANT SECURITY NOTES**:

1. **Live Trading Only**: This app uses Alpaca's live trading API, not paper trading
2. **API Keys**: Never share your API keys or commit them to version control
3. **Risk Warning**: Trading involves substantial risk of loss
4. **Personal Use**: This app is designed for personal use only
5. **Data Encryption**: API credentials are encrypted on device
6. **HTTPS Only**: All network traffic uses secure connections

## Configuration

### Time Windows
- Set custom buy/sell windows in Settings
- Format: HH:mm (24-hour format)
- Example: Buy window 10:00-11:00, Sell window 14:00-15:00
- Get AI suggestions based on historical data

### Notifications
- Enable in Settings to receive alerts when time windows become active
- Requires notification permission (requested at runtime)
- Includes vibration for immediate attention

## Troubleshooting

### WebSocket Connection Issues
- Ensure you have an active internet connection
- Verify API credentials are correct
- Check Alpaca API status at https://status.alpaca.markets
- Try pull-to-refresh to reconnect

### Build Issues
- Clean project: `./gradlew clean`
- Invalidate caches in Android Studio
- Ensure all dependencies are downloaded
- Check that google-services.json is present

### Trading Issues
- Verify account has sufficient buying power
- Ensure market is open (9:30 AM - 4:00 PM ET, weekdays)
- Check symbol is valid and tradeable
- Review error messages in the app

## Development Notes

### Project Structure
```
app/
├── src/main/
│   ├── java/com/alpaca/traderpro/
│   │   ├── data/
│   │   │   ├── database/     # Room database entities and DAOs
│   │   │   ├── model/        # Data models for API
│   │   │   ├── repository/   # Data repositories
│   │   │   └── service/      # API and WebSocket services
│   │   ├── domain/           # ViewModels and business logic
│   │   ├── ui/
│   │   │   ├── screens/      # Compose screens
│   │   │   ├── theme/        # Material3 theme
│   │   │   └── navigation/   # Navigation setup
│   │   ├── AlpacaTraderApp.kt
│   │   └── MainActivity.kt
│   └── res/
│       ├── values/           # Strings, colors, themes
│       ├── raw/              # Lottie animations
│       └── xml/              # Configuration files
```

### Adding New Features
1. Create data models in `data/model/`
2. Add repository methods in `data/repository/`
3. Update ViewModels in `domain/`
4. Create/modify Compose screens in `ui/screens/`
5. Add necessary animations and resources

## Contributing

This is a personal-use application. Contributions are welcome but please note:
- Maintain MVVM architecture
- Follow Material3 design guidelines
- Add animations for new features
- Update documentation
- Test thoroughly (live trading involved!)

## License

This project is provided as-is for educational and personal use.

## Disclaimer

**RISK WARNING**: Trading stocks and securities involves substantial risk. This application is provided for personal use only. The developers are not responsible for any financial losses incurred through the use of this application. Always trade responsibly and never invest more than you can afford to lose.

## API Documentation

- Alpaca API Docs: https://alpaca.markets/docs/
- WebSocket Streaming: https://alpaca.markets/docs/api-references/market-data-api/stock-pricing-data/realtime/
- Trading API: https://alpaca.markets/docs/api-references/trading-api/

## Support

For Alpaca API issues: https://alpaca.markets/support
For app issues: Open an issue on GitHub

---

**Made with ❤️ for day traders**
