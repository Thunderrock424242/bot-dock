package com.thunder.botdock.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

/**
 * Server config — written to: config/discordbot-server.toml
 *
 * Three sections:
 *   [bot]       — core JDA settings (token, channel)
 *   [modules]   — toggle built-in feature modules on/off
 *   [formats]   — customize the text format for each message type
 */
@EventBusSubscriber(modid = "discordbot", bus = EventBusSubscriber.Bus.MOD)
public class BotConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ── [bot] ────────────────────────────────────────────────────────────────

    public static final ModConfigSpec.ConfigValue<String> BOT_TOKEN;
    public static final ModConfigSpec.ConfigValue<String> CHANNEL_ID;

    // ── [modules] ────────────────────────────────────────────────────────────

    public static final ModConfigSpec.BooleanValue ENABLE_CHAT_BRIDGE;
    public static final ModConfigSpec.BooleanValue ENABLE_PLAYER_EVENTS;
    public static final ModConfigSpec.BooleanValue ENABLE_SERVER_STATS;

    public static final ModConfigSpec.BooleanValue NOTIFY_JOIN;
    public static final ModConfigSpec.BooleanValue NOTIFY_LEAVE;
    public static final ModConfigSpec.BooleanValue NOTIFY_DEATH;

    // ── [formats] ────────────────────────────────────────────────────────────

    public static final ModConfigSpec.ConfigValue<String> DISCORD_CHAT_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> MC_CHAT_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> JOIN_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> LEAVE_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> DEATH_FORMAT;

    public static final ModConfigSpec SPEC;

    static {

        // ── [bot] ────────────────────────────────────────────────────────────
        BUILDER.comment(
                "Core bot settings.",
                "Get your token at: https://discord.com/developers/applications",
                "Required intents: SERVER MEMBERS, MESSAGE CONTENT"
        ).push("bot");

        BOT_TOKEN = BUILDER
                .comment("Your Discord bot token. Keep this secret!")
                .define("token", "YOUR_BOT_TOKEN_HERE");

        CHANNEL_ID = BUILDER
                .comment(
                        "The Discord channel ID to use as the bridge / command channel.",
                        "Enable Developer Mode in Discord → right-click channel → Copy Channel ID."
                )
                .define("channelId", "YOUR_CHANNEL_ID_HERE");

        BUILDER.pop();

        // ── [modules] ────────────────────────────────────────────────────────
        BUILDER.comment(
                "Toggle built-in feature modules on or off.",
                "All modules are OPTIONAL — the bot will run fine with all of them disabled.",
                "Third-party mods can register their own modules via the API regardless of these settings."
        ).push("modules");

        ENABLE_CHAT_BRIDGE = BUILDER
                .comment("Relay chat messages between Minecraft and Discord.")
                .define("enableChatBridge", false);

        ENABLE_PLAYER_EVENTS = BUILDER
                .comment("Send player join, leave, and death notifications to Discord.")
                .define("enablePlayerEvents", false);

        ENABLE_SERVER_STATS = BUILDER
                .comment("Enable Discord commands: !list, !tps, !uptime, !help")
                .define("enableServerStats", false);

        BUILDER.comment("Fine-grained toggles for the Player Events module (only used if enablePlayerEvents = true).")
                .push("playerEvents");

        NOTIFY_JOIN = BUILDER.comment("Notify Discord when a player joins.").define("notifyJoin", true);
        NOTIFY_LEAVE = BUILDER.comment("Notify Discord when a player leaves.").define("notifyLeave", true);
        NOTIFY_DEATH = BUILDER.comment("Notify Discord when a player dies.").define("notifyDeath", true);

        BUILDER.pop(); // playerEvents
        BUILDER.pop(); // modules

        // ── [formats] ────────────────────────────────────────────────────────
        BUILDER.comment(
                "Customize the text sent to Discord or Minecraft for each event.",
                "Supports basic Markdown for Discord messages.",
                "Available placeholders per message type are noted in each comment."
        ).push("formats");

        DISCORD_CHAT_FORMAT = BUILDER
                .comment("Minecraft chat → Discord. Placeholders: {player}, {message}")
                .define("discordChatFormat", "**[MC] {player}:** {message}");

        MC_CHAT_FORMAT = BUILDER
                .comment(
                        "Discord → Minecraft chat. Placeholders: {user}, {message}",
                        "Supports Minecraft §color codes, e.g. §9[Discord]"
                )
                .define("mcChatFormat", "§9[Discord] §f{user}§7: {message}");

        JOIN_FORMAT = BUILDER
                .comment("Player join notification. Placeholders: {player}")
                .define("joinFormat", "➕ **{player}** joined the server.");

        LEAVE_FORMAT = BUILDER
                .comment("Player leave notification. Placeholders: {player}")
                .define("leaveFormat", "➖ **{player}** left the server.");

        DEATH_FORMAT = BUILDER
                .comment("Player death notification. Placeholders: {message} (the full death message)")
                .define("deathFormat", "💀 {message}");

        BUILDER.pop(); // formats

        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            boolean tokenSet = !BOT_TOKEN.get().equals("YOUR_BOT_TOKEN_HERE");
            boolean channelSet = !CHANNEL_ID.get().equals("YOUR_CHANNEL_ID_HERE");
            com.thunderrock.discordbot.DiscordBotMod.LOGGER.info(
                    "[DiscordBot] Config loaded — token set: {}, channel set: {}",
                    tokenSet, channelSet
            );
        }
    }
}