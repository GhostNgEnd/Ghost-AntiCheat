package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.prediction.bds.component.AABBShapeComponent;
import ac.ghost.anticheat.prediction.bds.component.FallDistanceComponent;
import ac.ghost.anticheat.prediction.bds.component.HoneyBlockFlag;
import ac.ghost.anticheat.prediction.bds.component.InsideBlockWithPosAndBlockComponent;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.prediction.bds.math.BdsMovementMath;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;


public final class InsideHoneyBlockSystem {
    private static final float HORIZONTAL_VELOCITY_MULTIPLIER =
            Float.intBitsToFloat(0x3ECCCCCD);
    private static final float DOWNWARD_VELOCITY_LIMIT =
            Float.intBitsToFloat(0xBDF5C28F);
    private static final float BLOCK_TOP_OFFSET =
            Float.intBitsToFloat(0x3F700000);
    private static final float HALF =
            Float.intBitsToFloat(0x3F000000);
    private static final float SIDE_CONTACT_OFFSET =
            Float.intBitsToFloat(0x3EDCCCCD);
    private static final float ZERO =
            Float.intBitsToFloat(0x00000000);

    private InsideHoneyBlockSystem() {
    }

    



    public static void fireEventsSystem(
            final InsideBlockWithPosAndBlockComponent<HoneyBlockFlag> component,
            final AABBShapeComponent shape,
            final StateVectorComponent stateVector,
            final FallDistanceComponent fallDistance) {
        if (component.isEmpty()) {
            return;
        }

        final Vec3 position = stateVector.getPosition();
        final Vec3 velocity = stateVector.getDelta();

        for (InsideBlockWithPosAndBlockComponent.Entry<HoneyBlockFlag> entry
                : component.entries()) {
            final float velocityX = velocity.x
                    * HORIZONTAL_VELOCITY_MULTIPLIER;
            final float velocityY = BdsMovementMath.maxss(
                    DOWNWARD_VELOCITY_LIMIT, velocity.y);
            final float velocityZ = velocity.z
                    * HORIZONTAL_VELOCITY_MULTIPLIER;

            velocity.x = velocityX;
            velocity.y = velocityY;
            velocity.z = velocityZ;

            final BlockVector3 blockPosition = entry.position();
            final float blockTop = (float) blockPosition.getY()
                    + BLOCK_TOP_OFFSET;
            if (position.y > blockTop || velocity.y >= ZERO) {
                continue;
            }

            final float halfWidth = shape.getWidth() * HALF;
            final float sideLimit = halfWidth + SIDE_CONTACT_OFFSET;

            final float blockCenterX = (float) blockPosition.getX() + HALF;
            final float deltaX = Math.abs(blockCenterX - position.x);
            if (deltaX > sideLimit) {
                fallDistance.setValue(ZERO);
                continue;
            }

            final float blockCenterZ = (float) blockPosition.getZ() + HALF;
            final float deltaZ = Math.abs(blockCenterZ - position.z);
            if (deltaZ > sideLimit) {
                fallDistance.setValue(ZERO);
            }
        }
    }

}
