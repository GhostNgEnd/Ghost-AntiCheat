package ac.ghost.anticheat.data.block;

import ac.ghost.anticheat.collision.BedrockCollision;
import ac.ghost.anticheat.collision.DynamicCollision;
import ac.ghost.anticheat.collision.GeyserCollisionData;
import ac.ghost.anticheat.collision.JavaCollisionShape;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;

import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.potion.Effect;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BlockLegacy {
    private final Block block;
    private final NetworkBlockState networkState;
    private final BlockVector3 position;
    private final int layer;

    public BlockLegacy(Block block, BlockVector3 position, int layer) {
        this(block, NetworkBlockState.legacy(block == null ? 0 : block.getFullId()), position, layer);
    }

    public BlockLegacy(Block block, NetworkBlockState networkState, BlockVector3 position, int layer) {
        this.block = block;
        this.networkState = networkState == null
                ? NetworkBlockState.legacy(block == null ? 0 : block.getFullId()) : networkState;
        this.position = position;
        this.layer = layer;
    }

    public boolean isFaceSturdy(GhostPlayer player) {
        if (this.block.getId() == BlockID.SCAFFOLDING) {
            return false;
        }

        
        
        
        List<Box> mapped = GeyserCollisionData.getBlockShape(this.networkState, this.block);
        if (mapped == null) {
            mapped = GeyserCollisionData.getBlockShape(this.block);
        }
        if (mapped != null) {
            return mapped.size() == 1 && mapped.get(0).equals(new Box(0, 0, 0, 1, 1, 1));
        }

        
        
        
        final AxisAlignedBB bb = this.block.getCollisionBoundingBox();
        return bb != null
                && Math.abs((bb.getMaxX() - bb.getMinX()) - 1.0) < 1.0E-6
                && Math.abs((bb.getMaxY() - bb.getMinY()) - 1.0) < 1.0E-6
                && Math.abs((bb.getMaxZ() - bb.getMinZ()) - 1.0) < 1.0E-6;
    }

    public boolean isAir() {
        return this.networkState.is("minecraft:air")
                || this.networkState.is("minecraft:cave_air")
                || this.networkState.is("minecraft:void_air")
                || this.block.getId() == BlockID.AIR;
    }

    public boolean blocksMotion(final GhostPlayer player) {
        return !isMovementCollisionPassThrough(this.block)
                && !isCobweb()
                && !isSweetBerryBush()
                && this.isSolid(player);
    }

    private boolean isSolid(GhostPlayer player) {
        List<Box> boxes = findCollision(player, new BlockVector3(0, 0, 0), Box.EMPTY, false);
        if (boxes.isEmpty()) {
            return false;
        } else {
            Box box = new Box(0, 0, 0, 0, 0, 0);
            for (Box box1 : boxes) {
                box = box1.union(box);
            }

            return box.getAverageSideLength() >= 0.7291666666666666 || box.getLengthY() >= 1.0;
        }
    }

    








    public Box getCollisionShape(final GhostPlayer player,
                                 final BlockVector3 position) {
        final List<Box> fragments = findCollision(
                player, position, player.entityContext.aabbShapeComponent.getAABB(), false);
        if (fragments.isEmpty()) {
            return Box.invalid();
        }

        Box completedShape = fragments.get(0);
        for (int index = 1; index < fragments.size(); index++) {
            completedShape = completedShape.union(fragments.get(index));
        }
        return completedShape;
    }

    public List<Box> findCollision(GhostPlayer player, BlockVector3 pos, Box playerAABB, boolean checkAAB) {
        final List<Box> list = new ArrayList<>();

        
        
        if (pos.getY() == player.entityContext.blockSource.getMinY() - 41) {
            addLocalCollisions(list,
                    BedrockCollision.getCollisionBox(player, playerAABB, pos, this),
                    pos, playerAABB, checkAAB);
            return list;
        }

        if (isMovementCollisionPassThrough(this.block)
                || isSweetBerryBush() || isCobweb()) {
            return list;
        }

        
        
        List<Box> collisions = DynamicCollision.getCollisionBox(player, pos, this);
        if (collisions == null) {
            collisions = BedrockCollision.getCollisionBox(player, playerAABB, pos, this);
        }
        if (collisions == null) {
            collisions = GeyserCollisionData.getBlockShape(this.networkState, this.block);
        }
        if (collisions == null) {
            
            
            
            collisions = JavaCollisionShape.getCollisionBox(player, pos, this.block);
        }
        if (collisions != null) {
            addLocalCollisions(list, collisions, pos, playerAABB, checkAAB);
            return list;
        }

        
        
        
        final AxisAlignedBB nukkitBB = this.block.getCollisionBoundingBox();
        if (nukkitBB != null) {
            final Box worldShape = normalizeCollisionBox(
                    nukkitBB,
                    this.block.getFloorX(), this.block.getFloorY(), this.block.getFloorZ(), pos
            );
            if (!checkAAB || worldShape.intersects(playerAABB)) {
                list.add(worldShape);
            }
        }
        return list;
    }

    private static void addLocalCollisions(List<Box> output, List<Box> local, BlockVector3 pos,
                                           Box playerAABB, boolean checkAAB) {
        for (Box shape : local) {
            Box worldShape = shape.offset(pos.getX(), pos.getY(), pos.getZ());
            if (!checkAAB || worldShape.intersects(playerAABB)) {
                output.add(worldShape);
            }
        }
    }


    static Box normalizeCollisionBox(AxisAlignedBB box, int sourceX, int sourceY, int sourceZ,
                                     BlockVector3 target) {
        return new Box(box).offset(
                target.getX() - sourceX,
                target.getY() - sourceY,
                target.getZ() - sourceZ
        );
    }

    private static boolean isMovementCollisionPassThrough(final Block block) {
        if (block == null) {
            return false;
        }

        final int id = block.getId();
        return isBubbleColumn(block)
                || id == BlockID.COBWEB
                || id == BlockID.SWEET_BERRY_BUSH
                || block.isWater() || Block.isWater(id) || Block.isLava(id);
    }

    private static boolean isBubbleColumn(final Block block) {
        return block != null && block.getId() == BlockID.BUBBLE_COLUMN;
    }

    public boolean isSweetBerryBush() {
        return block.getId() == BlockID.SWEET_BERRY_BUSH
                || networkState.is("minecraft:sweet_berry_bush");
    }

    public int getSweetBerryBushGrowth() {
        if (!isSweetBerryBush()) {
            return 0;
        }
        if (networkState.hasProperty("growth")) {
            return networkState.intProperty("growth", block.getDamage());
        }
        if (networkState.hasProperty("growth_stage")) {
            return networkState.intProperty("growth_stage", block.getDamage());
        }
        return networkState.intProperty("age", block.getDamage());
    }

    public boolean isCobweb() {
        return block.getId() == BlockID.COBWEB
                || networkState.is("minecraft:web")
                || networkState.is("minecraft:cobweb");
    }

    public boolean isPowderSnow() {
        return block.getId() == BlockID.POWDER_SNOW
                || networkState.is("minecraft:powder_snow");
    }

    
    public boolean hasClimbableProperty() {
        if (isScaffolding()) {
            return false;
        }
        final int id = block.getId();
        return id == BlockID.LADDER
                || id == BlockID.VINE
                || id == BlockID.VINES
                || id == BlockID.WEEPING_VINES
                || id == BlockID.TWISTING_VINES
                || id == BlockID.CAVE_VINES
                || id == BlockID.CAVE_VINES_BODY_WITH_BERRIES
                || id == BlockID.CAVE_VINES_HEAD_WITH_BERRIES
                || networkState.is("minecraft:ladder")
                || networkState.is("minecraft:vine")
                || networkState.is("minecraft:vines")
                || networkState.is("minecraft:weeping_vines")
                || networkState.is("minecraft:weeping_vines_plant")
                || networkState.is("minecraft:twisting_vines")
                || networkState.is("minecraft:twisting_vines_plant")
                || networkState.is("minecraft:cave_vines")
                || networkState.is("minecraft:cave_vines_body_with_berries")
                || networkState.is("minecraft:cave_vines_head_with_berries");
    }

    




    public boolean hasClimbHaltProperty() {
        return hasClimbableProperty();
    }

    public boolean isScaffolding() {
        return block.getId() == BlockID.SCAFFOLDING
                || networkState.is("minecraft:scaffolding");
    }

    public boolean isWater() {
        return block.isWater() || Block.isWater(block.getId())
                || networkState.is("minecraft:water")
                || networkState.is("minecraft:flowing_water");
    }

    public boolean isHoneyBlock() {
        return block.getId() == BlockID.HONEY_BLOCK
                || networkState.is("minecraft:honey_block");
    }

    public boolean isSlimeBlock() {
        return block.getId() == BlockID.SLIME_BLOCK
                || networkState.is("minecraft:slime_block");
    }

    public boolean isBed() {
        return block.getId() == BlockID.BED_BLOCK
                || networkState.identifierContains("_bed");
    }

    
    public float getJumpFactor() {
        return isHoneyBlock()
                ? Float.intBitsToFloat(0x3F19999A)
                : 1.0F;
    }

    
    public float getFriction() {
        if (isHoneyBlock() || isSlimeBlock()) {
            return Float.intBitsToFloat(0x3F4CCCCD);
        }

        final int id = block.getId();
        if (id == BlockID.ICE || id == BlockID.PACKED_ICE
                || id == BlockID.FROSTED_ICE) {
            return 0.98F;
        }
        if (id == BlockID.BLUE_ICE) {
            return 0.989F;
        }
        return 0.6F;
    }

    
    public float getCoefficientOfRestitution() {
        if (isSlimeBlock()) {
            return 1.0F;
        }
        if (isBed()) {
            return Float.intBitsToFloat(0x3F400000);
        }
        return 0.0F;
    }

}
