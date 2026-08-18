package ac.ghost.anticheat.prediction.bds.system.item;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;
import ac.ghost.anticheat.util.ItemUtil;


public final class ItemUseSlowdownSystem {
    private static final float FLOAT_EPSILON = 1.1920929E-7F;

    private ItemUseSlowdownSystem() {
    }

    public static void tick(final GhostPlayer player) {
        if (!NukkitItemUseStateSystem.isPredictionUsingItem(player)
                || !player.entityContext.itemInUseComponent.isPresent()) {
            return;
        }

        final float modifier = ItemUtil.itemUseSlowdownModifier(
                player, player.entityContext.itemInUseComponent.getItem());
        if (Float.isFinite(modifier)
                && Math.abs(modifier - 1.0F) > FLOAT_EPSILON) {
            player.entityContext.itemUseSlowdownModifierComponent.set(modifier);
        }
    }
}
