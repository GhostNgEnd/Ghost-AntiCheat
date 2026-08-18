package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerTickStartVelocityComponentTest {
    @Test
    void promotesUncertainVelocityIntoOneCertainTick() {
        final PlayerTickStartVelocityComponent component =
                new PlayerTickStartVelocityComponent();
        final Vec3 motion = new Vec3(0.3F, 0.4F, -0.2F);

        component.acknowledgeUncertain(motion);
        component.promoteUncertain();

        final PlayerTickStartVelocityComponent.Candidates candidates =
                component.begin(new Vec3(1.0F, 2.0F, 3.0F));
        assertFalse(candidates.ambiguous());
        assertEquals(PlayerTickStartVelocityComponent.Type.VELOCITY,
                candidates.ordinary().type());
        assertVector(motion, candidates.ordinary().velocity());
        assertFalse(component.hasCertainVelocity());
    }

    @Test
    void keepsAmbiguousVelocityForAtMostTwoMovementTicks() {
        final PlayerTickStartVelocityComponent component =
                new PlayerTickStartVelocityComponent();
        final Vec3 motion = new Vec3(0.3F, 0.4F, -0.2F);
        final Vec3 carried = new Vec3(0.01F, -0.08F, 0.02F);
        component.acknowledgeUncertain(motion);

        final PlayerTickStartVelocityComponent.Candidates first =
                component.begin(carried);
        assertTrue(first.ambiguous());
        component.finish(first, first.ordinary());
        assertTrue(component.hasUncertainVelocity());

        final PlayerTickStartVelocityComponent.Candidates second =
                component.begin(carried);
        assertTrue(second.ambiguous());
        assertFalse(component.hasUncertainVelocity());
        component.finish(second, second.ordinary());
        assertFalse(component.hasUncertainVelocity());
    }

    @Test
    void consumingUncertainVelocityRemovesItImmediately() {
        final PlayerTickStartVelocityComponent component =
                new PlayerTickStartVelocityComponent();
        component.acknowledgeUncertain(new Vec3(0.3F, 0.4F, -0.2F));

        final PlayerTickStartVelocityComponent.Candidates candidates =
                component.begin(Vec3.ZERO);
        component.finish(candidates, candidates.uncertain());

        assertFalse(component.hasUncertainVelocity());
        assertTrue(component.selectedServerVelocity());
    }

    private static void assertVector(final Vec3 expected,
                                     final Vec3 actual) {
        assertEquals(Float.floatToRawIntBits(expected.x),
                Float.floatToRawIntBits(actual.x));
        assertEquals(Float.floatToRawIntBits(expected.y),
                Float.floatToRawIntBits(actual.y));
        assertEquals(Float.floatToRawIntBits(expected.z),
                Float.floatToRawIntBits(actual.z));
    }
}
