package ac.ghost.anticheat.check.impl.elytra;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;


@CheckInfo(name = "ElytraB")
public final class ElytraB extends PacketCheck {
    private long lastJumpTick = Long.MIN_VALUE;

    public ElytraB(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        final long tick = ElytraActionUtil.actionTick(player, event);
        if (ElytraActionUtil.hasJumpIntent(player, event)) {
            this.lastJumpTick = tick;
        }

        if (!ElytraActionUtil.hasStartGliding(player, event)) {
            return;
        }

        
        
        final boolean recentlyJumped = this.lastJumpTick != Long.MIN_VALUE
                && tick >= this.lastJumpTick
                && tick - this.lastJumpTick <= 1L;
        if (recentlyJumped) {
            return;
        }

        fail("jump=false, tick=" + tick + ", lastJumpTick=" + this.lastJumpTick);
        ElytraActionUtil.suppressStartGliding(player, event);
    }
}
