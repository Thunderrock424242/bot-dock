# Bot Dock

Bot Dock is a server-side NeoForge mod for Minecraft 1.21.1. It hosts a Discord
bot directly inside your dedicated server and exposes optional modules for chat
bridging, player event notifications, and server status commands.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.233 or newer in the 21.1 line
- Java 21
- A Discord bot token with Server Members and Message Content intents enabled

## Configuration

Start the server once to generate `config/botdock-server.toml`, then set:

- `bot.token`
- `bot.channelId`
- Any modules you want under `modules`

Clients do not need this mod installed.
