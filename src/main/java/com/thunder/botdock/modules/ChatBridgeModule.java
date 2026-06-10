package com.thunder.botdock.modules;

import com.thunderrock.discordbot.DiscordBotEngine;
import com.thunderrock.discordbot.DiscordBotMod;
import com.thunderrock.discordbot.api.IBotModule;
import com.thunderrock.discordbot.api.IDiscordCommand;
import com.thunderrock.discordbot.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;

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