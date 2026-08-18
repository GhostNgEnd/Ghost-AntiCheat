package ac.ghost.anticheat.prediction.bds.system.input;

import ac.ghost.anticheat.data.BreakingData;
import ac.ghost.anticheat.prediction.bds.component.PlayerDestroyProgressCacheComponent;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.entity.EntityContext;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.block.BlockUtil;
import cn.nukkit.block.BlockItemFrame;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.types.PlayerActionType;
import cn.nukkit.network.protocol.types.PlayerBlockActionData;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;








public final class ProcessPlayerActionPacketSystem {
    private static final EnumSet<PlayerActionType> ALLOWED_ACTIONS = EnumSet.of(
            PlayerActionType.START_DESTROY_BLOCK,
            PlayerActionType.ABORT_DESTROY_BLOCK,
            PlayerActionType.PREDICT_DESTROY_BLOCK,
            PlayerActionType.CONTINUE_DESTROY_BLOCK
    );

    private ProcessPlayerActionPacketSystem() {
    }

    public static void tick(final EntityContext entity,
                            final PlayerAuthInputPacket packet) {
        if (!packet.getInputData().contains(AuthInputAction.PERFORM_BLOCK_ACTIONS)) {
            return;
        }

        final GhostPlayer player = entity.externalDataComponent.player();
        final PlayerDestroyProgressCacheComponent progress =
                entity.playerDestroyProgressCacheComponent;
        final Map<PlayerActionType, PlayerBlockActionData> validActions = new LinkedHashMap<>();
        for (Map.Entry<PlayerActionType, PlayerBlockActionData> entry : packet.getBlockActionData().entrySet()) {
            final PlayerBlockActionData action = entry.getValue();
            if (action == null || action.getAction() == null || entry.getKey() != action.getAction()) {
                continue;
            }

            final PlayerActionType actionType = action.getAction();
            final BlockVector3 position = action.getPosition();
            final int face = action.getFacing();
            if (!ALLOWED_ACTIONS.contains(actionType) || position == null || !MathUtil.isValid(position)) {
                continue;
            }
            if (actionType != PlayerActionType.ABORT_DESTROY_BLOCK && (face < 0 || face >= BlockFaceCount.COUNT)) {
                continue;
            }
            if (distanceSquared(position, entity.stateVectorComponent.getPosition().toBlockVector3()) > 144L) {
                BlockUtil.resendBlocksAroundArea(player.getSession(), position, face);
                continue;
            }

            final var state = entity.blockSource.getBlockState(position, 0);
            if (!(state.getBlock() instanceof BlockItemFrame)
                    && !BlockUtil.determineCanBreak(player, state)) {
                BlockUtil.resendBlocksAroundArea(player.getSession(), position, face);
                continue;
            }

            switch (actionType) {
                case START_DESTROY_BLOCK, CONTINUE_DESTROY_BLOCK -> {
                    if (progress.breakingData == null || !Objects.equals(position, progress.breakingData.getPosition())) {
                        if (progress.breakingData != null) {
                            BlockUtil.resendBlocksAroundArea(
                                    player.getSession(),
                                    progress.breakingData.getPosition(),
                                    progress.breakingData.getFace());
                        }
                        progress.breakingData = new BreakingData(
                                PlayerActionType.START_DESTROY_BLOCK, position, face);
                    } else {
                        progress.breakingData.setState(PlayerActionType.CONTINUE_DESTROY_BLOCK);
                    }

                    
                    progress.breakingData.setBreakingProcess(1F);
                }
                case ABORT_DESTROY_BLOCK -> progress.breakingData = null;
                case PREDICT_DESTROY_BLOCK -> {
                    if (progress.breakingData == null
                            || !Objects.equals(position, progress.breakingData.getPosition())
                            || progress.breakingData.getBreakingProcess() < 1F) {
                        BlockUtil.resendBlocksAroundArea(player.getSession(), position, face);
                        continue;
                    }
                    entity.blockSource.updateLegacyBlock(position, 0, 0);
                    progress.breakingData = null;
                }
                default -> {
                    continue;
                }
            }

            validActions.put(actionType, action);
        }

        packet.setBlockActionData(validActions);
        if (validActions.isEmpty()) {
            packet.getInputData().remove(AuthInputAction.PERFORM_BLOCK_ACTIONS);
        }
    }

    private static long distanceSquared(final BlockVector3 first,
                                        final BlockVector3 second) {
        final long x = (long) first.getX() - second.getX();
        final long y = (long) first.getY() - second.getY();
        final long z = (long) first.getZ() - second.getZ();
        return x * x + y * y + z * z;
    }

    private static final class BlockFaceCount {
        private static final int COUNT = 6;

        private BlockFaceCount() {
        }
    }
}
