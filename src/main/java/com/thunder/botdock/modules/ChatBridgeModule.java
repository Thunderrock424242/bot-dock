package com.thunder.botdock.modules;

import com.thunder.botdock.DiscordBotEngine;
import com.thunder.botdock.DiscordBotMod;
import com.thunder.botdock.api.IBotModule;
import com.thunder.botdock.api.IDiscordCommand;
import com.thunder.botdock.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.awt.*;
import java.util.Map;

/**
 * Optional module: bridges chat between Minecraft and Discord.
 *
 * When enabled:
 *  - In-game chat messages → Discord bridge channel
 *  - Discord bridge channel messages → all in-game players
 *
 * Enabled via config: modules.enableChatBridge = true
 */
public class ChatBridgeModule implements IBotModule {

    private MinecraftServer server;

    @Override
    public String getId() {
        return "discordbot:chat_bridge";
    }

    @Override
    public void onEnable(JDA jda, MinecraftServer server) {
        this.server = server;
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        NeoForge.EVENT_BUS.unregister(this);
    }

    // ── Minecraft → Discord ──────────────────────────────────────────────────

    @SubscribeEvent
    public void onPlayerChat(ServerChatEvent event) {
        String name = event.getPlayer().getGameProfile().getName();
        String message = event.getRawText();
        String format = BotConfig.DISCORD_CHAT_FORMAT.get()
                .replace("{player}", name)
                .replace("{message}", message);

        DiscordBotEngine.sendToBridgeChannel(format);
    }

    // ── Discord → Minecraft ──────────────────────────────────────────────────

    @Override
    public void onDiscordMessage(MessageReceivedEvent event) {
        if (server == null) return;

        String discordUser = event.getAuthor().getName();
        String content = event.getMessage().getContentDisplay();

        String format = BotConfig.MC_CHAT_FORMAT.get()
                .replace("{user}", discordUser)
                .replace("{message}", content);

        Component component = Component.literal(format);

        // Must execute on the server thread
        server.execute(() -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(component);
            }
            DiscordBotMod.LOGGER.info("[ChatBridge] Discord → MC | {}: {}", discordUser, content);
        });
    }

    @Override
    public void registerCommands(Map<String, IDiscordCommand> registry) {
        // No commands for this module — it's passive relay only
    }
}