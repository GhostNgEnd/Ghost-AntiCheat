package ac.ghost.anticheat.util.math;

import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;

import java.util.Optional;

public class Box implements Cloneable {
    public final static Box EMPTY = new Box(0, 0, 0, 0, 0, 0);

    public final static float EPSILON = 1.0E-7F;
    public final static float COLLISION_EPSILON = 1.0E-6F;

    public final float minX, minY, minZ;
    public final float maxX, maxY, maxZ;

    public Box(float x1, float y1, float z1, float x2, float y2, float z2) {
        this(x1, y1, z1, x2, y2, z2, true);
    }

    private Box(float x1, float y1, float z1,
                float x2, float y2, float z2,
                boolean normalize) {
        if (normalize) {
            this.minX = Math.min(x1, x2);
            this.minY = Math.min(y1, y2);
            this.minZ = Math.min(z1, z2);
            this.maxX = Math.max(x1, x2);
            this.maxY = Math.max(y1, y2);
            this.maxZ = Math.max(z1, z2);
            return;
        }

        this.minX = x1;
        this.minY = y1;
        this.minZ = z1;
        this.maxX = x2;
        this.maxY = y2;
        this.maxZ = z2;
    }

    
    public static Box invalid() {
        return new Box(
                Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE,
                false);
    }

    public boolean isValid() {
        return this.minX < this.maxX
                && this.minY < this.maxY
                && this.minZ < this.maxZ;
    }

    public Box(double x1, double y1, double z1, double x2, double y2, double z2) {
        this((float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2);
    }

    public Box(final AxisAlignedBB boundingBox) {
        this.minX = (float) boundingBox.getMinX();
        this.minY = (float) boundingBox.getMinY();
        this.minZ = (float) boundingBox.getMinZ();
        this.maxX = (float) boundingBox.getMaxX();
        this.maxY = (float) boundingBox.getMaxY();
        this.maxZ = (float) boundingBox.getMaxZ();
    }

    public Vec3 toVec3f(float width) {
        return new Vec3(this.minX + (width / 2F), this.minY, this.maxZ - (width / 2F));
    }

    public static Box of(Vec3 center, float dx, float dy, float dz) {
        return new Box(center.x - dx / 2.0F, center.y - dy / 2.0F, center.z - dz / 2.0F, center.x + dx / 2.0F, center.y + dy / 2.0F, center.z + dz / 2.0F);
    }

    public boolean equals(Box box) {
        return this.minX == box.minX && this.minY == box.minY && this.minZ == box.minZ &&
                this.maxX == box.maxX && this.maxY == box.maxY && this.maxZ == box.maxZ;
    }

    public float chooseMin(BlockFace.Axis axis) {
        return switch (axis) {
            case X -> this.minX;
            case Y -> this.minY;
            default -> this.minZ;
        };
    }

    public float chooseMax(BlockFace.Axis axis) {
        return switch (axis) {
            case X -> this.maxX;
            case Y -> this.maxY;
            default -> this.maxZ;
        };
    }

    public boolean isOverlapped(BlockFace.Axis axis, Box other) {
        return switch (axis) {
            case X -> other.maxY - this.minY > 0 && this.maxY - other.minY > 0 && other.maxZ - this.minZ > 0 && this.maxZ - other.minZ > 0;
            case Y -> other.maxX - this.minX > 0 && this.maxX - other.minX > 0 && other.maxZ - this.minZ > 0 && this.maxZ - other.minZ > 0;
            default -> other.maxX - this.minX > 0 && this.maxX - other.minX > 0 && other.maxY - this.minY > 0 && this.maxY - other.minY >0;
        };
    }

    public float calculateMaxDistance(BlockFace.Axis axis, Box other, float maxDist) {
        if (!isOverlapped(axis, other) || maxDist == 0) {
            return maxDist;
        }

        if (maxDist > 0) {
            float d1 = chooseMin(axis) - other.chooseMax(axis);

            if (d1 >= -COLLISION_EPSILON) {
                maxDist = Math.min(maxDist, d1);
            }
        } else {
            float d0 = chooseMax(axis) - other.chooseMin(axis);

            if (d0 <= COLLISION_EPSILON) {
                maxDist = Math.max(maxDist, d0);
            }
        }
        return maxDist;
    }

    public Optional<Vec3> clip(Vec3 from, Vec3 to) {
        return clip(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, from, to);
    }

    public static Optional<Vec3> clip(float d, float e, float f, float g, float h, float i, Vec3 vec3, Vec3 vec32) {
        float[] ds = new float[]{1.0F};
        float j = vec32.x - vec3.x;
        float k = vec32.y - vec3.y;
        float l = vec32.z - vec3.z;
        BlockFace direction = getDirection(d, e, f, g, h, i, vec3, ds, null, j, k, l);
        if (direction == null) {
            return Optional.empty();
        } else {
            float m = ds[0];
            return Optional.of(vec3.add(m * j, m * k, m * l));
        }
    }

    private static BlockFace getDirection(Box aABB, Vec3 vec3, float[] ds, BlockFace direction, float d, float e, float f) {
        return getDirection(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, vec3, ds, direction, d, e, f);
    }

    private static BlockFace getDirection(float d, float e, float f, float g, float h, float i, Vec3 vec3, float[] ds, BlockFace direction, float j, float k, float l) {
        if (j > 1.0E-7) {
            direction = clipPoint(ds, direction, j, k, l, d, e, h, f, i, BlockFace.WEST, vec3.x, vec3.y, vec3.z);
        } else if (j < -1.0E-7) {
            direction = clipPoint(ds, direction, j, k, l, g, e, h, f, i, BlockFace.EAST, vec3.x, vec3.y, vec3.z);
        }

        if (k > 1.0E-7) {
            direction = clipPoint(ds, direction, k, l, j, e, f, i, d, g, BlockFace.DOWN, vec3.y, vec3.z, vec3.x);
        } else if (k < -1.0E-7) {
            direction = clipPoint(ds, direction, k, l, j, h, f, i, d, g, BlockFace.UP, vec3.y, vec3.z, vec3.x);
        }

        if (l > 1.0E-7) {
            direction = clipPoint(ds, direction, l, j, k, f, d, g, e, h, BlockFace.NORTH, vec3.z, vec3.x, vec3.y);
        } else if (l < -1.0E-7) {
            direction = clipPoint(ds, direction, l, j, k, i, d, g, e, h, BlockFace.SOUTH, vec3.z, vec3.x, vec3.y);
        }

        return direction;
    }

    private static BlockFace clipPoint(float[] ds, BlockFace direction, float d, float e, float f, float g, float h, float i, float j, float k, BlockFace direction2, float l, float m, float n) {
        float o = (g - l) / d;
        float p = m + o * e;
        float q = n + o * f;
        if (0.0F < o && o < ds[0] && h - 1.0E-7 < p && p < i + 1.0E-7 && j - 1.0E-7 < q && q < k + 1.0E-7) {
            ds[0] = o;
            return direction2;
        } else {
            return direction;
        }
    }

    public Box withMinX(float minX) {
        return new Box(minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public Box withMinY(float minY) {
        return new Box(this.minX, minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public Box withMinZ(float minZ) {
        return new Box(this.minX, this.minY, minZ, this.maxX, this.maxY, this.maxZ);
    }

    public Box withMaxX(float maxX) {
        return new Box(this.minX, this.minY, this.minZ, maxX, this.maxY, this.maxZ);
    }

    public Box withMaxY(float maxY) {
        return new Box(this.minX, this.minY, this.minZ, this.maxX, maxY, this.maxZ);
    }

    public Box withMaxZ(float maxZ) {
        return new Box(this.minX, this.minY, this.minZ, this.maxX, this.maxY, maxZ);
    }

    public Box shrink(float x, float y, float z) {
        float d = this.minX;
        float e = this.minY;
        float f = this.minZ;
        float g = this.maxX;
        float h = this.maxY;
        float i = this.maxZ;
        if (x < 0.0) {
            d -= x;
        } else if (x > 0.0) {
            g -= x;
        }

        if (y < 0.0) {
            e -= y;
        } else if (y > 0.0) {
            h -= y;
        }

        if (z < 0.0) {
            f -= z;
        } else if (z > 0.0) {
            i -= z;
        }

        return new Box(d, e, f, g, h, i);
    }

    public Box stretch(Vec3 scale) {
        return this.stretch(scale.x, scale.y, scale.z);
    }

    public Box stretch(float x, float y, float z) {
        float d = this.minX;
        float e = this.minY;
        float f = this.minZ;
        float g = this.maxX;
        float h = this.maxY;
        float i = this.maxZ;
        if (x < 0.0) {
            d += x;
        } else if (x > 0.0) {
            g += x;
        }

        if (y < 0.0) {
            e += y;
        } else if (y > 0.0) {
            h += y;
        }

        if (z < 0.0) {
            f += z;
        } else if (z > 0.0) {
            i += z;
        }

        return new Box(d, e, f, g, h, i);
    }

    public Box expand(float x, float y, float z) {
        float d = this.minX - x;
        float e = this.minY - y;
        float f = this.minZ - z;
        float g = this.maxX + x;
        float h = this.maxY + y;
        float i = this.maxZ + z;
        return new Box(d, e, f, g, h, i);
    }

    public Box expand(float value) {
        return this.expand(value, value, value);
    }

    public Box intersection(Box Box) {
        float d = Math.max(this.minX, Box.minX);
        float e = Math.max(this.minY, Box.minY);
        float f = Math.max(this.minZ, Box.minZ);
        float g = Math.min(this.maxX, Box.maxX);
        float h = Math.min(this.maxY, Box.maxY);
        float i = Math.min(this.maxZ, Box.maxZ);
        return new Box(d, e, f, g, h, i);
    }

    public Box union(Box Box) {
        float d = Math.min(this.minX, Box.minX);
        float e = Math.min(this.minY, Box.minY);
        float f = Math.min(this.minZ, Box.minZ);
        float g = Math.max(this.maxX, Box.maxX);
        float h = Math.max(this.maxY, Box.maxY);
        float i = Math.max(this.maxZ, Box.maxZ);
        return new Box(d, e, f, g, h, i);
    }

    public Box offset(float x, float y, float z) {
        return new Box(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public Box offset(double x, double y, double z) {
        return this.offset((float) x, (float) y, (float) z);
    }

    public Box offset(Vec3 vec) {
        return this.offset(vec.x, vec.y, vec.z);
    }

    public boolean intersects(Box Box) {
        return this.intersects(Box.minX, Box.minY, Box.minZ, Box.maxX, Box.maxY, Box.maxZ);
    }

    public boolean intersects(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
    }

    public boolean intersects(Vec3 pos1, Vec3 pos2) {
        return this.intersects(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y), Math.min(pos1.z, pos2.z), Math.max(pos1.x, pos2.x), Math.max(pos1.y, pos2.y), Math.max(pos1.z, pos2.z));
    }

    public boolean contains(Vec3 pos) {
        return this.contains(pos.x, pos.y, pos.z);
    }

    public boolean contains(float x, float y, float z) {
        return x >= this.minX && x < this.maxX && y >= this.minY && y < this.maxY && z >= this.minZ && z < this.maxZ;
    }

    public Box contract(float x, float y, float z) {
        return this.expand(-x, -y, -z);
    }

    public Box contract(float value) {
        return this.expand(-value);
    }

    public Vec3 getMinPos() {
        return new Vec3(this.minX, this.minY, this.minZ);
    }

    public Vec3 getMaxPos() {
        return new Vec3(this.maxX, this.maxY, this.maxZ);
    }

    public float getAverageSideLength() {
        float d = this.getLengthX();
        float e = this.getLengthY();
        float f = this.getLengthZ();
        return (d + e + f) / 3;
    }

    public float getLengthX() {
        return this.maxX - this.minX;
    }

    public float getLengthY() {
        return this.maxY - this.minY;
    }

    public float getLengthZ() {
        return this.maxZ - this.minZ;
    }
    
    public boolean isNaN() {
        return Float.isNaN(this.minX) || Float.isNaN(this.minY) || Float.isNaN(this.minZ) || Float.isNaN(this.maxX) || Float.isNaN(this.maxY) || Float.isNaN(this.maxZ);
    }

    public String toString() {
        return "AABB[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    @Override
    public Box clone() {
        return new Box(
                minX, minY, minZ, maxX, maxY, maxZ, this.isValid());
    }
}
