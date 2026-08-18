package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MovementSpeedComponent;
import ac.ghost.anticheat.prediction.bds.system.attribute.SoulSpeedAttributeSystem;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.block.BlockID;
import cn.nukkit.math.BlockVector3;


public final class GroundTravelTypeSystem {
    private static final float SPECIAL_SLOW_BLOCK_FRICTION_MULTIPLIER =
            Float.intBitsToFloat(0x3F9CCCCD);
    private static final float FRICTION_SCALE =
            Float.intBitsToFloat(0x3F68F5C3);
    private static final float DEFAULT_GROUND_DAMPING =
            Float.intBitsToFloat(0x3F0BC6A9);

    private GroundTravelTypeSystem() {
    }

    public static void tick(final GhostPlayer player,
                            final MovementSpeedComponent movementSpeed) {
        final BlockVector3 blockPosition = accelerationFrictionPosition(player.entityContext.stateVectorComponent.getPosition());
        final var blockState = player.entityContext.localConstBlockSourceFactoryComponent.create().getBlockState(blockPosition, 0);

        final float sampledFriction = blockState.getFriction();
        float adjustedFriction = sampledFriction;
        if (blockState.getBlock().getId() == BlockID.SOUL_SAND
                && !SoulSpeedAttributeSystem.hasSoulSpeed(player.entityContext)) {
            adjustedFriction = adjustedFriction
                    * SPECIAL_SLOW_BLOCK_FRICTION_MULTIPLIER;
        }

        float damping = adjustedFriction * FRICTION_SCALE;
        if (damping == 0.0F) {
            damping = DEFAULT_GROUND_DAMPING;
        }

        final float ratio = DEFAULT_GROUND_DAMPING / damping;
        final float ratioSquared = ratio * ratio;
        final float ratioCubed = ratioSquared * ratio;
        final float baseMovementSpeed = movementSpeed.getValue();
        final float resolvedMovementSpeed = baseMovementSpeed * ratioCubed;
        movementSpeed.setValue(resolvedMovementSpeed);
    }

    private static BlockVector3 accelerationFrictionPosition(final Vec3 position) {
        final float x = position.x;
        final float y = position.y - 0.1F;
        final float z = position.z;
        return new BlockVector3(floorFloat(x), floorFloat(y), floorFloat(z));
    }

    private static int floorFloat(final float value) {
        final int truncated = (int) value;
        return value < truncated ? truncated - 1 : truncated;
    }
}
