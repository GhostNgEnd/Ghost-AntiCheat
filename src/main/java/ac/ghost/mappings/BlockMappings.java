package ac.ghost.mappings;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFence;
import cn.nukkit.block.BlockFenceGate;
import cn.nukkit.block.BlockLeaves;
import cn.nukkit.block.BlockShulkerBox;
import cn.nukkit.block.BlockStairs;
import cn.nukkit.block.BlockWall;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.Locale;

public class BlockMappings {
    private static final IntSet FENCE_BLOCKS = new IntOpenHashSet();
    private static final IntSet FENCE_GATE_BLOCKS = new IntOpenHashSet();
    private static final IntSet WALL_BLOCKS = new IntOpenHashSet();
    private static final IntSet SHULKER_BLOCKS = new IntOpenHashSet();
    private static final IntSet LEAVES_BLOCKS = new IntOpenHashSet();
    private static final IntSet STAIRS_BLOCKS = new IntOpenHashSet();

    public static void load() {
        FENCE_BLOCKS.clear();
        FENCE_GATE_BLOCKS.clear();
        WALL_BLOCKS.clear();
        SHULKER_BLOCKS.clear();
        LEAVES_BLOCKS.clear();
        STAIRS_BLOCKS.clear();

        if (Block.list != null) {
            for (int id = 0; id < Block.list.length; id++) {
                if (Block.list[id] == null) {
                    continue;
                }

                final Block block;
                try {
                    block = Block.get(id);
                    if (block == null) {
                        continue;
                    }
                } catch (Exception ignored) {
                    continue;
                }

                final String name = block.getClass().getSimpleName()
                        .toLowerCase(Locale.ROOT);
                if (block instanceof BlockFenceGate) {
                    FENCE_GATE_BLOCKS.add(id);
                } else if (block instanceof BlockFence) {
                    FENCE_BLOCKS.add(id);
                } else if (block instanceof BlockWall) {
                    WALL_BLOCKS.add(id);
                } else if (block instanceof BlockShulkerBox) {
                    SHULKER_BLOCKS.add(id);
                } else if (block instanceof BlockLeaves || name.contains("leaves")) {
                    LEAVES_BLOCKS.add(id);
                } else if (block instanceof BlockStairs) {
                    STAIRS_BLOCKS.add(id);
                }
            }
        }
    }

    public static IntSet getStairsBlocks() {
        return STAIRS_BLOCKS;
    }

    public static IntSet getLeavesBlocks() {
        return LEAVES_BLOCKS;
    }

    public static IntSet getShulkerBlocks() {
        return SHULKER_BLOCKS;
    }

    public static IntSet getFenceGateBlocks() {
        return FENCE_GATE_BLOCKS;
    }

    public static IntSet getWallBlocks() {
        return WALL_BLOCKS;
    }

    public static IntSet getFenceBlocks() {
        return FENCE_BLOCKS;
    }
}
