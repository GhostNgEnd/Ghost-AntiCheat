package ac.ghost.anticheat.check.impl.breaking;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.data.BreakingData;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.PlayerActionType;
import cn.nukkit.network.protocol.types.PlayerBlockActionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@CheckInfo(name = "WrongBreak")
public final class WrongBreak extends PacketCheck {
    private BlockVector3 activeBlock;

    public WrongBreak(final GhostPlayer player) {
        super(player);
        syncFromServerState();
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        final Object packet = event.getPacket();
        if (packet instanceof PlayerAuthInputPacket authInput) {
            handleAuthInput(authInput);
            return;
        }

        if (!BreakingUtil.isLegacyBreakPacket(packet)) {
            return;
        }

        final BreakingUtil.Kind kind = BreakingUtil.legacyKind(packet);
        if (kind == null) {
            return;
        }
        final BlockVector3 position = BreakingUtil.legacyPosition(packet);
        if (kind == BreakingUtil.Kind.FINISH && !matchesActive(position)) {
            fail("action=FINISH, last=" + BreakingUtil.pos(resolveActive())
                    + ", pos=" + BreakingUtil.pos(position));
            event.setCancelled(true);
            return;
        }
        updateLegacyState(kind, position);
    }

    private void handleAuthInput(final PlayerAuthInputPacket packet) {
        if (packet.getBlockActionData() == null
                || packet.getBlockActionData().isEmpty()) {
            return;
        }

        final BlockVector3 previous = resolveActive();
        final List<BlockVector3> startsOrContinues = new ArrayList<>();
        BlockVector3 preferredNext = null;

        
        
        
        for (final Map.Entry<PlayerActionType, PlayerBlockActionData> entry
                : packet.getBlockActionData().entrySet()) {
            final BreakingUtil.Kind kind = BreakingUtil.kind(entry.getKey());
            final PlayerBlockActionData data = entry.getValue();
            if (kind == null || data == null || data.getPosition() == null) {
                continue;
            }
            if (kind == BreakingUtil.Kind.START || kind == BreakingUtil.Kind.CONTINUE) {
                startsOrContinues.add(data.getPosition());
                
                
                if (preferredNext == null || kind == BreakingUtil.Kind.CONTINUE) {
                    preferredNext = data.getPosition();
                }
            }
        }

        for (final Map.Entry<PlayerActionType, PlayerBlockActionData> entry
                : new ArrayList<>(packet.getBlockActionData().entrySet())) {
            final BreakingUtil.Kind kind = BreakingUtil.kind(entry.getKey());
            final PlayerBlockActionData data = entry.getValue();
            if (kind != BreakingUtil.Kind.FINISH
                    || data == null || data.getPosition() == null) {
                continue;
            }

            final BlockVector3 finish = data.getPosition();
            boolean valid = Objects.equals(finish, previous);
            if (!valid) {
                for (final BlockVector3 candidate : startsOrContinues) {
                    if (Objects.equals(finish, candidate)) {
                        valid = true;
                        break;
                    }
                }
            }

            if (!valid) {
                fail("action=FINISH, last=" + BreakingUtil.pos(previous)
                        + ", pos=" + BreakingUtil.pos(finish));
                BreakingUtil.removeAction(packet, entry.getKey());
            }
        }

        boolean survivingTerminalAction = false;
        for (final PlayerActionType type : packet.getBlockActionData().keySet()) {
            final BreakingUtil.Kind kind = BreakingUtil.kind(type);
            if (kind == BreakingUtil.Kind.CANCEL || kind == BreakingUtil.Kind.FINISH) {
                survivingTerminalAction = true;
                break;
            }
        }

        if (preferredNext != null) {
            this.activeBlock = preferredNext;
        } else if (survivingTerminalAction) {
            this.activeBlock = null;
        }
    }

    private void updateLegacyState(final BreakingUtil.Kind kind,
                                   final BlockVector3 position) {
        switch (kind) {
            case START, CONTINUE -> this.activeBlock = position;
            case CANCEL, FINISH -> this.activeBlock = null;
        }
    }

    private boolean matchesActive(final BlockVector3 position) {
        return Objects.equals(position, resolveActive());
    }

    private BlockVector3 resolveActive() {
        if (this.activeBlock != null) {
            return this.activeBlock;
        }
        syncFromServerState();
        return this.activeBlock;
    }

    private void syncFromServerState() {
        final BreakingData data = player.entityContext
                .playerDestroyProgressCacheComponent.breakingData;
        if (data != null) {
            this.activeBlock = data.getPosition();
        }
    }
}
