# Android TV Browser

A lightweight Android TV browser with comprehensive ad blocking, YouTube ad blocker, and full settings control.

## Features

✨ **Lightweight & Fast**
- Minimal dependencies
- Optimized for TV screens
- Quick startup and navigation

🚫 **Comprehensive Ad Blocking**
- YouTube ad blocker
- General ad blocking (domains & patterns)
- Tracking prevention
- Script blocking (optional)
- Custom blocklist management
- Automatic blocklist updates from public sources

📺 **TV Optimized**
- D-Pad navigation support
- Large, touch-friendly UI
- Leanback Launcher support
- Hardware remote control compatibility

⚙️ **Full Settings Control**
- Enable/disable ad blocking per type
- Custom home page
- Text size adjustment
- JavaScript control
- Cookie & cache management
- History control
- Media autoplay settings

## Building

```bash
# Clone the repository
git clone https://github.com/vinod-rajput-vg/android-tv-browser.git
cd android-tv-browser

# Build the APK
./gradlew assembleRelease

# Debug build
./gradlew assembleDebug
```

## Installation

1. Build the project as shown above
2. Transfer the APK to your Android TV device
3. Install via file manager or ADB:
   ```bash
   adb install app/build/outputs/apk/release/app-release.apk
   ```

## Architecture

### Core Components

**BrowserEngine.kt**
- WebView configuration
- JavaScript injection
- User agent management

**AdBlocker.kt**
- Pattern-based ad detection
- Domain blocking
- DOM element removal
- XHR request blocking

**BlocklistManager.kt**
- Fetches public blocklists
- Manages cached domains
- Automatic updates (24h interval)

**PreferencesManager.kt**
- Centralized preference management
- Settings persistence
- Type-safe accessors

## Ad Blocking Strategy

1. **Pattern Matching** - Blocks URLs matching ad patterns
2. **Domain Blocking** - Blocks known ad domains
3. **DOM Removal** - Hides ad elements from page
4. **Script Injection** - Removes ads dynamically
5. **Request Blocking** - Blocks XHR ad requests

### YouTube Ad Blocking
- Blocks Google ads domains
- Blocks DoubleClick requests
- Removes ad containers
- Blocks video pre-rolls and mid-rolls

## Settings

### Ad Blocking
- Enable/Disable main ad blocker
- YouTube ads specifically
- Tracking scripts
- All JavaScript (nuclear option)

### Browser
- Custom home page URL
- Text size (50-200%)
- JavaScript enable/disable

### Privacy
- Cookie management
- Cache control
- History tracking

### Media
- Auto-play videos

## Performance

- **APK Size**: ~10-15MB (release)
- **Memory**: ~60-80MB typical usage
- **Startup**: <2 seconds
- **Page Load**: Same as standard browser with ad overhead removed

## Permissions

- `INTERNET` - Web browsing
- `ACCESS_NETWORK_STATE` - Network detection
- `WRITE_EXTERNAL_STORAGE` - Download management
- `READ_EXTERNAL_STORAGE` - File access

## Known Limitations

- Some sites may break with script blocking enabled
- Custom blocklists require manual entry
- No built-in password manager
- No sync across devices

## Future Enhancements

- [ ] Bookmark management
- [ ] Tab support
- [ ] Download manager
- [ ] Search engine selection
- [ ] Dark mode toggle
- [ ] Night mode
- [ ] Reading mode
- [ ] VPN/Proxy support

## License

MIT License - feel free to use and modify

## Contributing

Fork the repo and submit pull requests for improvements!
