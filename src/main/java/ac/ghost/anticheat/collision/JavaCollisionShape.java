package ac.ghost.anticheat.collision;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAzalea;
import cn.nukkit.block.BlockBrewingStand;
import cn.nukkit.block.BlockCandleCake;
import cn.nukkit.block.BlockChorusFlower;
import cn.nukkit.block.BlockChorusPlant;
import cn.nukkit.block.BlockComposter;
import cn.nukkit.block.BlockGrindstone;
import cn.nukkit.block.BlockHopper;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockLanternCopperBase;
import cn.nukkit.block.BlockPistonHead;
import cn.nukkit.block.BlockShelf;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;

import java.util.ArrayList;
import java.util.List;






public final class JavaCollisionShape {
    private static final List<Box> BREWING_STAND = boxes(
            0.5, 0.0625, 0.5, 0.875, 0.125, 0.875,
            0.5, 0.5, 0.5, 0.125, 0.75, 0.125
    );
    private static final List<Box> CANDLE_CAKE = boxes(
            0.5, 0.25, 0.5, 0.875, 0.5, 0.875,
            0.5, 0.6875, 0.5, 0.125, 0.375, 0.125
    );
    private static final List<Box> COMPOSTER = boxes(
            0.5, 0.0625, 0.5, 1, 0.125, 1,
            0.0625, 0.5625, 0.5, 0.125, 0.875, 1,
            0.5625, 0.5625, 0.0625, 0.875, 0.875, 0.125,
            0.5625, 0.5625, 0.9375, 0.875, 0.875, 0.125,
            0.9375, 0.5625, 0.5, 0.125, 0.875, 0.75
    );
    private static final List<Box> AZALEA = boxes(
            0.5, 0.5, 0.5, 0.25, 1, 0.25,
            0.1875, 0.75, 0.5, 0.375, 0.5, 1,
            0.6875, 0.75, 0.1875, 0.625, 0.5, 0.375,
            0.6875, 0.75, 0.8125, 0.625, 0.5, 0.375,
            0.8125, 0.75, 0.5, 0.375, 0.5, 0.25
    );
    private static final List<Box> COPPER_LANTERN_HANGING = boxes(
            0.5, 0.28125, 0.5, 0.375, 0.4375, 0.375,
            0.5, 0.5625, 0.5, 0.25, 0.125, 0.25
    );
    private static final List<Box> COPPER_LANTERN_STANDING = boxes(
            0.5, 0.21875, 0.5, 0.375, 0.4375, 0.375,
            0.5, 0.5, 0.5, 0.25, 0.125, 0.25
    );

    private static final List<Box> GRINDSTONE_FLOOR_Z = boxes(
            0.1875, 0.40625, 0.5, 0.125, 0.8125, 0.25,
            0.8125, 0.40625, 0.5, 0.125, 0.8125, 0.25,
            0.5, 0.625, 0.5, 0.5, 0.75, 0.75,
            0.1875, 0.625, 0.34375, 0.125, 0.375, 0.0625,
            0.1875, 0.625, 0.65625, 0.125, 0.375, 0.0625,
            0.8125, 0.625, 0.34375, 0.125, 0.375, 0.0625,
            0.8125, 0.625, 0.65625, 0.125, 0.375, 0.0625
    );
    private static final List<Box> GRINDSTONE_FLOOR_X = boxes(
            0.5, 0.40625, 0.1875, 0.25, 0.8125, 0.125,
            0.5, 0.40625, 0.8125, 0.25, 0.8125, 0.125,
            0.5, 0.625, 0.5, 0.75, 0.75, 0.5,
            0.34375, 0.625, 0.1875, 0.0625, 0.375, 0.125,
            0.34375, 0.625, 0.8125, 0.0625, 0.375, 0.125,
            0.65625, 0.625, 0.1875, 0.0625, 0.375, 0.125,
            0.65625, 0.625, 0.8125, 0.0625, 0.375, 0.125
    );
    private static final List<Box> GRINDSTONE_CEILING_Z = boxes(
            0.5, 0.375, 0.5, 0.5, 0.75, 0.75,
            0.1875, 0.375, 0.5, 0.125, 0.375, 0.375,
            0.8125, 0.375, 0.5, 0.125, 0.375, 0.375,
            0.1875, 0.78125, 0.5, 0.125, 0.4375, 0.25,
            0.8125, 0.78125, 0.5, 0.125, 0.4375, 0.25
    );
    private static final List<Box> GRINDSTONE_CEILING_X = boxes(
            0.5, 0.375, 0.5, 0.75, 0.75, 0.5,
            0.5, 0.375, 0.1875, 0.375, 0.375, 0.125,
            0.5, 0.375, 0.8125, 0.375, 0.375, 0.125,
            0.5, 0.78125, 0.1875, 0.25, 0.4375, 0.125,
            0.5, 0.78125, 0.8125, 0.25, 0.4375, 0.125
    );

    private JavaCollisionShape() {
    }

    public static List<Box> getCollisionBox(GhostPlayer player, BlockVector3 pos, Block state) {
        if (state instanceof BlockBrewingStand) return BREWING_STAND;
        if (state instanceof BlockCandleCake) return CANDLE_CAKE;
        if (state instanceof BlockComposter) return COMPOSTER;
        if (state instanceof BlockAzalea) return AZALEA;
        if (state instanceof BlockShelf shelf) return shelfShape(shelf.getBlockFace());
        if (state instanceof BlockLanternCopperBase lantern)
            return lantern.isHanging() ? COPPER_LANTERN_HANGING : COPPER_LANTERN_STANDING;
        if (state instanceof BlockHopper hopper) return hopperShape(hopper.getBlockFace());
        if (state instanceof BlockGrindstone grindstone) return grindstoneShape(grindstone);
        if (state instanceof BlockPistonHead pistonHead) return pistonHeadShape(pistonHead.getBlockFace(), false);
        if (state instanceof BlockChorusPlant) return chorusPlantShape(player, pos);
        return null;
    }

    static List<Box> shelfShape(BlockFace facing) {
        return switch (facing) {
            case NORTH -> boxes(
                    0.5, 0.125, 0.84375, 1, 0.25, 0.3125,
                    0.5, 0.625, 0.90625, 1, 0.75, 0.1875,
                    0.5, 0.875, 0.75, 1, 0.25, 0.125);
            case SOUTH -> boxes(
                    0.5, 0.125, 0.15625, 1, 0.25, 0.3125,
                    0.5, 0.625, 0.09375, 1, 0.75, 0.1875,
                    0.5, 0.875, 0.25, 1, 0.25, 0.125);
            case WEST -> boxes(
                    0.84375, 0.125, 0.5, 0.3125, 0.25, 1,
                    0.90625, 0.625, 0.5, 0.1875, 0.75, 1,
                    0.75, 0.875, 0.5, 0.125, 0.25, 1);
            default -> boxes(
                    0.15625, 0.125, 0.5, 0.3125, 0.25, 1,
                    0.09375, 0.625, 0.5, 0.1875, 0.75, 1,
                    0.25, 0.875, 0.5, 0.125, 0.25, 1);
        };
    }

    static List<Box> hopperShape(BlockFace facing) {
        List<Box> result = new ArrayList<>(10);
        
        result.add(new Box(0, 0.625, 0, 0.25, 0.6875, 1));
        result.add(new Box(0.25, 0.625, 0, 1, 0.6875, 0.25));
        result.add(new Box(0.25, 0.625, 0.75, 1, 0.6875, 1));
        result.add(new Box(0.75, 0.625, 0.25, 1, 0.6875, 0.75));
        result.add(new Box(0, 0.6875, 0, 0.125, 1, 1));
        result.add(new Box(0.125, 0.6875, 0, 1, 1, 0.125));
        result.add(new Box(0.125, 0.6875, 0.875, 1, 1, 1));
        result.add(new Box(0.875, 0.6875, 0.125, 1, 1, 0.875));

        if (facing == BlockFace.DOWN) {
            result.add(new Box(0.375, 0, 0.375, 0.625, 0.6875, 0.625));
            result.add(new Box(0.25, 0.25, 0.25, 0.75, 0.6875, 0.75));
        } else {
            result.add(new Box(0.25, 0.25, 0.25, 0.75, 0.6875, 0.75));
            result.add(switch (facing) {
                case NORTH -> new Box(0.375, 0.25, 0, 0.625, 0.5, 0.25);
                case SOUTH -> new Box(0.375, 0.25, 0.75, 0.625, 0.5, 1);
                case WEST -> new Box(0, 0.25, 0.375, 0.25, 0.5, 0.625);
                default -> new Box(0.75, 0.25, 0.375, 1, 0.5, 0.625);
            });
        }
        return result;
    }

    static List<Box> grindstoneShape(BlockGrindstone block) {
        BlockFace face = block.getBlockFace();
        return switch (block.getAttachmentType()) {
            case BlockGrindstone.TYPE_ATTACHMENT_STANDING ->
                    face.getAxis() == BlockFace.Axis.Z ? GRINDSTONE_FLOOR_Z : GRINDSTONE_FLOOR_X;
            case BlockGrindstone.TYPE_ATTACHMENT_HANGING ->
                    face.getAxis() == BlockFace.Axis.Z ? GRINDSTONE_CEILING_Z : GRINDSTONE_CEILING_X;
            default -> grindstoneWallShape(face);
        };
    }

    private static List<Box> grindstoneWallShape(BlockFace face) {
        return switch (face) {
            case NORTH -> boxes(
                    0.5, 0.5, 0.375, 0.5, 0.75, 0.75,
                    0.1875, 0.5, 0.375, 0.125, 0.375, 0.375,
                    0.8125, 0.5, 0.375, 0.125, 0.375, 0.375,
                    0.1875, 0.5, 0.78125, 0.125, 0.25, 0.4375,
                    0.8125, 0.5, 0.78125, 0.125, 0.25, 0.4375);
            case SOUTH -> boxes(
                    0.5, 0.5, 0.625, 0.5, 0.75, 0.75,
                    0.1875, 0.5, 0.625, 0.125, 0.375, 0.375,
                    0.8125, 0.5, 0.625, 0.125, 0.375, 0.375,
                    0.1875, 0.5, 0.21875, 0.125, 0.25, 0.4375,
                    0.8125, 0.5, 0.21875, 0.125, 0.25, 0.4375);
            case WEST -> boxes(
                    0.375, 0.5, 0.5, 0.75, 0.75, 0.5,
                    0.375, 0.5, 0.1875, 0.375, 0.375, 0.125,
                    0.375, 0.5, 0.8125, 0.375, 0.375, 0.125,
                    0.78125, 0.5, 0.1875, 0.4375, 0.25, 0.125,
                    0.78125, 0.5, 0.8125, 0.4375, 0.25, 0.125);
            default -> boxes(
                    0.625, 0.5, 0.5, 0.75, 0.75, 0.5,
                    0.625, 0.5, 0.1875, 0.375, 0.375, 0.125,
                    0.625, 0.5, 0.8125, 0.375, 0.375, 0.125,
                    0.21875, 0.5, 0.1875, 0.4375, 0.25, 0.125,
                    0.21875, 0.5, 0.8125, 0.4375, 0.25, 0.125);
        };
    }

    static List<Box> pistonHeadShape(BlockFace face) {
        return pistonHeadShape(face, false);
    }

    static List<Box> pistonHeadShape(BlockFace face, boolean shortArm) {
        
        
        
        return switch (face) {
            case NORTH -> List.of(
                    new Box(0, 0, 0, 1, 1, 0.25),
                    new Box(0.375, 0.375, 0.25, 0.625, 0.625, shortArm ? 1 : 1.25));
            case SOUTH -> List.of(
                    new Box(0, 0, 0.75, 1, 1, 1),
                    new Box(0.375, 0.375, shortArm ? 0 : -0.25, 0.625, 0.625, 0.75));
            case WEST -> List.of(
                    new Box(0, 0, 0, 0.25, 1, 1),
                    new Box(0.25, 0.375, 0.375, shortArm ? 1 : 1.25, 0.625, 0.625));
            case EAST -> List.of(
                    new Box(0.75, 0, 0, 1, 1, 1),
                    new Box(shortArm ? 0 : -0.25, 0.375, 0.375, 0.75, 0.625, 0.625));
            case DOWN -> List.of(
                    new Box(0, 0, 0, 1, 0.25, 1),
                    new Box(0.375, 0.25, 0.375, 0.625, shortArm ? 1 : 1.25, 0.625));
            default -> List.of(
                    new Box(0, 0.75, 0, 1, 1, 1),
                    new Box(0.375, shortArm ? 0 : -0.25, 0.375, 0.625, 0.75, 0.625));
        };
    }

    static List<Box> chorusPlantShape(GhostPlayer player, BlockVector3 pos) {
        List<Box> result = new ArrayList<>(7);
        result.add(new Box(0.1875, 0.1875, 0.1875, 0.8125, 0.8125, 0.8125));
        if (connectsChorus(player, pos, BlockFace.DOWN, true))
            result.add(new Box(0.1875, 0, 0.1875, 0.8125, 0.1875, 0.8125));
        if (connectsChorus(player, pos, BlockFace.UP, false))
            result.add(new Box(0.1875, 0.8125, 0.1875, 0.8125, 1, 0.8125));
        if (connectsChorus(player, pos, BlockFace.NORTH, false))
            result.add(new Box(0.1875, 0.1875, 0, 0.8125, 0.8125, 0.1875));
        if (connectsChorus(player, pos, BlockFace.SOUTH, false))
            result.add(new Box(0.1875, 0.1875, 0.8125, 0.8125, 0.8125, 1));
        if (connectsChorus(player, pos, BlockFace.WEST, false))
            result.add(new Box(0, 0.1875, 0.1875, 0.1875, 0.8125, 0.8125));
        if (connectsChorus(player, pos, BlockFace.EAST, false))
            result.add(new Box(0.8125, 0.1875, 0.1875, 1, 0.8125, 0.8125));
        return result;
    }

    private static boolean connectsChorus(GhostPlayer player, BlockVector3 pos,
                                          BlockFace face, boolean endStone) {
        Block neighbour = player.entityContext.blockSource.getBlockState(
                pos.getX() + face.getXOffset(),
                pos.getY() + face.getYOffset(),
                pos.getZ() + face.getZOffset(), 0
        ).getBlock();
        return neighbour instanceof BlockChorusPlant
                || neighbour instanceof BlockChorusFlower
                || endStone && neighbour.getId() == BlockID.END_STONE;
    }

    private static List<Box> boxes(double... centerAndSize) {
        List<Box> result = new ArrayList<>(centerAndSize.length / 6);
        for (int i = 0; i < centerAndSize.length; i += 6) {
            double x = centerAndSize[i], y = centerAndSize[i + 1], z = centerAndSize[i + 2];
            double sx = centerAndSize[i + 3], sy = centerAndSize[i + 4], sz = centerAndSize[i + 5];
            result.add(new Box(x - sx / 2, y - sy / 2, z - sz / 2,
                    x + sx / 2, y + sy / 2, z + sz / 2));
        }
        return List.copyOf(result);
    }
}
