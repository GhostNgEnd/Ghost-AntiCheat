package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.NukkitAdapter;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.types.GameType;


public final class VerticalFlySpeedControlSystem {
    private static final float HORIZONTAL_IDLE_LIMIT =
            Float.intBitsToFloat(0x3C23D70A);
    private static final float WANT_UP_IMPULSE =
            Float.intBitsToFloat(0x3E19999A);
    private static final float WANT_UP_SLOW_IMPULSE =
            Float.intBitsToFloat(0x3D4CCCCD);
    private static final float WANT_DOWN_IMPULSE =
            Float.intBitsToFloat(0xBE6147AE);
    private static final float WANT_DOWN_SLOW_IMPULSE =
            Float.intBitsToFloat(0xBE19999A);
    private static final float CREATIVE_IDLE_VERTICAL_DAMPING =
            Float.intBitsToFloat(0x3EC00000);

    private VerticalFlySpeedControlSystem() {
    }

    





    public static void tick(final GhostPlayer player,
                            final StateVectorComponent stateVector) {
        
        if (!player.entityContext.movementAbilitiesComponent.isFlying()) {
            return;
        }

        final Vec3 velocity = stateVector.getDelta();
        final boolean wantUp = NukkitAdapter.hasInput(
                player, AuthInputAction.WANT_UP);
        final boolean wantDown = NukkitAdapter.hasInput(
                player, AuthInputAction.WANT_DOWN);

        if (wantUp && wantDown) {
            velocity.y = 0.0F;
            return;
        }

        float verticalInput = 0.0F;
        if (wantUp) {
            verticalInput += WANT_UP_IMPULSE;
        }
        if (NukkitAdapter.hasInput(player, AuthInputAction.WANT_UP_SLOW)) {
            verticalInput += WANT_UP_SLOW_IMPULSE;
        }
        if (wantDown) {
            verticalInput += WANT_DOWN_IMPULSE;
        }
        if (NukkitAdapter.hasInput(player, AuthInputAction.WANT_DOWN_SLOW)) {
            verticalInput += WANT_DOWN_SLOW_IMPULSE;
        }

        final boolean idleHorizontalInput = Math.max(
                Math.abs(player.entityContext.mobTravelComponent.getInput().x),
                Math.abs(player.entityContext.mobTravelComponent.getInput().z)) < HORIZONTAL_IDLE_LIMIT;
        if (idleHorizontalInput
                && verticalInput == 0.0F
                && player.entityContext.actorGameTypeComponent.value == GameType.CREATIVE) {
            velocity.y *= CREATIVE_IDLE_VERTICAL_DAMPING;
        }

        velocity.y += verticalInput
                * player.entityContext.movementAbilitiesComponent.getVerticalFlySpeed();
    }
}
