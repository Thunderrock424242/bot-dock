package com.thunder.botdock.api;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.MinecraftServer;

import java.util.Map;

/**
 * A Bot Module is a self-contained feature bundle that plugs into the Discord Bot Host engine.
 *
 * Implement this interface to create your own module — either as part of this mod
 * or from a completely separate NeoForge mod that lists discordbot as a dependency.
 *
 * ── Minimal example ──────────────────────────────────────────────────────────
 *
 *   public class MyModule implements IBotModule {
 *
 *       {@literal @}Override public String getId() { return "mymod:my_module"; }
 *
 *       {@literal @}Override
 *       public void onEnable(JDA jda, MinecraftServer server) {
 *           // Called once when the module is registered and the bot is ready.
 *           // Register NeoForge event listeners here if needed.
 *           NeoForge.EVENT_BUS.register(this);
 *       }
 *
 *       {@literal @}Override
 *       public void onDisable() {
 *           // Called on server shutdown. Clean up resources here.
 *       }
 *
 *       {@literal @}Override
 *       public void registerCommands(Map<String, IDiscordCommand> registry) {
 *           registry.put("ping", (event, args, srv) ->
 *               DiscordBotEngine.sendToChannel(event.getChannel().getId(), "Pong!"));
 *       }
 *   }
 *
 * ── Registering your module ───────────────────────────────────────────────────
 *
 *   Register it inside a ServerStartedEvent handler, AFTER checking the engine is ready:
 *
 *   {@literal @}SubscribeEvent
 *   public void onServerStarted(ServerStartedEvent event) {
 *       if (DiscordBotEngine.isReady()) {
 *           DiscordBotEngine.registerModule(new MyModule());
 *       }
 *   }
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface IBotModule {

    /**
     * A unique namespaced ID for this module, e.g. "mymod:my_module".
     * Used for logging and debugging.
     */
    String getId();

    /**
     * Called when the module is registered and the bot is confirmed ready.
     * Use this to store references to JDA/server and register NeoForge event listeners.
     *
     * @param jda    The live JDA instance
     * @param server The running MinecraftServer
     */
    void onEnable(JDA jda, MinecraftServer server);

    /**
     * Called when the server is shutting down.
     * Unregister event listeners and release any resources here.
     */
    void onDisable();

    /**
     * Called when a non-command message is received in the configured bridge channel.
     * Override this to react to Discord messages (e.g. relay them to Minecraft chat).
     *
     * Default: no-op.
     *
     * @param event The full JDA MessageReceivedEvent
     */
    default void onDiscordMessage(MessageReceivedEvent event) {}

    /**
     * Register !commands that this module handles.
     * Add entries to the provided registry map: trigger (without !) → handler.
     *
     * Example:
     *   registry.put("mycommand", (event, args, server) -> { ... });
     *
     * Default: no commands registered.
     *
     * @param registry The shared command registry. Keys must be lowercase, no leading '!'.
     */
    default void registerCommands(Map<String, IDiscordCommand> registry) {}
}