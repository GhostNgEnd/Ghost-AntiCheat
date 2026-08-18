package ac.ghost.anticheat.prediction;

import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.entity.Entity;
import cn.nukkit.network.protocol.types.AuthInputAction;

import java.util.Set;





public final class NukkitAdapter {
    private NukkitAdapter() {}

    public static cn.nukkit.Player getPlayer(GhostPlayer player) {
        return player.getSession();
    }

    public static boolean hasFlag(GhostPlayer player, int flag) {
        return getPlayer(player).getDataFlag(Entity.DATA_FLAGS, flag);
    }

    public static void setFlag(GhostPlayer player, int flag, boolean value) {
        getPlayer(player).setDataFlag(Entity.DATA_FLAGS, flag, value);
    }

    public static boolean hasInput(GhostPlayer player, AuthInputAction action) {
        return player.entityContext.playerActionComponent.actions().contains(action);
    }

    public static Set<AuthInputAction> getInputData(GhostPlayer player) {
        return player.entityContext.playerActionComponent.actions();
    }
}
