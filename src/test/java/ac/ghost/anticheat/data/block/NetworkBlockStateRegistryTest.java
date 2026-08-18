package ac.ghost.anticheat.data.block;

import cn.nukkit.GameVersion;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NetworkBlockStateRegistryTest {

    @Test
    void loadsNetworkEndian113PaletteByIndexedRuntimeId() throws Exception {
        NetworkBlockState air = loadState(GameVersion.V1_13_0, 0);
        NetworkBlockState stone = loadState(GameVersion.V1_13_0, 1);

        assertNotNull(air);
        assertTrue(air.is("minecraft:air"));
        assertNotNull(stone);
        assertTrue(stone.is("minecraft:stone"));
    }

    @Test
    void loads116PaletteByIndexedRuntimeId() throws Exception {
        NetworkBlockState first = loadState(GameVersion.V1_16_0, 0);

        assertNotNull(first);
        assertTrue(first.is("minecraft:acacia_button"));
        assertEquals(395, first.blockId());
    }

    private static NetworkBlockState loadState(GameVersion version,
                                                int networkId) throws Exception {
        Method load = NetworkBlockStateRegistry.class
                .getDeclaredMethod("load", GameVersion.class);
        load.setAccessible(true);
        Object registry = load.invoke(null, version);

        Method resolve = registry.getClass().getDeclaredMethod(
                "tryResolve", GameVersion.class, int.class);
        resolve.setAccessible(true);
        return (NetworkBlockState) resolve.invoke(registry, version, networkId);
    }
}
