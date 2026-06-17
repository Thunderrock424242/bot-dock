package com.thunder.botdock;

import com.mojang.logging.LogUtils;
import com.thunder.botdock.config.BotConfig;
import com.thunder.botdock.modules.ChatBridgeModule;
import com.thunder.botdock.modules.PlayerEventsModule;
import com.thunder.botdock.modules.ServerStatsModule;
import com.thunder.botdock.modules.SupportDeskModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

@Mod(BotDockMod.MOD_ID)
public class BotDockMod {

    public static final String MOD_ID = "botdock";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BotDockMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onDedicatedServerSetup);
        modEventBus.addListener(BotConfig::onLoad);
        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.SERVER, BotConfig.SPEC, "botdock-server.toml");
    }

    private void onDedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
        LOGGER.info("[BotDock] Server setup complete; bot will start after the server finishes loading.");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        String token = BotConfig.BOT_TOKEN.get();

        if (!BotConfig.isTokenConfigured()) {
            LOGGER.error("[BotDock] ====================================================");
            LOGGER.error("[BotDock] No bot token set. Edit config/botdock-server.toml");
            LOGGER.error("[BotDock] ====================================================");
            return;
        }

        boolean started = BotDockEngine.start(event.getServer(), token);
        if (!started) return;

        if (BotConfig.ENABLE_CHAT_BRIDGE.get()) {
            BotDockEngine.registerModule(new ChatBridgeModule());
            LOGGER.info("[BotDock] Module enabled: Chat Bridge");
        }
        if (BotConfig.ENABLE_PLAYER_EVENTS.get()) {
            BotDockEngine.registerModule(new PlayerEventsModule());
            LOGGER.info("[BotDock] Module enabled: Player Events");
        }
        if (BotConfig.ENABLE_SERVER_STATS.get()) {
            BotDockEngine.registerModule(new ServerStatsModule());
            LOGGER.info("[BotDock] Module enabled: Server Stats");
        }
        if (BotConfig.ENABLE_SUPPORT_DESK.get()) {
            BotDockEngine.registerModule(new SupportDeskModule());
            LOGGER.info("[BotDock] Module enabled: Support Desk");
        }
        if (BotConfig.ENABLE_QA_RESPONDER.get()) {
            LOGGER.warn("[BotDock] Q&A Responder is reserved but not implemented yet.");
        }
        if (BotConfig.ENABLE_PLAYTEST_DESK.get()) {
            LOGGER.warn("[BotDock] Playtest Desk is reserved but not implemented yet.");
        }

        LOGGER.info("[BotDock] Bot is running. {} module(s) active.", BotDockEngine.getModuleCount());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        BotDockEngine.shutdown();
    }
}
