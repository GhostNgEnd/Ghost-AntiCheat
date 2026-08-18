package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MovementSpeedComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerFlyingTravelComponent;
import cn.nukkit.math.BlockVector3;


public final class PlayerFlyingMoveSpeed {
    private static final float NORMAL_MULTIPLIER = Float.intBitsToFloat(0x3F800000);
    private static final float SPRINT_MULTIPLIER = Float.intBitsToFloat(0x40000000);
    private static final float IDLE_INPUT_LIMIT = Float.intBitsToFloat(0x3C23D70A);

    private PlayerFlyingMoveSpeed() {
    }

    public static void tick(final GhostPlayer player,
                            final MovementSpeedComponent movementSpeed,
                            final PlayerFlyingTravelComponent flyingTravel) {
        final boolean noClip = player.entityContext.movementAbilitiesComponent.isNoClip();
        if (!noClip && player.entityContext.onGroundFlagComponent.isPresent()) {
            final BlockVector3 blockPosition =
                    player.entityContext.stateVectorComponent.getPosition().down(0.1F).toBlockVector3();
            flyingTravel.setSurfaceFriction(
                    player.entityContext.localConstBlockSourceFactoryComponent.create().getBlockState(blockPosition, 0)
                            .getFriction());
        } else {
            flyingTravel.setSurfaceFriction(NORMAL_MULTIPLIER);
        }

        final float absoluteX = Math.abs(player.entityContext.mobTravelComponent.getInput().x);
        final float absoluteZ = Math.abs(player.entityContext.mobTravelComponent.getInput().z);
        final float maximumHorizontalInput = Math.max(absoluteX, absoluteZ);
        flyingTravel.setIdleHorizontalInput(
                maximumHorizontalInput < IDLE_INPUT_LIMIT);

        final float sprintMultiplier = player.entityContext.actorDataFlagComponent.has(cn.nukkit.entity.Entity.DATA_FLAG_SPRINTING)
                ? SPRINT_MULTIPLIER
                : NORMAL_MULTIPLIER;
        final float resolvedMoveSpeed =
                player.entityContext.movementAbilitiesComponent.getFlySpeed()
                        * sprintMultiplier;
        movementSpeed.setValue(resolvedMoveSpeed);
    }
}
