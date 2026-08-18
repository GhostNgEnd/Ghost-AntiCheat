package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.prediction.bds.world.LocalConstBlockSource;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.entity.Entity;








public final class AutoClimbSystem {
    private static final float AUTO_CLIMB_VELOCITY =
            Float.intBitsToFloat(0x3E4CCCCD);

    private AutoClimbSystem() {
    }

    public static void tick(final GhostPlayer player,
                            final StateVectorComponent stateVector) {
        if (!player.entityContext.horizontalCollisionFlagComponent.isPresent()
                || !isClimbing(player, stateVector)) {
            return;
        }

        final Vec3 velocity = stateVector.getDelta();
        velocity.y = AUTO_CLIMB_VELOCITY;
        player.entityContext.autoClimbTravelFlagComponent.setPresent(true);
    }

    




    public static boolean isClimbing(final GhostPlayer player,
                                     final StateVectorComponent stateVector) {
        if (player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_WALLCLIMBING)) {
            return true;
        }
        if (!player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_CAN_CLIMB)) {
            return false;
        }
        return isClimbableAtFeet(player, stateVector);
    }

    public static boolean isClimbing(final GhostPlayer player) {
                return isClimbing(player, player.entityContext.stateVectorComponent);
    }

    private static boolean isClimbableAtFeet(
            final GhostPlayer player,
            final StateVectorComponent stateVector) {
        final BlockLegacy block = blockAtFeet(player, stateVector);
        if (block == null) {
            return false;
        }
        return block.hasClimbableProperty();
    }

    
    public static boolean hasClimbHaltPropertyAtFeet(
            final GhostPlayer player,
            final StateVectorComponent stateVector) {
        final BlockLegacy block = blockAtFeet(player, stateVector);
        return block != null && block.hasClimbHaltProperty();
    }

    public static boolean hasClimbHaltPropertyAtFeet(final GhostPlayer player) {
                return hasClimbHaltPropertyAtFeet(player, player.entityContext.stateVectorComponent);
    }

    private static BlockLegacy blockAtFeet(
            final GhostPlayer player,
            final StateVectorComponent stateVector) {
        final Vec3 position = stateVector.getPosition();
        final int x = floor(position.x);
        final int y = floor(player.entityContext.aabbShapeComponent.getAABB().minY);
        final int z = floor(position.z);
        final LocalConstBlockSource source =
                player.entityContext.localConstBlockSourceFactoryComponent.create();
        if (!source.isChunkLoaded(x, z)) {
            return null;
        }
        return source.getBlockState(x, y, z, 0);
    }

    private static int floor(final float value) {
        final int truncated = (int) value;
        return value < truncated ? truncated - 1 : truncated;
    }
}
