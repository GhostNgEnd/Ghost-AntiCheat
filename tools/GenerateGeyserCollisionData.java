import org.cloudburstmc.nbt.NbtList;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.registry.populator.BlockRegistryPopulator;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;





public final class GenerateGeyserCollisionData {
    private static final int MAGIC = 0x424F4152;
    private static final int FORMAT_VERSION = 2;
    private static final int FENCE = 1;
    private static final int PANE = 2;
    private static final int STAIRS = 3;
    private static final int WALL = 4;
    private static final int CHORUS = 5;
    private static final int PISTON = 6;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected output path");
        }


        System.out.println("Bootstrapping " + Blocks.AIR);
        List<BlockState> javaStates = BlockRegistries.BLOCK_STATES.get();

        NbtMap collisionRoot = readMap("mappings/collisions.nbt");
        int[] collisionIndices = collisionRoot.getIntArray("indices");
        List<List<Double>> rawShapes = decodeShapes(collisionRoot);

        NbtMap mappingRoot = readMap("mappings/blocks.nbt");
        List<NbtMap> bedrockMappings = mappingRoot.getList("bedrock_mappings", NbtType.COMPOUND);

        Method buildBedrockState = BlockRegistryPopulator.class.getDeclaredMethod(
                "buildBedrockState", BlockState.class, NbtMap.class
        );
        buildBedrockState.setAccessible(true);

        Map<NbtMap, Integer> bedrockStateToShape = new HashMap<>();
        Map<NbtMap, Boolean> ambiguousStates = new HashMap<>();
        TreeSet<String> ambiguousNames = new TreeSet<>();
        Map<Integer, Integer> dynamicShapes = new TreeMap<>();
        int ambiguous = 0;
        for (int i = 0; i < javaStates.size(); i++) {
            NbtMap bedrockState = (NbtMap) buildBedrockState.invoke(null, javaStates.get(i), bedrockMappings.get(i));
            Integer previous = bedrockStateToShape.putIfAbsent(bedrockState, collisionIndices[i]);
            if (previous != null && previous != collisionIndices[i]) {
                ambiguous++;
                ambiguousStates.put(bedrockState, true);
                ambiguousNames.add(bedrockState.getString("name"));
            }
            Integer dynamicKey = dynamicKey(javaStates.get(i).toString());
            if (dynamicKey != null) {
                Integer old = dynamicShapes.putIfAbsent(dynamicKey, collisionIndices[i]);
                if (old != null && old != collisionIndices[i]) {
                    throw new IllegalStateException("Conflicting dynamic shape key " + dynamicKey
                            + ": " + old + " vs " + collisionIndices[i]);
                }
            }
        }

        Object nukkitRoot = readAny("runtime_block_states_844.dat");
        if (!(nukkitRoot instanceof List<?> nukkitStates)) {
            throw new IllegalStateException("Unexpected Nukkit palette root: " + nukkitRoot.getClass());
        }

        Map<Integer, Integer> fullIdToShape = new TreeMap<>();
        int matched = 0;
        int unmatched = 0;
        TreeSet<String> unmatchedNames = new TreeSet<>();
        for (Object object : nukkitStates) {
            NbtMap state = (NbtMap) object;
            NbtMap key = NbtMap.builder()
                    .putString("name", state.getString("name"))
                    .putCompound("states", state.getCompound("states"))
                    .build();
            Integer shape = bedrockStateToShape.get(key);
            if (shape == null || ambiguousStates.containsKey(key)) {
                unmatched++;
                unmatchedNames.add(state.getString("name"));
                continue;
            }
            matched++;
            int fullId = state.getInt("id") << 6 | state.getShort("data") & 0x3F;
            fullIdToShape.put(fullId, shape);
        }

        Path output = Path.of(args[0]);
        Files.createDirectories(output.getParent());
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(output)))) {
            out.writeInt(MAGIC);
            out.writeInt(FORMAT_VERSION);
            out.writeShort(rawShapes.size());
            for (List<Double> shape : rawShapes) {
                out.writeShort(shape.size() / 6);
                for (int i = 0; i < shape.size(); i += 6) {
                    float centerX = shape.get(i).floatValue();
                    float centerY = shape.get(i + 1).floatValue();
                    float centerZ = shape.get(i + 2).floatValue();
                    float sizeX = shape.get(i + 3).floatValue();
                    float sizeY = shape.get(i + 4).floatValue();
                    float sizeZ = shape.get(i + 5).floatValue();
                    out.writeFloat(centerX - sizeX / 2F);
                    out.writeFloat(centerY - sizeY / 2F);
                    out.writeFloat(centerZ - sizeZ / 2F);
                    out.writeFloat(centerX + sizeX / 2F);
                    out.writeFloat(centerY + sizeY / 2F);
                    out.writeFloat(centerZ + sizeZ / 2F);
                }
            }
            out.writeInt(fullIdToShape.size());
            for (Map.Entry<Integer, Integer> entry : fullIdToShape.entrySet()) {
                out.writeInt(entry.getKey());
                out.writeShort(entry.getValue());
            }
            out.writeInt(dynamicShapes.size());
            for (Map.Entry<Integer, Integer> entry : dynamicShapes.entrySet()) {
                out.writeInt(entry.getKey());
                out.writeShort(entry.getValue());
            }
        }

        System.out.println("javaStates=" + javaStates.size()
                + " shapes=" + rawShapes.size()
                + " mappings=" + fullIdToShape.size()
                + " dynamicMappings=" + dynamicShapes.size()
                + " matchedPaletteEntries=" + matched
                + " unmatchedPaletteEntries=" + unmatched
                + " ambiguousBedrockStates=" + ambiguous
                + " bytes=" + Files.size(output));
        System.out.println("ambiguousNames=" + ambiguousNames);
        System.out.println("unmatchedNames=" + unmatchedNames);
    }

    private static Integer dynamicKey(String state) {
        int bracket = state.indexOf('[');
        String name = bracket < 0 ? state : state.substring(0, bracket);
        Map<String, String> properties = parseProperties(state, bracket);

        if (name.endsWith("_fence") && !name.endsWith("_fence_gate")) {
            return FENCE << 24 | connectionMask(properties);
        }
        if (name.equals("minecraft:iron_bars") || name.endsWith("_glass_pane") || name.endsWith("copper_bars")) {
            return PANE << 24 | connectionMask(properties);
        }
        if (name.endsWith("_stairs")) {
            int facing = direction(properties.get("facing"));
            int half = "top".equals(properties.get("half")) ? 1 : 0;
            int shape = switch (properties.getOrDefault("shape", "straight")) {
                case "inner_left" -> 1;
                case "inner_right" -> 2;
                case "outer_left" -> 3;
                case "outer_right" -> 4;
                default -> 0;
            };
            return STAIRS << 24 | facing | half << 2 | shape << 3;
        }
        if (name.endsWith("_wall")) {
            int key = bool(properties, "up") ? 1 : 0;
            key |= wall(properties.get("north")) << 1;
            key |= wall(properties.get("east")) << 3;
            key |= wall(properties.get("south")) << 5;
            key |= wall(properties.get("west")) << 7;
            return WALL << 24 | key;
        }
        if (name.equals("minecraft:chorus_plant")) {
            int key = bool(properties, "down") ? 1 : 0;
            key |= (bool(properties, "up") ? 1 : 0) << 1;
            key |= (bool(properties, "north") ? 1 : 0) << 2;
            key |= (bool(properties, "east") ? 1 : 0) << 3;
            key |= (bool(properties, "south") ? 1 : 0) << 4;
            key |= (bool(properties, "west") ? 1 : 0) << 5;
            return CHORUS << 24 | key;
        }
        if (name.equals("minecraft:piston") || name.equals("minecraft:sticky_piston")) {
            int key = direction6(properties.get("facing"));
            key |= (bool(properties, "extended") ? 1 : 0) << 3;
            return PISTON << 24 | key;
        }
        return null;
    }

    private static Map<String, String> parseProperties(String state, int bracket) {
        Map<String, String> result = new HashMap<>();
        if (bracket < 0) {
            return result;
        }
        String body = state.substring(bracket + 1, state.length() - 1);
        for (String entry : body.split(",")) {
            int equals = entry.indexOf('=');
            result.put(entry.substring(0, equals), entry.substring(equals + 1));
        }
        return result;
    }

    private static int connectionMask(Map<String, String> properties) {
        int mask = bool(properties, "north") ? 1 : 0;
        mask |= (bool(properties, "east") ? 1 : 0) << 1;
        mask |= (bool(properties, "south") ? 1 : 0) << 2;
        mask |= (bool(properties, "west") ? 1 : 0) << 3;
        return mask;
    }

    private static int direction(String value) {
        return switch (value) {
            case "east" -> 1;
            case "south" -> 2;
            case "west" -> 3;
            default -> 0;
        };
    }

    private static int wall(String value) {
        return switch (value) {
            case "low" -> 1;
            case "tall" -> 2;
            default -> 0;
        };
    }

    private static int direction6(String value) {
        return switch (value) {
            case "up" -> 1;
            case "north" -> 2;
            case "south" -> 3;
            case "west" -> 4;
            case "east" -> 5;
            default -> 0;
        };
    }

    private static boolean bool(Map<String, String> properties, String key) {
        return Boolean.parseBoolean(properties.getOrDefault(key, "false"));
    }

    @SuppressWarnings("unchecked")
    private static List<List<Double>> decodeShapes(NbtMap root) {
        List<?> collisions = root.getList("collisions", NbtType.LIST);
        List<List<Double>> result = new ArrayList<>(collisions.size());
        for (Object shapeObject : collisions) {
            List<?> shape = (List<?>) shapeObject;
            List<Double> flattened = new ArrayList<>(shape.size() * 6);
            for (Object boxObject : shape) {
                for (Object coordinate : (List<?>) boxObject) {
                    flattened.add(((Number) coordinate).doubleValue());
                }
            }
            result.add(List.copyOf(flattened));
        }
        return result;
    }

    private static NbtMap readMap(String resource) throws Exception {
        return (NbtMap) readAny(resource);
    }

    private static Object readAny(String resource) throws Exception {
        try (InputStream stream = GenerateGeyserCollisionData.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Missing resource " + resource);
            }
            try (var input = NbtUtils.createGZIPReader(stream)) {
                return input.readTag();
            }
        }
    }
}
