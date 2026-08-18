package ac.ghost.anticheat.prediction.bds.system.liquid.common;

import ac.ghost.anticheat.data.FluidState;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Mutable;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.MathHelper;

import java.util.ArrayList;
import java.util.List;








public final class LiquidPhysicsSystem {
    public static final float WATER_PUSH = 0.014F;
    public static final float LAVA_PUSH = 0.0035F;
    private static final float MIN_FLOW_LENGTH = 0.0001F;
    private static final float MIN_FLOW_LENGTH_SQUARED =
            MIN_FLOW_LENGTH * MIN_FLOW_LENGTH;

    private LiquidPhysicsSystem() {
    }

    public static Sample sampleWater(final GhostPlayer player) {
        return sample(player, FluidState.FluidType.WATER, WATER_PUSH,
                0.001F, 0.401F);
    }

    public static Sample sampleLava(final GhostPlayer player) {
        return sample(player, FluidState.FluidType.LAVA, LAVA_PUSH,
                0.1F, 0.4F);
    }

    



    public static Result sampleLiquids(final GhostPlayer player,
                                       final boolean applyPush) {
        final Sample lava = sampleLava(player);
        final Sample water = sampleWater(player);
        final FluidState.FluidType selectedType =
                selectType(lava.touching(), water.touching());
        final Vec3 selectedPush;
        if (player.entityContext.movementAbilitiesComponent.isFlying()) {
            
            
            
            selectedPush = Vec3.ZERO;
        } else if (selectedType == FluidState.FluidType.LAVA) {
            selectedPush = lava.appliedPush();
        } else if (selectedType == FluidState.FluidType.WATER) {
            selectedPush = water.appliedPush();
        } else {
            selectedPush = Vec3.ZERO;
        }

        if (applyPush && selectedPush.lengthSquared() > 0.0F) {
            player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().add(selectedPush));
        }
        return new Result(water, lava, selectedType, selectedPush);
    }

    public static Vec3 peekSelectedPush(final GhostPlayer player) {
        return sampleLiquids(player, false).selectedPush();
    }

    private static Sample sample(final GhostPlayer player,
                                 final FluidState.FluidType type,
                                 final float pushStrength,
                                 final float horizontalContraction,
                                 final float verticalContraction) {
        if (isRegionUnloaded(player)) {
            return Sample.EMPTY;
        }

        final Box box = contractedQueryBox(
                player.entityContext.aabbShapeComponent.getAABB(),
                horizontalContraction,
                verticalContraction);

        final int minX = MathHelper.floor(box.minX);
        final int maxX = MathHelper.floor(box.maxX + 1.0D);
        final int minY = MathHelper.floor(box.minY);
        final int maxY = MathHelper.floor(box.maxY + 1.0D);
        final int minZ = MathHelper.floor(box.minZ);
        final int maxZ = MathHelper.floor(box.maxZ + 1.0D);

        boolean touching = false;
        boolean downwardFlow = false;
        float highestSurfaceY = -Float.MAX_VALUE;
        Vec3 accumulatedFlow = Vec3.ZERO;
        boolean hasPositiveLiquidDepth = false;
        final List<BlockVector3> liquidCells = new ArrayList<>();
        final Mutable mutable = new Mutable();

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    mutable.set(x, y, z);
                    final FluidState fluid = player.entityContext.localConstBlockSourceFactoryComponent.create().getFluidState(mutable);
                    if (fluid.fluid() != type) {
                        continue;
                    }

                    touching = true;
                    
                    
                    
                    highestSurfaceY = Math.max(highestSurfaceY, y + 1.0F);
                    liquidCells.add(new BlockVector3(x, y, z));
                    hasPositiveLiquidDepth |= fluid.level() > 0
                            || hasPositiveDepthNeighbor(player, type, x, y, z);
                }
            }
        }

        if (hasPositiveLiquidDepth) {
            for (final BlockVector3 cell : liquidCells) {
                final FluidState fluid = player.entityContext.localConstBlockSourceFactoryComponent.create().getFluidState(cell);
                final Vec3 flow = fluid.getFlow(player, cell);
                accumulatedFlow = accumulatedFlow.add(flow);
                downwardFlow |= flow.y < 0.0F;
            }
        }

        Vec3 applied = Vec3.ZERO;
        final float flowLengthSquared = accumulatedFlow.lengthSquared();
        if (touching && hasSignificantFlow(flowLengthSquared)) {
            applied = accumulatedFlow.multiply(
                    pushStrength / (float) Math.sqrt(flowLengthSquared));
        }

        return new Sample(
                touching,
                touching ? highestSurfaceY : 0.0F,
                downwardFlow,
                accumulatedFlow,
                applied,
                box);
    }

    private static boolean isRegionUnloaded(final GhostPlayer player) {
        final Box box = player.entityContext.aabbShapeComponent.getAABB().expand(1);
        final int minX = (int) Math.floor(box.minX);
        final int maxX = (int) Math.ceil(box.maxX);
        final int minZ = (int) Math.floor(box.minZ);
        final int maxZ = (int) Math.ceil(box.maxZ);
        return !player.entityContext.blockSource.hasChunksAt(minX, minZ, maxX, maxZ);
    }

    private static Box contractedQueryBox(final Box source,
                                          final float horizontal,
                                          final float vertical) {
        float minX = source.minX + horizontal;
        float maxX = source.maxX - horizontal;
        float minY = source.minY + vertical;
        float maxY = source.maxY - vertical;
        float minZ = source.minZ + horizontal;
        float maxZ = source.maxZ - horizontal;

        if (minX > maxX) {
            final float middle = (source.minX + source.maxX) * 0.5F;
            minX = maxX = middle;
        }
        if (minY > maxY) {
            final float middle = (source.minY + source.maxY) * 0.5F;
            minY = maxY = middle;
        }
        if (minZ > maxZ) {
            final float middle = (source.minZ + source.maxZ) * 0.5F;
            minZ = maxZ = middle;
        }
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static boolean hasPositiveDepthNeighbor(final GhostPlayer player,
                                                    final FluidState.FluidType type,
                                                    final int x,
                                                    final int y,
                                                    final int z) {
        return hasPositiveDepth(player, type, x - 1, y, z)
                || hasPositiveDepth(player, type, x + 1, y, z)
                || hasPositiveDepth(player, type, x, y, z - 1)
                || hasPositiveDepth(player, type, x, y, z + 1);
    }

    private static boolean hasPositiveDepth(final GhostPlayer player,
                                            final FluidState.FluidType type,
                                            final int x,
                                            final int y,
                                            final int z) {
        final FluidState neighbor =
                player.entityContext.localConstBlockSourceFactoryComponent.create().getFluidState(x, y, z);
        return neighbor.fluid() == type && neighbor.level() > 0;
    }

    static FluidState.FluidType selectType(final boolean touchingLava,
                                           final boolean touchingWater) {
        if (touchingLava) {
            return FluidState.FluidType.LAVA;
        }
        return touchingWater
                ? FluidState.FluidType.WATER
                : FluidState.FluidType.EMPTY;
    }

    static boolean hasSignificantFlow(final float lengthSquared) {
        return lengthSquared >= MIN_FLOW_LENGTH_SQUARED;
    }

    public record Sample(boolean touching,
                         float surfaceHeight,
                         boolean downwardFlow,
                         Vec3 accumulatedFlow,
                         Vec3 appliedPush,
                         Box queryBox) {
        public static final Sample EMPTY = new Sample(
                false, 0.0F, false, Vec3.ZERO, Vec3.ZERO, Box.EMPTY);
    }

    public record Result(Sample water,
                         Sample lava,
                         FluidState.FluidType selectedType,
                         Vec3 selectedPush) {
    }
}
