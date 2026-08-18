package ac.ghost.anticheat.prediction.bds.system.input;

import ac.ghost.anticheat.check.impl.hitboxes.Hitboxes;
import ac.ghost.anticheat.check.impl.reach.Reach;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.entity.EntityContext;
import ac.ghost.anticheat.prediction.nukkit.NukkitPlayerTickAdapter;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;




public final class SendPlayerAuthInputReceivedEventSystem {
    private SendPlayerAuthInputReceivedEventSystem() {
    }

    
    public static void validate(final EntityContext entity,
                                final PlayerAuthInputPacket packet) {
        ProcessPlayerActionPacketSystem.tick(entity, packet);
    }

    
    public static void tick(final EntityContext entity) {
        final GhostPlayer player = entity.externalDataComponent.player();
        if (!player.isExempted()) {
            final Reach reach = (Reach) player.getCheckHolder().get(Reach.class);
            if (reach != null) {
                reach.pollQueuedHits();
            }

            final Hitboxes hitboxes = (Hitboxes) player.getCheckHolder()
                    .get(Hitboxes.class);
            if (hitboxes != null) {
                hitboxes.pollQueuedHits();
            }
        }

        NukkitPlayerTickAdapter.onAcceptedAuthInput(player);
    }
}
