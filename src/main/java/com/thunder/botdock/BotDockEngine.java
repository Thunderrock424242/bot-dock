package com.thunder.botdock;

import com.thunder.botdock.api.IBotModule;
import com.thunder.botdock.api.IDiscordCommand;
import com.thunder.botdock.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Core engine for the Discord bot hosted by Bot Dock.
 *
 * This class manages:
 * - JDA lifecycle
 * - Bot Dock module registration
 * - Command registration for Discord !commands and slash commands
 * - Dispatch for buttons, select menus, modals, and Discord messages
 * - Sending messages to Discord from the codebase
 */
public class BotDockEngine {

    private static JDA jda;
    private static MinecraftServer server;
    private static String bridgeChannelId;
    private static String commandChannelId;
    private static String guildId;

    private static final List<IBotModule> modules = new ArrayList<>();
    private static final Map<String, IDiscordCommand> commands = new LinkedHashMap<>();
    private static final Map<String, CommandData> slashCommands = new LinkedHashMap<>();

    private BotDockEngine() {
    }

    /**
     * Starts JDA and connects to Discord.
     *
     * @return true if successful, false on failure
     */
    public static boolean start(MinecraftServer mcServer, String token) {
        server = mcServer;
        bridgeChannelId = BotConfig.CHANNEL_BRIDGE.get();
        commandChannelId = BotConfig.CHANNEL_COMMANDS.get();
        guildId = BotConfig.BOT_GUILD_ID.get();

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new InternalDiscordListener())
                    .build();

            jda.awaitReady();
            BotDockMod.LOGGER.info("[BotDock] Connected as: {}", jda.getSelfUser().getAsTag());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            BotDockMod.LOGGER.error("[BotDock] Interrupted while connecting", e);
        } catch (Exception e) {
            BotDockMod.LOGGER.error("[BotDock] Failed to start: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Gracefully shuts down the bot and calls onDisable() on all modules.
     */
    public static void shutdown() {
        if (jda == null) return;

        modules.forEach(module -> {
            try {
                module.onDisable();
            } catch (Exception e) {
                BotDockMod.LOGGER.warn("[BotDock] Error disabling module {}: {}", module.getId(), e.getMessage());
            }
        });
        modules.clear();
        commands.clear();
        slashCommands.clear();
        jda.shutdown();
        jda = null;
        bridgeChannelId = null;
        commandChannelId = null;
        guildId = null;
        BotDockMod.LOGGER.info("[BotDock] Shut down cleanly.");
    }

    /**
     * Registers a bot module. Modules are feature bundles that hook into bot
     * events and Minecraft events.
     */
    public static void registerModule(IBotModule module) {
        if (!isReady()) {
            BotDockMod.LOGGER.warn("[BotDock] Cannot register module '{}'; bot is not running.", module.getId());
            return;
        }
        modules.add(module);
        module.onEnable(jda, server);
        module.registerCommands(commands);
        module.registerSlashCommands(slashCommands);
        syncSlashCommands();
    }

    /**
     * Returns how many modules are currently registered.
     */
    public static int getModuleCount() {
        return modules.size();
    }

    /**
     * Registers a standalone !command without needing a full module.
     */
    public static void registerCommand(String trigger, IDiscordCommand handler) {
        commands.put(trigger.toLowerCase(), handler);
        BotDockMod.LOGGER.info("[BotDock] Registered command: !{}", trigger);
    }

    /**
     * Registers a standalone Discord slash command without needing a full module.
     */
    public static void registerSlashCommand(CommandData commandData) {
        slashCommands.put(commandData.getName(), commandData);
        BotDockMod.LOGGER.info("[BotDock] Registered slash command: /{}", commandData.getName());
        syncSlashCommands();
    }

    /**
     * Sends a message to the configured bridge channel.
     */
    public static void sendToBridgeChannel(String message) {
        sendToChannel(bridgeChannelId, message);
    }

    /**
     * Sends a message to any Discord channel by ID.
     */
    public static void sendToChannel(@Nullable String channelId, String message) {
        if (!isReady() || channelId == null || channelId.isBlank()) return;

        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            BotDockMod.LOGGER.warn("[BotDock] Channel not found: {}", channelId);
            return;
        }
        channel.sendMessage(message).queue(
                success -> {
                },
                error -> BotDockMod.LOGGER.warn("[BotDock] Failed to send message: {}", error.getMessage())
        );
    }

    /**
     * Returns true if JDA is running and connected.
     */
    public static boolean isReady() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    /**
     * Returns the raw JDA instance. Useful for advanced module use cases.
     */
    @Nullable
    public static JDA getJDA() {
        return jda;
    }

    /**
     * Returns the running MinecraftServer instance.
     */
    @Nullable
    public static MinecraftServer getServer() {
        return server;
    }

    /**
     * Returns the configured bridge channel ID.
     */
    public static String getBridgeChannelId() {
        return bridgeChannelId;
    }

    /**
     * Returns the configured command channel ID, if one is set.
     */
    @Nullable
    public static String getCommandChannelId() {
        return commandChannelId;
    }

    /**
     * Returns the configured guild ID, if one is set.
     */
    @Nullable
    public static String getGuildId() {
        return guildId;
    }

    private static void syncSlashCommands() {
        if (!isReady() || slashCommands.isEmpty()) return;

        List<CommandData> commandData = new ArrayList<>(slashCommands.values());
        if (BotConfig.isConfiguredId(guildId)) {
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                BotDockMod.LOGGER.warn("[BotDock] Cannot register slash commands; guild not found: {}", guildId);
                return;
            }
            guild.updateCommands().addCommands(commandData).queue(
                    commands -> BotDockMod.LOGGER.info("[BotDock] Synced {} guild slash command(s).", commands.size()),
                    error -> BotDockMod.LOGGER.warn("[BotDock] Failed to sync guild slash commands: {}", error.getMessage())
            );
            return;
        }

        jda.updateCommands().addCommands(commandData).queue(
                commands -> BotDockMod.LOGGER.info("[BotDock] Synced {} global slash command(s).", commands.size()),
                error -> BotDockMod.LOGGER.warn("[BotDock] Failed to sync global slash commands: {}", error.getMessage())
        );
    }

    private static boolean shouldHandleLegacyCommand(String channelId) {
        return !BotConfig.isConfiguredId(commandChannelId) || channelId.equals(commandChannelId);
    }

    private static void dispatchToModules(String hookName, Consumer<IBotModule> callback) {
        for (IBotModule module : List.copyOf(modules)) {
            try {
                callback.accept(module);
            } catch (Exception e) {
                BotDockMod.LOGGER.warn("[BotDock] Module {} threw on {}: {}", module.getId(), hookName, e.getMessage());
            }
        }
    }

    private static class InternalDiscordListener extends ListenerAdapter {

        @Override
        public void onMessageReceived(@NotNull MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;

            String content = event.getMessage().getContentDisplay().trim();
            String channelId = event.getChannel().getId();

            if (content.startsWith("!")) {
                if (shouldHandleLegacyCommand(channelId)) {
                    String[] parts = content.substring(1).split("\\s+", 2);
                    String trigger = parts[0].toLowerCase();
                    String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];

                    IDiscordCommand command = commands.get(trigger);
                    if (command != null) {
                        try {
                            command.execute(event, args, server);
                        } catch (Exception e) {
                            BotDockMod.LOGGER.error("[BotDock] Error executing !{}: {}", trigger, e.getMessage());
                            sendToChannel(channelId, "An error occurred running that command.");
                        }
                    }
                    return;
                }
                return;
            }

            dispatchToModules("onDiscordMessage", module -> module.onDiscordMessage(event));
        }

        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
            dispatchToModules("onSlashCommand", module -> module.onSlashCommand(event));
        }

        @Override
        public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
            dispatchToModules("onButtonInteraction", module -> module.onButtonInteraction(event));
        }

        @Override
        public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
            dispatchToModules("onStringSelectInteraction", module -> module.onStringSelectInteraction(event));
        }

        @Override
        public void onModalInteraction(@NotNull ModalInteractionEvent event) {
            dispatchToModules("onModalInteraction", module -> module.onModalInteraction(event));
        }
    }
}
