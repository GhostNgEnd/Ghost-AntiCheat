package ac.ghost.anticheat.prediction.bds.system.player;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.AbilitiesComponent;
import ac.ghost.anticheat.prediction.bds.component.MovementAbilitiesComponent;





public final class UpdateAbilitiesSystem {
    private UpdateAbilitiesSystem() {
    }

    public static void tick(final GhostPlayer player) {
        final AbilitiesComponent abilities = player.entityContext.abilitiesComponent;
        int bits = 0;
        if (abilities.getBoolean(AbilitiesComponent.FLYING)) {
            bits |= MovementAbilitiesComponent.FLYING;
        }
        if (abilities.getBoolean(AbilitiesComponent.MAY_FLY)) {
            bits |= MovementAbilitiesComponent.MAY_FLY;
        }
        if (abilities.getBoolean(AbilitiesComponent.INSTABUILD)) {
            bits |= MovementAbilitiesComponent.INSTABUILD;
        }
        if (abilities.getBoolean(AbilitiesComponent.OPERATOR_COMMANDS)) {
            bits |= MovementAbilitiesComponent.OPERATOR_COMMANDS;
        }
        if (abilities.getBoolean(AbilitiesComponent.NO_CLIP)) {
            bits |= MovementAbilitiesComponent.NO_CLIP;
        }
        if (abilities.getBoolean(AbilitiesComponent.WORLD_BUILDER)) {
            bits |= MovementAbilitiesComponent.WORLD_BUILDER;
        }

        final MovementAbilitiesComponent movementAbilities =
                player.entityContext.movementAbilitiesComponent;
        movementAbilities.setBits(bits);
        movementAbilities.setFlySpeed(
                abilities.getFloat(AbilitiesComponent.FLY_SPEED));
        movementAbilities.setVerticalFlySpeed(
                abilities.getFloat(AbilitiesComponent.VERTICAL_FLY_SPEED));
    }
}
