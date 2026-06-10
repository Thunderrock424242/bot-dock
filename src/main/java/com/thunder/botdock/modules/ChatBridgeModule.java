package com.thunder.botdock.modules;

import com.thunder.botdock.BotDockEngine;
import com.thunder.botdock.BotDockMod;
import com.thunder.botdock.api.IBotModule;
import com.thunder.botdock.api.IDiscordCommand;
import com.thunder.botdock.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.Map;

/**
 * Optional module that bridges chat between Minecraft and Discord.
 */
public class ChatBridgeModule implements IBotModule {

    private MinecraftServer server;

    @Override
    public String getId() {
        return "botdock:chat_bridge";
    }

    @Override
    public void onEnable(JDA jda, MinecraftServer server) {
        this.server = server;
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        NeoForge.EVENT_BUS.unregister(this);
        this.server = null;
    }

    @SubscribeEvent
    public void onPlayerChat(ServerChatEvent event) {
        String name = event.getPlayer().getGameProfile().getName();
        String message = event.getRawText();
        String format = BotConfig.DISCORD_CHAT_FORMAT.get()
                .replace("{player}", name)
                .replace("{message}", message);

        BotDockEngine.sendToBridgeChannel(format);
    }

    @Override
    public void onDiscordMessage(MessageReceivedEvent event) {
        if (server == null) return;

        String discordUser = event.getAuthor().getName();
        String content = event.getMessage().getContentDisplay();

        String format = BotConfig.MC_CHAT_FORMAT.get()
                .replace("{user}", discordUser)
                .replace("{message}", content);

        Component component = Component.literal(format);

        server.execute(() -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(component);
            }
            BotDockMod.LOGGER.info("[ChatBridge] Discord to Minecraft | {}: {}", discordUser, content);
        });
    }

    @Override
    public void registerCommands(Map<String, IDiscordCommand> registry) {
        // Passive relay only.
    }
}
