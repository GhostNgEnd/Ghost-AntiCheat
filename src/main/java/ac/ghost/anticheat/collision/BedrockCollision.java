package ac.ghost.anticheat.collision;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.data.block.PowderSnowBlock;
import ac.ghost.anticheat.data.block.NetworkBlockState;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAmethystCluster;
import cn.nukkit.block.BlockAnvil;
import cn.nukkit.block.BlockBell;
import cn.nukkit.block.BlockCake;
import cn.nukkit.block.BlockCauldron;
import cn.nukkit.block.BlockChain;
import cn.nukkit.block.BlockChest;
import cn.nukkit.block.BlockDaylightDetector;
import cn.nukkit.block.BlockDecoratedPot;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.block.BlockDripleafBig;
import cn.nukkit.block.BlockEndPortal;
import cn.nukkit.block.BlockEndPortalFrame;
import cn.nukkit.block.BlockEndRod;
import cn.nukkit.block.BlockEnderChest;
import cn.nukkit.block.BlockFarmland;
import cn.nukkit.block.BlockFenceGate;
import cn.nukkit.block.BlockFire;
import cn.nukkit.block.BlockGrassPath;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockItemFrame;
import cn.nukkit.block.BlockLantern;
import cn.nukkit.block.BlockLadder;
import cn.nukkit.block.BlockLightningRodBase;
import cn.nukkit.block.BlockNetherPortal;
import cn.nukkit.block.BlockPowderSnow;
import cn.nukkit.block.BlockPressurePlateBase;
import cn.nukkit.block.BlockRail;
import cn.nukkit.block.BlockSapling;
import cn.nukkit.block.BlockScaffolding;
import cn.nukkit.block.BlockSculkSensor;
import cn.nukkit.block.BlockSculkShrieker;
import cn.nukkit.block.BlockSnowLayer;
import cn.nukkit.block.BlockSporeBlossom;
import cn.nukkit.block.BlockTrapdoor;
import cn.nukkit.block.BlockTripWire;
import cn.nukkit.block.BlockTurtleEgg;
import cn.nukkit.block.BlockWitherRose;
import cn.nukkit.block.properties.enums.DripleafTilt;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.types.AuthInputAction;

import java.util.ArrayList;
import java.util.List;


public final class BedrockCollision {
    private static final List<Box> EMPTY_SHAPE = List.of();
    private static final List<Box> SOLID_SHAPE = List.of(new Box(0, 0, 0, 1, 1, 1));

    private static final List<Box> BED_SHAPE = List.of(new Box(0, 0, 0, 1, 0.5625F, 1));
    private static final List<Box> HONEY_SHAPE = List.of(
            new Box(0.0625F, 0, 0.0625F, 0.9375F, 1, 0.9375F));
    private static final List<Box> LECTERN_SHAPE = List.of(new Box(0, 0, 0, 1, 0.9F, 1));
    private static final List<Box> CONDUIT_SHAPE = List.of(new Box(0.25F, 0, 0.25F, 0.75F, 0.5F, 0.75F));
    private static final List<Box> CACTUS_SHAPE = List.of(new Box(0.0625F, 0, 0.0625F, 0.9375F, 1, 0.9375F));

    private static final List<Box> SOUL_SAND_SHAPE = List.of(new Box(0, 0, 0, 1, 0.875F, 1));
    private static final List<Box> ENCHANTING_TABLE_SHAPE = List.of(new Box(0, 0, 0, 1, 0.75F, 1));
    
    
    
    private static final List<Box> BREWING_STAND_SHAPE = List.of(
            new Box(0, 0, 0, 1, 0.125F, 1),
            new Box(0.4375F, 0, 0.4375F, 0.5625F, 0.875F, 0.5625F)
    );
    private static final List<Box> FARMLAND_SHAPE = List.of(new Box(0, 0, 0, 1, 0.9375F, 1));
    private static final List<Box> DAYLIGHT_DETECTOR_SHAPE = List.of(new Box(0, 0, 0, 1, 0.375F, 1));
    private static final List<Box> DECORATED_POT_SHAPE = List.of(new Box(0.0625F, 0, 0.0625F, 0.9375F, 1, 0.9375F));
    private static final List<Box> SCULK_HALF_BLOCK_SHAPE = List.of(new Box(0, 0, 0, 1, 0.5F, 1));

    private static final List<Box> TURTLE_EGG_SHAPE = List.of(new Box(0.2F, 0, 0.2F, 0.8F, 0.45F, 0.8F));

    private static final List<Box> CHAIN_X_SHAPE = List.of(new Box(0, 0.40625F, 0.40625F, 1, 0.59375F, 0.59375F));
    private static final List<Box> CHAIN_Y_SHAPE = List.of(new Box(0.40625F, 0, 0.40625F, 0.59375F, 1, 0.59375F));
    private static final List<Box> CHAIN_Z_SHAPE = List.of(new Box(0.40625F, 0.40625F, 0, 0.59375F, 0.59375F, 1));

    private static final List<Box> END_ROD_X_SHAPE = List.of(new Box(0, 0.375F, 0.375F, 1, 0.625F, 0.625F));
    private static final List<Box> END_ROD_Y_SHAPE = List.of(new Box(0.375F, 0, 0.375F, 0.625F, 1, 0.625F));
    private static final List<Box> END_ROD_Z_SHAPE = List.of(new Box(0.375F, 0.375F, 0, 0.625F, 0.625F, 1));

    private static final List<Box> LIGHTNING_ROD_X_SHAPE = List.of(new Box(0, 0.4375F, 0.4375F, 1, 0.5625F, 0.5625F));
    private static final List<Box> LIGHTNING_ROD_Y_SHAPE = List.of(new Box(0.4375F, 0, 0.4375F, 0.5625F, 1, 0.5625F));
    private static final List<Box> LIGHTNING_ROD_Z_SHAPE = List.of(new Box(0.4375F, 0.4375F, 0, 0.5625F, 0.5625F, 1));

    private static final Box BIG_DRIPLEAF_LEAF = new Box(0, 0.6875F, 0, 1, 0.8125F, 1);
    private static final Box BIG_DRIPLEAF_STEM_NORTH = new Box(0.4375F, 0, 0.75F, 0.5625F, 0.6875F, 0.875F);
    private static final Box BIG_DRIPLEAF_STEM_SOUTH = new Box(0.4375F, 0, 0.125F, 0.5625F, 0.6875F, 0.25F);
    private static final Box BIG_DRIPLEAF_STEM_WEST = new Box(0.75F, 0, 0.4375F, 0.875F, 0.6875F, 0.5625F);
    private static final Box BIG_DRIPLEAF_STEM_EAST = new Box(0.125F, 0, 0.4375F, 0.25F, 0.6875F, 0.5625F);

    




    private static final List<Box> SINGLE_CHEST_SHAPE = List.of(
            new Box(0.025F, 0.0F, 0.025F, 0.975F, 0.95F, 0.975F));

    private static final List<Box> TRAPDOOR_EAST_SHAPE = List.of(new Box(0, 0, 0, 0.1825F, 1, 1));
    private static final List<Box> TRAPDOOR_WEST_SHAPE = List.of(new Box(0.8175F, 0, 0, 1, 1, 1));
    private static final List<Box> TRAPDOOR_SOUTH_SHAPE = List.of(new Box(0, 0, 0, 1, 1, 0.1825F));
    private static final List<Box> TRAPDOOR_NORTH_SHAPE = List.of(new Box(0, 0, 0.8175F, 1, 1, 1));
    private static final List<Box> TRAPDOOR_BOTTOM_SHAPE = List.of(new Box(0, 0, 0, 1, 0.1825F, 1));
    private static final List<Box> TRAPDOOR_TOP_SHAPE = List.of(new Box(0, 0.8175F, 0, 1, 1, 1));

    private static final List<Box> DOOR_NORTH_SHAPE = List.of(new Box(0, 0, 0, 1, 1, 0.1825F));
    private static final List<Box> DOOR_SOUTH_SHAPE = List.of(new Box(0, 0, 0.8175F, 1, 1, 1));
    private static final List<Box> DOOR_EAST_SHAPE = List.of(new Box(0.8175F, 0, 0, 1, 1, 1));
    private static final List<Box> DOOR_WEST_SHAPE = List.of(new Box(0, 0, 0, 0.1825F, 1, 1));

    private static final List<Box> LADDER_NORTH_SHAPE = List.of(new Box(0, 0, 0.8125F, 1, 1, 1));
    private static final List<Box> LADDER_SOUTH_SHAPE = List.of(new Box(0, 0, 0, 1, 1, 0.1875F));
    private static final List<Box> LADDER_WEST_SHAPE = List.of(new Box(0.8125F, 0, 0, 1, 1, 1));
    private static final List<Box> LADDER_EAST_SHAPE = List.of(new Box(0, 0, 0, 0.1875F, 1, 1));

    private static final List<Box> LANTERN_SHAPE = List.of(new Box(0.3125F, 0, 0.3125F, 0.6875F, 0.5F, 0.6875F));
    private static final List<Box> HANGING_LANTERN_SHAPE = List.of(new Box(0.3125F, 0.125F, 0.3125F, 0.6875F, 0.625F, 0.6875F));
    private static final List<Box> WATER_LILY_SHAPE = List.of(new Box(0.0625F, 0, 0.0625F, 0.9375F, 0.09375F, 0.9375F));
    private static final List<Box> ANVIL_X_SHAPE = List.of(new Box(0, 0, 0.125F, 1, 1, 0.875F));
    private static final List<Box> ANVIL_Z_SHAPE = List.of(new Box(0.125F, 0, 0, 0.875F, 1, 1));
    private static final List<Box> END_PORTAL_FRAME_SHAPE = List.of(new Box(0, 0, 0, 1, 0.8125F, 1));

    private static final int DATA_FLAG_DESCEND_THROUGH_BLOCK = 71;
    private static final List<Box> SCAFFOLDING_NORMAL_SHAPE;
    private static final List<Box> CAULDRON_SHAPE;

    static {
        SCAFFOLDING_NORMAL_SHAPE = List.of(
                new Box(0, 0.875F, 0, 1, 1, 1)
        );

        float f = 0.125F;
        CAULDRON_SHAPE = List.of(
                new Box(0, 0, 0, 1, 0.3125F, 1),
                new Box(0, 0, 0, f, 1, 1),
                new Box(0, 0, 0, 1, 1, f),
                new Box(1 - f, 0, 0, 1, 1, 1),
                new Box(0, 0, 1 - f, 1, 1, 1)
        );
    }

    private BedrockCollision() {
    }

    public static List<Box> getCollisionBox(final GhostPlayer player, final Box box,
                                            final BlockVector3 position, final BlockLegacy blockState) {
        if (blockState == null || position == null) {
            return EMPTY_SHAPE;
        }

        final Block state = blockState.getBlock();
        final NetworkBlockState network = blockState.getNetworkState();

        
        if (player != null && position.getY() == player.entityContext.blockSource.getMinY() - 41) {
            return SOLID_SHAPE;
        }
        if (state == null || state.getId() == BlockID.AIR
                || network.is("minecraft:air")
                || network.is("minecraft:cave_air")
                || network.is("minecraft:void_air")) {
            return EMPTY_SHAPE;
        }

        
        if (state instanceof BlockLadder || network.is("minecraft:ladder")) {
            return ladderCollision(state, network);
        }

        
        
        
        if (!(state instanceof BlockScaffolding)
                && !network.is("minecraft:scaffolding")
                && blockState.hasClimbableProperty()) {
            return EMPTY_SHAPE;
        }

        
        if (isMovementPassThrough(state, network)) {
            return EMPTY_SHAPE;
        }

        if (state instanceof BlockSnowLayer || network.is("minecraft:snow_layer")) {
            return snowLayerCollision(state, network);
        }
        if (state instanceof BlockFarmland || state instanceof BlockGrassPath
                || network.is("minecraft:farmland") || network.is("minecraft:grass_path")
                || network.is("minecraft:dirt_path")) {
            return FARMLAND_SHAPE;
        }
        if (state instanceof BlockCake || network.is("minecraft:cake")) {
            return cakeCollision(state, network);
        }
        if (state instanceof BlockDaylightDetector || network.identifierContains("daylight_detector")) {
            return DAYLIGHT_DETECTOR_SHAPE;
        }
        if (state instanceof BlockTurtleEgg || network.is("minecraft:turtle_egg")) {
            return TURTLE_EGG_SHAPE;
        }
        if (state.getId() == BlockID.DRAGON_EGG || network.is("minecraft:dragon_egg")) {
            return SOLID_SHAPE;
        }
        if (state instanceof BlockChain || network.is("minecraft:chain") || network.identifier().endsWith("_chain")) {
            return chainCollision(state, network);
        }
        if (state instanceof BlockEndRod || network.is("minecraft:end_rod")) {
            return endRodCollision(state, network);
        }
        if (state instanceof BlockLightningRodBase || network.identifierContains("lightning_rod")) {
            return lightningRodCollision(state, network);
        }
        if (state instanceof BlockAmethystCluster || network.is("minecraft:amethyst_cluster")) {
            return amethystClusterCollision(state, network);
        }
        if (state instanceof BlockSculkSensor || state instanceof BlockSculkShrieker
                || network.identifierContains("sculk_sensor") || network.is("minecraft:sculk_shrieker")) {
            return SCULK_HALF_BLOCK_SHAPE;
        }
        if (state instanceof BlockDecoratedPot || network.is("minecraft:decorated_pot")) {
            return DECORATED_POT_SHAPE;
        }
        if (state instanceof BlockSporeBlossom || network.is("minecraft:spore_blossom")) {
            return EMPTY_SHAPE;
        }
        if (state instanceof BlockDripleafBig || network.is("minecraft:big_dripleaf")) {
            return bigDripleafCollision(state, network);
        }

        if (isBellFloor(state, network)) {
            final List<Box> base = GeyserCollisionData.getBlockShape(network, state);
            if (base == null) {
                return localNukkitShape(state, 0.1875F);
            }
            final List<Box> collisions = new ArrayList<>(base.size());
            for (Box collision : base) {
                collisions.add(new Box(collision.minX, collision.minY, collision.minZ,
                        collision.maxX, collision.maxY - 0.1875F, collision.maxZ));
            }
            return collisions;
        }

        if (state.getId() == BlockID.POINTED_DRIPSTONE || network.is("minecraft:pointed_dripstone")) {
            if (player == null || box == null
                    || !player.entityContext.playerActionComponent.actions().contains(AuthInputAction.VERTICAL_COLLISION)) {
                return EMPTY_SHAPE;
            }

            List<Box> original = GeyserCollisionData.getBlockShape(network, state);
            if (original == null || original.isEmpty()) {
                original = localNukkitShape(state, 0.0F);
            }
            if (original.isEmpty()) {
                return EMPTY_SHAPE;
            }

            float minY = 1.0F;
            float maxY = 0.0F;
            for (Box collision : original) {
                minY = Math.min(minY, collision.minY);
                maxY = Math.max(maxY, collision.maxY);
            }

            List<Box> vertical = List.of(new Box(0, minY, 0, 1, maxY, 1));
            Box verticalOffset = vertical.get(0).offset(position.getX(), position.getY(), position.getZ());
            boolean likelyYCollision = verticalOffset.calculateMaxDistance(
                    BlockFace.Axis.Y, player.entityContext.aabbShapeComponent.getAABB(), player.entityContext.stateVectorComponent.getDelta().y) != player.entityContext.stateVectorComponent.getDelta().y;
            return likelyYCollision && verticalOffset.intersects(box) ? vertical : EMPTY_SHAPE;
        }

        if (state instanceof BlockEndPortalFrame || network.is("minecraft:end_portal_frame")) {
            return END_PORTAL_FRAME_SHAPE;
        }

        if (state instanceof BlockPowderSnow || network.is("minecraft:powder_snow")) {
            return PowderSnowBlock.getCollisionShape(player, position);
        }

        if (state instanceof BlockAnvil || network.identifierContains("anvil")) {
            BlockFace face = horizontalFacing(network,
                    state instanceof BlockAnvil anvil ? anvil.getBlockFace() : BlockFace.NORTH);
            return face.getAxis() == BlockFace.Axis.X ? ANVIL_X_SHAPE : ANVIL_Z_SHAPE;
        }

        if (state instanceof BlockPressurePlateBase || network.identifierContains("pressure_plate")) {
            return EMPTY_SHAPE;
        }

        if (state instanceof BlockDoor || network.identifierContains("_door")) {
            return doorCollision(player, position, state, network);
        }
        if (state instanceof BlockTrapdoor || network.identifierContains("trapdoor")) {
            return trapdoorCollision(state, network);
        }
        if (state instanceof BlockEnderChest || network.is("minecraft:ender_chest")) {
            return SINGLE_CHEST_SHAPE;
        }
        if (state instanceof BlockChest || network.identifierContains("chest")) {
            return SINGLE_CHEST_SHAPE;
        }
        if (state instanceof BlockCauldron || network.identifierContains("cauldron")) {
            return CAULDRON_SHAPE;
        }
        if (state instanceof BlockFenceGate || network.identifierContains("fence_gate")) {
            return fenceGateCollision(state, network);
        }
        if (state instanceof BlockScaffolding || network.is("minecraft:scaffolding")) {
            if (player == null) {
                return EMPTY_SHAPE;
            }
            








            final float top = position.getY() + 1.0F;
            final boolean atOrAboveTop = player.entityContext.aabbShapeComponent.getAABB().minY
                    >= top - Box.COLLISION_EPSILON;
            return atOrAboveTop && !player.entityContext.actorDataFlagComponent.has(
                    DATA_FLAG_DESCEND_THROUGH_BLOCK)
                    ? SCAFFOLDING_NORMAL_SHAPE : EMPTY_SHAPE;
        }

        if (state instanceof BlockLantern
                || is(network, state, "minecraft:lantern", BlockID.LANTERN)
                || is(network, state, "minecraft:soul_lantern", BlockID.SOUL_LANTERN)) {
            boolean hanging = network.booleanProperty("hanging",
                    state instanceof BlockLantern lantern && lantern.isHanging());
            return hanging ? HANGING_LANTERN_SHAPE : LANTERN_SHAPE;
        }
        if (network.is("minecraft:sea_pickle") || state.getId() == BlockID.SEA_PICKLE) {
            return EMPTY_SHAPE;
        }
        if (network.identifierContains("_bed") || state.getId() == BlockID.BED_BLOCK) {
            return BED_SHAPE;
        }
        if (network.is("minecraft:honey_block") || state.getId() == BlockID.HONEY_BLOCK) {
            return HONEY_SHAPE;
        }
        if (network.is("minecraft:lectern") || state.getId() == BlockID.LECTERN) {
            return LECTERN_SHAPE;
        }
        if (network.is("minecraft:soul_sand") || state.getId() == BlockID.SOUL_SAND) {
            return SOUL_SAND_SHAPE;
        }
        if (network.is("minecraft:enchanting_table") || state.getId() == BlockID.ENCHANTING_TABLE) {
            return ENCHANTING_TABLE_SHAPE;
        }
        if (network.is("minecraft:brewing_stand") || state.getId() == BlockID.BREWING_STAND_BLOCK) {
            return BREWING_STAND_SHAPE;
        }
        if (network.is("minecraft:conduit") || state.getId() == BlockID.CONDUIT) {
            return CONDUIT_SHAPE;
        }
        if (network.is("minecraft:cactus") || state.getId() == BlockID.CACTUS) {
            return CACTUS_SHAPE;
        }
        if (network.is("minecraft:waterlily") || network.is("minecraft:water_lily")
                || state.getId() == BlockID.WATER_LILY) {
            return WATER_LILY_SHAPE;
        }

        return null;
    }

    public static List<Box> getSupportBox(final Block state) {
        return isFullBlock(state) ? SOLID_SHAPE : EMPTY_SHAPE;
    }

    public static boolean isFullBlock(final Block state) {
        return GeyserCollisionData.isSolidCollisionEquivalent(state);
    }


    private static boolean isMovementPassThrough(Block state, NetworkBlockState network) {
        final String identifier = network.identifier();
        return state.getId() == BlockID.BUBBLE_COLUMN
                || state instanceof BlockEndPortal
                || state instanceof BlockFire
                || state instanceof BlockItemFrame
                || state instanceof BlockNetherPortal
                || state instanceof BlockRail
                || state instanceof BlockSapling
                || state instanceof BlockTripWire
                || state instanceof BlockWitherRose
                || network.is("minecraft:bubble_column")
                || network.is("minecraft:end_portal")
                || network.is("minecraft:fire")
                || network.is("minecraft:soul_fire")
                || network.is("minecraft:frame")
                || network.is("minecraft:glow_frame")
                || network.is("minecraft:portal")
                || network.is("minecraft:nether_portal")
                || network.is("minecraft:trip_wire")
                || network.is("minecraft:wither_rose")
                || identifier.endsWith("_rail")
                || identifier.endsWith("_sapling");
    }

    private static List<Box> ladderCollision(Block state, NetworkBlockState network) {
        BlockFace face = horizontalFacing(network, null);
        if (face == null && network.hasProperty("facing_direction")) {
            face = facingDirection(network, null);
        }
        if (face == null && state instanceof BlockLadder ladder) {
            face = switch (ladder.getDamage() & 7) {
                case 3 -> BlockFace.SOUTH;
                case 4 -> BlockFace.WEST;
                case 5 -> BlockFace.EAST;
                case 2 -> BlockFace.NORTH;
                default -> null;
            };
        }
        if (face == null) {
            return EMPTY_SHAPE;
        }
        return switch (face) {
            case SOUTH -> LADDER_SOUTH_SHAPE;
            case WEST -> LADDER_WEST_SHAPE;
            case EAST -> LADDER_EAST_SHAPE;
            default -> LADDER_NORTH_SHAPE;
        };
    }

    private static List<Box> snowLayerCollision(Block state, NetworkBlockState network) {
        int layersMinusOne = network.intProperty("height", state.getDamage() & 7);
        layersMinusOne = Math.max(0, Math.min(7, layersMinusOne));
        if (layersMinusOne == 0) {
            return EMPTY_SHAPE;
        }
        return List.of(new Box(0, 0, 0, 1, layersMinusOne * 0.125F, 1));
    }

    private static List<Box> cakeCollision(Block state, NetworkBlockState network) {
        int bites = network.intProperty("bite_counter", state.getDamage() & 7);
        bites = Math.max(0, Math.min(6, bites));
        float minX = (1 + bites * 2) / 16.0F;
        return List.of(new Box(minX, 0, 0.0625F, 0.9375F, 0.5F, 0.9375F));
    }

    private static List<Box> chainCollision(Block state, NetworkBlockState network) {
        String axisName = network.stringProperty("pillar_axis", "");
        BlockFace.Axis axis = switch (axisName.toLowerCase(java.util.Locale.ROOT)) {
            case "x" -> BlockFace.Axis.X;
            case "z" -> BlockFace.Axis.Z;
            case "y" -> BlockFace.Axis.Y;
            default -> state instanceof BlockChain chain ? chain.getPillarAxis() : BlockFace.Axis.Y;
        };
        return switch (axis) {
            case X -> CHAIN_X_SHAPE;
            case Z -> CHAIN_Z_SHAPE;
            default -> CHAIN_Y_SHAPE;
        };
    }

    private static List<Box> endRodCollision(Block state, NetworkBlockState network) {
        BlockFace face = facingDirection(network,
                state instanceof BlockEndRod rod ? rod.getBlockFace() : BlockFace.UP);
        return switch (face.getAxis()) {
            case X -> END_ROD_X_SHAPE;
            case Z -> END_ROD_Z_SHAPE;
            default -> END_ROD_Y_SHAPE;
        };
    }

    private static List<Box> lightningRodCollision(Block state, NetworkBlockState network) {
        BlockFace face = facingDirection(network,
                state instanceof BlockLightningRodBase rod ? rod.getBlockFace() : BlockFace.UP);
        return switch (face.getAxis()) {
            case X -> LIGHTNING_ROD_X_SHAPE;
            case Z -> LIGHTNING_ROD_Z_SHAPE;
            default -> LIGHTNING_ROD_Y_SHAPE;
        };
    }

    private static List<Box> amethystClusterCollision(Block state, NetworkBlockState network) {
        BlockFace fallback = state instanceof BlockAmethystCluster cluster
                ? cluster.getBlockFace() : BlockFace.UP;
        return attachedDirectionalCrystal(facingDirection(network, fallback),
                7.0F / 16.0F, 3.0F / 16.0F);
    }

    private static List<Box> attachedDirectionalCrystal(BlockFace face, float height, float offset) {
        return List.of(switch (face) {
            case DOWN -> new Box(offset, 1.0F - height, offset, 1.0F - offset, 1.0F, 1.0F - offset);
            case NORTH -> new Box(offset, offset, 1.0F - height, 1.0F - offset, 1.0F - offset, 1.0F);
            case SOUTH -> new Box(offset, offset, 0, 1.0F - offset, 1.0F - offset, height);
            case WEST -> new Box(0, offset, offset, height, 1.0F - offset, 1.0F - offset);
            case EAST -> new Box(1.0F - height, offset, offset, 1.0F, 1.0F - offset, 1.0F - offset);
            default -> new Box(offset, 0, offset, 1.0F - offset, height, 1.0F - offset);
        });
    }

    private static List<Box> bigDripleafCollision(Block state, NetworkBlockState network) {
        boolean hasHead = network.booleanProperty("big_dripleaf_head",
                !(state instanceof BlockDripleafBig dripleaf) || dripleaf.hasHead());
        String tiltName = network.stringProperty("big_dripleaf_tilt", "");
        boolean stable;
        if (!tiltName.isEmpty()) {
            stable = "none".equalsIgnoreCase(tiltName);
        } else if (state instanceof BlockDripleafBig dripleaf) {
            DripleafTilt tilt = dripleaf.getTilt();
            stable = tilt != null && tilt.isStable();
        } else {
            stable = true;
        }
        if (!hasHead || !stable) {
            return EMPTY_SHAPE;
        }

        BlockFace face = horizontalFacing(network,
                state instanceof BlockDripleafBig dripleaf ? dripleaf.getBlockFace() : BlockFace.NORTH);
        Box stem = switch (face) {
            case SOUTH -> BIG_DRIPLEAF_STEM_SOUTH;
            case WEST -> BIG_DRIPLEAF_STEM_WEST;
            case EAST -> BIG_DRIPLEAF_STEM_EAST;
            default -> BIG_DRIPLEAF_STEM_NORTH;
        };
        return List.of(BIG_DRIPLEAF_LEAF, stem);
    }

    private static List<Box> fenceGateCollision(Block state, NetworkBlockState network) {
        boolean open = network.booleanProperty("open_bit",
                state instanceof BlockFenceGate gate && gate.isOpen());
        if (open) {
            return EMPTY_SHAPE;
        }
        BlockFace face = horizontalFacing(network,
                state instanceof BlockFenceGate gate ? gate.getBlockFace() : BlockFace.NORTH);
        if (face.getAxis() == BlockFace.Axis.X) {
            return List.of(new Box(0.375F, 0, 0, 0.625F, 1.5F, 1));
        }
        return List.of(new Box(0, 0, 0.375F, 1, 1.5F, 0.625F));
    }

    private static List<Box> localNukkitShape(Block state, float lowerMaxY) {
        var bb = state.getCollisionBoundingBox();
        if (bb == null) {
            return EMPTY_SHAPE;
        }
        double x = state.getFloorX();
        double y = state.getFloorY();
        double z = state.getFloorZ();
        return List.of(new Box(
                bb.getMinX() - x,
                bb.getMinY() - y,
                bb.getMinZ() - z,
                bb.getMaxX() - x,
                bb.getMaxY() - y - lowerMaxY,
                bb.getMaxZ() - z
        ));
    }

    private static BlockFace facingDirection(NetworkBlockState network, BlockFace fallback) {
        BlockFace horizontal = horizontalFacing(network, null);
        if (horizontal != null) {
            return horizontal;
        }
        return switch (network.intProperty("facing_direction", faceIndex(fallback))) {
            case 0 -> BlockFace.DOWN;
            case 1 -> BlockFace.UP;
            case 2 -> BlockFace.NORTH;
            case 3 -> BlockFace.SOUTH;
            case 4 -> BlockFace.WEST;
            case 5 -> BlockFace.EAST;
            default -> fallback == null ? BlockFace.UP : fallback;
        };
    }

    private static int faceIndex(BlockFace face) {
        if (face == null) return 1;
        return switch (face) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
            default -> 1;
        };
    }

    private static boolean isBellFloor(Block state, NetworkBlockState network) {
        if (!(state instanceof BlockBell) && !network.is("minecraft:bell")) {
            return false;
        }
        String attachment = network.stringProperty("attachment",
                network.stringProperty("bell_attachment", ""));
        if (!attachment.isEmpty()) {
            return "standing".equalsIgnoreCase(attachment) || "floor".equalsIgnoreCase(attachment);
        }
        return state instanceof BlockBell bell
                && bell.getAttachmentType() == BlockBell.TYPE_ATTACHMENT_STANDING;
    }

    private static List<Box> trapdoorCollision(Block state, NetworkBlockState network) {
        final int damage = state.getDamage();
        boolean open = network.booleanProperty("open_bit",
                (damage & BlockTrapdoor.TRAPDOOR_OPEN_BIT) != 0);
        if (!open) {
            boolean top = network.booleanProperty("upside_down_bit",
                    (damage & BlockTrapdoor.TRAPDOOR_TOP_BIT) != 0);
            return top ? TRAPDOOR_TOP_SHAPE : TRAPDOOR_BOTTOM_SHAPE;
        }

        BlockFace facing = horizontalFacing(network, null);
        if (facing == null) {
            
            
            
            
            facing = switch (network.intProperty("direction", damage & BlockTrapdoor.DIRECTION_MASK)) {
                case 1 -> BlockFace.WEST;
                case 2 -> BlockFace.SOUTH;
                case 3 -> BlockFace.NORTH;
                default -> BlockFace.EAST;
            };
        }
        return switch (facing) {
            case SOUTH -> TRAPDOOR_SOUTH_SHAPE;
            case WEST -> TRAPDOOR_WEST_SHAPE;
            case EAST -> TRAPDOOR_EAST_SHAPE;
            default -> TRAPDOOR_NORTH_SHAPE;
        };
    }

    private static List<Box> doorCollision(GhostPlayer player, BlockVector3 pos,
                                           Block state, NetworkBlockState network) {
        
        
        
        Block lowerBlock = state;
        Block upperBlock = state;
        NetworkBlockState lowerNetwork = network;
        NetworkBlockState upperNetwork = network;

        boolean top = network.booleanProperty("upper_block_bit",
                state instanceof BlockDoor door && door.isTop());
        if (player != null) {
            BlockLegacy paired = player.entityContext.blockSource.getBlockState(
                    pos.getX(), pos.getY() + (top ? -1 : 1), pos.getZ(), 0
            );
            if (sameDoor(network, state, paired)) {
                if (top) {
                    lowerBlock = paired.getBlock();
                    lowerNetwork = paired.getNetworkState();
                } else {
                    upperBlock = paired.getBlock();
                    upperNetwork = paired.getNetworkState();
                }
            }
        }

        int lowerDamage = lowerBlock.getDamage();
        int upperDamage = upperBlock.getDamage();

        
        
        BlockFace direction = doorFacing(lowerNetwork, lowerBlock);
        if (direction == null) {
            direction = BlockFace.fromHorizontalIndex(
                    lowerDamage & BlockDoor.DOOR_DIRECTION_BIT
            ).rotateYCCW();
        }

        boolean open = lowerNetwork.hasProperty("open_bit")
                ? lowerNetwork.booleanProperty("open_bit", false)
                : (lowerDamage & BlockDoor.DOOR_OPEN_BIT) != 0;
        boolean rightHinged = upperNetwork.hasProperty("door_hinge_bit")
                ? upperNetwork.booleanProperty("door_hinge_bit", false)
                : (upperDamage & BlockDoor.DOOR_HINGE_BIT) != 0;

        return switch (direction) {
            case SOUTH -> !open ? DOOR_NORTH_SHAPE : (rightHinged ? DOOR_WEST_SHAPE : DOOR_EAST_SHAPE);
            case WEST -> !open ? DOOR_EAST_SHAPE : (rightHinged ? DOOR_NORTH_SHAPE : DOOR_SOUTH_SHAPE);
            case NORTH -> !open ? DOOR_SOUTH_SHAPE : (rightHinged ? DOOR_EAST_SHAPE : DOOR_WEST_SHAPE);
            default -> !open ? DOOR_WEST_SHAPE : (rightHinged ? DOOR_SOUTH_SHAPE : DOOR_NORTH_SHAPE);
        };
    }

    private static boolean sameDoor(NetworkBlockState currentNetwork, Block currentBlock,
                                    BlockLegacy paired) {
        if (paired == null || paired.getBlock() == null) {
            return false;
        }
        NetworkBlockState pairedNetwork = paired.getNetworkState();
        String currentIdentifier = currentNetwork.identifier();
        String pairedIdentifier = pairedNetwork.identifier();
        if (!currentIdentifier.isEmpty() && !pairedIdentifier.isEmpty()) {
            return currentIdentifier.equals(pairedIdentifier);
        }
        return paired.getBlock() instanceof BlockDoor
                && currentBlock instanceof BlockDoor
                && paired.getBlock().getId() == currentBlock.getId();
    }

    private static BlockFace doorFacing(NetworkBlockState network, Block state) {
        BlockFace direction = horizontalFacing(network, null);
        if (direction != null) {
            return direction.rotateYCCW();
        }
        if (network.hasProperty("direction")) {
            int legacyDirection = network.intProperty(
                    "direction", state.getDamage() & BlockDoor.DOOR_DIRECTION_BIT
            );
            return BlockFace.fromHorizontalIndex(
                    legacyDirection & BlockDoor.DOOR_DIRECTION_BIT
            ).rotateYCCW();
        }
        return null;
    }

    private static BlockFace horizontalFacing(NetworkBlockState network, BlockFace fallback) {
        BlockFace result = DynamicCollision.horizontalFace(network.stringProperty(
                "minecraft:cardinal_direction", network.stringProperty("cardinal_direction", "")));
        return result == null ? fallback : result;
    }

    private static boolean is(NetworkBlockState network, Block state, String identifier, int id) {
        return network.is(identifier) || state.getId() == id;
    }
}
