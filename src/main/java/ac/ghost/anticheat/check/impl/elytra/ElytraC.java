package ac.ghost.anticheat.check.impl.elytra;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;


@CheckInfo(name = "ElytraC")
public final class ElytraC extends PacketCheck {
    private long lastStartGlideTick = Long.MIN_VALUE;

    public ElytraC(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        if (!ElytraActionUtil.hasStartGliding(player, event)) {
            return;
        }

        final long tick = ElytraActionUtil.actionTick(player, event);
        if (this.lastStartGlideTick != Long.MIN_VALUE
                && tick >= this.lastStartGlideTick
                && tick - this.lastStartGlideTick <= 1L) {
            fail("tick=" + tick + ", last=" + this.lastStartGlideTick);
            ElytraActionUtil.suppressStartGliding(player, event);
        }
        this.lastStartGlideTick = tick;
    }
}
