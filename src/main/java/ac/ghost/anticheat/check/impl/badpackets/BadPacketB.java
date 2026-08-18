package ac.ghost.anticheat.check.impl.badpackets;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.MathUtil;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;

@CheckInfo(name = "Bad Packet", type = "B")
public class BadPacketB extends PacketCheck {
    public BadPacketB(GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(DataPacketReceiveEvent event) {
        if (!(event.getPacket() instanceof PlayerAuthInputPacket packet)) {
            return;
        }

        float wrappedY = MathUtil.wrapDegrees(packet.getYaw());
        float wrappedX = MathUtil.clamp(packet.getPitch(), -90, 90);
        if (wrappedY != packet.getYaw()) { 
            fail("claimedYaw=" + packet.getYaw() + ", wrappedYaw=" + wrappedY);
        } else if (wrappedX != packet.getPitch()) {
            fail("claimedPitch=" + packet.getPitch() + ", wrappedPitch=" + wrappedX);
        } else {
            return;
        }

        
        player.kick("Invalid auth input packet!");
    }
}
