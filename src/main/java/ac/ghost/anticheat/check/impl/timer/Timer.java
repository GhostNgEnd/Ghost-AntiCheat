package ac.ghost.anticheat.check.impl.timer;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.annotations.Experimental;
import ac.ghost.anticheat.check.api.impl.PingBasedCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import ac.ghost.anticheat.util.LatencyUtil;
import cn.nukkit.network.protocol.MovePlayerPacket;








@Experimental
@CheckInfo(name = "Timer")
public final class Timer extends PingBasedCheck {
    
    private static final long AVERAGE_DISTANCE = 50_000_000L;
    
    private static final long BALANCE_LIMIT = 63_000_000L;

    private long lastNS;
    private long balance;
    private long prevTick;
    private long loseBalance;
    private boolean beforeAuthInput;

    
    private long legacyPacketTick = -1L;

    public Timer(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onLatencyAccepted(final LatencyUtil.Latency latency) {
        if (!this.beforeAuthInput) {
            return;
        }
        this.beforeAuthInput = false;

        if (latency.ns() > System.nanoTime() + this.balance) {
            final long distance = (latency.ns()
                    - (System.nanoTime() + this.balance))
                    - (AVERAGE_DISTANCE / 2L);
            this.balance += distance;
            this.loseBalance = Math.max(0L, this.loseBalance - distance);
        }
    }

    
    public boolean isInvalidAuthInput(final long tick) {
        return this.isInvalid(tick);
    }

    




    public boolean isInvalidMovePlayer(final MovePlayerPacket packet,
                                       final int protocol) {
        final long tick;
        if (BedrockProtocolCapabilities.hasMovePlayerFrame(protocol)) {
            tick = packet.frame;
        } else {
            tick = ++this.legacyPacketTick;
        }
        return this.isInvalid(tick);
    }

    private boolean isInvalid(final long tick) {
        final boolean loading = player.entityContext
                .playerLoadingScreenComponent.active;
        final int sinceLoading = player.entityContext
                .playerLoadingScreenComponent.ticksSinceChange;

        if (this.lastNS == 0L || loading || sinceLoading < 20) {
            this.lastNS = System.nanoTime();
            this.prevTick = tick;
            this.balance = 0L;
            return false;
        }

        boolean valid = true;

        final long distance = System.nanoTime() - this.lastNS;
        final long neededDistance = (tick - this.prevTick) * AVERAGE_DISTANCE;

        if (this.balance > BALANCE_LIMIT) {
            if (this.balance - this.loseBalance <= BALANCE_LIMIT) {
                this.loseBalance -= AVERAGE_DISTANCE;
            } else {
                this.fail("balance " + displayBalance(this.balance));
            }

            
            
            player.getTeleportUtil().teleport(
                    player.getTeleportUtil().getLastKnowValid());
            this.balance -= AVERAGE_DISTANCE;
            valid = false;
        } else {
            final long maxBalanceAdvantage = (long) Math.max(0D,
                    Ghost.getConfig().maxBalanceAdvantage() * 1_000_000D);
            if (this.balance <= -Math.abs(
                    maxBalanceAdvantage + AVERAGE_DISTANCE)
                    && Ghost.getConfig().maxBalanceAdvantage() > 0L) {
                this.loseBalance = Math.abs(this.balance);
                this.balance = -AVERAGE_DISTANCE;
            }
        }

        this.balance -= distance - neededDistance;
        this.lastNS = Math.max(this.lastNS, System.nanoTime());
        this.prevTick = tick;
        this.beforeAuthInput = true;
        return !valid;
    }

    
    private static long displayBalance(final long balanceNs) {
        final long millis = Math.max(0L, balanceNs / 1_000_000L);
        return Math.min(9_999L, millis);
    }
}
