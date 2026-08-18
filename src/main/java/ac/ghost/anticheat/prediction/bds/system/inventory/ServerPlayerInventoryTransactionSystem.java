package ac.ghost.anticheat.prediction.bds.system.inventory;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.compensated.cache.container.ContainerCache;
import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.entity.EntityContext;
import ac.ghost.anticheat.prediction.bds.inventory.InventoryTransactionError;
import ac.ghost.anticheat.prediction.nukkit.inventory.NukkitInventoryTransactionAdapter;
import ac.ghost.anticheat.prediction.nukkit.inventory.NukkitInventoryTransactionDataAdapter;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;
import ac.ghost.anticheat.util.ItemUtil;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.types.GameType;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;







public final class ServerPlayerInventoryTransactionSystem {
    private static final int MAX_ACTIONS = 100;
    private static final float MAX_REPORTED_FROM_POSITION_DISTANCE = 6.0F;
    private static final float DIRECTION_EPSILON = 1.0E-4F;
    private static final float TARGET_AABB_INFLATION = 0.5F;

    private final EntityContext entity;
    private final GhostPlayer player;
    private final NukkitInventoryTransactionAdapter nukkitAdapter;

    public ServerPlayerInventoryTransactionSystem(final EntityContext entity) {
        this.entity = entity;
        this.player = entity.externalDataComponent.player();
        this.nukkitAdapter = new NukkitInventoryTransactionAdapter(this.player);
    }

    public InventoryTransactionError handle(final InventoryTransactionPacket packet) {
        this.entity.serverPlayerInventoryTransactionComponent.processing = true;
        final InventoryTransactionError result;
        if (packet == null || !validActionCount(packet.actions)) {
            result = InventoryTransactionError.BALANCE_MISMATCH;
        } else {
            result = switch (packet.transactionType) {
                case InventoryTransactionPacket.TYPE_NORMAL -> handleNormal(packet);
                case InventoryTransactionPacket.TYPE_RELEASE_ITEM -> handleRelease(packet);
                case InventoryTransactionPacket.TYPE_USE_ITEM -> handleUse(packet);
                case InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY ->
                        handleUseOnActor(packet);
                default -> InventoryTransactionError.NONE;
            };
        }
        this.entity.serverPlayerInventoryTransactionComponent
                .setLastInventoryTransactionError(result.value());
        return result;
    }

    private InventoryTransactionError handleNormal(final InventoryTransactionPacket packet) {
        if (isServerAuthoritativeLegacyDrop(packet.actions)) {
            return this.nukkitAdapter.handleNormal(packet)
                    ? InventoryTransactionError.NONE
                    : InventoryTransactionError.BALANCE_MISMATCH;
        }
        
        
        
        return validateActionSources(packet.actions);
    }

    private InventoryTransactionError handleRelease(final InventoryTransactionPacket packet) {
        if (!(packet.transactionData instanceof ReleaseItemData data)) {
            return InventoryTransactionError.STATE_MISMATCH;
        }
        final InventoryTransactionError actions = validateActionSources(packet.actions);
        if (!actions.isSuccess()) {
            return actions;
        }

        final int selectedSlot = NukkitInventoryTransactionDataAdapter.hotbarSlot(
                data, this.player.compensatedInventory.heldItemSlot);
        if (selectedSlot != this.player.compensatedInventory.heldItemSlot) {
            return InventoryTransactionError.STATE_MISMATCH;
        }
        final Item held = authoritativeHeldItem();
        final Item claimed = NukkitInventoryTransactionDataAdapter.itemInHand(data);
        if (!matchesClaim(held, claimed)) {
            return InventoryTransactionError.SOURCE_ITEM_MISMATCH;
        }

        if (data.actionType == InventoryTransactionPacket.RELEASE_ITEM_ACTION_RELEASE) {
            final RiptideResolution riptide = resolveRiptideRelease(held);
            if (riptide.level() > 0) {
                this.entity.serverPlayerInventoryTransactionComponent.queueRiptide(
                        this.entity.itemInUseComponent.getTridentUseTicks(),
                        riptide.level(), riptide.item());
            }
            NukkitItemUseStateSystem.release(this.player);
        }
        return InventoryTransactionError.NONE;
    }

    private InventoryTransactionError handleUse(final InventoryTransactionPacket packet) {
        if (!(packet.transactionData instanceof UseItemData data)) {
            return InventoryTransactionError.STATE_MISMATCH;
        }
        final int selectedSlot = data.hotbarSlot;
        if (selectedSlot < 0 || selectedSlot > 8
                || selectedSlot != this.player.compensatedInventory.heldItemSlot) {
            return InventoryTransactionError.STATE_MISMATCH;
        }
        final InventoryTransactionError actions = validateActionSources(packet.actions);
        if (!actions.isSuccess()) {
            return actions;
        }

        final Item held = authoritativeHeldItem();
        if (!matchesClaim(held, data.itemInHand)) {
            return InventoryTransactionError.SOURCE_ITEM_MISMATCH;
        }
        if (data.blockPos == null || !MathUtil.isValid(data.blockPos)) {
            return InventoryTransactionError.STATE_MISMATCH;
        }

        final boolean accepted = switch (data.actionType) {
            case InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR -> {
                final boolean result = this.nukkitAdapter.handleClickAir(data);
                if (result) {
                    this.nukkitAdapter.handleLegacyArmorEquip(packet, data);
                }
                yield result;
            }
            case InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK ->
                    this.nukkitAdapter.handleClickBlock(data);
            default -> true;
        };
        return accepted ? InventoryTransactionError.NONE
                : InventoryTransactionError.STATE_MISMATCH;
    }

    private InventoryTransactionError handleUseOnActor(
            final InventoryTransactionPacket packet) {
        if (!(packet.transactionData instanceof UseItemOnEntityData data)
                || !this.player.getSession().isAlive()) {
            return InventoryTransactionError.STATE_MISMATCH;
        }
        final EntityCache target = this.player.entityRegistry.getEntity(data.entityRuntimeId);
        if (target == null || target.getCurrent() == null) {
            return InventoryTransactionError.STATE_MISMATCH;
        }
        final InventoryTransactionError actions = validateActionSources(packet.actions);
        if (!actions.isSuccess()) {
            return actions;
        }

        final int selectedSlot = NukkitInventoryTransactionDataAdapter.hotbarSlot(
                data, this.player.compensatedInventory.heldItemSlot);
        if (selectedSlot != this.player.compensatedInventory.heldItemSlot) {
            return InventoryTransactionError.STATE_MISMATCH;
        }
        final Item held = authoritativeHeldItem();
        final Item claimed = NukkitInventoryTransactionDataAdapter.itemInHand(data);
        if (!matchesClaim(held, claimed)) {
            return InventoryTransactionError.SOURCE_ITEM_MISMATCH;
        }

        final Vec3 playerPosition = this.entity.stateVectorComponent.getPosition();
        final Vec3 reportedFrom = NukkitInventoryTransactionDataAdapter.fromPos(data);
        if (reportedFrom == null || playerPosition.distanceTo(reportedFrom)
                > MAX_REPORTED_FROM_POSITION_DISTANCE) {
            return InventoryTransactionError.STATE_MISMATCH;
        }

        final Vec3 targetPosition = target.getCurrent().getPos();
        final float maxPickRange = maxPickRange();
        if (targetPosition == null
                || playerPosition.distanceTo(targetPosition) > maxPickRange) {
            return InventoryTransactionError.STATE_MISMATCH;
        }

        if (Ghost.getConfig().serverAuthoritativeEntityInteractionsStrict()
                && !validInteractionDirection(target, playerPosition, maxPickRange)) {
            return InventoryTransactionError.STATE_MISMATCH;
        }
        return InventoryTransactionError.NONE;
    }


    private static boolean isServerAuthoritativeLegacyDrop(
            final NetworkInventoryAction[] actions) {
        if (actions == null || actions.length != 2) {
            return false;
        }
        boolean world = false;
        boolean container = false;
        for (final NetworkInventoryAction action : actions) {
            if (action == null) {
                return false;
            }
            if (action.sourceType == NetworkInventoryAction.SOURCE_WORLD
                    && action.inventorySlot
                    == InventoryTransactionPacket.ACTION_MAGIC_SLOT_DROP_ITEM) {
                world = true;
            } else if (action.sourceType == NetworkInventoryAction.SOURCE_CONTAINER) {
                container = true;
            }
        }
        return world && container;
    }

    private InventoryTransactionError validateActionSources(
            final NetworkInventoryAction[] actions) {
        if (actions == null || actions.length == 0) {
            return InventoryTransactionError.NONE;
        }
        if (!validActionCount(actions)) {
            return InventoryTransactionError.BALANCE_MISMATCH;
        }
        for (final NetworkInventoryAction action : actions) {
            if (action == null || action.oldItem == null || action.newItem == null
                    || action.oldItem.getCount() < 0 || action.newItem.getCount() < 0) {
                return InventoryTransactionError.BALANCE_MISMATCH;
            }
            if (action.sourceType != NetworkInventoryAction.SOURCE_CONTAINER) {
                continue;
            }
            final ContainerCache container = this.player.compensatedInventory
                    .getContainer((byte) action.windowId);
            if (container == null || action.inventorySlot < 0
                    || action.inventorySlot >= container.getContainerSize()) {
                return InventoryTransactionError.STATE_MISMATCH;
            }
            final Item serverItem = container.get(action.inventorySlot).getData();
            if (!ItemUtil.sameDefinition(this.player, serverItem, action.oldItem)
                    || count(serverItem) != count(action.oldItem)) {
                return InventoryTransactionError.SOURCE_ITEM_MISMATCH;
            }
        }
        return InventoryTransactionError.NONE;
    }

    private boolean validInteractionDirection(final EntityCache target,
                                              final Vec3 playerPosition,
                                              final float maxPickRange) {
        final Vec3 eye = playerPosition.add(0.0F,
                this.entity.aabbShapeComponent.getDimensions().eyeHeight(), 0.0F);
        final Vec3 view = MathUtil.getRotationVector(
                this.entity.actorRotationComponent.getPitch(),
                this.entity.actorRotationComponent.getYaw()).normalize();
        final Vec3 rayEnd = eye.add(view.multiply(maxPickRange + 1.0F));
        final Box targetBox = target.getCurrent().getBoundingBox()
                .expand(TARGET_AABB_INFLATION);
        if (targetBox.contains(eye) || targetBox.clip(eye, rayEnd).isPresent()) {
            return true;
        }

        final Vec3 toTarget = target.getCurrent().getPos().subtract(playerPosition);
        final float length = toTarget.length();
        if (length < DIRECTION_EPSILON) {
            return false;
        }
        final Vec3 direction = toTarget.divide(length);
        final float dot = view.x * direction.x + view.y * direction.y
                + view.z * direction.z;
        return dot >= this.entity.playerMovementSettingsComponent
                .playerMovementActionDirectionThreshold();
    }

    private float maxPickRange() {
        return this.entity.actorGameTypeComponent.value == GameType.CREATIVE
                ? 6.0F : 4.5F;
    }

    private Item authoritativeHeldItem() {
        final Item server = this.player.getSession().getInventory().getItemInHand();
        if (server != null && !server.isNull()) {
            return server;
        }
        return this.player.compensatedInventory.inventoryContainer.getHeldItemData();
    }

    private RiptideResolution resolveRiptideRelease(final Item serverHeld) {
        final Item activeUse = this.entity.itemInUseComponent.getItem();
        final Item compensated = this.player.compensatedInventory.inventoryContainer
                .getHeldItemData();
        for (final Item candidate : new Item[]{serverHeld, activeUse, compensated}) {
            final int level = riptideLevel(candidate);
            if (level > 0) {
                return new RiptideResolution(candidate.clone(), level);
            }
        }
        return new RiptideResolution(Item.AIR_ITEM, 0);
    }

    private boolean matchesClaim(final Item authoritative, final Item claimed) {
        if (isEmpty(authoritative) && isEmpty(claimed)) {
            return true;
        }
        return ItemUtil.sameDefinition(this.player, authoritative, claimed);
    }

    private static int count(final Item item) {
        return isEmpty(item) ? 0 : item.getCount();
    }

    private static boolean isEmpty(final Item item) {
        return item == null || item.isNull() || item.getCount() <= 0;
    }

    private static boolean validActionCount(final NetworkInventoryAction[] actions) {
        return actions == null || actions.length <= MAX_ACTIONS;
    }

    private static int riptideLevel(final Item item) {
        return item != null && !item.isNull() && item.getId() == ItemID.TRIDENT
                ? CompensatedInventory.getEnchantmentLevel(
                item, Enchantment.ID_TRIDENT_RIPTIDE) : 0;
    }

    private record RiptideResolution(Item item, int level) {
    }
}
