package ac.ghost.anticheat.packets.server;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.data.vanilla.AttributeInstance;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;
import ac.ghost.anticheat.prediction.nukkit.NukkitClientVisibleMovementMetadataAdapter;
import ac.ghost.anticheat.prediction.bds.system.player.UpdateAbilitiesSystem;
import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.util.EntityMetadataUtil;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.AdventureSettingsPacket;
import cn.nukkit.network.protocol.DisconnectPacket;
import cn.nukkit.network.protocol.RespawnPacket;
import cn.nukkit.network.protocol.StartGamePacket;
import cn.nukkit.network.protocol.SetPlayerGameTypePacket;
import cn.nukkit.network.protocol.SetEntityDataPacket;
import cn.nukkit.network.protocol.UpdateAbilitiesPacket;
import cn.nukkit.network.protocol.UpdateAttributesPacket;
import cn.nukkit.network.protocol.types.AbilityLayer;
import cn.nukkit.network.protocol.types.AuthoritativeMovementMode;
import cn.nukkit.network.protocol.types.GameType;
import cn.nukkit.network.protocol.types.PlayerAbility;

import java.util.Set;
import java.util.LinkedHashSet;

public class ServerDataPackets implements Listener {
    @EventHandler
    public void onPacket(final DataPacketSendEvent event) {
        final Player nukkitPlayer = event.getPlayer();
        if (nukkitPlayer == null) {
            return;
        }

        GhostPlayer managedPlayer = Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
        if (managedPlayer == null && event.getPacket() instanceof StartGamePacket) {
            sanitizeLoginMovementSpeed(nukkitPlayer);
            managedPlayer = Ghost.getInstance().getPlayerManager().add(nukkitPlayer);
            managedPlayer.runtimeEntityId = nukkitPlayer.getId();
        }
        if (managedPlayer == null) {
            return;
        }
        final GhostPlayer player = managedPlayer;

        if (event.getPacket() instanceof DisconnectPacket) {
            Ghost.getInstance().getPlayerManager().remove(nukkitPlayer);
            return;
        }

        








        if (event.getPacket() instanceof RespawnPacket packet
                && packet.respawnState
                == RespawnPacket.STATE_SEARCHING_FOR_SPAWN
                && !nukkitPlayer.isAlive()
                && !BedrockProtocolCapabilities.hasHandleTeleportAuthInput(
                nukkitPlayer.protocol)
                && !player.ghostMovementBridgeState.legacyRespawnTransition) {
            player.ghostMovementBridgeState.legacyRespawnTransition = true;
            player.resetForLegacyRespawnTransition();
        }

        if (event.getPacket() instanceof StartGamePacket packet) {
            player.runtimeEntityId = packet.entityRuntimeId;
            player.packetVisibleChunkCache.setDimension(packet.dimension);
            player.entityContext.playerLoadingScreenComponent.screenId = null;
            player.entityContext.playerLoadingScreenComponent.active = true;
            player.entityContext.playerLoadingScreenComponent.ticksSinceChange = 0;
            player.entityContext.playerLoadingScreenComponent.initialFallbackDeadlineNs = 0L;

            
            
            
            
            configureAuthoritativeMovement(packet,
                    nukkitPlayer.getAuthoritativeMovementMode(),
                    Ghost.getConfig().rewindHistory());
            player.latencyAdapter.sendLatencyStack(() ->
                    player.entityContext.actorGameTypeComponent.value = GameType.from(packet.playerGamemode));
        } else if (event.getPacket() instanceof UpdateAttributesPacket packet) {
            if (packet.entityId != player.runtimeEntityId) {
                return;
            }

            player.latencyAdapter.sendLatencyStack(() -> {
                for (final cn.nukkit.entity.Attribute entry : packet.entries) {
                    final AttributeInstance attribute = player.entityContext.attributesComponent.baseAttributeMap().get(entry.getName());
                    if (attribute == null) {
                        continue;
                    }

                    
                    
                    
                    
                    
                    
                    attribute.clearModifiers();
                    attribute.setBaseRange(entry.getMinValue(), entry.getMaxValue());
                    attribute.setBaseValue(entry.getDefaultValue());
                    attribute.setValue(entry.getValue());
                }
            });
        } else if (event.getPacket() instanceof SetEntityDataPacket packet) {
            if (packet.eid != player.runtimeEntityId) {
                
                
                
                final EntityCache cache = player.entityRegistry.getEntity(packet.eid);
                if (cache == null) {
                    return;
                }

                player.latencyAdapter.latencyUtil().queue(() ->
                        cache.applyMetadata(packet.metadata));
                return;
            }

            if (player.entityContext.vehicleComponent.value != null) {
                return;
            }

            final Set<Integer> flags = EntityMetadataUtil.copyFlags(packet.metadata);
            final Float width = EntityMetadataUtil.getFloat(packet.metadata, Entity.DATA_BOUNDING_BOX_WIDTH);
            final Float height = EntityMetadataUtil.getFloat(packet.metadata, Entity.DATA_BOUNDING_BOX_HEIGHT);
            final Float scale = EntityMetadataUtil.getFloat(packet.metadata, Entity.DATA_SCALE);
            if (flags == null && width == null && height == null && scale == null) {
                return;
            }

            final Boolean actionFlag = flags == null
                    ? null : flags.contains(Entity.DATA_FLAG_ACTION);
            if (actionFlag != null) {
                NukkitItemUseStateSystem.onMovementMetadataSent(player, actionFlag);
            }

            player.latencyAdapter.sendLatencyStack(() -> {
                if (actionFlag != null) {
                    NukkitItemUseStateSystem.onMovementMetadataAcknowledged(player, actionFlag);
                }

                
                
                
                
                NukkitClientVisibleMovementMetadataAdapter.apply(player, width, height, scale, flags);
            });
        } else if (event.getPacket() instanceof UpdateAbilitiesPacket packet) {
            if (packet.getEntityId() != player.runtimeEntityId) {
                return;
            }

            
            
            
            
            
            
            final GameType gameTypeAtAbilitySend = bedrockGameType(nukkitPlayer.getGamemode());

            player.latencyAdapter.sendLatencyStack(() -> {
                player.entityContext.actorGameTypeComponent.value = gameTypeAtAbilitySend;
                player.entityContext.abilitiesComponent.clearProtocolAbilities();
                for (final AbilityLayer layer : packet.getAbilityLayers()) {
                    
                    
                    
                    
                    
                    
                    
                    for (final PlayerAbility ability
                            : layer.getAbilitiesSet()) {
                        if (layer.getAbilityValues().contains(ability)) {
                            player.entityContext.abilitiesComponent.setProtocolAbility(ability, true);
                        } else {
                            player.entityContext.abilitiesComponent.setProtocolAbility(ability, false);
                        }
                    }

                    
                    
                    
                    
                    if (layer.getAbilitiesSet().contains(
                            PlayerAbility.FLY_SPEED)) {
                        if (Float.isFinite(layer.getFlySpeed())) {
                            player.entityContext.abilitiesComponent.setProtocolFlySpeed(layer.getFlySpeed());
                        }
                    }
                    if (layer.getAbilitiesSet().contains(
                            PlayerAbility.VERTICAL_FLY_SPEED)) {
                        if (Float.isFinite(layer.getVerticalFlySpeed())) {
                            player.entityContext.abilitiesComponent.setProtocolVerticalFlySpeed(layer.getVerticalFlySpeed());
                        }
                    }
                }
                
                
                
                player.entityContext.abilitiesComponent.resolveProtocolSnapshot();
                UpdateAbilitiesSystem.tick(player);
            });
        } else if (event.getPacket() instanceof AdventureSettingsPacket packet) {
            
            
            if (nukkitPlayer.protocol >= cn.nukkit.network.protocol.ProtocolInfo.v1_2_0
                    && packet.entityUniqueId != player.runtimeEntityId) {
                return;
            }

            final GameType gameTypeAtAbilitySend = bedrockGameType(nukkitPlayer.getGamemode());
            final Set<PlayerAbility> abilities = legacyAbilities(packet,
                    nukkitPlayer.protocol, nukkitPlayer.isCreative());
            final float flySpeed = nukkitPlayer.getFlySpeed();
            final float verticalFlySpeed = nukkitPlayer.getVerticalFlySpeed();

            player.latencyAdapter.sendLatencyStackAfterOutbound(() -> {
                player.entityContext.actorGameTypeComponent.value = gameTypeAtAbilitySend;
                player.entityContext.abilitiesComponent.applyProtocolSnapshot(
                        abilities, flySpeed, verticalFlySpeed);
                UpdateAbilitiesSystem.tick(player);
            });
        } else if (event.getPacket() instanceof SetPlayerGameTypePacket packet) {
            player.latencyAdapter.sendLatencyStack(() ->
                    player.entityContext.actorGameTypeComponent.value = GameType.from(packet.gamemode));
        }
    }

    private static void sanitizeLoginMovementSpeed(final Player player) {
        final float movementSpeed = player.getMovementSpeed();
        if (Float.isNaN(movementSpeed) || Float.isInfinite(movementSpeed)
                || movementSpeed < 0.0F) {
            player.setMovementSpeed(Player.DEFAULT_SPEED);
        }
    }

    private static GameType bedrockGameType(final int nukkitGameMode) {
        
        
        
        return switch (nukkitGameMode) {
            case Player.CREATIVE -> GameType.CREATIVE;
            case Player.ADVENTURE -> GameType.ADVENTURE;
            case Player.SPECTATOR -> GameType.SPECTATOR;
            default -> GameType.SURVIVAL;
        };
    }

    static void configureAuthoritativeMovement(final StartGamePacket packet,
                                               final AuthoritativeMovementMode mode,
                                               final int rewindHistory) {
        final MovementAuthoritySettings settings = movementAuthoritySettings(
                mode, rewindHistory);
        packet.authoritativeMovementMode = settings.mode();
        packet.isMovementServerAuthoritative = settings.serverAuthoritative();
        packet.rewindHistorySize = settings.rewindHistory();
    }

    static MovementAuthoritySettings movementAuthoritySettings(
            final AuthoritativeMovementMode mode,
            final int rewindHistory) {
        final AuthoritativeMovementMode effectiveMode = mode == null
                ? AuthoritativeMovementMode.CLIENT : mode;
        return new MovementAuthoritySettings(
                effectiveMode,
                effectiveMode != AuthoritativeMovementMode.CLIENT,
                effectiveMode == AuthoritativeMovementMode.SERVER_WITH_REWIND
                        ? Math.max(0, rewindHistory) : 0);
    }

    record MovementAuthoritySettings(AuthoritativeMovementMode mode,
                                     boolean serverAuthoritative,
                                     int rewindHistory) {
    }

    static Set<PlayerAbility> legacyAbilities(final AdventureSettingsPacket packet,
                                               final int protocol,
                                               final boolean creative) {
        final Set<PlayerAbility> abilities = new LinkedHashSet<>();
        for (final AdventureSettings.Type type : AdventureSettings.Type.values()) {
            final int flag = type.getId();
            if (type.isAbility() && flag > 0
                    
                    && (protocol >= cn.nukkit.network.protocol.ProtocolInfo.v1_2_0
                    || (flag & AdventureSettingsPacket.BITFLAG_SECOND_SET) == 0)
                    && packet.getFlag(flag)) {
                abilities.add(type.getAbility());
            }
        }
        
        
        
        if (creative) {
            abilities.add(PlayerAbility.INSTABUILD);
        }
        return abilities;
    }
}
