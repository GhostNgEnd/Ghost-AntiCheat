package ac.ghost.anticheat.packets.server;

import cn.nukkit.network.protocol.AdventureSettingsPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.AuthoritativeMovementMode;
import cn.nukkit.network.protocol.types.PlayerAbility;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ServerDataPacketsTest {
    @Test
    void startGameAdvertisesTheModeNukkitActuallyProcesses() {
        ServerDataPackets.MovementAuthoritySettings settings =
                ServerDataPackets.movementAuthoritySettings(
                        AuthoritativeMovementMode.CLIENT, 20);
        assertEquals(AuthoritativeMovementMode.CLIENT,
                settings.mode());
        assertFalse(settings.serverAuthoritative());
        assertEquals(0, settings.rewindHistory());

        settings = ServerDataPackets.movementAuthoritySettings(
                AuthoritativeMovementMode.SERVER, 20);
        assertEquals(AuthoritativeMovementMode.SERVER,
                settings.mode());
        assertTrue(settings.serverAuthoritative());
        assertEquals(0, settings.rewindHistory());

        settings = ServerDataPackets.movementAuthoritySettings(
                AuthoritativeMovementMode.SERVER_WITH_REWIND, 20);
        assertEquals(AuthoritativeMovementMode.SERVER_WITH_REWIND,
                settings.mode());
        assertTrue(settings.serverAuthoritative());
        assertEquals(20, settings.rewindHistory());
    }

    @Test
    void legacyAdventureSettingsMapToTheSameAbilitySlots() {
        final AdventureSettingsPacket packet = new AdventureSettingsPacket();
        packet.setFlag(AdventureSettingsPacket.ALLOW_FLIGHT, true);
        packet.setFlag(AdventureSettingsPacket.FLYING, true);
        packet.setFlag(AdventureSettingsPacket.NO_CLIP, true);
        packet.setFlag(AdventureSettingsPacket.MINE, true);

        final Set<PlayerAbility> abilities =
                ServerDataPackets.legacyAbilities(packet,
                        ProtocolInfo.v1_2_0, false);

        assertTrue(abilities.contains(PlayerAbility.MAY_FLY));
        assertTrue(abilities.contains(PlayerAbility.FLYING));
        assertTrue(abilities.contains(PlayerAbility.NO_CLIP));
        assertTrue(abilities.contains(PlayerAbility.MINE));
        assertFalse(abilities.contains(PlayerAbility.INSTABUILD));
    }

    @Test
    void creativeSuppliesLegacyInstabuildSemantic() {
        assertTrue(ServerDataPackets.legacyAbilities(
                new AdventureSettingsPacket(), ProtocolInfo.v1_1_0, true)
                .contains(PlayerAbility.INSTABUILD));
    }

    @Test
    void protocolOnePointOneDoesNotApplyUnencodedSecondFlagSet() {
        final AdventureSettingsPacket packet = new AdventureSettingsPacket();
        packet.setFlag(AdventureSettingsPacket.MINE, true);

        assertFalse(ServerDataPackets.legacyAbilities(
                packet, ProtocolInfo.v1_1_0, false)
                .contains(PlayerAbility.MINE));
    }
}
