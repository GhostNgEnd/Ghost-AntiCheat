package ac.ghost.anticheat.prediction.bds.system.liquid.water;

import ac.ghost.anticheat.data.FluidState;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Mutable;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;


public final class UnderWaterSensingSystem {
    private UnderWaterSensingSystem() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.actorHeadWasInWaterFlagComponent.setPresent(
                player.entityContext.actorHeadInWaterFlagComponent.isPresent());
        final Vec3 head = player.entityContext.stateVectorComponent.getPosition().up(
                player.entityContext.aabbShapeComponent.getDimensions().eyeHeight());
        final BlockVector3 block = head.toBlockVector3();
        final FluidState state = player.entityContext.localConstBlockSourceFactoryComponent
                .create().getFluidState(block);
        if (state.fluid() != FluidState.FluidType.WATER) {
            player.entityContext.actorHeadInWaterFlagComponent.setPresent(false);
            return;
        }

        final Mutable mutable = new Mutable().set(block);
        final float surfaceY = block.getY() + state.getHeight(player, mutable);
        player.entityContext.actorHeadInWaterFlagComponent.setPresent(head.y < surfaceY);
    }
}
