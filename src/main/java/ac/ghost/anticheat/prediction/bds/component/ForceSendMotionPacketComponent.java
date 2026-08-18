package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.movement.AcknowledgeServerMotionSystem;
import ac.ghost.anticheat.util.math.Vec3;


public final class ForceSendMotionPacketComponent {
    
    private boolean present;

    public boolean isPresent() {
        return this.present;
    }

    public void setPresent(final boolean value) {
        this.present = value;
    }

    public void clear() {
        this.present = false;
    }

    




    public void request(final GhostPlayer player, final Vec3 authoritativeVelocity) {
        final Vec3 velocity = authoritativeVelocity.clone();
        if (Ghost.getPluginInstance() == null) {
            AcknowledgeServerMotionSystem.uncertain(player, velocity);
            AcknowledgeServerMotionSystem.promote(player);
            return;
        }

        player.latencyAdapter.sendLatencyStackForcedFlush(
                () -> AcknowledgeServerMotionSystem.uncertain(player, velocity));
        player.latencyAdapter.sendLatencyStackAfterOutbound(
                () -> AcknowledgeServerMotionSystem.promote(player));
    }

    public void reset(final GhostPlayer player) {
        clear();
        player.entityContext.playerTickStartVelocityComponent.clear();
    }
}
