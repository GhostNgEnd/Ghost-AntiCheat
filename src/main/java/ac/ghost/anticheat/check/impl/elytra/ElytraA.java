package ac.ghost.anticheat.check.impl.elytra;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import cn.nukkit.event.server.DataPacketReceiveEvent;


@CheckInfo(name = "ElytraA")
public final class ElytraA extends PacketCheck {
    public ElytraA(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        if (!ElytraActionUtil.hasStartGliding(player, event)
                || !player.entityContext.actorDataFlagComponent.has(ActorDataFlag.GLIDING)) {
            return;
        }

        fail("alreadyGliding=true, tick="
                + ElytraActionUtil.actionTick(player, event));
        ElytraActionUtil.suppressStartGliding(player, event);
    }
}
