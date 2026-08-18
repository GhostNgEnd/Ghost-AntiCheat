package ac.ghost.anticheat.prediction.nukkit;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.collision.bds.system.ActorSetPosSystem;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.movement.PlayerBoundingBoxStateUpdateSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.UpdateHorizontalPoseSystem;
import ac.ghost.anticheat.prediction.bds.system.player.StartGlidingActionServerSystem;
import ac.ghost.anticheat.prediction.bds.system.player.UpdateAbilitiesSystem;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.network.protocol.types.PlayerAbility;
import cn.nukkit.network.protocol.types.InputMode;

import java.util.LinkedHashSet;
import java.util.Set;








public final class NukkitServerPlayerInitializationAdapter {
    private NukkitServerPlayerInitializationAdapter() {
    }

    public static void initialize(final GhostPlayer player) {
        final Player session = player.getSession();
        player.ghostMovementBridgeState.resetLegacyInputBridge();
        final Vec3 initialPosition = new Vec3(session.x, session.y, session.z);
        ActorSetPosSystem.setImmediate(player, initialPosition, false);
        player.entityContext.stateVectorComponent.setPreviousPosition(initialPosition.clone());
        player.entityContext.serverPlayerCurrentMovementComponent.setUnvalidatedPosition(initialPosition.clone());
        player.entityContext.serverPlayerCurrentMovementComponent.setPreviousUnvalidatedPosition(initialPosition.clone());

        player.entityContext.actorRotationComponent.initialize(
                (float) session.pitch, (float) session.yaw, (float) session.yaw);
        player.entityContext.playerInputModeComponent.set(
                initialInputMode(session));
        player.entityContext.actorGameTypeComponent.value = initialBedrockGameType(session.getGamemode());

        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_SNEAKING, session.isSneaking());
        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_CAN_CLIMB, true);
        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_SWIMMING, session.isSwimming());
        final boolean crawling =
                session.getDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_CRAWLING);
        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_CRAWLING,
                crawling);
        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_SPRINTING, session.isSprinting());
        player.entityContext.synchedActorDataComponent.setFlag(Entity.DATA_FLAG_SWIMMING, session.isSwimming());
        player.entityContext.synchedActorDataComponent.setFlag(Entity.DATA_FLAG_SNEAKING, session.isSneaking());
        player.entityContext.synchedActorDataComponent.setFlag(Entity.DATA_FLAG_CRAWLING,
                crawling);
        player.entityContext.synchedActorDataComponent.setFlag(Entity.DATA_FLAG_SPRINTING, session.isSprinting());
        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_GLIDING,
                session.isGliding() && StartGlidingActionServerSystem.hasElytraEquipped(player.entityContext));
        player.entityContext.synchedActorDataComponent.setFlag(Entity.DATA_FLAG_GLIDING,
                player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_GLIDING));
        UpdateHorizontalPoseSystem.tick(player.entityContext);
        PlayerBoundingBoxStateUpdateSystem.tick(player.entityContext);
        PlayerBoundingBoxStateUpdateSystem.initialize(
                player.entityContext, session.getWidth(), session.getHeight());

        final AdventureSettings settings = session.getAdventureSettings();
        final Set<PlayerAbility> initialAbilities = new LinkedHashSet<>();
        for (AdventureSettings.Type type : AdventureSettings.Type.values()) {
            if (type.isAbility() && settings.get(type)) {
                initialAbilities.add(type.getAbility());
            }
        }
        player.entityContext.abilitiesComponent.applyProtocolSnapshot(
                initialAbilities,
                session.getFlySpeed(),
                session.getVerticalFlySpeed());
        UpdateAbilitiesSystem.tick(player);
        player.entityContext.playerFlyingTravelComponent.setPresent(
                player.entityContext.movementAbilitiesComponent.isFlying());

        player.entityContext.playerMovementSettingsComponent.setServerAuthoritativeMovementStrict(
                Ghost.getConfig().serverAuthoritativeMovementStrict());
        player.entityContext.playerMovementSettingsComponent.setPlayerPositionAcceptanceThreshold(
                Ghost.getConfig().playerPositionAcceptanceThreshold());
        player.entityContext.playerMovementSettingsComponent.setPlayerMovementActionDirectionThreshold(
                Ghost.getConfig().movementActionDirectionThreshold());
        player.entityContext.playerMovementSettingsComponent.setPlayerRewindHistorySizeTicks(
                Ghost.getConfig().rewindHistory());
        player.entityContext.clientAcceptanceThresholdsComponent.setPositionThresholdEnabled(false);
    }

    private static cn.nukkit.network.protocol.types.GameType initialBedrockGameType(
            final int nukkitGameMode) {
        return switch (nukkitGameMode) {
            case Player.CREATIVE -> cn.nukkit.network.protocol.types.GameType.CREATIVE;
            case Player.ADVENTURE -> cn.nukkit.network.protocol.types.GameType.ADVENTURE;
            case Player.SPECTATOR -> cn.nukkit.network.protocol.types.GameType.SPECTATOR;
            default -> cn.nukkit.network.protocol.types.GameType.SURVIVAL;
        };
    }

    private static InputMode initialInputMode(final Player session) {
        if (session.getLoginChainData() == null) {
            return InputMode.UNDEFINED;
        }
        int mode = session.getLoginChainData().getCurrentInputMode();
        if (mode <= 0 || mode >= InputMode.COUNT.getOrdinal()) {
            mode = session.getLoginChainData().getDefaultInputMode();
        }
        if (mode <= 0 || mode >= InputMode.COUNT.getOrdinal()) {
            return InputMode.UNDEFINED;
        }
        return InputMode.fromOrdinal(mode);
    }
}
