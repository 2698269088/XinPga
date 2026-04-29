# XinPga

A promotional plugin built on the **xinbot 2.x** framework, supporting multi-message sending, multiple modes, and complete internationalization.

[🇨🇳 中文文档](README.md) | [🇺🇸 English Documentation](README_EN.md)

## ✨ Features

- 🌍 **Complete Internationalization** - Supports 7 languages (English, Simplified Chinese, Traditional Chinese, German, French, Japanese, Russian)
- 🤖 **xinbot 2.x Framework** - Fully compatible with xinbot 2.x plugin architecture
- 📢 **Multiple Sending Modes** - Supports both public chat and private message modes
- 🔀 **Multi-thread Sending** - Independent multi-thread announcement sending feature
- 🎲 **Random Sending** - Configurable random sending mode
- 👋 **Greeting System** - Customizable greeting format
- 🔢 **Number Replacement** - Replace consecutive numbers with mathematical fonts
- 🎮 **Remote Commands** - Control plugin via private messages
- ⚙️ **Flexible Configuration** - Rich configuration options for various needs

## Command Reference

| Command                             | Description                                    |
|-------------------------------------|------------------------------------------------|
| `/xpa start`                        | Start scheduled sending                        |
| `/xpa stop`                         | Stop scheduled sending                         |
| `/xpa multistart`                   | Start multi-thread announcement (private mode only) |
| `/xpa multistop`                    | Stop multi-thread announcement                 |
| `/xpa forcestop`                    | Force interrupt scheduled sending              |
| `/xpa string <index> <text>`        | Set sending content by index                   |
| `/xpa addmessage <message>`         | Add message to sending list                    |
| `/xpa removemessage <message>`      | Remove message from sending list               |
| `/xpa listmessages`                 | List all sending messages                      |
| `/xpa multistring <index> <text>`   | Set multi-thread sending content               |
| `/xpa multiaddmessage <message>`    | Add message to multi-thread list               |
| `/xpa multiremovemessage <message>` | Remove message from multi-thread list          |
| `/xpa multilistmessages`            | List all multi-thread messages                 |
| `/xpa syncmultithread <on/off>`     | Toggle sync multi-thread message list          |
| `/xpa multithreadinterval <sec>`    | Set multi-thread sending interval              |
| `/xpa time <seconds>`               | Set announcement sending interval              |
| `/xpa mode <PUBLIC/PRIVATE>`        | Set sending mode                               |
| `/xpa privateinterval <seconds>`    | Set private message interval                   |
| `/xpa messageinterval <seconds>`    | Set interval between messages                  |
| `/xpa randomSending <on/off>`       | Toggle random sending mode                     |
| `/xpa numberreplacement <on/off>`   | Toggle number replacement feature              |
| `/xpa minconsecutive <number>`      | Set minimum consecutive numbers                |
| `/xpa greeting <enable/disable>`    | Toggle greeting feature                        |
| `/xpa greeting format <format>`     | Modify greeting format (#name# as placeholder) |
| `/xpa updateplayerlist`             | Manually update online player list             |
| `/xpa blacklist add <player>`       | Add player to private message blacklist        |
| `/xpa blacklist remove <player>`    | Remove player from blacklist                   |
| `/xpa blacklist list`               | List private message blacklist                 |
| `/xpa admin add <player>`           | Add player to administrator list               |
| `/xpa admin remove <player>`        | Remove player from administrator list          |
| `/xpa admin list`                   | List administrators                            |
| `/xpa reload`                       | Reload configuration file                      |
| `/xpa debug`                        | Display plugin information                     |
| `/xpa help`                         | Display help information                       |

**Note: Do not add "/" when using commands in console**

## 🌍 Multi-language Support

XinPga now supports complete internationalization with the following 7 languages:

- 🇺🇸 **English** (en_us)
- 🇨🇳 **Simplified Chinese** (zh_cn)
- 🇹🇼 **Traditional Chinese** (zh_tw)
- 🇩🇪 **German** (de_de)
- 🇫🇷 **French** (fr_fr)
- 🇯🇵 **Japanese** (ja_jp)
- 🇷🇺 **Russian** (ru_ru)

All user interface text, command prompts, help information, and log messages are fully localized. Language files are located in the `src/main/resources/lang/` directory. You can customize translations or add new language support as needed.

## 🤖 xinbot 2.x Framework Support

This plugin is fully compatible with the **xinbot 2.x** version plugin architecture, adopting modern plugin development patterns:

- ✅ Uses `xin.bbtt.mcbot.plugin.Plugin` interface
- ✅ Event-driven plugin system
- ✅ Dynamic command registration and management
- ✅ Integrated LangManager for internationalization
- ✅ Compatible with all core features of xinbot 2.x

> **Note**: This plugin is NOT compatible with xinbot 1.x. Please ensure your xinbot framework version is 2.x or higher.

## Remote Command Usage

Administrators can send remote commands to the bot via private messages to control plugin behavior:

```
#command xpa <subcommand> [parameters...]
```

Or

```
#cmd xpa <subcommand> [parameters...]
```

### Supported Remote Commands

- `start` - Start scheduled sending
- `stop` - Stop scheduled sending
- `multistart` - Start multi-thread announcement (private mode only)
- `multistop` - Stop multi-thread announcement
- `forcestop` - Force interrupt scheduled sending
- `string <index> <text>` - Set sending content by index
- `addmessage <message>` - Add message to sending list
- `removemessage <message>` - Remove message from sending list
- `listmessages` - List all sending messages
- `multistring <index> <text>` - Set multi-thread sending content
- `multiaddmessage <message>` - Add message to multi-thread list
- `multiremovemessage <message>` - Remove message from multi-thread list
- `multilistmessages` - List all multi-thread messages
- `syncmultithread <on/off>` - Toggle sync multi-thread message list
- `multithreadinterval <seconds>` - Set multi-thread sending interval
- `time <seconds>` - Set announcement sending interval
- `mode <PUBLIC/PRIVATE>` - Set sending mode
- `privateinterval <seconds>` - Set private message interval
- `messageinterval <seconds>` - Set interval between messages
- `randomSending <on/off>` - Toggle random sending mode
- `numberreplacement <on/off>` - Toggle number replacement feature
- `minconsecutive <number>` - Set minimum consecutive numbers
- `greeting <enable/disable>` - Toggle greeting feature
- `greeting format <format>` - Modify greeting format (#name# as placeholder)
- `updateplayerlist` - Manually update online player list
- `blacklist add <player>` - Add player to private message blacklist
- `blacklist remove <player>` - Remove player from blacklist
- `blacklist list` - List private message blacklist
- `reload` - Reload configuration file
- `admin add <player>` - Add player to administrator list
- `admin remove <player>` - Remove player from administrator list
- `admin list` - List administrators

### Features Requiring Configuration for Remote Commands

The following features cannot be used directly via remote commands for security reasons and must be executed in console or by modifying configuration files:
- `admin add <player>` - Add player to administrator list
- `admin remove <player>` - Remove player from administrator list
- `admin list` - List administrators

### Features Not Available via Remote Commands

The following features cannot be used directly via remote commands for user experience considerations and must be executed in console:
- `debug` - Display plugin information

## 📦 Installation & Configuration

### Installation Steps

1. Ensure you have **xinbot 2.x** or higher installed
2. Place the compiled `.jar` file into xinbot's `plugins` directory
3. Start xinbot, the plugin will automatically load and generate default configuration
4. Edit `config.json` to customize plugin behavior
5. To change language, configure in the settings (default is English)

### Configuration File

```json
{
  // Whether to enable plugin functionality
  "enabled": true,
  // Announcement sending interval (seconds)
  "intervalSeconds": 40,
  // Main message list
  "messages": [
    "Hello",
    "This promotional tool is based on xinbot framework, open-sourced on GitHub.",
    "xinbot brings you Bukkit-like plugin development experience (github.com/2698269088/XinPga)"
  ],
  // Whether to append random strings to messages
  "appendRandomString": true,
  // Random string length
  "randomLength": 5,
  // Sending mode (PUBLIC=public chat, PRIVATE=private message)
  "sendMode": "PRIVATE",
  // Private message sending interval (seconds)
  "privateMessageInterval": 10,
  // Interval between messages (seconds)
  "messageInterval": 4,
  // Private message blacklist
  "privateMessageBlacklist": [
    "e_2"
  ],
  // Administrator list
  "administrators": [],
  // Whether to enable remote command functionality
  "remoteCommandEnabled": true,
  // Whether to enable admin functionality in remote commands
  "remoteCommandAdminEnabled": false,
  // Whether to enable random sending
  "randomSendingEnabled": false,
  // Whether to enable greetings
  "greetingEnabled": false,
  // Greeting format, use #name# as player placeholder
  "greetingFormat": "Hi, #name#, ",
  // Whether to enable multi-thread sending
  "multiThreadEnabled": false,
  // Independent message list for multi-thread sending
  "multiThreadMessages": [
    "Multi-thread message 1",
    "Multi-thread message 2"
  ],
  // Multi-thread sending duration (seconds)
  "multiThreadDuration": 30,
  // Multi-thread message interval (seconds)
  "multiThreadInterval": 2,
  // Multi-thread check interval (seconds)
  "multiThreadCheckInterval": 5,
  // Whether to enable number replacement (replace consecutive numbers with math bold font)
  "numberReplacementEnabled": false,
  // Minimum consecutive numbers threshold for replacement
  "minConsecutiveNumbers": 5,
  // Whether to sync main message list and multi-thread message list
  "syncMultiThreadMessages": false
}
```

## 🛠️ Development & Build

### Tech Stack

- **Java 17+** - Programming language
- **Maven** - Build tool
- **xinbot 2.x** - Minecraft Bot framework
- **LangManager** - Internationalization manager

### Building the Project

```bash
# Clone the repository
git clone https://github.com/2698269088/XinPga.git

# Enter project directory
cd XinPga

# Build with Maven
mvn clean package

# Compiled jar file will be in target/ directory
```

### Adding New Language Support

To add support for a new language:

1. Create a new language file in `src/main/resources/lang/` (e.g., `es_es.lang`)
2. Copy contents from `en_us.lang` as template
3. Translate all key-value pairs to target language
4. Ensure all translation keys match the base file
5. Rebuild the project

## 📝 License

This project is open-sourced under the GPL-3.0 License. See the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Issues and Pull Requests are welcome! If you find bugs or have feature suggestions, please let us know.

## 📧 Contact

- GitHub: [2698269088](https://github.com/2698269088)
- Project URL: [https://github.com/2698269088/XinPga](https://github.com/2698269088/XinPga)

---

**Made with ❤️ for the xinbot community**
