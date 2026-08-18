package ac.ghost.anticheat.check.impl.elytra;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.annotations.Experimental;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;


@Experimental
@CheckInfo(name = "ElytraE")
public final class ElytraE extends PacketCheck {
    public ElytraE(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        if (!ElytraActionUtil.hasStartGliding(player, event)
                || !player.entityContext.movementAbilitiesComponent.isFlying()) {
            return;
        }

        fail("flying=true, tick="
                + ElytraActionUtil.actionTick(player, event));
        ElytraActionUtil.suppressStartGliding(player, event);
    }
}
