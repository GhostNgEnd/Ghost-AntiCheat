package ac.ghost.anticheat.util;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;

import java.util.HashSet;
import java.util.Set;

public final class EntityMetadataUtil {
    private EntityMetadataUtil() {
    }

    public static Float getFloat(final EntityMetadata metadata, final int key) {
        if (metadata == null || !metadata.exists(key)) {
            return null;
        }

        final float value = metadata.getFloat(key);
        return Float.isFinite(value) ? value : null;
    }

    public static Set<Integer> copyFlags(final EntityMetadata metadata) {
        if (metadata == null) {
            return null;
        }

        final boolean hasBaseFlags = metadata.exists(Entity.DATA_FLAGS);
        final boolean hasExtendedFlags = metadata.exists(Entity.DATA_FLAGS_EXTENDED);
        if (!hasBaseFlags && !hasExtendedFlags) {
            return null;
        }

        final Set<Integer> flags = new HashSet<>();
        if (hasBaseFlags) {
            copyBits(flags, metadata.getLong(Entity.DATA_FLAGS), 0);
        }
        if (hasExtendedFlags) {
            copyBits(flags, metadata.getLong(Entity.DATA_FLAGS_EXTENDED), Long.SIZE);
        }
        return flags;
    }

    public static boolean hasFlag(final EntityMetadata metadata, final int flag) {
        if (metadata == null || flag < 0) {
            return false;
        }

        if (flag < Long.SIZE) {
            return metadata.exists(Entity.DATA_FLAGS)
                    && (metadata.getLong(Entity.DATA_FLAGS) & (1L << flag)) != 0;
        }

        final int extendedBit = flag - Long.SIZE;
        return extendedBit < Long.SIZE
                && metadata.exists(Entity.DATA_FLAGS_EXTENDED)
                && (metadata.getLong(Entity.DATA_FLAGS_EXTENDED) & (1L << extendedBit)) != 0;
    }

    private static void copyBits(final Set<Integer> flags, final long rawFlags, final int offset) {
        for (int bit = 0; bit < Long.SIZE; bit++) {
            if ((rawFlags & (1L << bit)) != 0) {
                flags.add(offset + bit);
            }
        }
    }
}
