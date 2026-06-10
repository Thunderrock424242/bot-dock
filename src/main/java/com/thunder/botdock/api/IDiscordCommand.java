package com.thunder.botdock.api;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.MinecraftServer;

/**
 * Represents a single !command handler in Discord.
 */
@FunctionalInterface
public interface IDiscordCommand {

    /**
     * Executes this command.
     *
     * @param event  The JDA message event.
     * @param args   Words after the command trigger, split by spaces.
     * @param server The running MinecraftServer. May be null while stopping.
     */
    void execute(MessageReceivedEvent event, String[] args, MinecraftServer server);
}
