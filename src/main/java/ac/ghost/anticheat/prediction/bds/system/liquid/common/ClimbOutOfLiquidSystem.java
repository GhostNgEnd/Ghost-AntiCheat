package ac.ghost.anticheat.prediction.bds.system.liquid.common;

import ac.ghost.anticheat.data.FluidState;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Mutable;
import ac.ghost.anticheat.util.math.Vec3;


public final class ClimbOutOfLiquidSystem {
    private static final float COLLISION_TEST_Y_EXTRA =
            Float.intBitsToFloat(0x3F19999A);
    private static final float CLIMB_OUT_VELOCITY =
            Float.intBitsToFloat(0x3E99999A);

    private ClimbOutOfLiquidSystem() {
    }

    public static void tick(final GhostPlayer player,
                            final float travelStartY) {
        if (!player.entityContext.horizontalCollisionFlagComponent.isPresent()) {
            return;
        }

        final Vec3 velocity = player.entityContext.stateVectorComponent.getDelta();
        final float testY = velocity.y + COLLISION_TEST_Y_EXTRA
                - player.entityContext.stateVectorComponent.getPosition().y + travelStartY;
        if (doesNotCollide(player,
                player.entityContext.aabbShapeComponent.getAABB()
                        .offset(velocity.x, testY, velocity.z))) {
            player.entityContext.stateVectorComponent.getDelta().y = CLIMB_OUT_VELOCITY;
        }
    }

    private static boolean doesNotCollide(final GhostPlayer player, final Box box) {
        return player.entityContext.blockSource.noCollision(box)
                && !containsFluid(player, box);
    }

    private static boolean containsFluid(final GhostPlayer player, final Box box) {
        final int minX = (int) Math.floor(box.minX);
        final int maxX = (int) Math.ceil(box.maxX);
        final int minY = (int) Math.floor(box.minY);
        final int maxY = (int) Math.ceil(box.maxY);
        final int minZ = (int) Math.floor(box.minZ);
        final int maxZ = (int) Math.ceil(box.maxZ);
        final Mutable mutable = new Mutable();

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    mutable.set(x, y, z);
                    final FluidState fluid = player.entityContext.blockSource.getFluidState(mutable);
                    if (fluid.fluid() != FluidState.FluidType.EMPTY) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
