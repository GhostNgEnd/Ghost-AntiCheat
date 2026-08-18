package ac.ghost.anticheat.teleport;

import ac.ghost.anticheat.prediction.nukkit.NukkitEntityPositionAdapter;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.NukkitAdapter;
import ac.ghost.anticheat.prediction.bds.component.PlayerPositionModeComponent;
import ac.ghost.anticheat.prediction.bds.entity.EntitySystems;
import ac.ghost.anticheat.prediction.bds.system.teleport.ServerPlayerTeleportProtectionSystem;
import ac.ghost.anticheat.prediction.bds.system.teleport.TeleportPositionModeEventSystem;
import ac.ghost.anticheat.teleport.data.TeleportData;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.Player;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.SetEntityMotionPacket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class TeleportUtil {
    private final GhostPlayer player;
    private boolean clearMovementHistoryAfterInput;

    private Vec3 lastKnowValid = Vec3.ZERO;

    private final Queue<TeleportData> queuedTeleports =
            new ConcurrentLinkedQueue<>();

    public TeleportUtil(final GhostPlayer player) {
        this.player = player;
    }

    public Vec3 getLastKnowValid() {
        return this.lastKnowValid.clone();
    }

    public Queue<TeleportData> getQueuedTeleports() {
        return this.queuedTeleports;
    }

    
    public void teleport(final Vec3 footPosition) {
        this.teleportNetwork(footPosition.up(NukkitEntityPositionAdapter.getYOffset(player)));
    }

    



    public void teleportNetwork(final Vec3 networkPosition) {
        if (this.isHardTeleporting()) {
            return;
        }
        final TeleportData data = new TeleportData(
                networkPosition,
                player.entityContext.actorRotationComponent.getPitch(),
                player.entityContext.actorRotationComponent.getYaw(),
                player.entityContext.actorRotationComponent.getHeadYaw(),
                false,
                MovePlayerPacket.MODE_TELEPORT,
                4,
                false);
        this.send(data);
    }

    




    public void correctMovement(final Vec3 footPosition,
                                final Vec3 velocity,
                                final boolean onGround,
                                final long inputTick) {
        if (this.isHardTeleporting()) {
            return;
        }

        final Vec3 networkPosition = footPosition.up(
                NukkitEntityPositionAdapter.getYOffset(player));
        final TeleportData data = new TeleportData(
                networkPosition,
                player.entityContext.actorRotationComponent.getPitch(),
                player.entityContext.actorRotationComponent.getYaw(),
                player.entityContext.actorRotationComponent.getHeadYaw(),
                onGround,
                MovePlayerPacket.MODE_TELEPORT,
                4,
                true);
        this.send(data);
        this.sendMotion(velocity, inputTick);
    }

    
    public void resend(final TeleportData data) {
        if (data == null) {
            return;
        }
        if (!data.isKeepVelocity()) {
            this.teleportNetwork(data.getPosition());
            return;
        }

        this.correctMovement(
                data.getPosition().down(NukkitEntityPositionAdapter.getYOffset(player)),
                player.entityContext.stateVectorComponent.getDelta(),
                data.isOnGround(),
                player.entityContext.serverPlayerMovementSyncComponent
                        .clientBoundPacketTick());
    }

    









    public void resendPending(final TeleportData data) {
        if (data == null) {
            return;
        }

        final MovePlayerPacket packet = new MovePlayerPacket();
        data.writeTo(packet, player.runtimeEntityId);
        this.sendRaw(packet);

        TeleportPositionModeEventSystem.onOutbound(player, data.getMode());
        ServerPlayerTeleportProtectionSystem.tick(player, data.getPosition());

        if (data.isKeepVelocity()) {
            this.sendMotion(
                    player.entityContext.stateVectorComponent.getDelta(),
                    player.entityContext.serverPlayerMovementSyncComponent
                            .clientBoundPacketTick());
        }
    }

    private void sendMotion(final Vec3 velocity, final long inputTick) {
        if (velocity == null
                || !Float.isFinite(velocity.x)
                || !Float.isFinite(velocity.y)
                || !Float.isFinite(velocity.z)
                || velocity.lengthSquared() <= 0.0F) {
            return;
        }

        final SetEntityMotionPacket packet = new SetEntityMotionPacket();
        packet.eid = player.runtimeEntityId;
        packet.motionX = velocity.x;
        packet.motionY = velocity.y;
        packet.motionZ = velocity.z;
        packet.tick = Math.max(0L, inputTick);
        this.sendRaw(packet);
    }

    










    public void queue(final TeleportData data) {
        if (data == null) {
            return;
        }
        this.queuedTeleports.add(data);
        TeleportPositionModeEventSystem.onOutbound(player, data.getMode());
        ServerPlayerTeleportProtectionSystem.tick(player, data.getPosition());

        player.latencyAdapter.sendLatencyStackAfterOutbound(data::accept);
    }

    private void send(final TeleportData data) {
        final MovePlayerPacket packet = new MovePlayerPacket();
        data.writeTo(packet, player.runtimeEntityId);
        this.sendRaw(packet);

        
        
        
        
        this.queue(data);
    }

    
    private void sendRaw(final DataPacket packet) {
        final Player nukkitPlayer = NukkitAdapter.getPlayer(player);
        packet.protocol = nukkitPlayer.protocol;
        packet.gameVersion = nukkitPlayer.getGameVersion();
        nukkitPlayer.getNetworkSession().sendPacket(packet);
    }

    
    public void beginInputTick() {
        if (player.entityContext.hasTeleportedFlagComponent.isPresent()) {
            player.entityContext.hasTeleportedFlagComponent.clear();
            player.entityContext.playerPositionModeComponent.setMode(
                    this.firstHardTeleport() == null
                            ? PlayerPositionModeComponent.NORMAL
                            : PlayerPositionModeComponent.TELEPORT);
        }
    }

    
    public void finishInputTick() {
        if (!this.clearMovementHistoryAfterInput) {
            return;
        }
        this.clearMovementHistoryAfterInput = false;
        EntitySystems.resetPlayerMovement(player.entityContext);
    }

    public void onAccepted() {
        this.clearMovementHistoryAfterInput = true;

        final TeleportData next = this.firstHardTeleport();
        if (next == null) {
            player.entityContext.isBeingTeleportedFlagComponent.clear();
            player.entityContext.serverPlayerTeleportingFlagComponent.clear();
        } else {
            player.entityContext.isBeingTeleportedFlagComponent.setPresent(true);
            TeleportPositionModeEventSystem.onOutbound(player, next.getMode());
            ServerPlayerTeleportProtectionSystem.tick(player,
                    next.getPosition());
        }
    }

    public void updateProtection() {
        final TeleportData current = this.firstHardTeleport();
        ServerPlayerTeleportProtectionSystem.tick(player,
                current == null ? null : current.getPosition());
    }

    private TeleportData firstHardTeleport() {
        return this.queuedTeleports.peek();
    }

    public void clearPendingTeleports() {
        this.queuedTeleports.clear();
        player.entityContext.isBeingTeleportedFlagComponent.clear();
        player.entityContext.serverPlayerTeleportingFlagComponent.clear();
        player.entityContext.playerPositionModeComponent.setMode(
                PlayerPositionModeComponent.NORMAL);
    }

    public boolean isTeleporting() {
        return player.entityContext.isBeingTeleportedFlagComponent.isPresent()
                || player.entityContext.hasTeleportedFlagComponent.isPresent();
    }

    public boolean isHardTeleporting() {
        return player.entityContext.isBeingTeleportedFlagComponent.isPresent()
                || this.firstHardTeleport() != null;
    }

    public void acceptPredictedMovementPosition(final Vec3 position) {
        this.lastKnowValid = position.clone();
    }
}
