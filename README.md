# XinPga

基于xinbot制作的宣传插件，支持多信息发送和多模式支持。

## 指令帮助

| 指令 | 描述 |
|------|------|
| `/xpa start` | 启动定时发送 |
| `/xpa stop` | 停止定时发送 |
| `/xpa string <文本信息>` | 设置发送内容（兼容旧命令） |
| `/xpa addmessage <消息>` | 添加消息到发送列表 |
| `/xpa removemessage <消息>` | 从发送列表移除消息 |
| `/xpa listmessages` | 列出所有发送消息 |
| `/xpa time <秒>` | 设置公告发送间隔 |
| `/xpa mode <PUBLIC/PRIVATE&>` | 设置发送模式 |
| `/xpa privateinterval <秒>` | 设置私聊发送间隔 |
| `/xpa messageinterval <秒>` | 设置消息间发送间隔 |
| `/xpa blacklist add <玩家名>` | 添加玩家到私聊黑名单 |
| `/xpa blacklist remove <玩家名>` | 从私聊黑名单移除玩家 |
| `/xpa blacklist /list/` | 列出私聊黑名单 |
| `/xpa reload` | 重载配置文件 |
| `/xpa help` | 显示此帮助信息 |
**注：使用指令时无需在控制台添加"/"**

## 配置文件帮助

```json
{
  // 是否启用插件功能
  "enabled": false,
  // 公告模式下的发送间隔（秒）
  "intervalSeconds": 40,
  // 要发送的消息列表
  "messages": [
    "你好啊",
    "本宣传工具基于xinbot框架制作，已在GitHub开源。xinbot带给您类Bukkit的插件开发体验（github.com/2698269088/XinPga）"
  ],
  // 是否在消息末尾追加随机字符串
  "appendRandomString": true,
  // 随机字符串长度
  "randomLength": 5,
  // 发送模式：PUBLIC（公告）或 PRIVATE（私聊）
  "sendMode": "PRIVATE",
  // 私聊模式下的发送间隔（秒）
  "privateMessageInterval": 2,
  // 多条消息之间的发送间隔（秒）
  "messageInterval": 1,
  // 私聊黑名单玩家列表
  "privateMessageBlacklist": [
    "e_2"
  ]
}
