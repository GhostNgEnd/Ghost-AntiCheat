package ac.ghost.anticheat.player;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.holder.CheckHolder;
import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.compensated.world.PacketVisibleChunkCache;
import ac.ghost.anticheat.port.nukkit.NukkitLatencyAdapter;
import ac.ghost.anticheat.prediction.bds.entity.EntityRegistry;
import ac.ghost.anticheat.prediction.bds.entity.EntitySystems;
import ac.ghost.anticheat.prediction.bds.entity.EntityContext;
import ac.ghost.anticheat.player.state.GhostDebugState;
import ac.ghost.anticheat.player.state.GhostMovementBridgeState;
import ac.ghost.anticheat.prediction.nukkit.NukkitServerPlayerInitializationAdapter;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;
import ac.ghost.anticheat.prediction.bds.world.BlockSource;
import ac.ghost.anticheat.teleport.TeleportUtil;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;








public final class GhostPlayer {
    @Getter
    private final Player session;

    public long runtimeEntityId;
    @Getter
    private final TeleportUtil teleportUtil = new TeleportUtil(this);

    @Getter
    private final CheckHolder checkHolder;

    
    public final PacketVisibleChunkCache packetVisibleChunkCache = new PacketVisibleChunkCache(this);
    
    public final EntityRegistry entityRegistry = new EntityRegistry(this);
    
    public final EntityContext entityContext = this.entityRegistry.createEntity(
            this, new BlockSource(this, this.packetVisibleChunkCache));
    public final NukkitLatencyAdapter latencyAdapter = new NukkitLatencyAdapter(this);
    public final CompensatedInventory compensatedInventory = new CompensatedInventory(this);

    @Getter
    private final Map<UUID, CommandSender> trackedDebugPlayers = new ConcurrentHashMap<>();

    public GhostPlayer(Player session) {
        this.session = session;
        NukkitServerPlayerInitializationAdapter.initialize(this);

        
        
        this.checkHolder = new CheckHolder(this);

        
    }

    public boolean isClosed() {
        return !this.session.isConnected();
    }

    public void kick(String reason) {
        
        
        if (isExempted()) {
            return;
        }
        this.session.kick(Ghost.getInstance().getAlertManager().getPrefix() + reason);
    }

    
    public boolean isExempted() {
        try {
            return this.session.hasPermission("ghost.exempt");
        } catch (Exception ignored) {
            return false;
        }
    }

    
    public boolean isMovementExempted() {
        return isExempted();
    }

    




    public void resetForAntiCheatSwitch() {
        this.teleportUtil.clearPendingTeleports();
        this.latencyAdapter.latencyUtil().reset();
        this.entityContext.serverPlayerMovementSyncComponent.finishCorrectionTick();
        this.entityContext.serverPlayerMovementSyncComponent.clearCorrectionState();
        this.entityContext.forceSendMotionPacketComponent.reset(this);
        EntitySystems.resetPlayerMovement(this.entityContext);
        NukkitServerPlayerInitializationAdapter.initialize(this);
        this.checkHolder.reload();
    }

    




    public void resetForLegacyRespawnTransition() {
        this.teleportUtil.clearPendingTeleports();
        this.entityContext.forceSendMotionPacketComponent.reset(this);
        EntitySystems.resetPlayerMovement(this.entityContext);
        NukkitItemUseStateSystem.reset(this);
        this.entityContext.unloadedChunkTimerComponent.update(false);
        this.entityContext.stateVectorComponent.setDelta(Vec3.ZERO.clone());
        this.ghostMovementBridgeState.resetLegacyInputBridge();
    }


    
    public final GhostDebugState ghostDebugState = new GhostDebugState();
    public final GhostMovementBridgeState ghostMovementBridgeState =
            new GhostMovementBridgeState();

}
