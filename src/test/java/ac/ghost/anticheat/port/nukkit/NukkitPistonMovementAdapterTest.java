package ac.ghost.anticheat.port.nukkit;

import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockFace;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NukkitPistonMovementAdapterTest {
    @Test
    void decodesOnlyRealHalfBlockPacketSteps() {
        assertVector(new Vec3(0.5F, 0, 0),
                NukkitPistonMovementAdapter.pistonDisplacement(
                        1, 0.5F, 0.0F, BlockFace.EAST));
        assertVector(new Vec3(-0.5F, 0, 0),
                NukkitPistonMovementAdapter.pistonDisplacement(
                        3, 0.5F, 1.0F, BlockFace.EAST));

        assertNull(NukkitPistonMovementAdapter.pistonDisplacement(
                1, 0.0F, 1.0F, BlockFace.EAST));
        assertNull(NukkitPistonMovementAdapter.pistonDisplacement(
                2, 1.0F, 0.5F, BlockFace.EAST));
    }

    @Test
    void submitsOnlyWhenThePacketVisibleShapeTouchesThePlayer() {
        final List<Box> shapes = List.of(new Box(1, 0, 0, 2, 1, 1));
        assertTrue(NukkitPistonMovementAdapter.intersectsAny(
                new Box(0.7F, 0, 0.2F, 1.3F, 1.8F, 0.8F), shapes));
        assertFalse(NukkitPistonMovementAdapter.intersectsAny(
                new Box(3, 0, 0, 3.6F, 1.8F, 0.6F), shapes));
    }

    private static void assertVector(final Vec3 expected, final Vec3 actual) {
        assertTrue(actual != null
                && expected.x == actual.x
                && expected.y == actual.y
                && expected.z == actual.z);
    }
}
