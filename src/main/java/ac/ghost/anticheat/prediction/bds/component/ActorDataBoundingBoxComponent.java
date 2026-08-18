package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.data.EntityDimensions;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;


public final class ActorDataBoundingBoxComponent {
    private Vec3 value = Vec3.ZERO.clone();

    public Vec3 getValue() {
        return value.clone();
    }

    public void setValue(final Vec3 value) {
        this.value = value == null ? Vec3.ZERO.clone() : value.clone();
    }

    public void readFrom(final GhostPlayer player) {
        final Box box = player.entityContext.aabbShapeComponent.getAABB();
        this.value = new Vec3(
                box.maxX - box.minX,
                box.maxY - box.minY,
                box.maxZ - box.minZ);
    }

    public void writeTo(final GhostPlayer player) {
        final float width = Math.max(0.0F, value.x);
        final float height = Math.max(0.0F, value.y);
        final float depth = Math.max(0.0F, value.z);
        if (width <= 0.0F || height <= 0.0F || depth <= 0.0F) {
            return;
        }

        final Vec3 position = player.entityContext.stateVectorComponent.getPosition();
        final float eyeHeight = player.entityContext.aabbShapeComponent.getDimensions().eyeHeight();
        player.entityContext.aabbShapeComponent.setDimensions(
                EntityDimensions.fixed(width, height).withEyeHeight(eyeHeight),
                position);

        final float halfWidth = width * 0.5F;
        final float halfDepth = depth * 0.5F;
        player.entityContext.aabbShapeComponent.setAABB(new Box(
                position.x - halfWidth,
                position.y,
                position.z - halfDepth,
                position.x + halfWidth,
                position.y + height,
                position.z + halfDepth));
    }
}
