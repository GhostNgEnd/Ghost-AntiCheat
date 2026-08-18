package ac.ghost.anticheat.data;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Mutable;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.block.BlockID;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;

public class FluidState {
    public enum FluidType {
        WATER, LAVA, EMPTY
    }

    private final FluidType fluid;
    private final float height;
    private final int level;

    public FluidState(FluidType fluid, float height, int level) {
        this.fluid = fluid;
        this.height = height;
        this.level = level;
    }

    public FluidType fluid() {
        return this.fluid;
    }

    public float height() {
        return this.height;
    }

    public int level() {
        return this.level;
    }

    public float getHeight(final GhostPlayer player, final Mutable pos) {
        return isFluidAboveEqual(player, pos) ? 1.0F : this.height();
    }

    private boolean isFluidAboveEqual(GhostPlayer player, Mutable pos) {
        return fluid == player.entityContext.blockSource.getFluidState(pos.getX(), pos.getY() + 1, pos.getZ()).fluid();
    }

    public Vec3 getFlow(final GhostPlayer player, final BlockVector3 vector3i) {
        if (player.entityContext.blockSource.getBlockState(vector3i, 0).getBlock().getId() == BlockID.BUBBLE_COLUMN) {
            return new Vec3(0, 0, 0);
        }

        Vec3 vec3 = new Vec3(0, 0, 0);
        int i = this.getEffectiveFlowDecay();

        final Mutable mutable = new Mutable();
        for (BlockFace direction : BlockFace.HORIZONTALS) {
            mutable.set(vector3i.getX() + direction.getXOffset(), vector3i.getY() + direction.getYOffset(), vector3i.getZ() + direction.getZOffset());
            final FluidState fluidState1 = player.entityContext.blockSource.getFluidState(mutable);
            int j = fluidState1.fluid() == this.fluid() ? fluidState1.getEffectiveFlowDecay() : -1;

            if (j < 0) {
                if (!player.entityContext.blockSource.getBlockState(mutable, 0).blocksMotion(player)) {
                    FluidState below = player.entityContext.blockSource.getFluidState(new BlockVector3(mutable.getX(), mutable.getY() - 1, mutable.getZ()));
                    if (below.fluid() == this.fluid()) {
                        j = below.getEffectiveFlowDecay();
                        if (j >= 0) {
                            int k = j - (i - 8);
                            vec3 = vec3.add((mutable.getX() - vector3i.getX()) * k, (mutable.getY() - vector3i.getY()) * k, (mutable.getZ() - vector3i.getZ()) * k);
                        }
                    }
                }
            } else {
                int l = j - i;
                vec3 = vec3.add((mutable.getX() - vector3i.getX()) * l, (mutable.getY() - vector3i.getY()) * l, (mutable.getZ() - vector3i.getZ()) * l);
            }
        }

        if (this.level() >= 8) {
            for (BlockFace direction : BlockFace.HORIZONTALS) {
                BlockVector3 blockpos1 = new BlockVector3(vector3i.getX() + direction.getXOffset(), vector3i.getY() + direction.getYOffset(), vector3i.getZ() + direction.getZOffset());

                if (this.isSolidFace(player, blockpos1, direction) || this.isSolidFace(player, blockpos1.up(), direction)) {
                    vec3 = vec3.normalize().add(0, -6, 0);
                    break;
                }
            }
        }

        return vec3.normalize();
    }

    public int getEffectiveFlowDecay() {
        return this.level() >= 8 ? 0 : this.level();
    }

    private boolean isSolidFace(GhostPlayer player, BlockVector3 blockPos, BlockFace direction) {
        BlockLegacy blockState = player.entityContext.blockSource.getBlockState(blockPos, 0);
        FluidState fluidState = player.entityContext.blockSource.getFluidState(blockPos);
        if (fluidState.fluid() == fluid()) {
            return false;
        }
        if (direction == BlockFace.UP) {
            return true;
        }
        final int id = blockState.getBlock().getId();
        if (id == BlockID.ICE || id == BlockID.FROSTED_ICE || id == BlockID.BLUE_ICE || id == BlockID.PACKED_ICE) {
            return false;
        }
        return blockState.isFaceSturdy(player);
    }
}
