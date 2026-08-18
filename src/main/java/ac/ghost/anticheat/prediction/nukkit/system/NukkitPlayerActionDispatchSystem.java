package ac.ghost.anticheat.prediction.nukkit.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import ac.ghost.anticheat.prediction.bds.component.PlayerActionComponent;
import ac.ghost.anticheat.prediction.bds.system.movement.UpdateHorizontalPoseSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.PlayerBoundingBoxStateUpdateSystem;
import ac.ghost.anticheat.prediction.bds.system.player.SneakTriggerActionSystem;
import ac.ghost.anticheat.prediction.bds.system.player.StartGlidingActionServerSystem;
import ac.ghost.anticheat.prediction.bds.system.player.StopGlidingActionServerSystem;
import ac.ghost.anticheat.prediction.nukkit.inventory.NukkitItemUseAdapter;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.AuthInputAction;

import java.util.Iterator;









public final class NukkitPlayerActionDispatchSystem {
    private NukkitPlayerActionDispatchSystem() {
    }

    public static void tick(final GhostPlayer player) {
        if (!player.entityContext.itemInUseComponent.isPresent()) {
            player.entityContext.itemInUseComponent.setTridentUseTicks(0);
        }

        if (player.entityContext.actorDataFlagComponent.has(ActorDataFlag.SWIMMING)) {
            player.ghostMovementBridgeState.ticksSinceSwimming++;
        } else {
            player.ghostMovementBridgeState.ticksSinceSwimming = 0;
        }

        if (player.entityContext.actorDataFlagComponent.has(ActorDataFlag.CRAWLING)) {
            player.ghostMovementBridgeState.ticksSinceCrawling++;
        } else {
            player.ghostMovementBridgeState.ticksSinceCrawling = 0;
        }

        final boolean startUsingItemPresent = player.entityContext.playerActionComponent.actions()
                .contains(AuthInputAction.START_USING_ITEM);
        final Iterator<AuthInputAction> iterator = player.entityContext.playerActionComponent.actions().iterator();
        while (iterator.hasNext()) {
            final AuthInputAction input = iterator.next();
            switch (input) {
                case START_GLIDING -> {
                    if (!StartGlidingActionServerSystem.tick(player.entityContext)) {
                        iterator.remove();
                        player.entityContext.playerActionComponent.clear(PlayerActionComponent.START_GLIDING);
                    }
                }
                case STOP_GLIDING -> StopGlidingActionServerSystem.tick(player);
                
                
                case START_SPRINTING, STOP_SPRINTING -> { }
                
                
                case START_SNEAKING, STOP_SNEAKING -> { }
                case START_SWIMMING -> SneakTriggerActionSystem.startSwimming(player);
                case STOP_SWIMMING -> SneakTriggerActionSystem.stopSwimming(player);
                case START_FLYING, STOP_FLYING -> { }
                case STOP_SPIN_ATTACK -> {
                    if (player.entityContext.riptideTridentSpinAttackComponent.stopRequested()) {
                        ac.ghost.anticheat.prediction.bds.system.spinattack.StoreSpinAttackResultSystem.stop(player);
                        player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().multiply(-0.2F));
                    } else {
                        iterator.remove();
                    }
                }
                case START_USING_ITEM -> {
                    final Item item = player.compensatedInventory.inventoryContainer
                            .getHeldItemData();
                    if (!NukkitItemUseStateSystem.beginFromAuthInput(player, item)) {
                        iterator.remove();
                    }
                }
                case START_CRAWLING -> SneakTriggerActionSystem.startCrawling(player);
                case STOP_CRAWLING -> SneakTriggerActionSystem.stopCrawling(player);
            }
        }

        SneakTriggerActionSystem.tickSneaking(player);
        NukkitItemUseStateSystem.finishAuthInput(player, startUsingItemPresent);
        UpdateHorizontalPoseSystem.tick(player.entityContext);
        PlayerBoundingBoxStateUpdateSystem.tick(player.entityContext);

        if (player.ghostMovementBridgeState.nukkitItemUseStateComponent.getPendingUseSource()
                == ac.ghost.anticheat.prediction.nukkit.component.NukkitItemUseStateComponent.PendingUseSource.METADATA
                && !player.entityContext.itemInUseComponent.isPresent()) {
            NukkitItemUseAdapter.releaseItem(player.getSession());
            player.ghostMovementBridgeState.nukkitItemUseStateComponent.setPendingUseSource(
                    ac.ghost.anticheat.prediction.nukkit.component.NukkitItemUseStateComponent.PendingUseSource.NONE);
        }
        player.entityContext.riptideTridentSpinAttackComponent.setStopRequested(false);
    }
}
