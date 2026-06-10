package com.thunder.botdock.modules;

import com.thunder.botdock.BotDockEngine;
import com.thunder.botdock.api.IBotModule;
import com.thunder.botdock.api.IDiscordCommand;
import com.thunder.botdock.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;

/**
 * Optional module that sends player join, leave, and death notifications to Discord.
 */
public class PlayerEventsModule implements IBotModule {

    @Override
    public String getId() {
        return "botdock:player_events";
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
        String message = BotConfig.JOIN_FORMAT.get().replace("{player}", name);
        BotDockEngine.sendToBridgeChannel(message);
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!BotConfig.NOTIFY_LEAVE.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String name = player.getGameProfile().getName();
        String message = BotConfig.LEAVE_FORMAT.get().replace("{player}", name);
        BotDockEngine.sendToBridgeChannel(message);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!BotConfig.NOTIFY_DEATH.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String deathMessage = player.getCombatTracker().getDeathMessage().getString();
        String message = BotConfig.DEATH_FORMAT.get().replace("{message}", deathMessage);
        BotDockEngine.sendToBridgeChannel(message);
    }

    @Override
    public void registerCommands(Map<String, IDiscordCommand> registry) {
        // No commands for this module.
    }
}
