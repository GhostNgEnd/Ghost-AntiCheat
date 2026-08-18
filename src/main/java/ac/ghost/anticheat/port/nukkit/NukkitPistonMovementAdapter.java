package ac.ghost.anticheat.port.nukkit;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityMoveByPistonEvent;
import cn.nukkit.math.BlockFace;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class NukkitPistonMovementAdapter implements Listener {
    private static final float MOVE_STEP = 0.5F;
    private static final float EPSILON = 1.0E-5F;

    private final Map<Long, Vec3> startPositions = new ConcurrentHashMap<>();
    private final Set<Long> scheduled = ConcurrentHashMap.newKeySet();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonMove(final EntityMoveByPistonEvent event) {
        if (!(event.getEntity() instanceof Player session)) {
            return;
        }

        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(session);
        if (player == null
                || !player.entityContext.shouldBeSimulatedComponent.isPresent()
                || !player.entityContext.pushableByBlockComponent.isPresent()) {
            return;
        }

        final long runtimeId = session.getId();
        this.startPositions.putIfAbsent(runtimeId, new Vec3(
                (float) session.x, (float) session.y, (float) session.z));
        if (!this.scheduled.add(runtimeId)) {
            return;
        }

        Ghost.getPluginInstance().getServer().getScheduler().scheduleTask(
                Ghost.getPluginInstance(), () -> captureDisplacement(runtimeId, session));
    }

    private void captureDisplacement(final long runtimeId, final Player session) {
        this.scheduled.remove(runtimeId);
        final Vec3 start = this.startPositions.remove(runtimeId);
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(session);
        if (start == null || player == null || player.isClosed()) {
            return;
        }

        final Vec3 displacement = new Vec3(
                (float) session.x - start.x,
                (float) session.y - start.y,
                (float) session.z - start.z);
        if (displacement.lengthSquared() <= 1.0E-12F) {
            return;
        }

        player.ghostMovementBridgeState.pendingPistonMovement.submit(displacement);
    }

    static Vec3 pistonDisplacement(final int state,
                                   final float progress,
                                   final float lastProgress,
                                   final BlockFace facing) {
        if (facing == null || state != 1 && state != 3
                || !Float.isFinite(progress) || !Float.isFinite(lastProgress)) {
            return null;
        }
        final float distance = Math.abs(progress - lastProgress);
        if (distance <= EPSILON || distance > MOVE_STEP + EPSILON) {
            return null;
        }
        final BlockFace direction = state == 1 ? facing : facing.getOpposite();
        return new Vec3(
                distance * direction.getXOffset(),
                distance * direction.getYOffset(),
                distance * direction.getZOffset());
    }

    static boolean intersectsAny(final Box player, final List<Box> shapes) {
        if (player == null || !player.isValid()) {
            return false;
        }
        for (final Box shape : shapes) {
            if (player.expand(EPSILON).intersects(shape)) {
                return true;
            }
        }
        return false;
    }
}
