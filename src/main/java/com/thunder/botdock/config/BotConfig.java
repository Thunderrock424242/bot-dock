package com.thunder.botdock.config;

import com.thunder.botdock.BotDockMod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server config written to config/botdock-server.toml.
 */
public class BotConfig {

    public static final String DEFAULT_BOT_TOKEN = "YOUR_BOT_TOKEN_HERE";
    public static final String DEFAULT_CHANNEL_ID = "YOUR_CHANNEL_ID_HERE";

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> BOT_TOKEN;
    public static final ModConfigSpec.ConfigValue<String> CHANNEL_ID;

    public static final ModConfigSpec.BooleanValue ENABLE_CHAT_BRIDGE;
    public static final ModConfigSpec.BooleanValue ENABLE_PLAYER_EVENTS;
    public static final ModConfigSpec.BooleanValue ENABLE_SERVER_STATS;

    public static final ModConfigSpec.BooleanValue NOTIFY_JOIN;
    public static final ModConfigSpec.BooleanValue NOTIFY_LEAVE;
    public static final ModConfigSpec.BooleanValue NOTIFY_DEATH;

    public static final ModConfigSpec.ConfigValue<String> DISCORD_CHAT_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> MC_CHAT_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> JOIN_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> LEAVE_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> DEATH_FORMAT;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment(
                "Core Discord bot settings.",
                "Get your token at: https://discord.com/developers/applications",
                "Required intents: Server Members and Message Content"
        ).push("bot");

        BOT_TOKEN = BUILDER
                .comment("Your Discord bot token. Keep this secret.")
                .define("token", DEFAULT_BOT_TOKEN);

        CHANNEL_ID = BUILDER
                .comment(
                        "The Discord channel ID to use as the bridge and command channel.",
                        "Enable Developer Mode in Discord, right-click the channel, then copy the channel ID."
                )
                .define("channelId", DEFAULT_CHANNEL_ID);

        BUILDER.pop();

        BUILDER.comment(
                "Toggle built-in feature modules on or off.",
                "All modules are optional.",
                "Third-party mods can register their own modules through the API regardless of these settings."
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

        BUILDER.comment("Fine-grained toggles for the Player Events module.")
                .push("playerEvents");

        NOTIFY_JOIN = BUILDER.comment("Notify Discord when a player joins.").define("notifyJoin", true);
        NOTIFY_LEAVE = BUILDER.comment("Notify Discord when a player leaves.").define("notifyLeave", true);
        NOTIFY_DEATH = BUILDER.comment("Notify Discord when a player dies.").define("notifyDeath", true);

        BUILDER.pop();
        BUILDER.pop();

        BUILDER.comment(
                "Customize text sent to Discord or Minecraft.",
                "Supports basic Markdown for Discord messages.",
                "Available placeholders per message type are noted in each comment."
        ).push("formats");

        DISCORD_CHAT_FORMAT = BUILDER
                .comment("Minecraft chat to Discord. Placeholders: {player}, {message}")
                .define("discordChatFormat", "**[MC] {player}:** {message}");

        MC_CHAT_FORMAT = BUILDER
                .comment("Discord chat to Minecraft. Placeholders: {user}, {message}")
                .define("mcChatFormat", "[Discord] {user}: {message}");

        JOIN_FORMAT = BUILDER
                .comment("Player join notification. Placeholder: {player}")
                .define("joinFormat", "**{player}** joined the server.");

        LEAVE_FORMAT = BUILDER
                .comment("Player leave notification. Placeholder: {player}")
                .define("leaveFormat", "**{player}** left the server.");

        DEATH_FORMAT = BUILDER
                .comment("Player death notification. Placeholder: {message}")
                .define("deathFormat", "{message}");

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private BotConfig() {
    }

    public static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getType() != ModConfig.Type.SERVER) return;

        boolean tokenSet = !BOT_TOKEN.get().equals(DEFAULT_BOT_TOKEN);
        boolean channelSet = !CHANNEL_ID.get().equals(DEFAULT_CHANNEL_ID);
        BotDockMod.LOGGER.info("[BotDock] Config loaded; token set: {}, channel set: {}", tokenSet, channelSet);
    }
}
