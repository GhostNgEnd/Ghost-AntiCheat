package ac.ghost.anticheat.check.api.impl;

import ac.ghost.anticheat.check.api.Check;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;

public class PacketCheck extends Check {
    public PacketCheck(GhostPlayer player) {
        super(player);
    }

    public void onPacketSend(DataPacketSendEvent event) {
    }

    public void onPacketReceive(DataPacketReceiveEvent event) {
    }
}
