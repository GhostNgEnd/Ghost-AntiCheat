package ac.ghost.anticheat.util.block;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.mappings.BlockMappings;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import cn.nukkit.network.protocol.types.GameType;


public class BlockUtil {

    public static void restoreCorrectBlock(Player player, BlockVector3 vector, Block blockState) {
        restoreCorrectBlock(player, vector, blockState, 0);
    }

    private static void restoreCorrectBlock(Player player, BlockVector3 vector, Block blockState, int layer) {
        restoreCorrectBlock(player, vector, blockState, layer, UpdateBlockPacket.FLAG_ALL_PRIORITY);
    }

    private static void restoreCorrectBlock(Player player, BlockVector3 vector, Block blockState,
                                            int layer, int flags) {
        if (player == null || vector == null || blockState == null || player.getLevel() == null) {
            return;
        }

        final UpdateBlockPacket packet = new UpdateBlockPacket();
        packet.x = vector.getX();
        packet.y = vector.getY();
        packet.z = vector.getZ();
        packet.blockId = blockState.getId();
        packet.blockData = blockState.getDamage();
        packet.blockRuntimeId = player.getLevel().getBlockRuntimeId(
                player.getGameVersion(),
                vector.getX(),
                vector.getY(),
                vector.getZ(),
                layer
        );
        packet.flags = flags;
        packet.dataLayer = layer;
        player.dataPacket(packet);
    }

    




    public static void resendBlocksAroundArea(Player player, BlockVector3 blockPos, int face) {
        if (player == null || blockPos == null || player.getLevel() == null) {
            return;
        }

        resendBlockColumn(player, blockPos);
        if (face >= 0 && face < 6) {
            resendBlockColumn(player, getBlockPosition(blockPos, face));
        }
    }

    private static void resendBlockColumn(Player player, BlockVector3 blockPos) {
        resendMainLayerBlock(player, blockPos);
        resendMainLayerBlock(player, blockPos.add(0, 1, 0));
        resendMainLayerBlock(player, blockPos.subtract(0, 1, 0));
    }

    private static void resendMainLayerBlock(Player player, BlockVector3 blockPos) {
        final Block block = player.getLevel().getBlock(
                blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0);
        restoreCorrectBlock(player, blockPos, block, 0, UpdateBlockPacket.FLAG_ALL);
    }

    public static BlockVector3 getBlockPosition(BlockVector3 blockPos, int face) {
        return switch (face) {
            case 0 -> blockPos.subtract(0, 1, 0);
            case 1 -> blockPos.add(0, 1, 0);
            case 2 -> blockPos.subtract(0, 0, 1);
            case 3 -> blockPos.add(0, 0, 1);
            case 4 -> blockPos.subtract(1, 0, 0);
            case 5 -> blockPos.add(1, 0, 0);
            default -> blockPos;
        };
    }

    public static void restoreCorrectBlock(Player player, BlockVector3 blockPos) {
        if (player == null || blockPos == null || player.getLevel() == null) {
            return;
        }

        for (int layer = 0; layer <= 1; layer++) {
            final Block block = player.getLevel().getBlock(
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ(),
                    layer
            );
            restoreCorrectBlock(player, blockPos, block, layer);
        }
    }

    public static boolean determineCanBreak(final GhostPlayer player, final BlockLegacy state) {
        int id = state.getBlock().getId();
        if (id == BlockID.AIR || id == BlockID.LAVA || id == BlockID.STILL_LAVA || id == BlockID.WATER || id == BlockID.STILL_WATER) {
            return false;
        }

        float destroyTime = (float) state.getBlock().getHardness();
        return destroyTime != -1 || player.entityContext.actorGameTypeComponent.value == GameType.CREATIVE;
    }

    public static boolean connectsToDirection(Block blockState, BlockFace direction) {
        if (!BlockMappings.getFenceGateBlocks().contains(blockState.getId())) {
            return false;
        }

        if (!(blockState instanceof cn.nukkit.block.BlockFenceGate gate)) {
            return false;
        }

        final BlockFace gateFace = gate.getBlockFace();
        
        
        return (gateFace.getXOffset() != 0 && direction.getXOffset() == 0)
                || (gateFace.getZOffset() != 0 && direction.getZOffset() == 0);
    }

    public static boolean isExceptionForConnection(Block blockState) {
        int id = blockState.getId();
        return BlockMappings.getLeavesBlocks().contains(id) || id == BlockID.BARRIER ||
                id == BlockID.CARVED_PUMPKIN || id == BlockID.JACK_O_LANTERN || id == BlockID.MELON_BLOCK || id == BlockID.PUMPKIN
                || BlockMappings.getShulkerBlocks().contains(id);
    }

}
