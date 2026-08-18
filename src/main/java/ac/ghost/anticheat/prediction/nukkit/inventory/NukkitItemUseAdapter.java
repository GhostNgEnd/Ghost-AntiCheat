package ac.ghost.anticheat.prediction.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.item.Item;


public final class NukkitItemUseAdapter {
    private NukkitItemUseAdapter() {
    }

    public static void releaseItem(final Player player) {
        if (player == null || !player.isUsingItem()) {
            return;
        }
        final Item item = player.getInventory().getItemInHand();
        final int useTicks = player.getServer().getTick() - player.getStartActionTick();
        try {
            if (!item.onRelease(player, useTicks)) {
                player.getInventory().sendContents(player);
            }
        } finally {
            player.setUsingItem(false);
        }
    }
}
