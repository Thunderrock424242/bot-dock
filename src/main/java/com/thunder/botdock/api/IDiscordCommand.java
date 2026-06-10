package com.thunder.botdock.api;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.MinecraftServer;

/**
 * Represents a single !command handler in the Discord bot.
 *
 * Registered either through IBotModule.registerCommands() or directly via
 * DiscordBotEngine.registerCommand(trigger, handler).
 *
 * ── Example ──────────────────────────────────────────────────────────────────
 *
 *   DiscordBotEngine.registerCommand("seed", (event, args, server) -> {
 *       long seed = server.overworld().getSeed();
 *       DiscordBotEngine.sendToChannel(
 *           event.getChannel().getId(),
 *           "🌍 World seed: `" + seed + "`"
 *       );
 *   });
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
@FunctionalInterface
public interface IDiscordCommand {

    /**
     * Execute this command.
     *
     * @param event  The full JDA MessageReceivedEvent (includes channel, author, guild, etc.)
     * @param args   Any words after the command trigger, split by spaces. May be empty.
     * @param server The running MinecraftServer. May be null if the server is stopping.
     */
    void execute(MessageReceivedEvent event, String[] args, MinecraftServer server);
}