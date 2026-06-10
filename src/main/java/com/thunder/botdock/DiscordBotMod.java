package com.thunder.botdock;

import com.mojang.logging.LogUtils;
import com.thunder.botdock.config.BotConfig;
import com.thunder.botdock.modules.ChatBridgeModule;
import com.thunder.botdock.modules.PlayerEventsModule;
import com.thunder.botdock.modules.ServerStatsModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

@Mod(DiscordBotMod.MOD_ID)
public class DiscordBotMod {

    public static final String MOD_ID = "discordbot";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DiscordBotMod(IEventBus modEventBus) {
        modEventBus.addListener(this::onDedicatedServerSetup);
        NeoForge.EVENT_BUS.register(this);

        // Register server-side config
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, BotConfig.SPEC, "discordbot-server.toml");
    }

    private void onDedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
        LOGGER.info("[DiscordBot] Server setup — bot will start after the server finishes loading.");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        String token = BotConfig.BOT_TOKEN.get();

        if (token.isBlank() || token.equals("YOUR_BOT_TOKEN_HERE")) {
            LOGGER.error("[DiscordBot] ====================================================");
            LOGGER.error("[DiscordBot] No bot token set! Edit config/discordbot-server.toml");
            LOGGER.error("[DiscordBot] ====================================================");
            return;
        }

        // Start the core bot engine
        boolean started = DiscordBotEngine.start(event.getServer(), token);
        if (!started) return;

        // Register optional built-in modules based on config
        if (BotConfig.ENABLE_CHAT_BRIDGE.get()) {
            DiscordBotEngine.registerModule(new ChatBridgeModule());
            LOGGER.info("[DiscordBot] Module enabled: Chat Bridge");
        }
        if (BotConfig.ENABLE_PLAYER_EVENTS.get()) {
            DiscordBotEngine.registerModule(new PlayerEventsModule());
            LOGGER.info("[DiscordBot] Module enabled: Player Events (join/leave/death)");
        }
        if (BotConfig.ENABLE_SERVER_STATS.get()) {
            DiscordBotEngine.registerModule(new ServerStatsModule());
            LOGGER.info("[DiscordBot] Module enabled: Server Stats (!list, !tps, !uptime)");
        }

        LOGGER.info("[DiscordBot] Bot is running. {} module(s) active.", DiscordBotEngine.getModuleCount());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        DiscordBotEngine.shutdown();
    }
}