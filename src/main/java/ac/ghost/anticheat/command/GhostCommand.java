package ac.ghost.anticheat.command;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.alert.AlertManager;
import ac.ghost.anticheat.config.ConfigLoader;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

import java.util.UUID;

public class GhostCommand extends Command {
    public GhostCommand() {
        super("ghost", "Ghost anticheat management",
                "/ghost <alert|reload|debug> [player]", new String[]{});
        this.setPermission("ghost.admin");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        if ("alert".equalsIgnoreCase(args[0])) {
            final var manager = Ghost.getInstance().getAlertManager();
            final String prefix = manager.getPrefix();
            if (manager.hasAlert(sender)) {
                manager.removeAlert(sender);
                sender.sendMessage(prefix + "Alerts §cdisabled");
            } else {
                manager.addAlert(sender);
                sender.sendMessage(prefix + "Alerts §aenabled");
            }
            return true;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            Ghost.setConfig(ConfigLoader.load(Ghost.getPluginInstance()));
            for (final GhostPlayer player : Ghost.getInstance().getPlayerManager().values()) {
                player.getCheckHolder().reload();
            }
            sender.sendMessage(Ghost.getInstance().getAlertManager().getPrefix()
                    + "§fReloaded config! New config: " + Ghost.getConfig());
            return true;
        }

        if ("debug".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                return true;
            }

            final GhostPlayer target = Ghost.getInstance().getPlayerManager().entrySet().stream()
                    .filter(entry -> entry.getKey().getName().equalsIgnoreCase(args[1]))
                    .map(java.util.Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                sender.sendMessage(Ghost.getInstance().getAlertManager().getPrefix()
                        + "§cFailed to find player session.");
                return true;
            }

            final UUID uuid = sender.isPlayer()
                    ? sender.asPlayer().getUniqueId()
                    : AlertManager.CONSOLE_UUID;
            if (target.getTrackedDebugPlayers().containsKey(uuid)) {
                target.getTrackedDebugPlayers().remove(uuid);
            } else {
                target.getTrackedDebugPlayers().put(uuid, sender);
            }
            return true;
        }

        sender.sendMessage(Ghost.getInstance().getAlertManager().getPrefix() + "Unknown subcommand.");
        sendUsage(sender);
        return true;
    }

    private static void sendUsage(final CommandSender sender) {
        final String prefix = Ghost.getInstance().getAlertManager().getPrefix();
        sender.sendMessage(prefix + "Usage: /ghost alert");
        sender.sendMessage(prefix + "Usage: /ghost reload");
        sender.sendMessage(prefix + "Usage: /ghost debug <player>");
    }
}
