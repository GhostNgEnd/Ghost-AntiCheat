package ac.ghost.anticheat.player.manager;

import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.Player;

import java.util.HashMap;

public class GhostPlayerManager extends HashMap<Player, GhostPlayer> {
    public GhostPlayer add(Player player) {
        final GhostPlayer bp = new GhostPlayer(player);
        
        this.put(player, bp);
        return bp;
    }
}
