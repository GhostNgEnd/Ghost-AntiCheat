package ac.ghost.anticheat.check.impl.scaffolding;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.check.impl.breaking.BreakingUtil;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import ac.ghost.anticheat.util.block.BlockUtil;
import cn.nukkit.block.BlockID;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.v113.UseItemPacket_v113;

import java.util.Locale;

@CheckInfo(name = "FarPlace")
public final class FarPlace extends PacketCheck {
    private static final float REACH_EPSILON = 0.15F;

    public FarPlace(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        if (BreakingUtil.inVehicle(player)) {
            return;
        }

        final float maxReach = BreakingUtil.maxBlockReach(player);
        if (!Float.isFinite(maxReach)) {
            return;
        }

        if (event.getPacket() instanceof PlayerAuthInputPacket packet) {
            final InventoryTransactionPacket transaction = packet.getItemUseTransaction();
            if (transaction == null || !isFarPlacement(transaction, packet, maxReach)) {
                return;
            }

            packet.setItemUseTransaction(null);
            packet.getInputData().remove(AuthInputAction.PERFORM_ITEM_INTERACTION);
            resync(transaction);
            return;
        }

        if (event.getPacket() instanceof UseItemPacket_v113 packet
                && BedrockProtocolCapabilities.usesLegacyUseItem(
                        player.getSession().protocol)
                && isFarLegacyPlacement(packet, maxReach)) {
            event.setCancelled(true);
            final BlockVector3 blockPos = new BlockVector3(packet.x, packet.y, packet.z);
            BlockUtil.resendBlocksAroundArea(player.getSession(), blockPos, packet.face);
            player.getSession().getInventory().sendContents(player.getSession());
            return;
        }

        if (event.getPacket() instanceof InventoryTransactionPacket transaction
                && isFarPlacement(transaction, null, maxReach)) {
            event.setCancelled(true);
            resync(transaction);
        }
    }

    private boolean isFarLegacyPlacement(final UseItemPacket_v113 packet,
                                         final float maxReach) {
        if (packet.face < 0 || packet.face > 5
                || !isPlaceableBlock(packet.item)) {
            return false;
        }
        final BlockVector3 blockPos = new BlockVector3(packet.x, packet.y, packet.z);
        final float distance = BreakingUtil.distanceToBlock(player, blockPos, null);
        if (distance <= maxReach + REACH_EPSILON) {
            return false;
        }
        fail(String.format(Locale.ROOT, "distance=%.3f, max=%.3f, pos=%s",
                distance, maxReach, BreakingUtil.pos(blockPos)));
        return true;
    }

    private boolean isFarPlacement(final InventoryTransactionPacket transaction,
                                   final PlayerAuthInputPacket authInput,
                                   final float maxReach) {
        if (transaction.transactionType != InventoryTransactionPacket.TYPE_USE_ITEM
                || !(transaction.transactionData instanceof UseItemData data)
                || data.actionType != InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK
                || data.blockPos == null
                || !isPlaceableBlock(data.itemInHand)) {
            return false;
        }

        final float distance = BreakingUtil.distanceToBlock(
                player, data.blockPos, authInput);
        if (distance <= maxReach + REACH_EPSILON) {
            return false;
        }

        fail(String.format(Locale.ROOT, "distance=%.3f, max=%.3f, pos=%s",
                distance, maxReach, BreakingUtil.pos(data.blockPos)));
        return true;
    }

    private static boolean isPlaceableBlock(final Item item) {
        return item != null
                && !item.isNull()
                && item.canBePlaced()
                && item.getBlockId() != BlockID.AIR
                && item.getBlockId() != BlockID.SCAFFOLDING;
    }

    private void resync(final InventoryTransactionPacket transaction) {
        if (!(transaction.transactionData instanceof UseItemData data)
                || data.blockPos == null) {
            return;
        }
        BlockUtil.resendBlocksAroundArea(player.getSession(), data.blockPos,
                faceIndex(data.face));
        player.getSession().getInventory().sendContents(player.getSession());
    }

    private static int faceIndex(final BlockFace face) {
        if (face == null) {
            return -1;
        }
        return switch (face) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
            default -> -1;
        };
    }
}
