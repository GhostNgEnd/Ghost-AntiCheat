package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;


public final class InsidePowderSnowBlockSystem {
    
    
    private static final float HORIZONTAL_MOVEMENT_MULTIPLIER =
            Float.intBitsToFloat(0x3F666666);
    private static final float VERTICAL_MOVEMENT_MULTIPLIER =
            Float.intBitsToFloat(0x3FC00000);
    private static final Vec3 MOVEMENT_MULTIPLIER = new Vec3(
            HORIZONTAL_MOVEMENT_MULTIPLIER,
            VERTICAL_MOVEMENT_MULTIPLIER,
            HORIZONTAL_MOVEMENT_MULTIPLIER);

    private InsidePowderSnowBlockSystem() {
    }

    public static void updateCanStandOnSnowFlag(final GhostPlayer player) {
        final Object item = player.compensatedInventory.armorContainer.get(3).getData();
        player.entityContext.canStandOnSnowFlagComponent.setPresent(
                item instanceof Item boots && boots.getId() == ItemID.LEATHER_BOOTS);
    }

    public static void tickEntityInside(final GhostPlayer player,
                                        final BlockLegacy blockState,
                                        final BlockVector3 position) {
        if (!blockState.isPowderSnow()) {
            return;
        }
        player.entityContext.insidePowderSnowBlockComponent.add(position, blockState);
    }

    public static void applyMovementSlowdown(final GhostPlayer player) {
        if (player.entityContext.insidePowderSnowBlockComponent.isEmpty()) {
            return;
        }
        player.entityContext.blockMovementSlowdownMultiplierComponent.add(MOVEMENT_MULTIPLIER);
        player.entityContext.blockMovementSlowdownAppliedComponent.markApplied();
    }

}
