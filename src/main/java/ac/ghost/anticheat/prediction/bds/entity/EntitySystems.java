package ac.ghost.anticheat.prediction.bds.entity;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.nukkit.data.ReplayableActorInput;
import ac.ghost.anticheat.prediction.bds.component.ServerPlayerMovementComponent;
import ac.ghost.anticheat.prediction.bds.system.input.PlayerInputFilterServerSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.AccumulateHistory;
import ac.ghost.anticheat.prediction.bds.system.movement.AddMovementTickNeededForCatchup;
import ac.ghost.anticheat.prediction.bds.system.movement.DiscardHistory;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;


public final class EntitySystems {
    private EntitySystems() {
    }

    public static ServerPlayerMovementComponent.Acceptance acceptPlayerAuthInput(
            final EntityContext entity,
            final PlayerAuthInputPacket packet) {
        return entity.serverPlayerMovementComponent.addPlayerAuthInputPacket(packet);
    }

    public static ReplayableActorInput beginPlayerAuthInput(final EntityContext entity,
                                                            final long inputTick) {
        final GhostPlayer player = entity.externalDataComponent.player();
        final ServerPlayerMovementComponent.HistoryRecord record =
                entity.serverPlayerMovementComponent.beginProcessing(inputTick);
        DiscardHistory.tick(player);
        entity.serverPlayerMovementComponent.capturePreSimulationState(player);
        entity.replayStateComponent.replace(record.replayState());
        entity.antiCheatRewindFlagComponent.setPresent(false);

        AddMovementTickNeededForCatchup.tick(player, inputTick);
        PlayerInputFilterServerSystem.onEntityChanged(player);
        return record.input();
    }

    public static void completePlayerAuthInput(final EntityContext entity,
                                               final long inputTick) {
        final GhostPlayer player = entity.externalDataComponent.player();
        AccumulateHistory.tick(player);
        final ServerPlayerMovementComponent.HistoryRecord record =
                entity.serverPlayerMovementComponent.processingRecord();
        if (record != null && record.inputTick() == inputTick) {
            record.setReplayStateTracker(entity.replayStateTrackerComponent.copy());
        }
        entity.serverPlayerMovementComponent.completeProcessing(inputTick);
        entity.actorMovementTickNeededComponent.clear();
        entity.moveRequestComponent.clear();
        entity.applyReplayStateTrackerRequestComponent.clear();
        entity.antiCheatRewindFlagComponent.setPresent(false);
    }

    public static void resetPlayerMovement(final EntityContext entity) {
        entity.serverPlayerMovementComponent.reset();
        entity.serverPlayerMovementSyncComponent.reset();
        entity.clientAcceptanceThresholdsComponent.setPositionThresholdEnabled(false);
        entity.replayStateComponent.clear();
        entity.replayStateTrackerComponent.clear();
        entity.actorDataDirtyFlagsComponent.clear();
        entity.applyReplayStateTrackerRequestComponent.clear();
        entity.actorMovementTickNeededComponent.clear();
        entity.moveRequestComponent.clear();
        entity.antiCheatRewindFlagComponent.setPresent(false);
    }
}
