# XinPga

基于 **xinbot 2.x** 框架制作的宣传插件，支持多信息发送、多模式支持和完整的国际化功能。

[🇨🇳 中文文档](README.md) | [🇺🇸 English Documentation](README_EN.md)

## ✨ 特性

- 🌍 **完整国际化支持** - 支持7种语言（英语、简体中文、繁体中文、德语、法语、日语、俄语）
- 🤖 **xinbot 2.x 框架** - 完全适配xinbot 2.x版本的插件架构
- 📢 **多种发送模式** - 支持公屏发送和私聊发送两种模式
- 🔀 **多线程发送** - 支持独立的多线程公告发送功能
- 🎲 **随机发送** - 可配置随机发送模式
- 👋 **问候语系统** - 支持自定义问候语格式
- 🔢 **数字替换** - 可将连续数字替换为数学字体
- 🎮 **远程命令** - 支持通过私聊发送远程命令控制插件
- ⚙️ **灵活配置** - 丰富的配置选项满足各种需求

## 指令帮助

| 指令                                | 描述                    |
|-----------------------------------|-----------------------|
| `/xpa start`                      | 启动定时发送                |
| `/xpa stop`                       | 停止定时发送                |
| `/xpa multistart`                 | 启动多线程公告发送（仅在私聊模式下可用）  |
| `/xpa multistop`                  | 停止多线程公告发送             |
| `/xpa forcestop`                  | 强制中断定时发送              |
| `/xpa string <编号> <文本>`           | 设置指定编号的发送内容           |
| `/xpa addmessage <消息>`            | 添加消息到发送列表             |
| `/xpa removemessage <消息>`         | 从发送列表移除消息             |
| `/xpa listmessages`               | 列出所有发送消息              |
| `/xpa multistring <编号> <文本>`      | 设置多线程发送内容             |
| `/xpa multiaddmessage <消息>`       | 添加消息到多线程发送列表          |
| `/xpa multiremovemessage <消息>`    | 从多线程发送列表移除消息          |
| `/xpa multilistmessages`          | 列出所有多线程发送消息           |
| `/xpa syncmultithread <on/off>`   | 控制是否同步更新多线程消息列表       |
| `/xpa multithreadinterval <秒>`    | 设置多线程发送间隔             |
| `/xpa time <秒>`                   | 设置公告发送间隔              |
| `/xpa mode <PUBLIC/PRIVATE>`      | 设置发送模式                |
| `/xpa privateinterval <秒>`        | 设置私聊发送间隔              |
| `/xpa messageinterval <秒>`        | 设置消息间发送间隔             |
| `/xpa randomSending <on/off>`     | 设置随机发送模式              |
| `/xpa numberreplacement <on/off>` | 设置多线程数字替换功能          |
| `/xpa mainnumberreplacement <on/off>` | 设置主发送模式数字替换功能      |
| `/xpa minconsecutive <数字>`        | 设置最少连续数字数量            |
| `/xpa greeting <enable/disable>`  | 控制问候语开关               |
| `/xpa greeting format <格式>`       | 修改问候语格式，以#name#做玩家占位符 |
| `/xpa updateplayerlist`           | 手动更新在线玩家列表            |
| `/xpa blacklist add <玩家名>`        | 添加玩家到私聊黑名单            |
| `/xpa blacklist remove <玩家名>`     | 从私聊黑名单移除玩家            |
| `/xpa blacklist list`             | 列出私聊黑名单               |
| `/xpa admin add <玩家名>`            | 添加玩家到管理员列表            |
| `/xpa admin remove <玩家名>`         | 从管理员列表移除玩家            |
| `/xpa admin list`                 | 列出管理员                 |
| `/xpa reload`                     | 重载配置文件                |
| `/xpa debug`                      | 显示插件信息                |
| `/xpa help`                       | 显示帮助信息                |

**注：使用指令时无需在控制台添加"/"**

## 🌍 多语言支持

XinPga 现已支持完整的国际化功能，提供以下7种语言：

- 🇺🇸 **英语** (en_us) - English
- 🇨🇳 **简体中文** (zh_cn) - Simplified Chinese
- 🇹🇼 **繁体中文** (zh_tw) - Traditional Chinese
- 🇩🇪 **德语** (de_de) - Deutsch
- 🇫🇷 **法语** (fr_fr) - Français
- 🇯🇵 **日语** (ja_jp) - 日本語
- 🇷🇺 **俄语** (ru_ru) - Русский

所有用户界面文本、命令提示、帮助信息和日志消息都已完全本地化。语言文件位于 `src/main/resources/lang/` 目录下，您可以根据需要自定义翻译或添加新的语言支持。

## 🤖 xinbot 2.x 框架支持

本插件完全适配 **xinbot 2.x** 版本的插件架构，采用现代化的插件开发模式：

- ✅ 使用 `xin.bbtt.mcbot.plugin.Plugin` 接口
- ✅ 基于事件驱动的插件系统
- ✅ 支持动态命令注册和管理
- ✅ 集成 LangManager 进行国际化处理
- ✅ 兼容 xinbot 2.x 的所有核心功能

> **注意**：本插件不兼容 xinbot 1.x 版本，请确保您的 xinbot 框架版本为 2.x 或更高。

## 远程命令使用规范

管理员可以通过私聊方式向机器人发送远程命令来控制插件行为：

```
#command xpa <子命令> [参数...]
```

或者

```
#cmd xpa <子命令> [参数...]
```

### 支持的远程命令

- `start` - 启动定时发送
- `stop` - 停止定时发送
- `multistart` - 启动多线程公告发送（仅在私聊模式下可用）
- `multistop` - 停止多线程公告发送
- `forcestop` - 强制中断定时发送
- `string <编号> <文本>` - 设置指定编号的发送内容
- `addmessage <消息>` - 添加消息到发送列表
- `removemessage <消息>` - 从发送列表移除消息
- `listmessages` - 列出所有发送消息
- `multistring <编号> <文本>` - 设置多线程发送内容
- `multiaddmessage <消息>` - 添加消息到多线程发送列表
- `multiremovemessage <消息>` - 从多线程发送列表移除消息
- `multilistmessages` - 列出所有多线程发送消息
- `syncmultithread <on/off>` - 控制是否同步更新多线程消息列表
- `multithreadinterval <秒>` - 设置多线程发送间隔
- `time <秒>` - 设置公告发送间隔
- `mode <PUBLIC/PRIVATE>` - 设置发送模式
- `privateinterval <秒>` - 设置私聊发送间隔
- `messageinterval <秒>` - 设置消息间发送间隔
- `randomSending <on/off>` - 设置随机发送模式
- `numberreplacement <on/off>` - 设置多线程数字替换功能
- `mainnumberreplacement <on/off>` - 设置主发送模式数字替换功能
- `minconsecutive <数字>` - 设置最少连续数字数量
- `greeting <enable/disable>` - 控制问候语开关
- `greeting format <格式>` - 修改问候语格式，以#name#做玩家占位符
- `updateplayerlist` - 手动更新在线玩家列表
- `blacklist add <玩家名>` - 添加玩家到私聊黑名单
- `blacklist remove <玩家名>` - 从私聊黑名单移除玩家
- `blacklist list` - 列出私聊黑名单
- `reload` - 重载配置文件
- `admin add <玩家名>` - 添加玩家到管理员列表
- `admin remove <玩家名>` - 从管理员列表移除玩家
- `admin list` - 列出管理员

### 需要配置才能通过远程命令使用的功能

以下功能出于安全考虑无法直接在远程命令使用，必须在控制台中直接执行或修改配置文件：
- `admin add <玩家名>` - 添加玩家到管理员列表
- `admin remove <玩家名>` - 从管理员列表移除玩家
- `admin list` - 列出管理员

### 无法直接能通过远程命令使用的功能

以下功能出于使用体验考虑无法直接在远程命令使用，必须在控制台中直接执行：
- `debug` - 显示插件信息

## 📦 安装与配置

### 安装步骤

1. 确保您已安装 **xinbot 2.x** 或更高版本
2. 将编译好的 `.jar` 文件放入 xinbot 的 `plugins` 目录
3. 启动 xinbot，插件会自动加载并生成默认配置文件
4. 编辑 `config.json` 文件以自定义插件行为
5. 如需修改语言，可在配置文件中设置（默认为英语）

### 配置文件说明

```json
{
  // 是否启用插件功能
  "enabled": true,
  // 公告发送间隔（秒）
  "intervalSeconds": 40,
  // 主消息列表
  "messages": [
    "你好啊",
    "本宣传工具基于xinbot框架制作，已在GitHub开源。",
    "xinbot带给您类Bukkit的插件开发体验（github.com/2698269088/XinPga）"
  ],
  // 是否在消息末尾添加随机字符串
  "appendRandomString": true,
  // 随机字符串长度
  "randomLength": 5,
  // 发送模式（PUBLIC=公屏发送，PRIVATE=私聊发送）
  "sendMode": "PRIVATE",
  // 私聊消息发送间隔（秒）
  "privateMessageInterval": 10,
  // 消息间发送间隔（秒）
  "messageInterval": 4,
  // 私聊黑名单列表
  "privateMessageBlacklist": [
    "e_2"
  ],
  // 管理员列表
  "administrators": [],
  // 是否启用远程命令功能
  "remoteCommandEnabled": true,
  // 是否启用远程命令的admin功能
  "remoteCommandAdminEnabled": false,
  // 是否启用随机发送功能
  "randomSendingEnabled": false,
  // 是否启用问候语
  "greetingEnabled": false,
  // 修改问候语格式，以#name#做玩家占位符
  "greetingFormat": "hi，#name#，",
  // 是否启用多线程发送功能
  "multiThreadEnabled": false,
  // 多线程发送的独立消息列表
  "multiThreadMessages": [
    "多线程消息1",
    "多线程消息2"
  ],
  // 多线程发送的持续时间（秒）
  "multiThreadDuration": 30,
  // 多线程发送的消息间隔（秒）
  "multiThreadInterval": 2,
  // 多线程发送检查的间隔（秒）
  "multiThreadCheckInterval": 5,
  // 是否启用多线程数字替换功能（将连续数字替换为数学粗体字体）
  "numberReplacementEnabled": false,
  // 是否启用主发送模式数字替换功能（将连续数字替换为数学粗体字体）
  "mainNumberReplacementEnabled": false,
  // 最少连续数字数量，达到此数量才进行数字替换
  "minConsecutiveNumbers": 5,
  // 是否同步更新主消息列表和多线程消息列表
  "syncMultiThreadMessages": false
}
```

## 🛠️ 开发与构建

### 技术栈

- **Java 17+** - 编程语言
- **Maven** - 项目构建工具
- **xinbot 2.x** - Minecraft Bot 框架
- **LangManager** - 国际化管理器

### 构建项目

```bash
# 克隆仓库
git clone https://github.com/2698269088/XinPga.git

# 进入项目目录
cd XinPga

# 使用 Maven 构建
mvn clean package

# 编译后的 jar 文件位于 target/ 目录
```

### 添加新语言支持

如需添加新的语言支持：

1. 在 `src/main/resources/lang/` 目录下创建新的语言文件（例如：`es_es.lang`）
2. 复制 `en_us.lang` 的内容作为模板
3. 翻译所有键值对为目标语言
4. 确保所有翻译键与基准文件一致
5. 重新构建项目

## 📝 许可证

本项目采用 GPL3.0 许可证开源。详见 [LICENSE](LICENSE) 文件。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！如果您发现了 bug 或有新功能建议，请随时告诉我们。

## 📧 联系方式

- GitHub: [2698269088](https://github.com/2698269088)
- 项目地址: [https://github.com/2698269088/XinPga](https://github.com/2698269088/XinPga)

---

**Made with ❤️ for the xinbot community**