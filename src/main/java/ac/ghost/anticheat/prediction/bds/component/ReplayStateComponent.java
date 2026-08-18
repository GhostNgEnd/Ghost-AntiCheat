package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;


public final class ReplayStateComponent {
    private long inputTick = -1L;
    private Vec3 position = Vec3.ZERO.clone();
    private Vec3 previousPosition = Vec3.ZERO.clone();
    private Vec3 delta = Vec3.ZERO.clone();
    private Box aabb = new Box(0, 0, 0, 0, 0, 0);
    private boolean onGround;
    private boolean flying;
    private boolean valid;

    public static ReplayStateComponent capture(final GhostPlayer player,
                                               final long inputTick) {
        final ReplayStateComponent state = new ReplayStateComponent();
        state.inputTick = inputTick;
        state.position = player.entityContext.stateVectorComponent.getPosition().clone();
        state.previousPosition = player.entityContext.stateVectorComponent.getPreviousPosition().clone();
        state.delta = player.entityContext.stateVectorComponent.getDelta().clone();
        state.aabb = player.entityContext.aabbShapeComponent.getAABB().clone();
        state.onGround = player.entityContext.onGroundFlagComponent.isPresent();
        state.flying = player.entityContext.movementAbilitiesComponent.isFlying();
        state.valid = true;
        return state;
    }


    public ReplayStateComponent copy() {
        final ReplayStateComponent copy = new ReplayStateComponent();
        copy.inputTick = inputTick;
        copy.position = position.clone();
        copy.previousPosition = previousPosition.clone();
        copy.delta = delta.clone();
        copy.aabb = aabb.clone();
        copy.onGround = onGround;
        copy.flying = flying;
        copy.valid = valid;
        return copy;
    }

    public void replace(final ReplayStateComponent source) {
        if (source == null || !source.valid) { clear(); return; }
        final ReplayStateComponent copy = source.copy();
        inputTick = copy.inputTick;
        position = copy.position;
        previousPosition = copy.previousPosition;
        delta = copy.delta;
        aabb = copy.aabb;
        onGround = copy.onGround;
        flying = copy.flying;
        valid = true;
    }

    public void apply(final GhostPlayer player) {
        if (!valid) throw new IllegalStateException("ReplayStateComponent is empty");
        player.entityContext.stateVectorComponent.setPosition(position.clone());
        player.entityContext.stateVectorComponent.setPreviousPosition(previousPosition.clone());
        player.entityContext.stateVectorComponent.setDelta(delta.clone());
        player.entityContext.aabbShapeComponent.setAABB(aabb.clone());
        player.entityContext.onGroundFlagComponent.setPresent(onGround);
        int movementAbilityBits = player.entityContext.movementAbilitiesComponent.getBits();
        movementAbilityBits &= ~MovementAbilitiesComponent.FLYING;
        if (flying) {
            movementAbilityBits |= MovementAbilitiesComponent.FLYING;
        }
        player.entityContext.movementAbilitiesComponent.setBits(movementAbilityBits);
    }

    public void clear() {
        inputTick = -1L;
        position = Vec3.ZERO.clone();
        previousPosition = Vec3.ZERO.clone();
        delta = Vec3.ZERO.clone();
        aabb = new Box(0, 0, 0, 0, 0, 0);
        onGround = false;
        flying = false;
        valid = false;
    }

    public long getInputTick() { return inputTick; }
    
    public boolean isFlying() { return flying; }
    public boolean isValid() { return valid; }
    public Vec3 getPosition() { return position.clone(); }
    public Vec3 getPreviousPosition() { return previousPosition.clone(); }
    public Vec3 getDelta() { return delta.clone(); }
    public Box getAabb() { return aabb.clone(); }
    public boolean isOnGround() { return onGround; }
}
