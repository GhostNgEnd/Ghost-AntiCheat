package ac.ghost.anticheat.prediction.bds.system.item;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;


public final class ItemUseSlowdownApplySystem {
    private ItemUseSlowdownApplySystem() {
    }

    public static void tick(final GhostPlayer player) {
        if (!player.entityContext.itemUseSlowdownModifierComponent.isPresent()) {
            return;
        }

        final float modifier = player.entityContext.itemUseSlowdownModifierComponent.getValue();
        final float scale = modifier * modifier;
        player.entityContext.moveInputComponent.setEffective(
                player.entityContext.moveInputComponent.getEffectiveX() * scale,
                player.entityContext.moveInputComponent.getEffectiveY() * scale);

        
        
        final Vec3 input = player.entityContext.mobTravelComponent.getInput();
        player.entityContext.mobTravelComponent.setInput(new Vec3(
                input.x * scale, input.y, input.z * scale));
    }
}
