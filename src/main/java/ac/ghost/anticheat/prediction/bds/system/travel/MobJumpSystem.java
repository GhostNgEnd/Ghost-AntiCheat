package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.prediction.bds.entity.EntityContext;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.prediction.bds.system.block.ScaffoldingActionSystem;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.entity.Entity;
import cn.nukkit.network.protocol.types.AuthInputAction;


public final class MobJumpSystem {
    private static final float ASCENDABLE_VERTICAL_SPEED =
            Float.intBitsToFloat(0x3E19999A);
    private static final float CLIMB_VERTICAL_SPEED =
            Float.intBitsToFloat(0x3E4CCCCD);

    private MobJumpSystem() {
    }

    public static void tick(final EntityContext entity) {
        entity.stateVectorComponent.setDelta(apply(entity, entity.stateVectorComponent.getDelta()));
    }

    public static Vec3 apply(final EntityContext entity,
                             final Vec3 startVelocity) {
        final Vec3 velocity = startVelocity;
        if (entity.movementAbilitiesComponent.isFlying()) {
            return velocity;
        }

        final boolean descendThroughBlock = entity.actorDataFlagComponent.has(
                ScaffoldingActionSystem.DESCEND_THROUGH_BLOCK_FLAG);
        final boolean inScaffolding = entity.actorDataFlagComponent.has(
                Entity.DATA_FLAG_IN_SCAFFOLDING);
        final boolean inAscendable = entity.actorDataFlagComponent.has(
                Entity.DATA_FLAG_IN_ASCENDABLE_BLOCK);
        final boolean jumping = entity.mobIsJumpingFlagComponent.isPresent();

        





        if (!jumping) {
            return velocity;
        }

        
        
        if (!descendThroughBlock && (inScaffolding || inAscendable)) {
            
            
            entity.mobJumpComponent.setNoJumpDelay(10);
            return new Vec3(velocity.x, ASCENDABLE_VERTICAL_SPEED, velocity.z);
        }

        final StateVectorComponent stateVector = new StateVectorComponent();
        stateVector.setPosition(entity.stateVectorComponent.getPosition().clone());
        stateVector.setDelta(velocity.clone());
        if (AutoClimbSystem.isClimbing(entity.externalDataComponent.player(), stateVector)) {
            return new Vec3(velocity.x, CLIMB_VERTICAL_SPEED, velocity.z);
        }

        
        
        if (entity.mobJumpComponent.getNoJumpDelay() != 0) {
            return velocity;
        }
        return applyOrdinaryJump(entity, velocity);
    }

    private static Vec3 applyOrdinaryJump(final EntityContext entity,
                                          Vec3 velocity) {
        
        
        
        final boolean autoJumping = entity.moveInputComponent.hasStateFlag(
                ac.ghost.anticheat.prediction.bds.component.MoveInputComponent
                        .STATE_AUTO_JUMPING_IN_WATER);
        final boolean jumping = entity.moveInputComponent.hasStateFlag(
                ac.ghost.anticheat.prediction.bds.component.MoveInputComponent
                        .STATE_JUMPING);

        final boolean canJumpInWater =
                entity.externalDataComponent.player().ghostMovementBridgeState.waterSample.surfaceHeight() != 0;
        final boolean canJumpInLava =
                entity.serverPlayerMovementComponent.getCurrentInputTick() != 1L
                        && entity.externalDataComponent.player().ghostMovementBridgeState.lavaSample.touching();
        if ((jumping || autoJumping) && (canJumpInWater || canJumpInLava)) {
            














            final boolean swimmingWaterJump = canJumpInWater
                    && shouldClearTransitionWaterJumpY(entity);
            if (swimmingWaterJump) {
                velocity = new Vec3(velocity.x, 0.0F, velocity.z);
            } else {
                velocity = velocity.add(0, 0.04F, 0);
            }
        } else if (entity.onGroundFlagComponent.isPresent()
                && entity.playerActionComponent.actions()
                .contains(AuthInputAction.START_JUMPING)) {
            velocity = JumpFromGroundSystem.apply(entity, velocity);
        }

        return velocity;
    }

    
    private static boolean isSwimAmountTransitioningForMovementPass(
            final EntityContext entity) {
        final float amount = entity.swimAmountComponent.getCurrent();
        return amount > 0.0F && amount < 1.0F;
    }

    private static boolean shouldClearTransitionWaterJumpY(
            final EntityContext entity) {
        
        
        
        return entity.externalDataComponent.player().ghostMovementBridgeState.waterSample.touching()
                && isSwimAmountTransitioningForMovementPass(entity);
    }
}
