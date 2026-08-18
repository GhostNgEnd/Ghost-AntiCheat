package ac.ghost.anticheat.packets;

import ac.ghost.anticheat.prediction.nukkit.NukkitEntityPositionAdapter;
import ac.ghost.anticheat.collision.bds.system.ActorSetPosSystem;
import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.impl.timer.Timer;
import ac.ghost.anticheat.data.input.PredictionData;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import ac.ghost.anticheat.prediction.bds.component.AbilitiesComponent;
import ac.ghost.anticheat.prediction.nukkit.data.ReplayableActorInput;
import ac.ghost.anticheat.prediction.bds.component.ServerPlayerMovementComponent;
import ac.ghost.anticheat.prediction.bds.system.input.SendPlayerAuthInputReceivedEventSystem;
import ac.ghost.anticheat.prediction.bds.system.input.UpdateServerPlayerInputSystem;
import ac.ghost.anticheat.prediction.bds.entity.EntitySystems;
import ac.ghost.anticheat.prediction.bds.system.movement.PredictedMovementSystem;
import ac.ghost.anticheat.prediction.bds.system.player.UpdateAbilitiesSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.TickServerPlayerMovementFinalSystem;
import ac.ghost.anticheat.prediction.bds.system.spinattack.StoreSpinAttackRiptideLevelSystem;
import ac.ghost.anticheat.prediction.bds.system.teleport.TeleportToSystem;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;
import ac.ghost.anticheat.teleport.data.TeleportData;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.ChangeDimensionPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.PlayerInputPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.types.InputMode;

import java.util.Set;









public final class ServerNetworkHandler implements Listener {
    
    private static final float NUKKIT_FORCE_MOVEMENT_DISTANCE_SQUARED = 0.1F;


    @EventHandler
    public void handle(final DataPacketReceiveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final GhostPlayer player = this.player(event.getPlayer());
        if (player == null || player.isExempted()) {
            return;
        }

        if (player.ghostMovementBridgeState.legacyRespawnTransition) {
            if (!player.getSession().isAlive()) {
                






                if (event.getPacket() instanceof PlayerAuthInputPacket packet) {
                    event.setCancelled(true);
                    MovementPipelineDebugLogger.log(player, packet,
                            "respawn-dead-cancelled", true,
                            "prevent-nukkit-pre-death-reset");
                } else if (event.getPacket() instanceof MovePlayerPacket) {
                    event.setCancelled(true);
                }
                return;
            }
            player.ghostMovementBridgeState.legacyRespawnTransition = false;
        }

        if (event.getPacket() instanceof PlayerAuthInputPacket packet) {
            if (!player.getSession().isMovementServerAuthoritative()) {
                MovementPipelineDebugLogger.log(player, packet,
                        "receive-ignored", event.isCancelled(),
                        "session-not-server-authoritative");
                return;
            }

            finishInitialLoadingFallback(player);

            
            
            
            player.entityContext.playerLoadingScreenComponent.ticksSinceChange++;
            if (rejectTimerAuthInput(event, player, packet)) {
                return;
            }
            player.ghostMovementBridgeState
                    .predictionHasDigitalDirectionState = true;
            player.ghostMovementBridgeState.predictionHasRawMoveVector =
                    player.getSession().protocol >= ProtocolInfo.v1_21_50
                            && packet.getRawMoveVector() != null;
            MovementPipelineDebugLogger.log(player, packet, "receive",
                    event.isCancelled(), "auth-input");
            handlePlayerAuthInput(event, player, packet);
            return;
        }

        if (event.getPacket() instanceof MovePlayerPacket packet
                && !player.getSession().isMovementServerAuthoritative()) {
            finishInitialLoadingFallback(player);

            
            
            
            if (packet.eid == player.runtimeEntityId
                    && packet.mode == MovePlayerPacket.MODE_NORMAL) {
                player.entityContext.playerLoadingScreenComponent.ticksSinceChange++;
            }
            if (rejectTimerMovePlayer(event, player, packet)) {
                return;
            }
            handleLegacyMovePlayer(event, player, packet);
        }
    }

    





    private static void finishInitialLoadingFallback(final GhostPlayer player) {
        final var loading = player.entityContext.playerLoadingScreenComponent;
        if (!loading.active || loading.screenId != null
                || loading.initialFallbackDeadlineNs == 0L
                || System.nanoTime() < loading.initialFallbackDeadlineNs) {
            return;
        }

        loading.active = false;
        loading.ticksSinceChange = 0;
        loading.initialFallbackDeadlineNs = 0L;
    }

    
    private static boolean rejectTimerAuthInput(
            final DataPacketReceiveEvent event,
            final GhostPlayer player,
            final PlayerAuthInputPacket packet) {
        final Timer timer = (Timer) player.getCheckHolder().get(Timer.class);
        if (timer == null || packet.getTick() < 0L) {
            return false;
        }
        if (!timer.isInvalidAuthInput(packet.getTick())) {
            return false;
        }
        event.setCancelled(true);
        MovementPipelineDebugLogger.log(player, packet,
                "timer-rejected", true, "client-clock-ahead");
        return true;
    }

    



    private static boolean rejectTimerMovePlayer(
            final DataPacketReceiveEvent event,
            final GhostPlayer player,
            final MovePlayerPacket packet) {
        if (packet.eid != player.runtimeEntityId
                || packet.mode != MovePlayerPacket.MODE_NORMAL) {
            return false;
        }

        final Timer timer = (Timer) player.getCheckHolder().get(Timer.class);
        if (timer == null || !timer.isInvalidMovePlayer(
                packet, player.getSession().protocol)) {
            return false;
        }
        event.setCancelled(true);
        return true;
    }

    private static void handlePlayerAuthInput(
            final DataPacketReceiveEvent event,
            final GhostPlayer player,
            final PlayerAuthInputPacket packet) {
        final long claimedTick = packet.getTick();
        if (claimedTick < 0L) {
            MovementPipelineDebugLogger.log(player, packet, "invalid-tick",
                    event.isCancelled(), "negative-tick");
            player.kick("Impossible tick id=" + claimedTick);
            return;
        }

        
        
        
        SendPlayerAuthInputReceivedEventSystem.validate(player.entityContext, packet);

        final ServerPlayerMovementComponent.Acceptance acceptance =
                EntitySystems.acceptPlayerAuthInput(player.entityContext, packet);
        if (acceptance != ServerPlayerMovementComponent.Acceptance.ACCEPTED) {
            event.setCancelled(true);
            MovementPipelineDebugLogger.log(player, packet,
                    "acceptance-rejected", true, acceptance.name());
            return;
        }

        
        
        player.getTeleportUtil().beginInputTick();
        player.entityContext.serverPlayerMovementComponent.setCurrentInputTick(claimedTick);
        player.entityContext.serverPlayerMovementComponent.markAuthInputReceived();
        player.entityContext.serverPlayerMovementSyncComponent.setClientBoundPacketTick(claimedTick);
        final ReplayableActorInput replayableInput =
                EntitySystems.beginPlayerAuthInput(player.entityContext, claimedTick);

        try {

            final long trackedVehicleBefore = player.entityContext.vehicleComponent.value == null
                    ? 0L : player.entityContext.vehicleComponent.value.vehicleRuntimeId;
            final long sessionRiding = player.getSession().riding == null
                    ? 0L : player.getSession().riding.getId();
            final boolean dismountTransition = trackedVehicleBefore != 0L
                    && sessionRiding == 0L
                    && replayableInput.predictedVehicle() == 0L;
            if (dismountTransition) {
                player.entityContext.vehicleComponent.value = null;
            }

            UpdateServerPlayerInputSystem.tick(player, replayableInput, packet);

            
            
            
            
            StoreSpinAttackRiptideLevelSystem.tick(player);

            
            
            
            TickServerPlayerMovementFinalSystem.updateUnvalidatedPosition(player,
                    packet);
            SendPlayerAuthInputReceivedEventSystem.tick(player.entityContext);

            if (dismountTransition) {
                processExempted(player);
                player.packetVisibleChunkCache.cleanChunksAtPlayerPosition();
                MovementPipelineDebugLogger.log(player, packet,
                        "handler-return", event.isCancelled(),
                        "dismount-transition");
                return;
            }

            if (player.entityContext.vehicleComponent.value != null) {
                player.entityContext.stateVectorComponent.setPosition(player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedPosition());
                player.packetVisibleChunkCache.cleanChunksAtPlayerPosition();
                MovementPipelineDebugLogger.log(player, packet,
                        "handler-return", event.isCancelled(),
                        "tracked-vehicle");
                return;
            }

            if (player.getSession().isSleeping()) {
                MovementPipelineDebugLogger.log(player, packet,
                        "handler-return", event.isCancelled(), "sleeping");
                return;
            }

            if (player.getTeleportUtil().isHardTeleporting()) {
                MovementPipelineDebugLogger.log(player, packet,
                        "hard-teleport-before-consume", event.isCancelled(),
                        "queued-teleport-branch");
                player.getTeleportUtil().updateProtection();
                processQueuedTeleports(player, packet);
                player.packetVisibleChunkCache.cleanChunksAtPlayerPosition();
                MovementPipelineDebugLogger.log(player, packet,
                        "handler-return", event.isCancelled(),
                        "queued-teleport-branch");
                return;
            }

            final String movementPath;
            if (player.isMovementExempted()) {
                processExempted(player);
                movementPath = "movement-exempted";
            } else if (shouldRunPrediction(
                    player.getSession().protocol,
                    player.entityContext.playerLoadingScreenComponent.active,
                    player.entityContext.playerLoadingScreenComponent.ticksSinceChange,
                    player.entityContext.serverPlayerCurrentMovementComponent
                            .getUnvalidatedTickEnd().lengthSquared())) {
                PredictedMovementSystem.tick(player);
                movementPath = "prediction";
            } else {
                player.entityContext.stateVectorComponent.setDelta(Vec3.ZERO.clone());
                movementPath = "loading-screen-hold";
            }

            player.packetVisibleChunkCache.cleanChunksAtPlayerPosition();
            final boolean trackUnloadedChunk = shouldTrackUnloadedChunk(
                    player.getSession().protocol,
                    player.entityContext.playerLoadingScreenComponent.active,
                    player.entityContext.playerLoadingScreenComponent.ticksSinceChange);
            player.entityContext.unloadedChunkTimerComponent.update(
                    trackUnloadedChunk
                            && !player.entityContext.blockSource.isChunkLoadedAt(
                            player.entityContext.stateVectorComponent.getPosition().x,
                            player.entityContext.stateVectorComponent.getPosition().z));
            if (player.entityContext.unloadedChunkTimerComponent.insideUnloadedChunk) {
                player.getTeleportUtil().teleport(
                        player.getTeleportUtil().getLastKnowValid());
                MovementPipelineDebugLogger.log(player, packet,
                        "unloaded-chunk-teleport", event.isCancelled(),
                        movementPath);
            }

            TickServerPlayerMovementFinalSystem.tick(player, packet);
            MovementPipelineDebugLogger.log(player, packet,
                    "handler-complete", event.isCancelled(), movementPath);
        } finally {
            EntitySystems.completePlayerAuthInput(player.entityContext, claimedTick);
            player.getTeleportUtil().finishInputTick();
        }
    }

    




    private static void handleLegacyMovePlayer(
            final DataPacketReceiveEvent event,
            final GhostPlayer player,
            final MovePlayerPacket packet) {
        if (packet.eid != player.runtimeEntityId
                || packet.mode == MovePlayerPacket.MODE_PITCH) {
            return;
        }

        if (packet.mode != MovePlayerPacket.MODE_NORMAL) {
            rebaselineLegacyMove(player, packet);
            return;
        }

        final float yOffset = NukkitEntityPositionAdapter.getYOffset(player);
        final Vec3 bodyPosition = new Vec3(
                packet.x, packet.y - yOffset, packet.z);
        final Vec3 previous = player.entityContext
                .serverPlayerCurrentMovementComponent
                .getUnvalidatedPosition();
        final Vec3 observedDelta = bodyPosition.subtract(previous);

        final PlayerAuthInputPacket synthetic = new PlayerAuthInputPacket();
        synthetic.protocol = player.getSession().protocol;
        synthetic.setTick(++player.ghostMovementBridgeState.legacyInputTick);
        synthetic.setPosition(new Vector3f(packet.x, packet.y, packet.z));
        synthetic.setDelta(observedDelta.toVector3f());
        synthetic.setPitch(packet.pitch);
        synthetic.setYaw(packet.yaw);
        synthetic.setHeadYaw(packet.headYaw);
        synthetic.setInputMode(legacyInputMode(player));
        synthetic.setInputData(legacyActions(player));
        synthetic.setPredictedVehicle(packet.ridingEid);

        player.ghostMovementBridgeState
                .predictionHasDigitalDirectionState = false;
        player.ghostMovementBridgeState.predictionHasRawMoveVector = false;
        handlePlayerAuthInput(event, player, synthetic);
    }

    private static void rebaselineLegacyMove(final GhostPlayer player,
                                             final MovePlayerPacket packet) {
        final Vec3 position = new Vec3(
                packet.x,
                packet.y - NukkitEntityPositionAdapter.getYOffset(player),
                packet.z);
        ActorSetPosSystem.setImmediate(player, position, true);
        player.entityContext.serverPlayerCurrentMovementComponent
                .setUnvalidatedPosition(position.clone());
        player.entityContext.serverPlayerCurrentMovementComponent
                .setPreviousUnvalidatedPosition(position.clone());
        player.entityContext.actorRotationComponent.set(
                packet.pitch, packet.yaw, packet.headYaw);
        player.entityContext.onGroundFlagComponent.setPresent(packet.onGround);
    }

    private static InputMode legacyInputMode(final GhostPlayer player) {
        final InputMode tracked = player.entityContext.playerInputModeComponent
                .getProtocolValue();
        return tracked == null ? InputMode.UNDEFINED : tracked;
    }

    private static Set<AuthInputAction> legacyActions(
            final GhostPlayer player) {
        final Set<AuthInputAction> actions = player.ghostMovementBridgeState
                .consumeLegacyInputActions();
        if (player.entityContext.actorDataFlagComponent.has(
                cn.nukkit.entity.Entity.DATA_FLAG_SPRINTING)) {
            actions.add(AuthInputAction.SPRINTING);
        }
        if (player.entityContext.actorDataFlagComponent.has(
                cn.nukkit.entity.Entity.DATA_FLAG_SNEAKING)) {
            actions.add(AuthInputAction.SNEAKING);
        }
        return actions;
    }

    


    @EventHandler(priority = EventPriority.HIGHEST)
    public void finalizePlayerAuthInput(final DataPacketReceiveEvent event) {
        if (event.isCancelled()) {
            final GhostPlayer cancelledPlayer = this.player(event.getPlayer());
            if (cancelledPlayer != null
                    && event.getPacket() instanceof PlayerAuthInputPacket packet) {
                MovementPipelineDebugLogger.log(cancelledPlayer, packet,
                        "finalizer-saw-cancelled", true,
                        "cancelled-before-highest");
            }
            return;
        }

        final GhostPlayer player = this.player(event.getPlayer());
        if (player == null || player.isExempted()) {
            return;
        }

        final boolean authInput =
                event.getPacket() instanceof PlayerAuthInputPacket
                        && player.getSession().isMovementServerAuthoritative();
        final boolean legacyMove = event.getPacket() instanceof MovePlayerPacket
                && !player.getSession().isMovementServerAuthoritative();
        if (!authInput && !legacyMove) {
            return;
        }

        resetTransientInputState(player);

        if (player.entityContext.vehicleComponent.value != null && player.getSession().riding == null) {
            event.setCancelled(true);
            if (event.getPacket() instanceof PlayerAuthInputPacket packet) {
                MovementPipelineDebugLogger.log(player, packet,
                        "finalizer-cancelled", true,
                        "tracked-vehicle-without-session-riding");
            }
            return;
        }

        final Vec3 networkPosition = player.entityContext.stateVectorComponent
                .getPosition().add(0.0F,
                        NukkitEntityPositionAdapter.getYOffset(player), 0.0F);
        if (event.getPacket() instanceof PlayerAuthInputPacket packet) {
            packet.setPosition(networkPosition.toVector3f());
            TickServerPlayerMovementFinalSystem.writeAuthoritativeStateToPacket(
                    player, packet);
            MovementPipelineDebugLogger.log(player, packet,
                    "finalizer-rewrite", event.isCancelled(),
                    "authoritative-position-written");
        } else if (event.getPacket() instanceof MovePlayerPacket packet) {
            packet.x = networkPosition.x;
            packet.y = networkPosition.y;
            packet.z = networkPosition.z;
            packet.pitch = player.entityContext.actorRotationComponent
                    .getPitch();
            packet.yaw = player.entityContext.actorRotationComponent.getYaw();
            packet.headYaw = player.entityContext.actorRotationComponent
                    .getHeadYaw();
            packet.onGround = player.entityContext.onGroundFlagComponent
                    .isPresent();
        }

        if (player.getTeleportUtil().isHardTeleporting()) {
            event.setCancelled(true);
            if (event.getPacket() instanceof PlayerAuthInputPacket packet) {
                MovementPipelineDebugLogger.log(player, packet,
                        "finalizer-cancelled", true, "hard-teleporting");
            }
            return;
        }
        if (player.getTeleportUtil().isTeleporting()) {
            if (event.getPacket() instanceof PlayerAuthInputPacket packet) {
                MovementPipelineDebugLogger.log(player, packet,
                        "finalizer-pass", event.isCancelled(), "teleporting");
            }
            return;
        }
        if (event.getPacket() instanceof PlayerAuthInputPacket packet) {
            MovementPipelineDebugLogger.log(player, packet,
                    "finalizer-pass", event.isCancelled(), "normal");
        }
    }

    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void captureLegacyPlayerAction(
            final DataPacketReceiveEvent event) {
        if (!(event.getPacket() instanceof PlayerActionPacket packet)) {
            return;
        }
        final GhostPlayer player = this.player(event.getPlayer());
        if (player == null || player.isExempted()
                || player.getSession().isMovementServerAuthoritative()) {
            return;
        }

        final AuthInputAction action = switch (packet.action) {
            case PlayerActionPacket.ACTION_JUMP ->
                    AuthInputAction.START_JUMPING;
            case PlayerActionPacket.ACTION_START_SPRINT ->
                    AuthInputAction.START_SPRINTING;
            case PlayerActionPacket.ACTION_STOP_SPRINT ->
                    AuthInputAction.STOP_SPRINTING;
            case PlayerActionPacket.ACTION_START_SNEAK ->
                    AuthInputAction.START_SNEAKING;
            case PlayerActionPacket.ACTION_STOP_SNEAK ->
                    AuthInputAction.STOP_SNEAKING;
            case PlayerActionPacket.ACTION_START_SWIMMING ->
                    AuthInputAction.START_SWIMMING;
            case PlayerActionPacket.ACTION_STOP_SWIMMING ->
                    AuthInputAction.STOP_SWIMMING;
            case PlayerActionPacket.ACTION_START_GLIDE ->
                    AuthInputAction.START_GLIDING;
            case PlayerActionPacket.ACTION_STOP_GLIDE ->
                    AuthInputAction.STOP_GLIDING;
            case PlayerActionPacket.ACTION_START_CRAWLING ->
                    AuthInputAction.START_CRAWLING;
            case PlayerActionPacket.ACTION_STOP_CRAWLING ->
                    AuthInputAction.STOP_CRAWLING;
            case PlayerActionPacket.ACTION_START_FLYING ->
                    AuthInputAction.START_FLYING;
            case PlayerActionPacket.ACTION_STOP_FLYING ->
                    AuthInputAction.STOP_FLYING;
            case PlayerActionPacket.ACTION_HANDLED_TELEPORT ->
                    AuthInputAction.HANDLE_TELEPORT;
            case PlayerActionPacket.ACTION_START_SPIN_ATTACK ->
                    AuthInputAction.START_SPIN_ATTACK;
            case PlayerActionPacket.ACTION_STOP_SPIN_ATTACK ->
                    AuthInputAction.STOP_SPIN_ATTACK;
            default -> null;
        };
        player.ghostMovementBridgeState.queueLegacyInputAction(action);
        if (packet.action == PlayerActionPacket.ACTION_START_SNEAK) {
            player.entityContext.actorDataFlagComponent.set(
                    cn.nukkit.entity.Entity.DATA_FLAG_SNEAKING, true);
            player.entityContext.synchedActorDataComponent.setFlag(
                    cn.nukkit.entity.Entity.DATA_FLAG_SNEAKING, true);
        } else if (packet.action == PlayerActionPacket.ACTION_STOP_SNEAK) {
            player.entityContext.actorDataFlagComponent.set(
                    cn.nukkit.entity.Entity.DATA_FLAG_SNEAKING, false);
            player.entityContext.synchedActorDataComponent.setFlag(
                    cn.nukkit.entity.Entity.DATA_FLAG_SNEAKING, false);
        }
        if (packet.action == PlayerActionPacket.ACTION_JUMP) {
            player.ghostMovementBridgeState.queueLegacyInputAction(
                    AuthInputAction.JUMPING);
        }
    }

    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void captureLegacyPlayerInput(final DataPacketReceiveEvent event) {
        if (!(event.getPacket() instanceof PlayerInputPacket packet)) {
            return;
        }
        final GhostPlayer player = this.player(event.getPlayer());
        if (player == null || player.isExempted()
                || player.getSession().isMovementServerAuthoritative()) {
            return;
        }
        player.ghostMovementBridgeState.updateLegacyButtonState(
                packet.jumping, packet.sneaking);
        player.entityContext.actorDataFlagComponent.set(
                cn.nukkit.entity.Entity.DATA_FLAG_SNEAKING,
                packet.sneaking);
        player.entityContext.synchedActorDataComponent.setFlag(
                cn.nukkit.entity.Entity.DATA_FLAG_SNEAKING,
                packet.sneaking);
    }

    @EventHandler
    public void handle(final DataPacketSendEvent event) {
        final GhostPlayer player = this.player(event.getPlayer());
        if (player == null || player.isExempted()) {
            return;
        }

        if (event.getPacket() instanceof ChangeDimensionPacket packet) {
            player.latencyAdapter.sendLatencyStack();
            player.latencyAdapter.latencyUtil().queue(() -> {
                if (player.packetVisibleChunkCache.getDimension() != packet.dimension) {
                    player.entityContext.playerLoadingScreenComponent.screenId = packet.loadingScreenId;
                    player.entityContext.playerLoadingScreenComponent.active = true;
                    player.entityContext.playerLoadingScreenComponent.ticksSinceChange = 0;
                    player.entityContext.playerLoadingScreenComponent.initialFallbackDeadlineNs = 0L;
                }
                player.packetVisibleChunkCache.clearChunks();
                player.packetVisibleChunkCache.setDimension(packet.dimension);
                player.entityContext.actorDataFlagComponent.clear();
                player.entityContext.synchedActorDataComponent.clear();
                NukkitItemUseStateSystem.reset(player);
                player.entityContext.isHorizontalPoseFlagComponent.setPresent(false);
                player.entityContext.abilitiesRequestComponent.clear();
                player.entityContext.permissionFlyFlagComponent.clear();
                player.entityContext.abilitiesComponent.setBoolean(
                        AbilitiesComponent.FLYING, false);
                UpdateAbilitiesSystem.tick(player);
            });
        }

        if (!(event.getPacket() instanceof MovePlayerPacket packet)
                || packet.eid != player.runtimeEntityId
                || packet.mode == MovePlayerPacket.MODE_PITCH) {
            return;
        }

        







        if (isLegacyInitialPositionPacket(
                player.getSession().protocol, event.getPlayer().spawned)) {
            rebaselineLegacyMove(player, packet);
            player.getTeleportUtil().acceptPredictedMovementPosition(
                    player.entityContext.stateVectorComponent
                            .getPosition().clone());
            return;
        }

        
        
        
        if (packet.mode == MovePlayerPacket.MODE_NORMAL) {
            packet.mode = MovePlayerPacket.MODE_TELEPORT;
        }

        final TeleportData data = TeleportData.fromPacket(packet);
        player.getTeleportUtil().queue(data);
    }

    static boolean isLegacyInitialPositionPacket(final int protocol,
                                                 final boolean spawned) {
        return BedrockProtocolCapabilities.usesLegacyLoadingMovementGate(protocol)
                && !spawned;
    }

    private GhostPlayer player(final Player nukkitPlayer) {
        return nukkitPlayer == null ? null
                : Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
    }

    private static void resetTransientInputState(final GhostPlayer player) {
        
        
        player.entityContext.riptideTridentSpinAttackComponent.resetTickFlags();
        player.entityContext.serverPlayerInventoryTransactionComponent.processing = false;
    }

    private static void processQueuedTeleports(final GhostPlayer player,
                                               final PlayerAuthInputPacket packet) {
        final int protocol = player.getSession().protocol;
        final boolean serverAuthoritative =
                player.getSession().isMovementServerAuthoritative();

        TeleportData data;
        while ((data = player.getTeleportUtil().getQueuedTeleports().peek())
                != null) {
            final float distanceSquared = teleportDistanceSquared(data, packet);

            







            if (!serverAuthoritative) {
                if (!shouldAcceptPositionTeleport(distanceSquared)) {
                    player.getTeleportUtil().resendPending(data);
                    break;
                }

                player.getTeleportUtil().getQueuedTeleports().poll();
                processTeleport(player, data, packet);
                continue;
            }

            







            if (BedrockProtocolCapabilities.usesPreHandleTeleportAuthInput(
                    protocol)) {
                if (!shouldAcceptPositionTeleport(distanceSquared)) {
                    if (data.isAccepted()) {
                        player.getTeleportUtil().resendPending(data);
                    }
                    break;
                }

                player.getTeleportUtil().getQueuedTeleports().poll();
                processTeleport(player, data, packet);
                continue;
            }

            




            if (!data.isAccepted()) {
                break;
            }

            player.getTeleportUtil().getQueuedTeleports().poll();
            processTeleport(player, data, packet);
        }
    }

    private static float teleportDistanceSquared(
            final TeleportData data,
            final PlayerAuthInputPacket packet) {
        final Vec3 target = data.getPosition();
        final float deltaX = packet.getPosition().getX() - target.x;
        final float deltaY = packet.getPosition().getY() - target.y;
        final float deltaZ = packet.getPosition().getZ() - target.z;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    private static float teleportDistance(final TeleportData data,
                                          final PlayerAuthInputPacket packet) {
        return (float) Math.sqrt(teleportDistanceSquared(data, packet));
    }

    private static void processTeleport(final GhostPlayer player,
                                        final TeleportData data,
                                        final PlayerAuthInputPacket packet) {
        final float distance = teleportDistance(data, packet);

        final boolean handled = packet.getInputData()
                .contains(AuthInputAction.HANDLE_TELEPORT);

        
        
        TeleportToSystem.tick(player, data);
        player.getTeleportUtil().onAccepted();

        
        
        
        if (shouldResendTeleport(player.getSession().protocol, handled,
                distance, player.getTeleportUtil().isHardTeleporting())) {
            
            
            player.getTeleportUtil().resend(data);
        }
    }

    static boolean shouldResendTeleport(final int protocol,
                                        final boolean handled,
                                        final float distance,
                                        final boolean hardTeleporting) {
        if (hardTeleporting
                || !BedrockProtocolCapabilities.hasHandleTeleportAuthInput(protocol)) {
            return false;
        }
        return !handled || distance > 1.0E-3F;
    }

    static boolean shouldRunPrediction(final int protocol,
                                       final boolean loadingActive,
                                       final int ticksSinceLoadingChange,
                                       final float tickEndLengthSquared) {
        if (BedrockProtocolCapabilities.usesLegacyLoadingMovementGate(protocol)) {
            
            
            
            return !loadingActive && ticksSinceLoadingChange >= 2;
        }
        return (!loadingActive && ticksSinceLoadingChange >= 2)
                || tickEndLengthSquared > 0.0F;
    }

    static boolean shouldTrackUnloadedChunk(final int protocol,
                                            final boolean loadingActive,
                                            final int ticksSinceLoadingChange) {
        return !BedrockProtocolCapabilities.usesLegacyLoadingMovementGate(protocol)
                || (!loadingActive && ticksSinceLoadingChange >= 2);
    }

    static boolean shouldAcceptPositionTeleport(final float distanceSquared) {
        return Float.isFinite(distanceSquared)
                && distanceSquared <= NUKKIT_FORCE_MOVEMENT_DISTANCE_SQUARED;
    }

    private static void processExempted(final GhostPlayer player) {
        ActorSetPosSystem.setImmediate(player,
                player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedPosition(), true);
        player.entityContext.forceSendMotionPacketComponent.clear();
        player.entityContext.serverPlayerCurrentMovementComponent.setPredictionResult(new PredictionData(
                Vec3.ZERO,
                player.entityContext.stateVectorComponent.getDelta().y < 0.0F
                        && player.entityContext.playerActionComponent.actions().contains(
                        AuthInputAction.VERTICAL_COLLISION)
                        ? new Vec3(0.0F, 1.0F, 0.0F) : Vec3.ZERO,
                player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedTickEnd()));
        player.entityContext.stateVectorComponent.setDelta(player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedTickEnd().clone());
    }
}
