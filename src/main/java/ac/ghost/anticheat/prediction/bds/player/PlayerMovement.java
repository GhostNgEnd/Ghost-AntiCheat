package ac.ghost.anticheat.prediction.bds.player;

import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlagComponent;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;
import ac.ghost.anticheat.prediction.bds.component.SneakingComponent;
import ac.ghost.anticheat.util.math.Vec3;


public final class PlayerMovement {
    private static final int DIRECT_SNEAK_MASK =
            MoveInputComponent.SNEAK_DOWN | MoveInputComponent.DESCEND;
    private static final float DEFAULT_SNEAK_MOVEMENT_MULTIPLIER = 0.3F;

    private PlayerMovement() {
    }

    
    public static Vec3 calculateMoveVector(
            final Vec3 moveVector,
            final MoveInputComponent moveInputState,
            final boolean flying,
            final ActorDataFlagComponent actorDataFlags,
            final boolean inWater,
            final SneakingComponent sneaking) {
        final float multiplier = getSneakMovementMultiplier(
                moveInputState, flying, actorDataFlags, inWater, sneaking);
        return moveVector.multiply(multiplier, 1.0F, multiplier);
    }

    





    public static float getSneakMovementMultiplier(
            final MoveInputComponent moveInputState,
            final boolean flying,
            final ActorDataFlagComponent actorDataFlags,
            final boolean inWater,
            final SneakingComponent sneaking) {
        final boolean wantsSneak =
                (moveInputState.getFlags() & DIRECT_SNEAK_MASK) != 0
                        || actorDataFlags.has(ActorDataFlag.SNEAKING);
        if (!wantsSneak && !actorDataFlags.has(ActorDataFlag.CRAWLING)) {
            return 1.0F;
        }
        if (flying
                || actorDataFlags.has(ActorDataFlag.SWIMMING)
                || inWater) {
            return 1.0F;
        }
        return sneaking == null
                ? DEFAULT_SNEAK_MOVEMENT_MULTIPLIER
                : sneaking.getMovementScale();
    }
}
