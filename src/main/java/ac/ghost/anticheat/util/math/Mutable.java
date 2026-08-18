package ac.ghost.anticheat.util.math;

import cn.nukkit.math.BlockVector3;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Mutable {
    private int x, y, z;

    public Mutable(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Mutable(float x, float y, float z) {
        this.x = (int) Math.floor(x);
        this.y = (int) Math.floor(y);
        this.z = (int) Math.floor(z);
    }

    public Mutable() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Mutable add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Mutable add(BlockVector3 vector3i) {
        return add(vector3i.getX(), vector3i.getY(), vector3i.getZ());
    }

    public Mutable set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Mutable set(BlockVector3 vector3i) {
        return set(vector3i.getX(), vector3i.getY(), vector3i.getZ());
    }

    public Mutable set(BlockVector3 vector3i, BlockVector3 vector31) {
        return set(vector3i.getX() + vector31.getX(), vector3i.getY() + vector31.getY(), vector3i.getZ() + vector31.getZ());
    }
}
