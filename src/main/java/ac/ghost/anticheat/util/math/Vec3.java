package ac.ghost.anticheat.util.math;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Vec3 implements Cloneable {
    public final static Vec3 ZERO = new Vec3(0, 0, 0);

    public float x, y, z;

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(double x, double y, double z) {
        this((float) x, (float) y, (float) z);
    }

    public Vec3(Vector3f vector3f) {
        this.x = vector3f.x;
        this.y = vector3f.y;
        this.z = vector3f.z;
    }

    public Vec3(BlockVector3 vector3i) {
        this.x = vector3i.x;
        this.y = vector3i.y;
        this.z = vector3i.z;
    }

    public int compareTo(BlockVector3 vec3i) {
        if (this.getY() == vec3i.y) {
            return (int) (this.getZ() == vec3i.z ? this.getX() - vec3i.x : this.getZ() - vec3i.z);
        } else {
            return (int) (this.getY() - vec3i.y);
        }
    }

    public float distToCenterSqr(Vec3 vec3) {
        return distToCenterSqr(vec3.getX(), vec3.getY(), vec3.getZ());
    }

    public float distToCenterSqr(float d, float e, float f) {
        float g = this.getX() + 0.5F - d;
        float h = this.getY() + 0.5F - e;
        float i = this.getZ() + 0.5F - f;
        return g * g + h * h + i * i;
    }

    public Vector3f toVector3f() {
        return new Vector3f(this.x, this.y, this.z);
    }

    public BlockVector3 toBlockVector3() {
        return new BlockVector3((int) Math.floor(this.x), (int) Math.floor(this.y), (int) Math.floor(this.z));
    }

    public Vector3 toVector3() {
        return new Vector3(this.x, this.y, this.z);
    }

    public float squaredDistanceTo(Vec3 vec) {
        float d = vec.x - this.x;
        float e = vec.y - this.y;
        float f = vec.z - this.z;
        return d * d + e * e + f * f;
    }

    public float distanceTo(Vec3 vec) {
        return (float) Math.sqrt(squaredDistanceTo(vec));
    }

    public float horizontalLength() {
        return (float) Math.sqrt(horizontalLengthSquared());
    }

    public float horizontalLengthSquared() {
        return this.x * this.x + this.z * this.z;
    }

    public float lengthSquared() {
        return this.getX() * this.getX() + this.getY() * this.getY() + this.getZ() * this.getZ();
    }

    public float length() {
        return (float) Math.sqrt(this.lengthSquared());
    }

    public Vec3 add(float v) {
        return this.add(v, v, v);
    }

    public Vec3 add(Vec3 vec3) {
        return this.add(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 add(float v, float v1, float v2) {
        return new Vec3(this.x + v, this.y + v1, this.z + v2);
    }

    public Vec3 add(double v, double v1, double v2) {
        return this.add((float) v, (float) v1, (float) v2);
    }

    public Vec3 subtract(Vec3 v) {
        return this.subtract(v.getX(), v.getY(), v.getZ());
    }

    public Vec3 subtract(float v, float v1, float v2) {
        return new Vec3(this.x - v, this.y - v1, this.z - v2);
    }

    public Vec3 subtract(double v, double v1, double v2) {
        return this.subtract((float) v, (float) v1, (float) v2);
    }

    public Vec3 multiply(float a) {
        return this.multiply(a, a, a);
    }

    public Vec3 multiply(float v, float v1, float v2) {
        return new Vec3(this.x * v, this.y * v1, this.z * v2);
    }

    public Vec3 multiply(double v, double v1, double v2) {
        return this.multiply((float) v, (float) v1, (float) v2);
    }

    public Vec3 multiply(Vec3 v) {
        return this.multiply(v.getX(), v.getY(), v.getZ());
    }

    public Vec3 divide(float v) {
        return this.divide(v, v, v);
    }

    public Vec3 divide(float v, float v1, float v2) {
        return new Vec3(this.x / v, this.y / v1, this.z / v2);
    }

    public Vec3 up(float v) {
        return new Vec3(this.getX(), this.getY() + v, this.getZ());
    }

    public Vec3 down(float v) {
        return new Vec3(this.getX(), this.getY() - v, this.getZ());
    }

    public Vec3 north(float v) {
        return new Vec3(this.getX(), this.getY(), this.getZ() - v);
    }

    public Vec3 south(float v) {
        return new Vec3(this.getX(), this.getY(), this.getZ() + v);
    }

    public Vec3 east(float v) {
        return new Vec3(this.getX() + v, this.getY(), this.getZ());
    }

    public Vec3 west(float v) {
        return new Vec3(this.getX() - v, this.getY(), this.getZ());
    }

    public Vec3 normalize() {
        float length = this.length();
        if (Math.abs(length) < Float.intBitsToFloat(0x34000000)) {
            return Vec3.ZERO;
        } else {
            return new Vec3(this.getX() / length, this.getY() / length, this.getZ() / length);
        }
    }

    public String horizontalToString() {
        return "(" + this.x + ", " + this.z + ")";
    }

    public Vec3 clone() {
        return new Vec3(this.x, this.y, this.z);
    }
}
