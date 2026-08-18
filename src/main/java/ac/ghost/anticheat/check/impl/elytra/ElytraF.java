package ac.ghost.anticheat.check.impl.elytra;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;


@CheckInfo(name = "ElytraF")
public final class ElytraF extends PacketCheck {
    public ElytraF(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        if (!ElytraActionUtil.hasStartGliding(player, event)
                || !player.entityContext.onGroundFlagComponent.isPresent()) {
            return;
        }

        fail("onGround=true, tick="
                + ElytraActionUtil.actionTick(player, event));
        ElytraActionUtil.suppressStartGliding(player, event);
    }
}
