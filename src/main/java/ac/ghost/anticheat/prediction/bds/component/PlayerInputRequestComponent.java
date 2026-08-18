package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.data.FluidState;
import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;









public final class PlayerInputRequestComponent {
    
    
    private int scaffoldingSneakHoldTicks;

    private float mMoveX;
    private float mMoveY;

    private boolean mIsSprinting;
    private boolean mSprintCanceled;
    private boolean mStopSprinting;
    private boolean mIsPlayerRiding;
    private boolean mUnblockedToStand;
    private boolean mUnblockedToSneak;
    private boolean mUnblockedToCrawl;

    private Vec3 mBreathingPoint = Vec3.ZERO.clone();
    private boolean mBreathingInAir;
    private boolean mBreathingInLiquid;
    private boolean mHasFlyIntent;
    private boolean mHasGlideIntent;
    private boolean mInstabuild;
    private boolean mMayFly;

    public PlayerInputRequestComponent() {
        resetToServerDefaults();
    }

    



    public void resetToServerDefaults() {
        this.mMoveX = 0.0F;
        this.mMoveY = 0.0F;
        this.mIsSprinting = false;
        this.mSprintCanceled = false;
        this.mStopSprinting = false;
        this.mIsPlayerRiding = false;
        this.mUnblockedToStand = true;
        this.mUnblockedToSneak = true;
        this.mUnblockedToCrawl = true;
        this.mBreathingPoint = Vec3.ZERO.clone();
        this.mBreathingInAir = true;
        this.mBreathingInLiquid = false;
        this.mHasFlyIntent = false;
        this.mHasGlideIntent = false;
        this.mInstabuild = false;
        this.mMayFly = false;
    }

    public int getScaffoldingSneakHoldTicks() {
        return scaffoldingSneakHoldTicks;
    }

    public void setScaffoldingSneakHoldTicks(final int ticks) {
        this.scaffoldingSneakHoldTicks = ticks;
    }

    public void setMove(final float x, final float y) {
        this.mMoveX = x;
        this.mMoveY = y;
    }

    public float getMoveX() {
        return mMoveX;
    }

    public float getMoveY() {
        return mMoveY;
    }

    public boolean isSprinting() {
        return mIsSprinting;
    }

    public void setSprinting(final boolean value) {
        this.mIsSprinting = value;
    }

    public boolean isSprintCanceled() {
        return mSprintCanceled;
    }

    public void setSprintCanceled(final boolean value) {
        this.mSprintCanceled = value;
    }

    public boolean isStopSprinting() {
        return mStopSprinting;
    }

    public void setStopSprinting(final boolean value) {
        this.mStopSprinting = value;
    }

    public boolean isPlayerRiding() {
        return mIsPlayerRiding;
    }

    public void setPlayerRiding(final boolean value) {
        this.mIsPlayerRiding = value;
    }

    public boolean isUnblockedToStand() {
        return mUnblockedToStand;
    }

    public void setUnblockedToStand(final boolean value) {
        this.mUnblockedToStand = value;
    }

    public boolean isUnblockedToSneak() {
        return mUnblockedToSneak;
    }

    public void setUnblockedToSneak(final boolean value) {
        this.mUnblockedToSneak = value;
    }

    public boolean isUnblockedToCrawl() {
        return mUnblockedToCrawl;
    }

    public void setUnblockedToCrawl(final boolean value) {
        this.mUnblockedToCrawl = value;
    }

    public Vec3 getBreathingPoint() {
        return mBreathingPoint.clone();
    }

    public void setBreathingPoint(final Vec3 value) {
        this.mBreathingPoint = value == null
                ? Vec3.ZERO.clone() : value.clone();
    }

    public boolean isBreathingInAir() {
        return mBreathingInAir;
    }

    public void setBreathingInAir(final boolean value) {
        this.mBreathingInAir = value;
    }

    public boolean isBreathingInLiquid() {
        return mBreathingInLiquid;
    }

    public void setBreathingInLiquid(final boolean value) {
        this.mBreathingInLiquid = value;
    }

    public void setBreathingState(final Vec3 point,
                                  final boolean inAir,
                                  final boolean inLiquid) {
        setBreathingPoint(point);
        this.mBreathingInAir = inAir;
        this.mBreathingInLiquid = inLiquid;
    }

    public boolean hasFlyIntent() {
        return mHasFlyIntent;
    }

    public void setFlyIntent(final boolean value) {
        this.mHasFlyIntent = value;
    }

    public boolean hasGlideIntent() {
        return mHasGlideIntent;
    }

    public void setGlideIntent(final boolean value) {
        this.mHasGlideIntent = value;
    }

    public boolean isInstabuild() {
        return mInstabuild;
    }

    public void setInstabuild(final boolean value) {
        this.mInstabuild = value;
    }

    public boolean mayFly() {
        return mMayFly;
    }

    public void setMayFly(final boolean value) {
        this.mMayFly = value;
    }
    
    public void sense(final GhostPlayer player) {
        final BlockVector3 probe =
                player.entityContext.stateVectorComponent.getPosition().toBlockVector3();
        final BlockLegacy block = player.entityContext.localConstBlockSourceFactoryComponent
                .create().getBlockState(probe, 0);
        final FluidState fluid = player.entityContext.localConstBlockSourceFactoryComponent
                .create().getFluidState(probe);
        setBreathingState(
                player.entityContext.stateVectorComponent.getPosition(),
                block.isAir(),
                fluid.fluid() != FluidState.FluidType.EMPTY);
    }

}
