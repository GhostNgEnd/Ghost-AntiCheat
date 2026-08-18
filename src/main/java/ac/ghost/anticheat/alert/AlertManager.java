package ac.ghost.anticheat.alert;

import ac.ghost.anticheat.Ghost;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AlertManager {
    public final static UUID CONSOLE_UUID = new UUID(0, 0);


    private final Map<UUID, CommandSender> sources = new ConcurrentHashMap<>();

    public void alert(String verbose) {
        sources.values().forEach(source -> source.sendMessage(getPrefix() + verbose));
    }

    public void alertToPlayers(final List<CommandSender> sources, String verbose) {
        sources.forEach(source -> source.sendMessage(getPrefix() + verbose));
    }

    public String getPrefix() {
        return Ghost.getConfig().formattedPrefix();
    }

    


    public String getPrefix(CommandSender source) {
        return getPrefix();
    }

    public boolean hasAlert(CommandSender source) {
        return this.sources.containsKey(source.isPlayer() ? asPlayer(source).getUniqueId() : CONSOLE_UUID);
    }

    public void addAlert(CommandSender source) {
        this.sources.put(source.isPlayer() ? asPlayer(source).getUniqueId() : CONSOLE_UUID, source);
    }

    public void removeAlert(CommandSender source) {
        this.sources.remove(source.isPlayer() ? asPlayer(source).getUniqueId() : CONSOLE_UUID);
    }

    private Player asPlayer(CommandSender source) {
        return source.asPlayer();
    }
}
