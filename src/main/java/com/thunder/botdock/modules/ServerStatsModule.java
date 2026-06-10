package com.thunder.botdock.modules;

import com.thunder.botdock.BotDockEngine;
import com.thunder.botdock.api.IBotModule;
import com.thunder.botdock.api.IDiscordCommand;
import net.dv8tion.jda.api.JDA;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Optional module that adds Discord !commands for querying server information.
 */
public class ServerStatsModule implements IBotModule {

    @Override
    public String getId() {
        return "botdock:server_stats";
    }

    @Override
    public void onEnable(JDA jda, MinecraftServer server) {
        // Commands receive the active server instance when executed.
    }

    @Override
    public void onDisable() {
        // No state to clean up.
    }

    @Override
    public void registerCommands(Map<String, IDiscordCommand> registry) {
        registry.put("list", (event, args, srv) -> {
            if (srv == null) return;
            srv.execute(() -> {
                List<ServerPlayer> players = srv.getPlayerList().getPlayers();
                int max = srv.getPlayerList().getMaxPlayers();
                if (players.isEmpty()) {
                    BotDockEngine.sendToChannel(event.getChannel().getId(), "**No players online.** (0/" + max + ")");
                } else {
                    String names = players.stream()
                            .map(player -> player.getGameProfile().getName())
                            .reduce((left, right) -> left + ", " + right)
                            .orElse("None");
                    BotDockEngine.sendToChannel(
                            event.getChannel().getId(),
                            String.format("**Players online (%d/%d):** %s", players.size(), max, names)
                    );
                }
            });
        });

        registry.put("tps", (event, args, srv) -> {
            if (srv == null) return;
            srv.execute(() -> {
                double averageTickNanos = srv.getAverageTickTimeNanos();
                double tps = averageTickNanos > 0
                        ? Math.min(20.0, 1_000_000_000.0 / averageTickNanos)
                        : 20.0;
                String status = tps >= 19.0 ? "healthy" : tps >= 15.0 ? "busy" : "slow";
                BotDockEngine.sendToChannel(
                        event.getChannel().getId(),
                        String.format("**TPS:** %.2f / 20.00 (%s)", tps, status)
                );
            });
        });

        registry.put("uptime", (event, args, srv) -> {
            long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
            long hours = TimeUnit.MILLISECONDS.toHours(uptimeMs);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMs) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMs) % 60;
            BotDockEngine.sendToChannel(
                    event.getChannel().getId(),
                    String.format("**Server uptime:** %dh %dm %ds", hours, minutes, seconds)
            );
        });

        registry.put("help", (event, args, srv) ->
                BotDockEngine.sendToChannel(event.getChannel().getId(), buildHelpText(registry)));
    }

    private static String buildHelpText(Map<String, IDiscordCommand> registry) {
        StringBuilder builder = new StringBuilder("**Available Commands:**\n");
        registry.keySet().stream().sorted().forEach(command ->
                builder.append("`!").append(command).append("`\n")
        );
        return builder.toString().trim();
    }
}
