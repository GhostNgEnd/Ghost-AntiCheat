package ac.ghost.anticheat.prediction.nukkit.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.nukkit.component.NukkitItemUseStateComponent;
import ac.ghost.anticheat.util.ItemUtil;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;








public final class NukkitItemUseStateSystem {
    private NukkitItemUseStateSystem() {
    }

    public static boolean isPredictionUsingItem(final GhostPlayer player) {
        return player.ghostMovementBridgeState.nukkitItemUseStateComponent.isPredictionUsingItem();
    }

    public static boolean isUsingSpear(final GhostPlayer player) {
        return ItemUtil.isSpear(player, player.entityContext.itemInUseComponent.getItem());
    }

    public static void postTick(final GhostPlayer player) {
        final NukkitItemUseStateComponent bridge = player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        if (!player.entityContext.itemInUseComponent.isPresent()) {
            if (bridge.getPendingUseSource()
                    == NukkitItemUseStateComponent.PendingUseSource.INVENTORY_TRANSACTION
                    || bridge.isAwaitingMetadataStartConfirmation()) {
                return;
            }
            if (!player.entityContext.itemInUseComponent.getItem().isNull()) {
                releaseState(player, false);
            }
            return;
        }
        releaseIfHeldItemChanged(player);
    }

    public static void onMetadataUseStateChanged(final GhostPlayer player,
                                                 final boolean oldUsing,
                                                 final boolean newUsing) {
        final NukkitItemUseStateComponent bridge = player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        if (newUsing) {
            bridge.setAwaitingMetadataStartConfirmation(false);
        } else if (player.getSession().isUsingItem()
                && !player.entityContext.itemInUseComponent.getItem().isNull()) {
            bridge.setAwaitingMetadataStartConfirmation(true);
            bridge.setPendingUseSource(
                    NukkitItemUseStateComponent.PendingUseSource.INVENTORY_TRANSACTION);
        }
        if (oldUsing == newUsing) {
            return;
        }
    }

    public static void onMovementMetadataSent(final GhostPlayer player,
                                              final boolean using) {
        final NukkitItemUseStateComponent bridge = player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        final long inputTick = player.entityContext.serverPlayerMovementComponent.getCurrentInputTick();
        if (using) {
            bridge.setLastMetadataTrueSentPlayerTick(inputTick);
            return;
        }

        bridge.incrementPendingMetadataFalsePackets();
        if (bridge.getLastMetadataTrueSentPlayerTick() == inputTick
                && !player.entityContext.itemInUseComponent.getItem().isNull()) {
            bridge.setAwaitingMetadataStartConfirmation(true);
        }
    }

    public static void onMovementMetadataAcknowledged(final GhostPlayer player,
                                                      final boolean using) {
        final NukkitItemUseStateComponent bridge = player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        if (using) {
            bridge.setAwaitingMetadataStartConfirmation(false);
            return;
        }

        bridge.decrementPendingMetadataFalsePackets();
        bridge.setLastMetadataFalseAckPlayerTick(
                player.entityContext.serverPlayerMovementComponent.getCurrentInputTick());
        if (player.getSession().isUsingItem()
                && !player.entityContext.itemInUseComponent.getItem().isNull()) {
            bridge.setAwaitingMetadataStartConfirmation(true);
            bridge.setPendingUseSource(
                    NukkitItemUseStateComponent.PendingUseSource.INVENTORY_TRANSACTION);
        } else {
            bridge.setAwaitingMetadataStartConfirmation(false);
        }
    }

    public static void snapshotPredictionUseState(final GhostPlayer player) {
        releaseIfHeldItemChanged(player);
        player.ghostMovementBridgeState.nukkitItemUseStateComponent.setPredictionUsingItem(
                player.entityContext.itemInUseComponent.isPresent()
                        && !player.ghostMovementBridgeState.nukkitItemUseStateComponent
                        .isAwaitingMetadataStartConfirmation());
    }

    public static void onHeldItemChanged(final GhostPlayer player,
                                         final Item newHeldItem) {
        final Item usedItem = player.entityContext.itemInUseComponent.getItem();
        if (usedItem.isNull()) {
            return;
        }

        final NukkitItemUseStateComponent bridge =
                player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        final boolean changedSlot = bridge.getUseHotbarSlot() >= 0
                && bridge.getUseHotbarSlot()
                != player.compensatedInventory.heldItemSlot;
        if (!changedSlot
                && ItemUtil.sameDefinition(player, newHeldItem, usedItem)) {
            return;
        }

        
        
        
        releaseState(player, false);
    }

    public static void release(final GhostPlayer player) {
        final NukkitItemUseStateComponent bridge = player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        bridge.setButtonReleasePending(bridge.isUseButtonLatched());
        releaseState(player, false);
    }

    








    public static void cancelUse(final GhostPlayer player) {
        if (player == null || player.getSession() == null) {
            return;
        }

        player.getSession().setUsingItem(false);
        
        
        
        player.getSession().setDataFlag(
                Entity.DATA_FLAGS, Entity.DATA_FLAG_ACTION, false);
        releaseState(player, true);
    }

    



    public static void armNoSlowConsumeRollback(final GhostPlayer player) {
        if (player == null || player.getSession() == null
                || !player.entityContext.itemInUseComponent.isPresent()) {
            return;
        }
        player.ghostMovementBridgeState.nukkitItemUseStateComponent
                .setNoSlowConsumeRollbackPending(true);
    }

    public static boolean shouldRollbackNoSlowConsume(final GhostPlayer player) {
        if (player == null || player.getSession() == null) {
            return false;
        }
        final NukkitItemUseStateComponent bridge =
                player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        return bridge.isNoSlowConsumeRollbackPending()
                && bridge.getUseHotbarSlot() >= 0
                && bridge.getUseHotbarSlot()
                == player.compensatedInventory.heldItemSlot
                && !player.entityContext.itemInUseComponent.getItem().isNull();
    }

    





    public static void keepUsingAfterNoSlowConsume(final GhostPlayer player) {
        if (player == null || player.getSession() == null) {
            return;
        }
        final NukkitItemUseStateComponent bridge =
                player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        bridge.setNoSlowConsumeRollbackPending(false);

        final Item held = player.getSession().getInventory().getItemInHand();
        if (!canBeUsed(player, held)) {
            releaseState(player, false);
            return;
        }

        
        
        player.getSession().setUsingItem(true);
        begin(player, held);
        bridge.setUseButtonLatched(true);
        bridge.setButtonReleasePending(false);
        bridge.setAwaitingMetadataStartConfirmation(false);
        bridge.setPendingUseSource(NukkitItemUseStateComponent.PendingUseSource.NONE);
        bridge.setPredictionUsingItem(true);

        
        
        player.getSession().sendData(player.getSession());
    }

    public static void finishAuthInput(final GhostPlayer player,
                                       final boolean startUsingItemPresent) {
        final NukkitItemUseStateComponent bridge = player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        if (!bridge.isButtonReleasePending()) {
            return;
        }
        bridge.setButtonReleasePending(false);
        if (!startUsingItemPresent) {
            bridge.setUseButtonLatched(false);
        }
    }

    public static boolean resumeLatchedUse(final GhostPlayer player,
                                           final Item heldItem) {
        final NukkitItemUseStateComponent bridge = player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        if (!bridge.isUseButtonLatched() || !canBeUsed(player, heldItem)) {
            return false;
        }

        begin(player, heldItem);
        bridge.setPendingUseSource(NukkitItemUseStateComponent.PendingUseSource.NONE);
        bridge.setButtonReleasePending(false);
        if (bridge.isAwaitingMetadataStartConfirmation()) {
            player.entityContext.itemInUseComponent.setPresent(false);
        }
        return true;
    }

    public static void beginFromInventoryTransaction(final GhostPlayer player,
                                                     final Item heldItem) {
        if (!canBeUsed(player, heldItem)) {
            return;
        }
        begin(player, heldItem);
        final NukkitItemUseStateComponent bridge = player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        bridge.setPendingUseSource(
                NukkitItemUseStateComponent.PendingUseSource.INVENTORY_TRANSACTION);
        bridge.setAwaitingMetadataStartConfirmation(
                bridge.getLastMetadataFalseAckPlayerTick()
                        == player.entityContext.serverPlayerMovementComponent.getCurrentInputTick());
        player.entityContext.itemInUseComponent.setPresent(false);
    }

    public static boolean beginFromAuthInput(final GhostPlayer player,
                                             final Item heldItem) {
        final NukkitItemUseStateComponent bridge = player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        if (bridge.getPendingUseSource()
                == NukkitItemUseStateComponent.PendingUseSource.NONE) {
            return resumeLatchedUse(player, heldItem);
        }
        if (!canBeUsed(player, heldItem)) {
            return false;
        }

        begin(player, heldItem);
        bridge.setUseButtonLatched(true);
        bridge.setButtonReleasePending(false);
        final boolean awaitMetadata = bridge.isAwaitingMetadataStartConfirmation();
        player.entityContext.itemInUseComponent.setPresent(!awaitMetadata);
        if (!awaitMetadata) {
            bridge.setPendingUseSource(
                    NukkitItemUseStateComponent.PendingUseSource.NONE);
        }
        return true;
    }

    public static void applyAcknowledgedMetadata(final GhostPlayer player,
                                                 final boolean using) {
        final boolean oldUsing = player.entityContext.itemInUseComponent.isPresent();
        final Item activeItem = player.entityContext.itemInUseComponent.getItem();
        if (using && !activeItem.isNull()) {
            player.entityContext.itemInUseComponent.setPresent(true);
            player.ghostMovementBridgeState.nukkitItemUseStateComponent.setPendingUseSource(
                    NukkitItemUseStateComponent.PendingUseSource.NONE);
        } else if (!using) {
            player.entityContext.itemInUseComponent.setPresent(false);
            player.ghostMovementBridgeState.nukkitItemUseStateComponent.setPendingUseSource(
                    NukkitItemUseStateComponent.PendingUseSource.NONE);
        } else {
            player.ghostMovementBridgeState.nukkitItemUseStateComponent.setPendingUseSource(
                    NukkitItemUseStateComponent.PendingUseSource.METADATA);
        }
        onMetadataUseStateChanged(player, oldUsing,
                player.entityContext.itemInUseComponent.isPresent());
    }

    public static void reset(final GhostPlayer player) {
        player.entityContext.itemInUseComponent.clear();
        player.entityContext.itemInUseTicksDuringMovementComponent.clear();
        player.entityContext.itemUseSlowdownModifierComponent.clear();
        player.ghostMovementBridgeState.nukkitItemUseStateComponent.reset();
    }

    private static void begin(final GhostPlayer player, final Item item) {
        player.ghostMovementBridgeState.nukkitItemUseStateComponent
                .setNoSlowConsumeRollbackPending(false);
        player.entityContext.itemInUseComponent.begin(item, ItemUtil.useDurationTicks(player, item));
        player.entityContext.itemInUseTicksDuringMovementComponent.clear();
        player.ghostMovementBridgeState.nukkitItemUseStateComponent.setUseHotbarSlot(
                player.compensatedInventory.heldItemSlot);
    }

    private static boolean releaseIfHeldItemChanged(final GhostPlayer player) {
        final Item usedItem = player.entityContext.itemInUseComponent.getItem();
        if (usedItem.isNull()) {
            return false;
        }

        final NukkitItemUseStateComponent bridge =
                player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        final boolean changedSlot = bridge.getUseHotbarSlot() >= 0
                && bridge.getUseHotbarSlot()
                != player.compensatedInventory.heldItemSlot;
        final Item heldItem = player.compensatedInventory.inventoryContainer
                .getHeldItemData();
        if (changedSlot || heldItem == null || heldItem.isNull()
                || !ItemUtil.sameDefinition(player, heldItem, usedItem)) {
            releaseState(player, false);
            return true;
        }
        return false;
    }

    private static void releaseState(final GhostPlayer player,
                                     final boolean clearButtonLatch) {
        player.entityContext.itemInUseComponent.clear();
        player.entityContext.itemInUseTicksDuringMovementComponent.clear();
        player.entityContext.itemUseSlowdownModifierComponent.clear();
        final NukkitItemUseStateComponent bridge = player.ghostMovementBridgeState.nukkitItemUseStateComponent;
        bridge.setPendingUseSource(NukkitItemUseStateComponent.PendingUseSource.NONE);
        bridge.setAwaitingMetadataStartConfirmation(false);
        bridge.setPredictionUsingItem(false);
        bridge.setUseHotbarSlot(-1);
        bridge.setNoSlowConsumeRollbackPending(false);
        if (clearButtonLatch) {
            bridge.setUseButtonLatched(false);
            bridge.setButtonReleasePending(false);
        }
    }

    private static boolean canBeUsed(final GhostPlayer player,
                                     final Item item) {
        if (item == null || item.isNull()) {
            return false;
        }
        if (ItemUtil.hasUseDurationComponent(player, item)
                || item.canRelease() || item.getUseDuration() > 0) {
            return true;
        }
        final int itemId = item.getId();
        return itemId == ItemID.BOW || itemId == ItemID.CROSSBOW
                || itemId == ItemID.TRIDENT || itemId == ItemID.ENDER_EYE
                || itemId == ItemID.SPYGLASS || itemId == ItemID.POTION
                || ItemUtil.identifierEquals(player, item,
                "minecraft:ominous_bottle");
    }
}
