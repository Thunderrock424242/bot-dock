# Discord Bot Host — NeoForge 1.21.1

A server-side-only NeoForge mod that **hosts a Discord bot inside your Minecraft server process**. No separate bot host, no external process manager needed.

All built-in features are **optional modules** — the bot will run with every one of them disabled. Other mods can plug in their own modules and commands via the included API.

---

## Architecture

```
DiscordBotEngine          ← core: JDA lifecycle, module/command registry, send API
    │
    ├── IBotModule        ← interface any mod can implement to add features
    │     ├── ChatBridgeModule      (optional, config toggle)
    │     ├── PlayerEventsModule    (optional, config toggle)
    │     └── ServerStatsModule     (optional, config toggle)
    │
    └── IDiscordCommand   ← interface for !command handlers
```

---

## Quick Start

### 1. Create a Discord Bot

1. Go to [discord.com/developers/applications](https://discord.com/developers/applications) → **New Application**
2. Bot tab → **Add Bot** → copy the **Token**
3. Under **Privileged Gateway Intents**, enable:
    - ✅ Server Members Intent
    - ✅ Message Content Intent
4. OAuth2 tab → URL Generator → scope `bot`, permission `Send Messages` → invite to your server

### 2. Get Your Channel ID

Discord → User Settings → Advanced → **Developer Mode** on → right-click your channel → **Copy Channel ID**

### 3. Configure

Edit `config/discordbot-server.toml` (generated on first run):

```toml
[bot]
    token = "YOUR_BOT_TOKEN_HERE"
    channelId = "YOUR_CHANNEL_ID_HERE"

[modules]
    # All false by default — enable what you want
    enableChatBridge    = false
    enablePlayerEvents  = false
    enableServerStats   = false

    [modules.playerEvents]
        notifyJoin  = true
        notifyLeave = true
        notifyDeath = true

[formats]
    discordChatFormat = "**[MC] {player}:** {message}"
    mcChatFormat      = "§9[Discord] §f{user}§7: {message}"
    joinFormat        = "➕ **{player}** joined the server."
    leaveFormat       = "➖ **{player}** left the server."
    deathFormat       = "💀 {message}"
```

Restart the server after editing the config.

---

## Built-in Modules

| Module | Config key | What it does |
|---|---|---|
| Chat Bridge | `enableChatBridge` | Relays MC ↔ Discord chat |
| Player Events | `enablePlayerEvents` | Join / leave / death notifications |
| Server Stats | `enableServerStats` | `!list`, `!tps`, `!uptime`, `!help` commands |

---

## API — Adding Your Own Modules

Add `discordbot` as a dependency in your mod's `mods.toml`:

```toml
[[dependencies.yourmod]]
    modId = "discordbot"
    type  = "required"
    versionRange = "[1.0.0,)"
    ordering = "AFTER"
    side = "SERVER"
```

### Implementing IBotModule

```java
public class MyModule implements IBotModule {

    @Override
    public String getId() { return "yourmod:my_module"; }

    @Override
    public void onEnable(JDA jda, MinecraftServer server) {
        // Store references, register NeoForge event listeners, etc.
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        NeoForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void onDiscordMessage(MessageReceivedEvent event) {
        // Called for every non-command message in the bridge channel
    }

    @Override
    public void registerCommands(Map<String, IDiscordCommand> registry) {
        registry.put("mycommand", (event, args, server) -> {
            DiscordBotEngine.sendToChannel(event.getChannel().getId(), "Hello from MyMod!");
        });
    }
}
```

### Registering Your Module

```java
@SubscribeEvent
public void onServerStarted(ServerStartedEvent event) {
    if (DiscordBotEngine.isReady()) {
        DiscordBotEngine.registerModule(new MyModule());
    }
}
```

### Registering a Standalone Command (no full module needed)

```java
DiscordBotEngine.registerCommand("seed", (event, args, server) -> {
    long seed = server.overworld().getSeed();
    DiscordBotEngine.sendToChannel(event.getChannel().getId(), "🌍 Seed: `" + seed + "`");
});
```

### Sending Messages from Anywhere

```java
// To the configured bridge channel
DiscordBotEngine.sendToBridgeChannel("Something happened!");

// To any specific channel by ID
DiscordBotEngine.sendToChannel("1234567890", "Targeted message.");

// Access raw JDA for advanced use
JDA jda = DiscordBotEngine.getJDA();
```

---

## Build

```bash
./gradlew shadowJar
```

Output: `build/libs/discordbot-1.0.0.jar` — drop in your server's `mods/` folder.

JDA is shaded automatically. No extra jars needed.

---

## Requirements

- NeoForge 1.21.1
- Java 21
- Server-side only (clients don't need this mod)