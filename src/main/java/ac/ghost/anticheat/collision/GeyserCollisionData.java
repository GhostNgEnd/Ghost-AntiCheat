package ac.ghost.anticheat.collision;

import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.data.block.NetworkBlockState;
import cn.nukkit.block.Block;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;






public final class GeyserCollisionData {
    static final int FENCE = 1;
    static final int PANE = 2;
    static final int STAIRS = 3;
    static final int WALL = 4;
    static final int CHORUS = 5;
    static final int PISTON = 6;

    private static final int MAGIC = 0x47484F53;
    private static final int FORMAT_VERSION = 2;
    private static final List<List<Box>> SHAPES;
    private static final Map<Integer, Integer> BLOCK_SHAPES;
    private static final Map<Integer, Integer> DYNAMIC_SHAPES;

    static {
        try (InputStream resource = GeyserCollisionData.class.getClassLoader()
                .getResourceAsStream("ghost/geyser-collisions.bin")) {
            if (resource == null) {
                throw new IllegalStateException("Missing ghost/geyser-collisions.bin");
            }
            try (DataInputStream input = new DataInputStream(new BufferedInputStream(resource))) {
                if (input.readInt() != MAGIC) {
                    throw new IllegalStateException("Invalid Geyser collision data");
                }
                int version = input.readInt();
                if (version != FORMAT_VERSION) {
                    throw new IllegalStateException("Unsupported Geyser collision data version " + version);
                }

                int shapeCount = input.readUnsignedShort();
                List<List<Box>> shapes = new ArrayList<>(shapeCount);
                for (int shapeIndex = 0; shapeIndex < shapeCount; shapeIndex++) {
                    int boxCount = input.readUnsignedShort();
                    List<Box> boxes = new ArrayList<>(boxCount);
                    for (int boxIndex = 0; boxIndex < boxCount; boxIndex++) {
                        boxes.add(new Box(
                                input.readFloat(), input.readFloat(), input.readFloat(),
                                input.readFloat(), input.readFloat(), input.readFloat()
                        ));
                    }
                    shapes.add(List.copyOf(boxes));
                }
                SHAPES = List.copyOf(shapes);
                BLOCK_SHAPES = readMappings(input);
                DYNAMIC_SHAPES = readMappings(input);
            }
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private GeyserCollisionData() {
    }

    public static List<Box> getBlockShape(Block block) {
        return getBlockShape(null, block);
    }

    public static List<Box> getBlockShape(NetworkBlockState state, Block block) {
        if (state != null) {
            List<Box> shape = getBlockShape(state.legacyFullId());
            if (shape != null) {
                return shape;
            }
        }

        
        
        
        if (block != null && (state == null || block.getFullId() != state.legacyFullId())) {
            return getBlockShape(block.getFullId());
        }
        return null;
    }

    private static List<Box> getBlockShape(int legacyFullId) {
        Integer shape = BLOCK_SHAPES.get(toSnapshotFullId(legacyFullId));
        return shape == null ? null : SHAPES.get(shape);
    }

    






    private static int toSnapshotFullId(int legacyFullId) {
        int blockId = legacyFullId >> Block.DATA_BITS;
        int blockData = legacyFullId & Block.DATA_MASK;
        return blockId << 6 | blockData & 0x3F;
    }

    static List<Box> getDynamicShape(int type, int state) {
        Integer shape = DYNAMIC_SHAPES.get(type << 24 | state);
        return shape == null ? null : SHAPES.get(shape);
    }


    public static boolean isSolidCollisionEquivalent(Block block) {
        return isSolidCollisionEquivalent(null, block);
    }

    public static boolean isSolidCollisionEquivalent(NetworkBlockState state, Block block) {
        if (block == null) {
            return false;
        }
        return isSingleFullCube(getBlockShape(state, block));
    }

    static boolean isSingleFullCube(List<Box> shape) {
        return shape != null
                && shape.size() == 1
                && shape.get(0).equals(new Box(0, 0, 0, 1, 1, 1));
    }

    static int mappingCount() {
        return BLOCK_SHAPES.size();
    }

    static int dynamicMappingCount() {
        return DYNAMIC_SHAPES.size();
    }

    private static Map<Integer, Integer> readMappings(DataInputStream input) throws IOException {
        int count = input.readInt();
        Map<Integer, Integer> result = new HashMap<>(count * 4 / 3 + 1);
        for (int i = 0; i < count; i++) {
            result.put(input.readInt(), input.readUnsignedShort());
        }
        return Map.copyOf(result);
    }
}
