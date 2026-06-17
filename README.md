# Bot Dock

Bot Dock is a server-side NeoForge mod for Minecraft 1.21.1. It hosts a Discord
bot directly inside your dedicated server and exposes optional modules for chat
bridging, player event notifications, server status commands, and a generic
support desk panel.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.233 or newer in the 21.1 line
- Java 21
- A Discord bot token with Server Members and Message Content intents enabled

## Configuration

Start the server once to generate `config/botdock-server.toml`, then set:

- `bot.token`
- `bot.guildId` if you want fast guild slash command updates
- Channel IDs under `channels`
- Any modules you want under `modules`

Clients do not need this mod installed.

Example public server config:

```toml
[bot]
token = "YOUR_BOT_TOKEN_HERE"
guildId = "YOUR_DISCORD_SERVER_ID"

[modules]
chatBridge = true
playerEvents = true
serverStats = true
supportDesk = true
qaResponder = false
playtestDesk = false

[channels]
bridge = "BRIDGE_CHANNEL_ID"
commands = "BOT_COMMAND_CHANNEL_ID"
support = "SUPPORT_CHANNEL_ID"
bugReports = "BUG_REPORT_CHANNEL_ID"
crashReports = ""
feedback = "FEEDBACK_CHANNEL_ID"
suggestions = "SUGGESTIONS_CHANNEL_ID"
qa = []
qaTeam = ""

[roles]
staff = []
admin = []
moderator = []
qaTeam = []

[supportDesk]
panelTitle = "Server Support Desk"
enableBugReports = true
enableFeedback = true
enableSuggestions = true
enablePerformanceReports = true
```

Use `/supportdesk` or `!supportpanel` in Discord to post the support panel.
