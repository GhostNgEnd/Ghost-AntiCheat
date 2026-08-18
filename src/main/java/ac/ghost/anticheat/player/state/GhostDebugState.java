package ac.ghost.anticheat.player.state;

import ac.ghost.anticheat.util.math.Vec3;


public final class GhostDebugState {
    public int pistonTicks;
    public Vec3 pistonPosition = Vec3.ZERO;
    public String pistonEvent = "none";
    public int lavaTicks;
    public Vec3 lavaPosition = Vec3.ZERO;
    public String lavaEvent = "none";
    public int liquidTicks;
    public Vec3 liquidPosition = Vec3.ZERO;
    public String liquidEvent = "none";
    public long sprintEffectTraceUntilTick = Long.MIN_VALUE;
}
