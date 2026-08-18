package ac.ghost.anticheat.check.impl.multiactions;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.nukkit.component.NukkitItemUseStateComponent;
import ac.ghost.anticheat.util.ItemUtil;
import cn.nukkit.item.Item;


final class MultiActionUtil {
    private MultiActionUtil() {
    }

    







    static boolean isUsingSlowdownItem(final GhostPlayer player) {
        final Item usedItem = player.entityContext.itemInUseComponent.getItem();
        if (usedItem == null || usedItem.isNull()) {
            return false;
        }

        final NukkitItemUseStateComponent bridge =
                player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        final boolean active = player.entityContext.itemInUseComponent.isPresent()
                || bridge.getPendingUseSource()
                == NukkitItemUseStateComponent.PendingUseSource.INVENTORY_TRANSACTION
                || bridge.isUseButtonLatched();
        if (!active) {
            return false;
        }

        final int useSlot = bridge.getUseHotbarSlot();
        if (useSlot >= 0 && useSlot != player.compensatedInventory.heldItemSlot) {
            return false;
        }

        final Item heldItem = player.compensatedInventory.inventoryContainer.getHeldItemData();
        if (heldItem == null || heldItem.isNull()
                || !ItemUtil.sameDefinition(player, heldItem, usedItem)) {
            return false;
        }

        final float modifier = ItemUtil.itemUseSlowdownModifier(player, usedItem);
        return Float.isFinite(modifier) && modifier < 1.0F;
    }

    static String usedItemName(final GhostPlayer player) {
        final String identifier = ItemUtil.identifier(
                player, player.entityContext.itemInUseComponent.getItem());
        return identifier == null ? "unknown" : identifier;
    }
}
