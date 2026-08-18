package ac.ghost.anticheat.prediction.bds.system.player;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import ac.ghost.anticheat.prediction.bds.component.PlayerActionComponent;
import ac.ghost.anticheat.prediction.bds.system.movement.UpdateHorizontalPoseSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.PlayerBoundingBoxStateUpdateSystem;







public final class SneakTriggerActionSystem {
    private SneakTriggerActionSystem() {
    }

    public static void tickSneaking(final GhostPlayer player) {
        if (player.entityContext.playerActionComponent.has(PlayerActionComponent.START_SNEAKING)) {
            player.entityContext.actorDataFlagComponent.set(ActorDataFlag.SNEAKING, true);
        }
        if (player.entityContext.playerActionComponent.has(PlayerActionComponent.STOP_SNEAKING)) {
            player.entityContext.actorDataFlagComponent.set(ActorDataFlag.SNEAKING, false);
        }
    }

    public static void startSwimming(final GhostPlayer player) {
        if (!player.entityContext.playerActionComponent.has(PlayerActionComponent.START_SWIMMING)) {
            return;
        }
        player.entityContext.actorDataFlagComponent.set(ActorDataFlag.CRAWLING, false);
        player.ghostMovementBridgeState.ticksSinceCrawling = 0;
        setSwimmingState(player, true);
    }

    public static void stopSwimming(final GhostPlayer player) {
        if (player.entityContext.playerActionComponent.has(PlayerActionComponent.STOP_SWIMMING)) {
            setSwimmingState(player, false);
        }
    }

    private static void setSwimmingState(final GhostPlayer player,
                                         final boolean swimming) {
        player.entityContext.actorDataFlagComponent.set(ActorDataFlag.SWIMMING, swimming);
        UpdateHorizontalPoseSystem.tick(player.entityContext);
        PlayerBoundingBoxStateUpdateSystem.tick(player.entityContext);
    }

    public static void startCrawling(final GhostPlayer player) {
        if (player.entityContext.playerActionComponent.has(PlayerActionComponent.START_CRAWLING)) {
            player.entityContext.actorDataFlagComponent.set(ActorDataFlag.CRAWLING, true);
        }
    }

    public static void stopCrawling(final GhostPlayer player) {
        if (player.entityContext.playerActionComponent.has(PlayerActionComponent.STOP_CRAWLING)) {
            player.entityContext.actorDataFlagComponent.set(ActorDataFlag.CRAWLING, false);
        }
    }
}
