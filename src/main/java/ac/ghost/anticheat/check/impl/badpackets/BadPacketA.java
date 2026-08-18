package ac.ghost.anticheat.check.impl.badpackets;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.MathUtil;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;

@CheckInfo(name = "Bad Packet", type = "A")
public class BadPacketA extends PacketCheck {
    public BadPacketA(GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(DataPacketReceiveEvent event) {
        if (event.getPacket() instanceof PlayerAuthInputPacket packet) {
            Vector3f pos = packet.getPosition();
            Vector3f delta = packet.getDelta();
            Vector2f rot = new Vector2f(packet.getYaw(), packet.getPitch());
            if (!MathUtil.isValid(pos) || !MathUtil.isValid(rot) || !MathUtil.isValid(delta)) {
                fail("pos=" + pos + ", rot=" + rot + ", delta=" + delta);
                player.kick("Invalid auth input packet!");
            }
        }
    }
}
