package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.InsideBubbleColumnBlockComponent;
import ac.ghost.anticheat.prediction.bds.component.ServerPlayerMovementComponent;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Mutable;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockBubbleColumn;
import cn.nukkit.block.BlockID;
import cn.nukkit.math.BlockVector3;


public final class EntityInsideSystem {
    private static final Vec3 WEB_MOVEMENT_MULTIPLIER =
            new Vec3(0.25F, 0.05F, 0.25F);

    private EntityInsideSystem() {
    }

    



    public static void tickSetEntityInside(final GhostPlayer player) {
        if (player.entityContext.movementAbilitiesComponent.isNoClip()) {
            player.entityContext.insidePowderSnowBlockComponent.clear();
            return;
        }


        
        
        
        
        final BlockVector3 min = new BlockVector3(
                floorBlock(player.entityContext.aabbShapeComponent.getAABB().minX),
                floorBlock(player.entityContext.aabbShapeComponent.getAABB().minY),
                floorBlock(player.entityContext.aabbShapeComponent.getAABB().minZ));
        final BlockVector3 max = new BlockVector3(
                floorExclusiveMax(player.entityContext.aabbShapeComponent.getAABB().maxX),
                floorExclusiveMax(player.entityContext.aabbShapeComponent.getAABB().maxY),
                floorExclusiveMax(player.entityContext.aabbShapeComponent.getAABB().maxZ));

        final Mutable mutable = new Mutable();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    mutable.set(x, y, z);
                    SetEntityInsideSystem.setEntityInside(
                            player,
                            player.entityContext.localConstBlockSourceFactoryComponent.create()
                                    .getBlockState(x, y, z, 0),
                            mutable);
                }
            }
        }

        mergeInsideOnewayBlocks(player);
    }

    
    public static void restoreReplayInput(final GhostPlayer player) {
        if (!player.entityContext.antiCheatRewindFlagComponent.isPresent()) {
            return;
        }
        final ServerPlayerMovementComponent.HistoryRecord frame =
                player.entityContext.serverPlayerMovementComponent.find(
                        player.entityContext.replayStateComponent.getInputTick());
        if (frame == null) {
            return;
        }
        if (frame.insideWebBlock()) {
            player.entityContext.insideWebBlockComponent.markPresent();
        }
        if (frame.insidePowderSnowBlock()) {
            player.entityContext.insidePowderSnowBlockComponent.markReplayPresence();
        }
    }

    
    public static void applyReplayMovementSlowdown(final GhostPlayer player) {
        if (!player.entityContext.antiCheatRewindFlagComponent.isPresent()) {
            return;
        }
        SweetBerryBushMovementSlowdownSystem.tick(player);
        applyWebMovementSlowdown(player);
        InsidePowderSnowBlockSystem.applyMovementSlowdown(player);
        BlockMovementSlowdownMultiplierSystem.resistantMob(player);
        BlockMovementSlowdownMultiplierSystem.immunePlayer(player);
    }

    
    public static void collectHoneyBlock(final GhostPlayer player,
                                         final BlockLegacy blockState,
                                         final Mutable position) {
        final Block block = blockState.getBlock();
        if (block == null
                || block.getId() != BlockID.HONEY_BLOCK
                && !blockState.getNetworkState().is("minecraft:honey_block")) {
            return;
        }

        player.entityContext.insideHoneyBlockComponent.add(
                new BlockVector3(position.getX(), position.getY(), position.getZ()),
                blockState);
    }

    







    public static void collectOnewayBlock(final GhostPlayer player,
                                          final BlockLegacy blockState,
                                          final BlockVector3 position) {
        if (!blockState.isScaffolding()) {
            return;
        }

        final float x = position.getX();
        final float y = position.getY();
        final float z = position.getZ();
        player.entityContext.insideOnewayBlockComponent.add(
                new Box(
                        x, y, z, x + 1.0F, y + 1.0F, z + 1.0F));
    }

    





    private static void mergeInsideOnewayBlocks(final GhostPlayer player) {
        for (Box box : player.entityContext.insideOnewayBlockComponent.boxes()) {
            boolean duplicate = false;
            for (Box saved : player.entityContext.depenetrationComponent.collisionBoxes()) {
                if (sameBox(saved, box)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                player.entityContext.depenetrationComponent.collisionBoxes().add(box);
            }
        }
    }

    private static boolean sameBox(final Box left, final Box right) {
        return left != null && right != null
                && Float.floatToIntBits(left.minX) == Float.floatToIntBits(right.minX)
                && Float.floatToIntBits(left.minY) == Float.floatToIntBits(right.minY)
                && Float.floatToIntBits(left.minZ) == Float.floatToIntBits(right.minZ)
                && Float.floatToIntBits(left.maxX) == Float.floatToIntBits(right.maxX)
                && Float.floatToIntBits(left.maxY) == Float.floatToIntBits(right.maxY)
                && Float.floatToIntBits(left.maxZ) == Float.floatToIntBits(right.maxZ);
    }

    
    public static void collectWebBlock(final GhostPlayer player,
                                       final BlockLegacy blockState,
                                       final BlockVector3 position) {
        if (!blockState.isCobweb()) {
            return;
        }
        player.entityContext.insideWebBlockComponent.markPresent();
    }

    public static void applyWebMovementSlowdown(final GhostPlayer player) {
        if (!player.entityContext.insideWebBlockComponent.isPresent()) {
            return;
        }
        player.entityContext.blockMovementSlowdownMultiplierComponent.add(
                WEB_MOVEMENT_MULTIPLIER);
        player.entityContext.blockMovementSlowdownAppliedComponent.markApplied();
    }

    




    public static void collectBubbleColumn(final GhostPlayer player,
                                           final BlockLegacy blockState,
                                           final Mutable position) {
        final Block block = blockState.getBlock();
        if (!isBubbleColumn(block)) {
            return;
        }

        final boolean dragDown = isBubbleColumnDrag(blockState);
        final BlockLegacy above = player.entityContext.localConstBlockSourceFactoryComponent.create().getBlockState(
                position.getX(), position.getY() + 1, position.getZ(), 0);
        final InsideBubbleColumnBlockComponent.ContactState contactState =
                above.isAir()
                        ? InsideBubbleColumnBlockComponent.ContactState.ABOVE
                        : InsideBubbleColumnBlockComponent.ContactState.INSIDE;

        player.entityContext.insideBubbleColumnBlockComponent.add(
                new BlockVector3(position.getX(), position.getY(), position.getZ()),
                dragDown,
                contactState);
    }

    private static int floorBlock(final float value) {
        return (int) Math.floor(value);
    }

    private static int floorExclusiveMax(final float value) {
        return floorBlock(Math.nextDown(value));
    }

    private static boolean isBubbleColumn(final Block block) {
        return block != null && block.getId() == BlockID.BUBBLE_COLUMN;
    }

    private static boolean isBubbleColumnDrag(final BlockLegacy blockState) {
        if (blockState.getNetworkState().hasProperty("drag_down")) {
            return blockState.getNetworkState()
                    .booleanProperty("drag_down", false);
        }
        if (blockState.getNetworkState().hasProperty("drag_down_bit")) {
            return blockState.getNetworkState()
                    .booleanProperty("drag_down_bit", false);
        }
        final Block block = blockState.getBlock();
        return isBubbleColumn(block)
                && (block.getDamage() & Block.DATA_MASK)
                == BlockBubbleColumn.DIRECTION_DOWN;
    }
}
