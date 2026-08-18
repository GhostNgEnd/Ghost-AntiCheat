package ac.ghost.anticheat.prediction.bds.system.player;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.entity.EntityContext;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import ac.ghost.anticheat.prediction.bds.component.PlayerActionComponent;
import cn.nukkit.item.ItemID;





public final class StartGlidingActionServerSystem {
    private StartGlidingActionServerSystem() {
    }

    public static boolean hasElytraEquipped(final EntityContext entity) {
        final GhostPlayer player = entity.externalDataComponent.player();
        final cn.nukkit.item.Item chest = player.compensatedInventory
                .armorContainer.get(1).getData();
        return chest != null && chest.getId() == ItemID.ELYTRA;
    }

    public static boolean tick(final EntityContext entity) {
        if (!entity.playerActionComponent.has(PlayerActionComponent.START_GLIDING)) {
            return true;
        }
        entity.actorDataFlagComponent.set(ActorDataFlag.GLIDING,
                hasElytraEquipped(entity));
        return entity.actorDataFlagComponent.has(ActorDataFlag.GLIDING);
    }
}
