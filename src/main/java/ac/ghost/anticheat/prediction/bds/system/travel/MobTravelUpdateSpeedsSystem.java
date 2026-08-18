package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MovementSpeedComponent;


public final class MobTravelUpdateSpeedsSystem {
    private static final float SPRINTING_AIR_MOVE_SPEED =
            Float.intBitsToFloat(0x3CD4FDF3);
    private static final float NORMAL_AIR_MOVE_SPEED =
            Float.intBitsToFloat(0x3CA3D70A);

    private MobTravelUpdateSpeedsSystem() {
    }

    public static void tickAir(final GhostPlayer player,
                               final MovementSpeedComponent movementSpeed) {
        movementSpeed.setValue(player.entityContext.actorDataFlagComponent.has(cn.nukkit.entity.Entity.DATA_FLAG_SPRINTING)
                ? SPRINTING_AIR_MOVE_SPEED
                : NORMAL_AIR_MOVE_SPEED);
    }
}
