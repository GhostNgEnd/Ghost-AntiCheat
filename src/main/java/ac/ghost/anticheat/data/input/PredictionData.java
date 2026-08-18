package ac.ghost.anticheat.data.input;

import ac.ghost.anticheat.util.math.Vec3;

public record PredictionData(Vec3 before, Vec3 after, Vec3 tickEnd) {
}
