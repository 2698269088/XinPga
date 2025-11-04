# XinPga

基于xinbot制作的宣传插件，支持多信息发送和多模式支持。

## 指令帮助

| 指令 | 描述 |
|------|------|
| `/xpa start` | 启动定时发送 |
| `/xpa stop` | 停止定时发送 |
| `/xpa string <编号> <文本>` | 设置指定编号的发送内容 |
| `/xpa addmessage <消息>` | 添加消息到发送列表 |
| `/xpa removemessage <消息>` | 从发送列表移除消息 |
| `/xpa listmessages` | 列出所有发送消息 |
| `/xpa time <秒>` | 设置公告发送间隔 |
| `/xpa mode <PUBLIC/PRIVATE>` | 设置发送模式 |
| `/xpa privateinterval <秒>` | 设置私聊发送间隔 |
| `/xpa messageinterval <秒>` | 设置消息间发送间隔 |
| `/xpa updateplayerlist` | 手动更新在线玩家列表 |
| `/xpa blacklist add <玩家名>` | 添加玩家到私聊黑名单 |
| `/xpa blacklist remove <玩家名>` | 从私聊黑名单移除玩家 |
| `/xpa blacklist list` | 列出私聊黑名单 |
| `/xpa admin add <玩家名>` | 添加玩家到管理员列表 |
| `/xpa admin remove <玩家名>` | 从管理员列表移除玩家 |
| `/xpa admin list` | 列出管理员 |
| `/xpa reload` | 重载配置文件 |
| `/xpa debug` | 显示插件信息 |
| `/xpa help` | 显示帮助信息 |

**注：使用指令时无需在控制台添加"/"**

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
- `string <编号> <文本>` - 设置指定编号的发送内容
- `addmessage <消息>` - 添加消息到发送列表
- `removemessage <消息>` - 从发送列表移除消息
- `listmessages` - 列出所有发送消息
- `time <秒>` - 设置公告发送间隔
- `mode <PUBLIC/PRIVATE>` - 设置发送模式
- `privateinterval <秒>` - 设置私聊发送间隔
- `messageinterval <秒>` - 设置消息间发送间隔
- `updateplayerlist` - 手动更新在线玩家列表
- `blacklist add <玩家名>` - 添加玩家到私聊黑名单
- `blacklist remove <玩家名>` - 从私聊黑名单移除玩家
- `blacklist list` - 列出私聊黑名单
- `reload` - 重载配置文件
- `debug` - 显示插件信息

### 需要配置才能通过远程命令使用的功能

以下功能出于安全考虑无法直接在远程命令使用，必须在控制台中直接执行或修改配置文件：
- `admin add <玩家名>` - 添加玩家到管理员列表
- `admin remove <玩家名>` - 从管理员列表移除玩家
- `admin list` - 列出管理员

### 无法直接能通过远程命令使用的功能

以下功能出于使用体验考虑无法直接在远程命令使用，必须在控制台中直接执行：
- `debug` - 显示插件信息

## 配置文件帮助

```json
{
  // 是否启用插件功能
  "enabled": true,
  
  // 公告发送间隔（秒）
  "intervalSeconds": 40,
  
  // 宣传消息列表
  "messages": [
    "你好啊",
    "本宣传工具基于xinbot框架制作，已在GitHub开源。xinbot带给您类Bukkit的插件开发体验（github.com/2698269088/XinPga）"
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
  "remoteCommandAdminEnabled": false
}
}
