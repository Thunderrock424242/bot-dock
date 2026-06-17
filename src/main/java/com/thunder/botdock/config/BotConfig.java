package com.thunder.botdock.config;

import com.thunder.botdock.BotDockMod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Server config written to config/botdock-server.toml.
 */
public class BotConfig {

    public static final String DEFAULT_BOT_TOKEN = "YOUR_BOT_TOKEN_HERE";
    public static final String DEFAULT_EMPTY_ID = "";

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> BOT_TOKEN;
    public static final ModConfigSpec.ConfigValue<String> BOT_GUILD_ID;

    public static final ModConfigSpec.BooleanValue ENABLE_CHAT_BRIDGE;
    public static final ModConfigSpec.BooleanValue ENABLE_PLAYER_EVENTS;
    public static final ModConfigSpec.BooleanValue ENABLE_SERVER_STATS;
    public static final ModConfigSpec.BooleanValue ENABLE_SUPPORT_DESK;
    public static final ModConfigSpec.BooleanValue ENABLE_QA_RESPONDER;
    public static final ModConfigSpec.BooleanValue ENABLE_PLAYTEST_DESK;

    public static final ModConfigSpec.ConfigValue<String> CHANNEL_BRIDGE;
    public static final ModConfigSpec.ConfigValue<String> CHANNEL_COMMANDS;
    public static final ModConfigSpec.ConfigValue<String> CHANNEL_SUPPORT;
    public static final ModConfigSpec.ConfigValue<String> CHANNEL_BUG_REPORTS;
    public static final ModConfigSpec.ConfigValue<String> CHANNEL_CRASH_REPORTS;
    public static final ModConfigSpec.ConfigValue<String> CHANNEL_FEEDBACK;
    public static final ModConfigSpec.ConfigValue<String> CHANNEL_SUGGESTIONS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CHANNEL_QA;
    public static final ModConfigSpec.ConfigValue<String> CHANNEL_QA_TEAM;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> STAFF_ROLE_IDS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ADMIN_ROLE_IDS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MODERATOR_ROLE_IDS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> QA_ROLE_IDS;

    public static final ModConfigSpec.BooleanValue NOTIFY_JOIN;
    public static final ModConfigSpec.BooleanValue NOTIFY_LEAVE;
    public static final ModConfigSpec.BooleanValue NOTIFY_DEATH;

    public static final ModConfigSpec.ConfigValue<String> DISCORD_CHAT_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> MC_CHAT_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> JOIN_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> LEAVE_FORMAT;
    public static final ModConfigSpec.ConfigValue<String> DEATH_FORMAT;

    public static final ModConfigSpec.ConfigValue<String> SUPPORT_PANEL_TITLE;
    public static final ModConfigSpec.ConfigValue<String> SUPPORT_PANEL_DESCRIPTION;
    public static final ModConfigSpec.BooleanValue SUPPORT_ENABLE_BUG_REPORTS;
    public static final ModConfigSpec.BooleanValue SUPPORT_ENABLE_FEEDBACK;
    public static final ModConfigSpec.BooleanValue SUPPORT_ENABLE_SUGGESTIONS;
    public static final ModConfigSpec.BooleanValue SUPPORT_ENABLE_PERFORMANCE_REPORTS;
    public static final ModConfigSpec.ConfigValue<String> SUPPORT_BUG_LABEL;
    public static final ModConfigSpec.ConfigValue<String> SUPPORT_FEEDBACK_LABEL;
    public static final ModConfigSpec.ConfigValue<String> SUPPORT_SUGGESTION_LABEL;
    public static final ModConfigSpec.ConfigValue<String> SUPPORT_PERFORMANCE_LABEL;

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

        BOT_GUILD_ID = BUILDER
                .comment(
                        "Optional Discord guild/server ID.",
                        "When set, slash commands are registered to this guild and update faster.",
                        "Leave blank to register slash commands globally."
                )
                .define("guildId", DEFAULT_EMPTY_ID);

        BUILDER.pop();

        BUILDER.comment(
                "Toggle built-in feature modules on or off.",
                "All modules are optional.",
                "Third-party mods can register their own modules through the API regardless of these settings."
        ).push("modules");

        ENABLE_CHAT_BRIDGE = BUILDER
                .comment("Relay chat messages between Minecraft and Discord.")
                .define("chatBridge", false);

        ENABLE_PLAYER_EVENTS = BUILDER
                .comment("Send player join, leave, and death notifications to Discord.")
                .define("playerEvents", false);

        ENABLE_SERVER_STATS = BUILDER
                .comment("Enable Discord commands: !list, !tps, !uptime, !help")
                .define("serverStats", false);

        ENABLE_SUPPORT_DESK = BUILDER
                .comment("Enable a configurable support/report panel with Discord buttons and modals.")
                .define("supportDesk", false);

        ENABLE_QA_RESPONDER = BUILDER
                .comment("Reserved for a future configurable Q&A responder module.")
                .define("qaResponder", false);

        ENABLE_PLAYTEST_DESK = BUILDER
                .comment("Reserved for a future configurable playtest flow module.")
                .define("playtestDesk", false);

        BUILDER.comment("Fine-grained toggles for the Player Events module.")
                .push("playerEvents");

        NOTIFY_JOIN = BUILDER.comment("Notify Discord when a player joins.").define("notifyJoin", true);
        NOTIFY_LEAVE = BUILDER.comment("Notify Discord when a player leaves.").define("notifyLeave", true);
        NOTIFY_DEATH = BUILDER.comment("Notify Discord when a player dies.").define("notifyDeath", true);

        BUILDER.pop();
        BUILDER.pop();

        BUILDER.comment(
                "Discord channel IDs used by built-in modules.",
                "Enable Developer Mode in Discord, right-click a channel, then copy the channel ID.",
                "Leave optional channels blank to fall back to the support channel when possible."
        ).push("channels");

        CHANNEL_BRIDGE = BUILDER
                .comment("Minecraft/Discord chat bridge channel.")
                .define("bridge", DEFAULT_EMPTY_ID);

        CHANNEL_COMMANDS = BUILDER
                .comment("Channel for legacy !commands. Leave blank to allow them anywhere the bot can read.")
                .define("commands", DEFAULT_EMPTY_ID);

        CHANNEL_SUPPORT = BUILDER
                .comment("Default support desk channel and fallback report destination.")
                .define("support", DEFAULT_EMPTY_ID);

        CHANNEL_BUG_REPORTS = BUILDER
                .comment("Destination for bug reports. Falls back to support when blank.")
                .define("bugReports", DEFAULT_EMPTY_ID);

        CHANNEL_CRASH_REPORTS = BUILDER
                .comment("Destination for crash/log intake. Reserved for future crash modules.")
                .define("crashReports", DEFAULT_EMPTY_ID);

        CHANNEL_FEEDBACK = BUILDER
                .comment("Destination for feedback reports. Falls back to support when blank.")
                .define("feedback", DEFAULT_EMPTY_ID);

        CHANNEL_SUGGESTIONS = BUILDER
                .comment("Destination for suggestions. Falls back to feedback, then support when blank.")
                .define("suggestions", DEFAULT_EMPTY_ID);

        CHANNEL_QA = BUILDER
                .comment("Channels watched by future Q&A responder modules.")
                .defineListAllowEmpty("qa", List.of(), () -> DEFAULT_EMPTY_ID, value -> value instanceof String);

        CHANNEL_QA_TEAM = BUILDER
                .comment("Private Q&A team channel. Reserved for future Q&A workflows.")
                .define("qaTeam", DEFAULT_EMPTY_ID);

        BUILDER.pop();

        BUILDER.comment(
                "Discord role IDs used for staff/admin/moderation checks.",
                "Leave lists empty to use Discord permissions where a module supports that fallback."
        ).push("roles");

        STAFF_ROLE_IDS = BUILDER
                .comment("Role IDs treated as general staff.")
                .defineListAllowEmpty("staff", List.of(), () -> DEFAULT_EMPTY_ID, value -> value instanceof String);

        ADMIN_ROLE_IDS = BUILDER
                .comment("Role IDs allowed to administer Bot Dock panels and workflows.")
                .defineListAllowEmpty("admin", List.of(), () -> DEFAULT_EMPTY_ID, value -> value instanceof String);

        MODERATOR_ROLE_IDS = BUILDER
                .comment("Role IDs treated as moderators.")
                .defineListAllowEmpty("moderator", List.of(), () -> DEFAULT_EMPTY_ID, value -> value instanceof String);

        QA_ROLE_IDS = BUILDER
                .comment("Role IDs used by future Q&A/playtest workflows.")
                .defineListAllowEmpty("qaTeam", List.of(), () -> DEFAULT_EMPTY_ID, value -> value instanceof String);

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

        BUILDER.comment("Generic configurable support desk panel.").push("supportDesk");

        SUPPORT_PANEL_TITLE = BUILDER
                .comment("Title shown above the support desk buttons.")
                .define("panelTitle", "Server Support Desk");

        SUPPORT_PANEL_DESCRIPTION = BUILDER
                .comment("Description shown in the support desk panel message.")
                .define("panelDescription", "Choose the report type that best matches what you need.");

        SUPPORT_ENABLE_BUG_REPORTS = BUILDER
                .comment("Show the bug report button.")
                .define("enableBugReports", true);

        SUPPORT_ENABLE_FEEDBACK = BUILDER
                .comment("Show the feedback button.")
                .define("enableFeedback", true);

        SUPPORT_ENABLE_SUGGESTIONS = BUILDER
                .comment("Show the suggestion button.")
                .define("enableSuggestions", true);

        SUPPORT_ENABLE_PERFORMANCE_REPORTS = BUILDER
                .comment("Show the performance report button.")
                .define("enablePerformanceReports", true);

        SUPPORT_BUG_LABEL = BUILDER
                .comment("Button label for bug reports.")
                .define("bugReportLabel", "Bug Report");

        SUPPORT_FEEDBACK_LABEL = BUILDER
                .comment("Button label for feedback.")
                .define("feedbackLabel", "Feedback");

        SUPPORT_SUGGESTION_LABEL = BUILDER
                .comment("Button label for suggestions.")
                .define("suggestionLabel", "Suggestion");

        SUPPORT_PERFORMANCE_LABEL = BUILDER
                .comment("Button label for performance reports.")
                .define("performanceReportLabel", "Performance Report");

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private BotConfig() {
    }

    public static boolean isConfiguredId(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean isTokenConfigured() {
        String token = BOT_TOKEN.get();
        return token != null && !token.isBlank() && !token.equals(DEFAULT_BOT_TOKEN);
    }

    public static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getType() != ModConfig.Type.SERVER) return;

        BotDockMod.LOGGER.info(
                "[BotDock] Config loaded; token set: {}, guild set: {}, bridge channel set: {}",
                isTokenConfigured(),
                isConfiguredId(BOT_GUILD_ID.get()),
                isConfiguredId(CHANNEL_BRIDGE.get())
        );
    }
}
