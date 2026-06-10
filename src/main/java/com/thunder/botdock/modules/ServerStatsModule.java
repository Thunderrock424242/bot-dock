package com.thunder.botdock.modules;

import com.thunderrock.discordbot.DiscordBotEngine;
import com.thunderrock.discordbot.api.IBotModule;
import com.thunderrock.discordbot.api.IDiscordCommand;
import net.dv8tion.jda.api.JDA;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Optional module: adds Discord !commands for querying server information.
 *
 * Commands added by this module:
 *   !list    — shows online players and count
 *   !tps     — shows current server TPS
 *   !uptime  — shows how long the server has been running
 *   !help    — shows all registered commands
 *
 * Enabled via config: modules.enableServerStats = true
 */
public class ServerStatsModule implements IBotModule {

    private MinecraftServer server;

    @Override
    public String getId() {
        return "discordbot:server_stats";
    }

    @Override
    public void onEnable(JDA jda, MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void onDisable() {
        this.server = null;
    }

    @Override
    public void registerCommands(Map<String, IDiscordCommand> registry) {

        // !list — online player list
        registry.put("list", (event, args, srv) -> {
            if (srv == null) return;
            srv.execute(() -> {
                List<ServerPlayer> players = srv.getPlayerList().getPlayers();
                int max = srv.getPlayerList().getMaxPlayers();
                if (players.isEmpty()) {
                    DiscordBotEngine.sendToChannel(event.getChannel().getId(),
                            "**No players online.** (0/" + max + ")");
                } else {
                    String names = players.stream()
                            .map(p -> p.getGameProfile().getName())
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("None");
                    DiscordBotEngine.sendToChannel(event.getChannel().getId(),
                            String.format("**Players online (%d/%d):** %s", players.size(), max, names));
                }
            });
        });

        // !tps — server ticks per second
        registry.put("tps", (event, args, srv) -> {
            if (srv == null) return;
            srv.execute(() -> {
                // Average over all dimensions
                double avgTickNs = srv.getAverageTickTimeNanos();
                double tps = avgTickNs > 0
                        ? Math.min(20.0, 1_000_000_000.0 / avgTickNs)
                        : 20.0;
                String emoji = tps >= 19.0 ? "🟢" : tps >= 15.0 ? "🟡" : "🔴";
                DiscordBotEngine.sendToChannel(event.getChannel().getId(),
                        String.format("%s **TPS:** %.2f / 20.00", emoji, tps));
            });
        });

        // !uptime — how long the JVM has been running (approx server uptime)
        registry.put("uptime", (event, args, srv) -> {
            long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
            long hours   = TimeUnit.MILLISECONDS.toHours(uptimeMs);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMs) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMs) % 60;
            DiscordBotEngine.sendToChannel(event.getChannel().getId(),
                    String.format("⏱️ **Server uptime:** %dh %dm %ds", hours, minutes, seconds));
        });

        // !help — dynamically built from registered commands
        registry.put("help", (event, args, srv) -> {
            // We'll build the help text from what's currently registered
            // The engine will have the full registry by the time this executes
            DiscordBotEngine.sendToChannel(event.getChannel().getId(),
                    buildHelpText(registry));
        });
    }

    private static String buildHelpText(Map<String, IDiscordCommand> registry) {
        StringBuilder sb = new StringBuilder("**Available Commands:**\n");
        registry.keySet().stream().sorted().forEach(cmd ->
                sb.append("`!").append(cmd).append("`\n")
        );
        return sb.toString().trim();
    }
}