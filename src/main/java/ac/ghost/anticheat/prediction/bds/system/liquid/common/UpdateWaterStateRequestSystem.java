package ac.ghost.anticheat.prediction.bds.system.liquid.common;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.liquid.lava.InLavaSensingSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.water.InWaterSensingSystem;




public final class UpdateWaterStateRequestSystem {
    private UpdateWaterStateRequestSystem() {
    }

    public static LiquidPhysicsSystem.Result tick(final GhostPlayer player) {
        final LiquidPhysicsSystem.Result liquids =
                LiquidPhysicsSystem.sampleLiquids(player, true);

        InWaterSensingSystem.tick(player, liquids.water());
        TravelTypeSensingSystem.apply(player, liquids);
        InLavaSensingSystem.tick(player, liquids);
        return liquids;
    }
}
