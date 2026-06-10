package com.thunder.botdock;

import com.thunder.botdock.api.IBotModule;
import com.thunder.botdock.api.IDiscordCommand;
import com.thunder.botdock.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Core engine for the Discord Bot Host mod.
 *
 * This class manages:
 *  - JDA lifecycle (start / shutdown)
 *  - Module registration (IBotModule) — lifecycle hooks into bot + MC events
 *  - Command registration (IDiscordCommand) — slash-style !commands from Discord
 *  - Sending messages to Discord from anywhere in the codebase
 *
 * Other mods can interact with this engine via the public API in the api/ package.
 */
public class DiscordBotEngine {

    private static JDA jda;
    private static MinecraftServer server;
    private static String bridgeChannelId;

    // Registered modules (feature bundles)
    private static final List<IBotModule> modules = new ArrayList<>();

    // Registered !commands, keyed by their trigger (e.g. "list", "tps")
    private static final Map<String, IDiscordCommand> commands = new LinkedHashMap<>();

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Starts JDA and connects to Discord.
     * @return true if successful, false on failure
     */
    public static boolean start(MinecraftServer mcServer, String token) {
        server = mcServer;
        bridgeChannelId = BotConfig.CHANNEL_ID.get();

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new InternalDiscordListener())
                    .build();

            jda.awaitReady();
            DiscordBotMod.LOGGER.info("[DiscordBot] Connected as: {}", jda.getSelfUser().getAsTag());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            DiscordBotMod.LOGGER.error("[DiscordBot] Interrupted while connecting", e);
        } catch (Exception e) {
            DiscordBotMod.LOGGER.error("[DiscordBot] Failed to start: {}", e.getMessage());
        }
        return false;
    }

    /** Gracefully shuts down the bot and calls onDisable() on all modules. */
    public static void shutdown() {
        if (jda != null) {
            modules.forEach(m -> {
                try { m.onDisable(); } catch (Exception e) {
                    DiscordBotMod.LOGGER.warn("[DiscordBot] Error disabling module {}: {}", m.getId(), e.getMessage());
                }
            });
            modules.clear();
            commands.clear();
            jda.shutdown();
            jda = null;
            DiscordBotMod.LOGGER.info("[DiscordBot] Shut down cleanly.");
        }
    }

    // -------------------------------------------------------------------------
    // Module API
    // -------------------------------------------------------------------------

    /**
     * Registers a bot module. Modules are feature bundles that hook into
     * bot events and Minecraft events. Built-in modules and third-party mod
     * modules both use this same path.
     *
     * Call from ServerStartedEvent after DiscordBotEngine.start() returns true.
     */
    public static void registerModule(IBotModule module) {
        if (!isReady()) {
            DiscordBotMod.LOGGER.warn("[DiscordBot] Cannot register module '{}' — bot is not running.", module.getId());
            return;
        }
        modules.add(module);
        module.onEnable(jda, server);

        // Let the module register its own commands
        module.registerCommands(commands);
    }

    /** Returns how many modules are currently registered. */
    public static int getModuleCount() {
        return modules.size();
    }

    // -------------------------------------------------------------------------
    // Command API
    // -------------------------------------------------------------------------

    /**
     * Registers a standalone !command without needing a full module.
     * Useful for simple one-off commands from other mods.
     *
     * Example:
     *   DiscordBotEngine.registerCommand("weather", (event, args, server) -> {
     *       DiscordBotEngine.sendToChannel(event.getChannel().getId(), "It's sunny!");
     *   });
     */
    public static void registerCommand(String trigger, IDiscordCommand handler) {
        commands.put(trigger.toLowerCase(), handler);
        DiscordBotMod.LOGGER.info("[DiscordBot] Registered command: !{}", trigger);
    }

    // -------------------------------------------------------------------------
    // Messaging API
    // -------------------------------------------------------------------------

    /**
     * Sends a message to the configured bridge channel.
     * This is the main helper for modules that don't need a specific channel.
     */
    public static void sendToBridgeChannel(String message) {
        sendToChannel(bridgeChannelId, message);
    }

    /**
     * Sends a message to any Discord channel by ID.
     * Use this when you want to send to a channel other than the bridge.
     */
    public static void sendToChannel(@Nullable String channelId, String message) {
        if (!isReady() || channelId == null || channelId.isBlank()) return;

        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            DiscordBotMod.LOGGER.warn("[DiscordBot] Channel not found: {}", channelId);
            return;
        }
        channel.sendMessage(message).queue(
                s -> {},
                e -> DiscordBotMod.LOGGER.warn("[DiscordBot] Failed to send message: {}", e.getMessage())
        );
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** Returns true if JDA is running and connected. */
    public static boolean isReady() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    /** Returns the raw JDA instance. Useful for advanced module use cases. */
    @Nullable
    public static JDA getJDA() {
        return jda;
    }

    /** Returns the running MinecraftServer instance. */
    @Nullable
    public static MinecraftServer getServer() {
        return server;
    }

    /** Returns the configured bridge channel ID. */
    public static String getBridgeChannelId() {
        return bridgeChannelId;
    }

    // -------------------------------------------------------------------------
    // Internal Discord event listener
    // -------------------------------------------------------------------------

    private static class InternalDiscordListener extends ListenerAdapter {

        @Override
        public void onMessageReceived(@NotNull MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;

            String content = event.getMessage().getContentDisplay().trim();
            String channelId = event.getChannel().getId();

            // Dispatch !commands (work in any channel)
            if (content.startsWith("!")) {
                String[] parts = content.substring(1).split("\\s+", 2);
                String trigger = parts[0].toLowerCase();
                String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];

                IDiscordCommand cmd = commands.get(trigger);
                if (cmd != null) {
                    try {
                        cmd.execute(event, args, server);
                    } catch (Exception e) {
                        DiscordBotMod.LOGGER.error("[DiscordBot] Error executing !{}: {}", trigger, e.getMessage());
                        sendToChannel(channelId, "⚠️ An error occurred running that command.");
                    }
                }
                return;
            }

            // Forward non-command messages from the bridge channel to all modules
            if (channelId.equals(bridgeChannelId)) {
                modules.forEach(m -> {
                    try { m.onDiscordMessage(event); } catch (Exception e) {
                        DiscordBotMod.LOGGER.warn("[DiscordBot] Module {} threw on onDiscordMessage: {}", m.getId(), e.getMessage());
                    }
                });
            }
        }
    }
}