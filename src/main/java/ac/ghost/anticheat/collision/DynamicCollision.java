package ac.ghost.anticheat.collision;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.data.block.NetworkBlockState;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.block.BlockUtil;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.mappings.BlockMappings;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockChorusFlower;
import cn.nukkit.block.BlockChorusPlant;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockPistonBase;
import cn.nukkit.block.BlockPistonHead;
import cn.nukkit.block.BlockThin;
import cn.nukkit.block.BlockWall;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;

import java.util.List;
import java.util.Locale;


public final class DynamicCollision {
    private DynamicCollision() {
    }

    public static List<Box> getCollisionBox(GhostPlayer player, BlockVector3 pos, BlockLegacy blockState) {
        final Block state = blockState.getBlock();
        final NetworkBlockState network = blockState.getNetworkState();

        if (BlockMappings.getFenceBlocks().contains(state.getId())) {
            return fenceShape(
                    connectsFence(player, pos, state, BlockFace.NORTH),
                    connectsFence(player, pos, state, BlockFace.EAST),
                    connectsFence(player, pos, state, BlockFace.SOUTH),
                    connectsFence(player, pos, state, BlockFace.WEST)
            );
        }

        if (isPane(state)) {
            final int mask = paneConnectionMask(player, pos);
            return paneShape((mask & 1) != 0, (mask & 2) != 0,
                    (mask & 4) != 0, (mask & 8) != 0);
        }

        if (BlockMappings.getStairsBlocks().contains(state.getId())) {
            return stairShape(blockState, getStairShape(player, pos, blockState));
        }

        if (state instanceof BlockWall || network.identifier().endsWith("_wall")) {
            return wallShape(network, state instanceof BlockWall wall ? wall : null);
        }

        if (state instanceof BlockPistonBase
                || network.is("minecraft:piston")
                || network.is("minecraft:sticky_piston")) {
            return pistonBaseShape(player, pos, network, state instanceof BlockPistonBase p ? p : null);
        }

        if (state instanceof BlockPistonHead
                || network.is("minecraft:piston_arm_collision")
                || network.is("minecraft:sticky_piston_arm_collision")) {
            return pistonHeadShape(player, pos, network, state instanceof BlockPistonHead h ? h : null);
        }

        if (state instanceof BlockChorusPlant || network.is("minecraft:chorus_plant")) {
            return chorusShape(player, pos, network);
        }

        return null;
    }


    static List<Box> fenceShape(boolean north, boolean east, boolean south, boolean west) {
        return GeyserCollisionData.getDynamicShape(GeyserCollisionData.FENCE,
                connectionMask(north, east, south, west));
    }

    static List<Box> paneShape(boolean north, boolean east, boolean south, boolean west) {
        
        
        
        final java.util.ArrayList<Box> shape = new java.util.ArrayList<>(2);
        if (north || south) {
            final float minZ = north ? 0.0F : 0.5F;
            final float maxZ = south ? 1.0F : 0.5F;
            shape.add(new Box(0.4375F, 0, minZ, 0.5625F, 1, maxZ));
        }
        if (east || west) {
            final float minX = west ? 0.0F : 0.5F;
            final float maxX = east ? 1.0F : 0.5F;
            shape.add(new Box(minX, 0, 0.4375F, maxX, 1, 0.5625F));
        }
        if (shape.isEmpty()) {
            shape.add(new Box(0.4375F, 0, 0.4375F, 0.5625F, 1, 0.5625F));
        }
        return List.copyOf(shape);
    }

    static List<Box> stairShape(BlockLegacy state, StairShape shape) {
        int key = direction(stairDirection(state));
        key |= (stairTop(state) ? 1 : 0) << 2;
        key |= shape.ordinal() << 3;
        return GeyserCollisionData.getDynamicShape(GeyserCollisionData.STAIRS, key);
    }

    
    static List<Box> stairShape(Block state, StairShape shape) {
        return stairShape(new BlockLegacy(state, new BlockVector3(), 0), shape);
    }

    private static List<Box> wallShape(NetworkBlockState network, BlockWall wall) {
        final boolean hasPacketState = network.hasProperty("wall_post_bit")
                || network.hasProperty("wall_connection_type_north");
        final boolean post;
        final boolean north;
        final boolean east;
        final boolean south;
        final boolean west;
        if (hasPacketState) {
            post = network.booleanProperty("wall_post_bit", false);
            north = wall(network.stringProperty("wall_connection_type_north", "none")) != 0;
            east = wall(network.stringProperty("wall_connection_type_east", "none")) != 0;
            south = wall(network.stringProperty("wall_connection_type_south", "none")) != 0;
            west = wall(network.stringProperty("wall_connection_type_west", "none")) != 0;
        } else if (wall != null) {
            post = wall.isWallPost();
            north = wall.getConnectionType(BlockFace.NORTH) != BlockWall.WallConnectionType.NONE;
            east = wall.getConnectionType(BlockFace.EAST) != BlockWall.WallConnectionType.NONE;
            south = wall.getConnectionType(BlockFace.SOUTH) != BlockWall.WallConnectionType.NONE;
            west = wall.getConnectionType(BlockFace.WEST) != BlockWall.WallConnectionType.NONE;
        } else {
            return null;
        }

        
        
        
        
        
        
        float minZ = north ? 0.0F : 0.25F;
        float maxZ = south ? 1.0F : 0.75F;
        float minX = west ? 0.0F : 0.25F;
        float maxX = east ? 1.0F : 0.75F;

        if (north && south && !west && !east && !post) {
            minX = 0.3125F;
            maxX = 0.6875F;
        } else if (!north && !south && west && east && !post) {
            minZ = 0.3125F;
            maxZ = 0.6875F;
        }

        return List.of(new Box(minX, 0, minZ, maxX, 1.5F, maxZ));
    }

    private static int wall(String type) {
        if ("short".equalsIgnoreCase(type)) return 1;
        if ("tall".equalsIgnoreCase(type)) return 2;
        return 0;
    }

    private static int wall(BlockWall.WallConnectionType type) {
        if (type == BlockWall.WallConnectionType.SHORT) return 1;
        if (type == BlockWall.WallConnectionType.TALL) return 2;
        return 0;
    }

    private static List<Box> pistonBaseShape(GhostPlayer player, BlockVector3 pos,
                                             NetworkBlockState network, BlockPistonBase piston) {
        
        
        
        
        
        return List.of(new Box(0, 0, 0, 1, 1, 1));
    }

    








    private static List<Box> pistonHeadShape(GhostPlayer player, BlockVector3 pos,
                                             NetworkBlockState network, BlockPistonHead pistonHead) {
        final BlockFace face = pistonFacingDirection(
                network, pistonHead == null ? BlockFace.DOWN : pistonHead.getBlockFace());
        final float armMin = 0.3125F;
        final float armMax = 0.6875F;
        final List<Box> shapes = switch (face) {
            case NORTH -> List.of(
                    new Box(0, 0, 0, 1, 1, 0.25F),
                    new Box(armMin, armMin, 0.25F, armMax, armMax, 1.25F));
            case SOUTH -> List.of(
                    new Box(0, 0, 0.75F, 1, 1, 1),
                    new Box(armMin, armMin, -0.25F, armMax, armMax, 0.75F));
            case WEST -> List.of(
                    new Box(0, 0, 0, 0.25F, 1, 1),
                    new Box(0.25F, armMin, armMin, 1.25F, armMax, armMax));
            case EAST -> List.of(
                    new Box(0.75F, 0, 0, 1, 1, 1),
                    new Box(-0.25F, armMin, armMin, 0.75F, armMax, armMax));
            case DOWN -> List.of(
                    new Box(0, 0, 0, 1, 0.25F, 1),
                    new Box(armMin, 0.25F, armMin, armMax, 1.25F, armMax));
            default -> List.of(
                    new Box(0, 0.75F, 0, 1, 1, 1),
                    new Box(armMin, -0.25F, armMin, armMax, 0.75F, armMax));
        };

        return shapes;
    }

    







    private static BlockFace pistonFacingDirection(NetworkBlockState state, BlockFace fallback) {
        String direction = state.stringProperty("minecraft:cardinal_direction",
                state.stringProperty("cardinal_direction", ""));
        BlockFace horizontal = horizontalFace(direction);
        if (horizontal != null) {
            return horizontal;
        }
        if (!state.hasProperty("facing_direction")) {
            return fallback;
        }
        return switch (state.intProperty("facing_direction", -1)) {
            case 0 -> BlockFace.DOWN;
            case 1 -> BlockFace.UP;
            case 2 -> BlockFace.SOUTH;
            case 3 -> BlockFace.NORTH;
            case 4 -> BlockFace.EAST;
            case 5 -> BlockFace.WEST;
            default -> fallback;
        };
    }

    private static int pistonDirection(BlockFace face) {
        return switch (face) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
            default -> 0;
        };
    }

    private static List<Box> chorusShape(GhostPlayer player, BlockVector3 pos, NetworkBlockState state) {
        final boolean packetConnections = state.hasProperty("down") || state.hasProperty("up")
                || state.hasProperty("north") || state.hasProperty("east")
                || state.hasProperty("south") || state.hasProperty("west");
        int key;
        if (packetConnections) {
            key = state.booleanProperty("down", false) ? 1 : 0;
            key |= (state.booleanProperty("up", false) ? 1 : 0) << 1;
            key |= (state.booleanProperty("north", false) ? 1 : 0) << 2;
            key |= (state.booleanProperty("east", false) ? 1 : 0) << 3;
            key |= (state.booleanProperty("south", false) ? 1 : 0) << 4;
            key |= (state.booleanProperty("west", false) ? 1 : 0) << 5;
        } else {
            key = connectsChorus(player, pos, BlockFace.DOWN, true) ? 1 : 0;
            key |= (connectsChorus(player, pos, BlockFace.UP, false) ? 1 : 0) << 1;
            key |= (connectsChorus(player, pos, BlockFace.NORTH, false) ? 1 : 0) << 2;
            key |= (connectsChorus(player, pos, BlockFace.EAST, false) ? 1 : 0) << 3;
            key |= (connectsChorus(player, pos, BlockFace.SOUTH, false) ? 1 : 0) << 4;
            key |= (connectsChorus(player, pos, BlockFace.WEST, false) ? 1 : 0) << 5;
        }
        return GeyserCollisionData.getDynamicShape(GeyserCollisionData.CHORUS, key);
    }

    private static boolean connectsChorus(GhostPlayer player, BlockVector3 pos,
                                          BlockFace face, boolean endStone) {
        Block block = player.entityContext.blockSource.getBlockState(
                pos.getX() + face.getXOffset(), pos.getY() + face.getYOffset(),
                pos.getZ() + face.getZOffset(), 0).getBlock();
        return block instanceof BlockChorusPlant || block instanceof BlockChorusFlower
                || endStone && block.getId() == BlockID.END_STONE;
    }

    private static int connectionMask(boolean north, boolean east, boolean south, boolean west) {
        return (north ? 1 : 0) | (east ? 2 : 0) | (south ? 4 : 0) | (west ? 8 : 0);
    }

    private static int direction(BlockFace face) {
        return switch (face) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    private static boolean connectsFence(GhostPlayer player, BlockVector3 pos, Block current, BlockFace face) {
        BlockLegacy neighbourState = neighbour(player, pos, face);
        Block neighbour = neighbourState.getBlock();
        return (!BlockUtil.isExceptionForConnection(neighbour) && neighbourState.isFaceSturdy(player))
                || (BlockMappings.getFenceBlocks().contains(neighbour.getId())
                && (neighbour.getId() == BlockID.NETHER_BRICK_FENCE)
                == (current.getId() == BlockID.NETHER_BRICK_FENCE))
                || BlockUtil.connectsToDirection(neighbour, face.getOpposite());
    }

    private static boolean connectsPane(GhostPlayer player, BlockVector3 pos, BlockFace face) {
        BlockLegacy neighbourState = neighbour(player, pos, face);
        Block neighbour = neighbourState.getBlock();
        final boolean sturdy = neighbourState.isFaceSturdy(player);
        final String neighbourIdentifier = neighbourState.getNetworkState().identifier();
        return (!BlockUtil.isExceptionForConnection(neighbour) && sturdy)
                || isPane(neighbour)
                || BlockMappings.getWallBlocks().contains(neighbour.getId())
                || neighbour instanceof BlockWall
                || neighbourIdentifier.endsWith("_wall");
    }

    public static int paneConnectionMask(GhostPlayer player, BlockVector3 pos) {
        return connectionMask(
                connectsPane(player, pos, BlockFace.NORTH),
                connectsPane(player, pos, BlockFace.EAST),
                connectsPane(player, pos, BlockFace.SOUTH),
                connectsPane(player, pos, BlockFace.WEST));
    }

    public static String describePaneNeighbours(GhostPlayer player, BlockVector3 pos) {
        final StringBuilder result = new StringBuilder();
        for (final BlockFace face : List.of(BlockFace.NORTH, BlockFace.EAST,
                BlockFace.SOUTH, BlockFace.WEST)) {
            final BlockLegacy neighbourState = neighbour(player, pos, face);
            final Block neighbour = neighbourState.getBlock();
            final NetworkBlockState network = neighbourState.getNetworkState();
            if (result.length() > 0) result.append(';');
            result.append(face.name().toLowerCase(Locale.ROOT)).append("={")
                    .append("id=").append(neighbour.getId())
                    .append(",class=").append(neighbour.getClass().getSimpleName())
                    .append(",identifier=").append(network.identifier())
                    .append(",networkId=").append(network.networkId())
                    .append(",legacyFullId=").append(network.legacyFullId())
                    .append(",stateSturdy=").append(neighbourState.isFaceSturdy(player))
                    .append(",blockSturdy=").append(GeyserCollisionData.isSolidCollisionEquivalent(neighbour))
                    .append(",exception=").append(BlockUtil.isExceptionForConnection(neighbour))
                    .append(",pane=").append(isPane(neighbour))
                    .append(",wallClass=").append(neighbour instanceof BlockWall)
                    .append(",wallMapped=").append(BlockMappings.getWallBlocks().contains(neighbour.getId()))
                    .append('}');
        }
        return result.toString();
    }

    public static boolean isPane(Block state) {
        String name = state.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        return state instanceof BlockThin || state.getId() == BlockID.IRON_BARS
                || name.contains("glasspane") || name.contains("stainedglasspane");
    }

    private static StairShape getStairShape(GhostPlayer player, BlockVector3 pos, BlockLegacy state) {
        BlockFace direction = stairDirection(state);
        BlockLegacy forward = neighbour(player, pos, direction);
        if (sameHalfStair(state, forward)) {
            BlockFace other = stairDirection(forward);
            if (other.getAxis() != direction.getAxis()
                    && isDifferentOrientation(player, state, pos, other.getOpposite())) {
                return other == direction.rotateYCCW() ? StairShape.OUTER_LEFT : StairShape.OUTER_RIGHT;
            }
        }

        BlockLegacy behind = neighbour(player, pos, direction.getOpposite());
        if (sameHalfStair(state, behind)) {
            BlockFace other = stairDirection(behind);
            if (other.getAxis() != direction.getAxis()
                    && isDifferentOrientation(player, state, pos, other)) {
                return other == direction.rotateYCCW() ? StairShape.INNER_LEFT : StairShape.INNER_RIGHT;
            }
        }
        return StairShape.STRAIGHT;
    }

    private static boolean isDifferentOrientation(GhostPlayer player, BlockLegacy state,
                                                  BlockVector3 pos, BlockFace direction) {
        BlockLegacy adjacent = neighbour(player, pos, direction);
        return !sameHalfStair(state, adjacent) || stairDirection(adjacent) != stairDirection(state);
    }

    private static boolean sameHalfStair(BlockLegacy first, BlockLegacy second) {
        return BlockMappings.getStairsBlocks().contains(second.getBlock().getId())
                && stairTop(first) == stairTop(second);
    }

    private static boolean stairTop(BlockLegacy state) {
        NetworkBlockState network = state.getNetworkState();
        return network.booleanProperty("upside_down_bit", (state.getBlock().getDamage() & 4) != 0);
    }

    private static BlockFace stairDirection(BlockLegacy state) {
        NetworkBlockState network = state.getNetworkState();
        String cardinal = network.stringProperty("minecraft:cardinal_direction",
                network.stringProperty("cardinal_direction", ""));
        BlockFace fromString = horizontalFace(cardinal);
        if (fromString != null) {
            return fromString;
        }
        return switch (network.intProperty("weirdo_direction", state.getBlock().getDamage() & 3)) {
            case 0 -> BlockFace.EAST;
            case 1 -> BlockFace.WEST;
            case 2 -> BlockFace.SOUTH;
            default -> BlockFace.NORTH;
        };
    }

    static BlockFace facingDirection(NetworkBlockState state, BlockFace fallback) {
        String direction = state.stringProperty("minecraft:cardinal_direction",
                state.stringProperty("cardinal_direction", ""));
        BlockFace horizontal = horizontalFace(direction);
        if (horizontal != null) {
            return horizontal;
        }
        return switch (state.intProperty("facing_direction", pistonDirection(fallback))) {
            case 0 -> BlockFace.DOWN;
            case 1 -> BlockFace.UP;
            case 2 -> BlockFace.NORTH;
            case 3 -> BlockFace.SOUTH;
            case 4 -> BlockFace.WEST;
            case 5 -> BlockFace.EAST;
            default -> fallback;
        };
    }

    static BlockFace horizontalFace(String value) {
        if (value == null) return null;
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "north" -> BlockFace.NORTH;
            case "south" -> BlockFace.SOUTH;
            case "west" -> BlockFace.WEST;
            case "east" -> BlockFace.EAST;
            default -> null;
        };
    }

    private static BlockLegacy neighbour(GhostPlayer player, BlockVector3 pos, BlockFace face) {
        return player.entityContext.blockSource.getBlockState(
                pos.getX() + face.getXOffset(), pos.getY(), pos.getZ() + face.getZOffset(), 0);
    }

    enum StairShape {
        STRAIGHT,
        INNER_LEFT,
        INNER_RIGHT,
        OUTER_LEFT,
        OUTER_RIGHT
    }
}
