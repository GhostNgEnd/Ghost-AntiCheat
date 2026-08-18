package ac.ghost.anticheat.packets.player;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.MobEffectPacket;

public class PlayerEffectPackets implements Listener {
    @EventHandler
    public void onPacket(final DataPacketSendEvent event) {
        if (!(event.getPacket() instanceof MobEffectPacket packet)) {
            return;
        }

        final Player nukkitPlayer = event.getPlayer();
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
        if (player == null || packet.eid != player.runtimeEntityId) {
            return;
        }

        final int eventId = packet.eventId;
        final int effectId = packet.effectId;
        final int amplifier = packet.amplifier;
        final int duration = packet.duration;
        player.latencyAdapter.sendLatencyStack(() -> {
            switch (eventId) {
                case MobEffectPacket.EVENT_ADD, MobEffectPacket.EVENT_MODIFY ->
                        player.entityContext.mobEffectsComponent.addOrUpdate(effectId, amplifier, duration + 1);
                case MobEffectPacket.EVENT_REMOVE -> player.entityContext.mobEffectsComponent.remove(effectId);
            }

            
            
            
            
        });
    }
}
