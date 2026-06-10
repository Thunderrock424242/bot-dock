package com.thunder.botdock.modules;

import com.thunderrock.discordbot.DiscordBotEngine;
import com.thunderrock.discordbot.api.IBotModule;
import com.thunderrock.discordbot.api.IDiscordCommand;
import com.thunderrock.discordbot.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;

/**
 * Optional module: sends player join, leave, and death notifications to Discord.
 *
 * Each notification type is individually togglable in config:
 *   modules.notifyJoin    = true/false
 *   modules.notifyLeave   = true/false
 *   modules.notifyDeath   = true/false
 *
 * Enabled via config: modules.enablePlayerEvents = true
 */
public class PlayerEventsModule implements IBotModule {

    @Override
    public String getId() {
        return "discordbot:player_events";
    }

    @Override
    public void onEnable(JDA jda, MinecraftServer server) {
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        NeoForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!BotConfig.NOTIFY_JOIN.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String name = player.getGameProfile().getName();
        String msg = BotConfig.JOIN_FORMAT.get().replace("{player}", name);
        DiscordBotEngine.sendToBridgeChannel(msg);
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!BotConfig.NOTIFY_LEAVE.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String name = player.getGameProfile().getName();
        String msg = BotConfig.LEAVE_FORMAT.get().replace("{player}", name);
        DiscordBotEngine.sendToBridgeChannel(msg);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!BotConfig.NOTIFY_DEATH.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String deathMessage = player.getCombatTracker().getDeathMessage().getString();
        String msg = BotConfig.DEATH_FORMAT.get().replace("{message}", deathMessage);
        DiscordBotEngine.sendToBridgeChannel(msg);
    }

    @Override
    public void registerCommands(Map<String, IDiscordCommand> registry) {
        // No commands for this module
    }
}