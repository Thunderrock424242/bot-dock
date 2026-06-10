package com.thunder.botdock.api;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.MinecraftServer;

import java.util.Map;

/**
 * A Bot Dock module is a self-contained feature bundle that plugs into the hosted bot.
 *
 * Implement this interface to create your own module, either in this mod or in
 * another NeoForge mod that lists botdock as a dependency.
 */
public interface IBotModule {

    /**
     * A unique namespaced ID for this module, e.g. "mymod:my_module".
     */
    String getId();

    /**
     * Called when the module is registered and the bot is ready.
     */
    void onEnable(JDA jda, MinecraftServer server);

    /**
     * Called when the server is shutting down.
     */
    void onDisable();

    /**
     * Called when a non-command message is received in the configured bridge channel.
     */
    default void onDiscordMessage(MessageReceivedEvent event) {
    }

    /**
     * Register !commands handled by this module.
     */
    default void registerCommands(Map<String, IDiscordCommand> registry) {
    }
}
