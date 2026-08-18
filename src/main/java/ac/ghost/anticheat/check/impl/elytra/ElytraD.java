package ac.ghost.anticheat.check.impl.elytra;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.annotations.Experimental;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.player.StartGlidingActionServerSystem;
import cn.nukkit.event.server.DataPacketReceiveEvent;


@Experimental
@CheckInfo(name = "ElytraD")
public final class ElytraD extends PacketCheck {
    public ElytraD(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        if (!ElytraActionUtil.hasStartGliding(player, event)
                || StartGlidingActionServerSystem.hasElytraEquipped(player.entityContext)) {
            return;
        }

        fail("elytraEquipped=false, tick="
                + ElytraActionUtil.actionTick(player, event));
        ElytraActionUtil.suppressStartGliding(player, event);
    }
}
