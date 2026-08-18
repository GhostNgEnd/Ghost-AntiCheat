package ac.ghost.anticheat.teleport.data;

import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.network.protocol.MovePlayerPacket;


public final class TeleportData {
    private final Vec3 position;
    private final float pitch;
    private final float yaw;
    private final float headYaw;
    private final boolean onGround;
    private final int mode;
    private final int teleportCause;
    private final boolean keepVelocity;

    
    private volatile boolean accepted;

    public TeleportData(final Vec3 position, final float pitch,
                        final float yaw, final float headYaw,
                        final boolean onGround, final int mode,
                        final int teleportCause, final boolean keepVelocity) {
        this.position = position == null ? Vec3.ZERO.clone() : position.clone();
        this.pitch = pitch;
        this.yaw = yaw;
        this.headYaw = headYaw;
        this.onGround = onGround;
        this.mode = mode;
        this.teleportCause = teleportCause;
        this.keepVelocity = keepVelocity;
    }

    public Vec3 getPosition() { return this.position.clone(); }
    public float getPitch() { return this.pitch; }
    public float getYaw() { return this.yaw; }
    public float getHeadYaw() { return this.headYaw; }
    public boolean isOnGround() { return this.onGround; }
    public int getMode() { return this.mode; }
    public boolean isKeepVelocity() { return this.keepVelocity; }
    public boolean isAccepted() { return this.accepted; }

    public void accept() {
        this.accepted = true;
    }

    public static TeleportData fromPacket(final MovePlayerPacket packet) {
        return fromPacket(packet, false);
    }

    public static TeleportData fromPacket(final MovePlayerPacket packet,
                                          final boolean keepVelocity) {
        return new TeleportData(
                new Vec3(packet.x, packet.y, packet.z),
                packet.pitch, packet.yaw, packet.headYaw,
                packet.onGround, packet.mode, packet.teleportCause, keepVelocity);
    }

    public void writeTo(final MovePlayerPacket packet, final long runtimeEntityId) {
        packet.eid = runtimeEntityId;
        packet.x = (float) this.position.x;
        packet.y = (float) this.position.y;
        packet.z = (float) this.position.z;
        packet.pitch = this.pitch;
        packet.yaw = this.yaw;
        packet.headYaw = this.headYaw;
        packet.onGround = this.onGround;
        packet.mode = this.mode;
        packet.teleportCause = this.teleportCause;
    }
}
