package com.thunder.botdock.modules;

import com.thunder.botdock.BotDockEngine;
import com.thunder.botdock.api.IBotModule;
import com.thunder.botdock.api.IDiscordCommand;
import com.thunder.botdock.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic configurable support/report panel.
 */
public class SupportDeskModule implements IBotModule {

    private static final String SLASH_PANEL = "supportdesk";
    private static final String TEXT_PANEL = "supportpanel";
    private static final String BUTTON_PREFIX = "botdock:support:";
    private static final String MODAL_PREFIX = "botdock:support_modal:";
    private static final String SUMMARY_INPUT = "summary";
    private static final String DETAILS_INPUT = "details";

    @Override
    public String getId() {
        return "botdock:support_desk";
    }

    @Override
    public void onEnable(JDA jda, MinecraftServer server) {
        // Interaction events are routed by BotDockEngine.
    }

    @Override
    public void onDisable() {
        // No persistent state yet.
    }

    @Override
    public void registerCommands(Map<String, IDiscordCommand> registry) {
        registry.put(TEXT_PANEL, (event, args, server) -> {
            if (!canManagePanel(event.getMember())) {
                event.getChannel().sendMessage("You do not have permission to post the support desk panel.").queue();
                return;
            }
            postPanel(event.getChannel());
        });
    }

    @Override
    public void registerSlashCommands(Map<String, CommandData> registry) {
        registry.put(SLASH_PANEL, Commands.slash(SLASH_PANEL, "Post the configured support desk panel.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)));
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(SLASH_PANEL)) return;

        if (!canManagePanel(event.getMember())) {
            event.reply("You do not have permission to post the support desk panel.").setEphemeral(true).queue();
            return;
        }

        postPanel(event.getChannel());
        event.reply("Support desk panel posted.").setEphemeral(true).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (!componentId.startsWith(BUTTON_PREFIX)) return;

        ReportType type = ReportType.fromKey(componentId.substring(BUTTON_PREFIX.length()));
        if (type == null || !type.isEnabled()) {
            event.reply("That report type is not enabled.").setEphemeral(true).queue();
            return;
        }

        event.replyModal(buildModal(type)).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (!modalId.startsWith(MODAL_PREFIX)) return;

        ReportType type = ReportType.fromKey(modalId.substring(MODAL_PREFIX.length()));
        if (type == null || !type.isEnabled()) {
            event.reply("That report type is not enabled.").setEphemeral(true).queue();
            return;
        }

        String destinationId = destinationFor(type);
        if (!BotConfig.isConfiguredId(destinationId)) {
            event.reply("No report channel is configured for that report type.").setEphemeral(true).queue();
            return;
        }

        String summary = getModalValue(event, SUMMARY_INPUT);
        String details = getModalValue(event, DETAILS_INPUT);
        BotDockEngine.sendToChannel(destinationId, buildReportMessage(event, type, summary, details));
        event.reply("Thanks, your " + type.displayName().toLowerCase() + " was sent.").setEphemeral(true).queue();
    }

    private static void postPanel(MessageChannel channel) {
        List<Button> buttons = new ArrayList<>();
        addButton(buttons, ReportType.BUG, Button.danger(BUTTON_PREFIX + ReportType.BUG.key(), ReportType.BUG.label()));
        addButton(buttons, ReportType.FEEDBACK, Button.primary(BUTTON_PREFIX + ReportType.FEEDBACK.key(), ReportType.FEEDBACK.label()));
        addButton(buttons, ReportType.SUGGESTION, Button.success(BUTTON_PREFIX + ReportType.SUGGESTION.key(), ReportType.SUGGESTION.label()));
        addButton(buttons, ReportType.PERFORMANCE, Button.secondary(BUTTON_PREFIX + ReportType.PERFORMANCE.key(), ReportType.PERFORMANCE.label()));

        if (buttons.isEmpty()) {
            channel.sendMessage("The support desk has no enabled report types.").queue();
            return;
        }

        channel.sendMessage(buildPanelMessage())
                .addComponents(ActionRow.partitionOf(buttons))
                .queue();
    }

    private static void addButton(List<Button> buttons, ReportType type, Button button) {
        if (type.isEnabled()) {
            buttons.add(button);
        }
    }

    private static String buildPanelMessage() {
        return "**" + BotConfig.SUPPORT_PANEL_TITLE.get() + "**\n" + BotConfig.SUPPORT_PANEL_DESCRIPTION.get();
    }

    private static Modal buildModal(ReportType type) {
        TextInput summary = TextInput.create(SUMMARY_INPUT, TextInputStyle.SHORT)
                .setRequired(true)
                .setMinLength(5)
                .setMaxLength(100)
                .setPlaceholder("Short summary")
                .build();

        TextInput details = TextInput.create(DETAILS_INPUT, TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setMinLength(10)
                .setMaxLength(1000)
                .setPlaceholder("What happened? Include steps, context, or expected behavior.")
                .build();

        return Modal.create(MODAL_PREFIX + type.key(), type.displayName())
                .addComponents(Label.of("Summary", summary), Label.of("Details", details))
                .build();
    }

    private static String buildReportMessage(ModalInteractionEvent event, ReportType type, String summary, String details) {
        String reporter = event.getUser().getAsTag() + " (" + event.getUser().getId() + ")";
        String sourceChannel = event.getChannel() == null ? "unknown" : event.getChannel().getAsMention();

        return "**" + type.displayName() + "**\n"
                + "**Reporter:** " + reporter + "\n"
                + "**Source:** " + sourceChannel + "\n"
                + "**Summary:** " + trimForDiscord(summary, 150) + "\n\n"
                + trimForDiscord(details, 1500);
    }

    private static String getModalValue(ModalInteractionEvent event, String inputId) {
        ModalMapping mapping = event.getValue(inputId);
        return mapping == null ? "" : mapping.getAsString();
    }

    private static String destinationFor(ReportType type) {
        return switch (type) {
            case BUG -> firstConfigured(BotConfig.CHANNEL_BUG_REPORTS.get(), BotConfig.CHANNEL_SUPPORT.get());
            case FEEDBACK -> firstConfigured(BotConfig.CHANNEL_FEEDBACK.get(), BotConfig.CHANNEL_SUPPORT.get());
            case SUGGESTION -> firstConfigured(
                    BotConfig.CHANNEL_SUGGESTIONS.get(),
                    BotConfig.CHANNEL_FEEDBACK.get(),
                    BotConfig.CHANNEL_SUPPORT.get()
            );
            case PERFORMANCE -> firstConfigured(BotConfig.CHANNEL_BUG_REPORTS.get(), BotConfig.CHANNEL_SUPPORT.get());
        };
    }

    private static String firstConfigured(String... ids) {
        for (String id : ids) {
            if (BotConfig.isConfiguredId(id)) {
                return id;
            }
        }
        return "";
    }

    private static boolean canManagePanel(Member member) {
        if (member == null) return false;

        boolean hasConfiguredRoles = hasConfiguredRoleIds(BotConfig.ADMIN_ROLE_IDS.get())
                || hasConfiguredRoleIds(BotConfig.STAFF_ROLE_IDS.get());
        if (!hasConfiguredRoles) {
            return member.hasPermission(Permission.MANAGE_SERVER);
        }

        return hasAnyRole(member, BotConfig.ADMIN_ROLE_IDS.get()) || hasAnyRole(member, BotConfig.STAFF_ROLE_IDS.get());
    }

    private static boolean hasAnyRole(Member member, List<? extends String> configuredRoleIds) {
        if (!hasConfiguredRoleIds(configuredRoleIds)) return false;

        for (Role role : member.getRoles()) {
            for (String configuredRoleId : configuredRoleIds) {
                if (role.getId().equals(configuredRoleId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasConfiguredRoleIds(List<? extends String> roleIds) {
        for (String roleId : roleIds) {
            if (BotConfig.isConfiguredId(roleId)) {
                return true;
            }
        }
        return false;
    }

    private static String trimForDiscord(String value, int maxLength) {
        if (value == null) return "";
        if (value.length() <= maxLength) return value;
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private enum ReportType {
        BUG("bug"),
        FEEDBACK("feedback"),
        SUGGESTION("suggestion"),
        PERFORMANCE("performance");

        private final String key;

        ReportType(String key) {
            this.key = key;
        }

        private String key() {
            return key;
        }

        private String displayName() {
            return switch (this) {
                case BUG -> "Bug Report";
                case FEEDBACK -> "Feedback";
                case SUGGESTION -> "Suggestion";
                case PERFORMANCE -> "Performance Report";
            };
        }

        private String label() {
            return switch (this) {
                case BUG -> BotConfig.SUPPORT_BUG_LABEL.get();
                case FEEDBACK -> BotConfig.SUPPORT_FEEDBACK_LABEL.get();
                case SUGGESTION -> BotConfig.SUPPORT_SUGGESTION_LABEL.get();
                case PERFORMANCE -> BotConfig.SUPPORT_PERFORMANCE_LABEL.get();
            };
        }

        private boolean isEnabled() {
            return switch (this) {
                case BUG -> BotConfig.SUPPORT_ENABLE_BUG_REPORTS.get();
                case FEEDBACK -> BotConfig.SUPPORT_ENABLE_FEEDBACK.get();
                case SUGGESTION -> BotConfig.SUPPORT_ENABLE_SUGGESTIONS.get();
                case PERFORMANCE -> BotConfig.SUPPORT_ENABLE_PERFORMANCE_REPORTS.get();
            };
        }

        private static ReportType fromKey(String key) {
            for (ReportType type : values()) {
                if (type.key.equals(key)) {
                    return type;
                }
            }
            return null;
        }
    }
}
