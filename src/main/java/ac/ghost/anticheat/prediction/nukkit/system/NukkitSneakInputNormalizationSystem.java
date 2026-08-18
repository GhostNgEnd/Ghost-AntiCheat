package ac.ghost.anticheat.prediction.nukkit.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.types.InputMode;

import java.util.Set;

















public final class NukkitSneakInputNormalizationSystem {
    private NukkitSneakInputNormalizationSystem() {
    }

    public static void tick(final GhostPlayer player, final InputMode inputMode) {
        if (inputMode != InputMode.MOUSE) {
            player.ghostMovementBridgeState.nukkitSneakInputStateComponent.reset();
            return;
        }

        final Set<AuthInputAction> actions = player.entityContext.playerActionComponent.actions();
        final boolean rawPressed = actions.contains(AuthInputAction.SNEAK_PRESSED_RAW);
        final boolean rawReleased = actions.contains(AuthInputAction.SNEAK_RELEASED_RAW);
        final boolean rawCurrent = actions.contains(AuthInputAction.SNEAK_CURRENT_RAW);
        final boolean protocolHeldState = actions.contains(AuthInputAction.SNEAKING);

        
        
        
        final boolean rawSneaking;
        if (rawReleased) {
            rawSneaking = false;
        } else if (rawPressed) {
            rawSneaking = true;
        } else {
            rawSneaking = rawCurrent || protocolHeldState;
        }

        player.ghostMovementBridgeState.nukkitSneakInputStateComponent.setRawSneaking(rawSneaking);
        player.entityContext.actorDataFlagComponent.set(ActorDataFlag.SNEAKING, rawSneaking);

        
        
        if (rawSneaking) {
            actions.remove(AuthInputAction.STOP_SNEAKING);
        } else {
            actions.remove(AuthInputAction.START_SNEAKING);
        }
        player.entityContext.playerActionComponent.rebuildStateActionBits();
    }
}
