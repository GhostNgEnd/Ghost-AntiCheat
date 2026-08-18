package ac.ghost.anticheat.prediction.bds.system.player;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import ac.ghost.anticheat.prediction.bds.component.PlayerActionComponent;


public final class ActionSprintTriggerSystem {
    private ActionSprintTriggerSystem() {
    }

    




    public static boolean start(final GhostPlayer player) {
        if (!player.entityContext.playerActionComponent.has(PlayerActionComponent.START_SPRINTING)) {
            return true;
        }
        setSprinting(player, true);
        return true;
    }

    
    public static void stop(final GhostPlayer player) {
        if (player.entityContext.playerActionComponent.has(PlayerActionComponent.STOP_SPRINTING)) {
            setSprinting(player, false);
        }
    }

    



    public static void tick(final GhostPlayer player) {
        start(player);
        stop(player);
    }

    public static void setSprinting(final GhostPlayer player,
                                    final boolean sprinting) {
        player.entityContext.actorDataFlagComponent.set(ActorDataFlag.SPRINTING, sprinting);
        player.entityContext.synchedActorDataComponent.setFlag(ActorDataFlag.SPRINTING, sprinting);
        player.entityContext.attributesComponent.applySprintingModifier(sprinting);
    }
}
