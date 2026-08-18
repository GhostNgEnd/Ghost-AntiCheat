package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.movement.UpdateHorizontalPoseSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.PlayerBoundingBoxStateUpdateSystem;
import cn.nukkit.entity.Entity;


public final class ApplyReplayStateTrackerRequestComponent {
    private ReplayStateTrackerComponent tracker;

    public boolean isPresent() {
        return tracker != null;
    }

    public void set(final ReplayStateTrackerComponent tracker) {
        this.tracker = tracker == null ? null : tracker.copy();
    }

    public ReplayStateTrackerComponent tracker() {
        return tracker == null ? null : tracker.copy();
    }

    public void clear() {
        tracker = null;
    }

    
    public void apply(final GhostPlayer player) {
        if (!isPresent()) {
            return;
        }

        final ReplayStateTrackerComponent requestedTracker = tracker();
        player.entityContext.actorDataFlagComponent.applyMasked(
                requestedTracker.actorFlags0(), requestedTracker.actorFlags1(),
                requestedTracker.actorFlags2(),
                requestedTracker.changedActorFlags0(),
                requestedTracker.changedActorFlags1(),
                requestedTracker.changedActorFlags2());
        player.entityContext.actorDataHorseFlagComponent.setFlags(
                player.entityContext.actorDataHorseFlagComponent.getFlags()
                        ^ ((player.entityContext.actorDataHorseFlagComponent.getFlags()
                        ^ requestedTracker.horseFlags())
                        & requestedTracker.changedHorseFlags()));

        if (requestedTracker.jumpDurationChanged()) {
            player.entityContext.actorDataJumpDurationComponent.setDuration(
                    requestedTracker.jumpDuration());
        }

        UpdateHorizontalPoseSystem.tick(player.entityContext);
        PlayerBoundingBoxStateUpdateSystem.tick(player.entityContext);
        if (requestedTracker.boundingBoxChanged()) {
            player.entityContext.actorDataBoundingBoxComponent.setValue(
                    requestedTracker.boundingBox());
            player.entityContext.actorDataBoundingBoxComponent.writeTo(player);
        }
        if (requestedTracker.seatOffsetChanged()) {
            player.entityContext.actorDataSeatOffsetComponent.setValue(
                    requestedTracker.seatOffset());
        }

        player.entityContext.replayStateTrackerComponent.replace(requestedTracker);
        player.entityContext.actorDataDirtyFlagsComponent.markActorFlags(
                requestedTracker.changedActorFlags0(),
                requestedTracker.changedActorFlags1(),
                requestedTracker.changedActorFlags2());
        player.entityContext.actorDataDirtyFlagsComponent.markHorseFlags(
                requestedTracker.changedHorseFlags());
        if (requestedTracker.jumpDurationChanged()) {
            player.entityContext.actorDataDirtyFlagsComponent.markAuxiliary(1L);
        }
        if (requestedTracker.boundingBoxChanged()) {
            player.entityContext.actorDataDirtyFlagsComponent.markAuxiliary(1L << 1);
        }
        if (requestedTracker.seatOffsetChanged()) {
            player.entityContext.actorDataDirtyFlagsComponent.markAuxiliary(1L << 2);
        }

        player.entityContext.attributesComponent.applySprintingModifier(
                player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_SPRINTING));
        clear();
    }
}
