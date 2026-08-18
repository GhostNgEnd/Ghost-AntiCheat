package ac.ghost.anticheat.prediction.bds.entity;

import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.world.BlockSource;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;








public final class EntityRegistry {
    private final GhostPlayer player;
    private final Long2ObjectMap<EntityCache> entities = new Long2ObjectOpenHashMap<>();
    private final Map<Long, Long> uniqueIdToRuntimeId = new HashMap<>();
    private EntityContext serverPlayerEntity;

    public EntityRegistry(final GhostPlayer player) {
        this.player = player;
    }

    public Long2ObjectMap<EntityCache> entities() {
        return this.entities;
    }

    
    public EntityContext createEntity(final GhostPlayer externalPlayer,
                                      final BlockSource blockSource) {
        if (this.serverPlayerEntity == null) {
            this.serverPlayerEntity = new EntityContext(this, externalPlayer, blockSource);
        }
        return this.serverPlayerEntity;
    }

    public EntityContext serverPlayerEntity() {
        return this.serverPlayerEntity;
    }

    public void removeEntity(final long uniqueId) {
        final Long runtimeId = this.uniqueIdToRuntimeId.remove(uniqueId);
        if (runtimeId != null) {
            this.entities.remove(runtimeId.longValue());
        }
    }

    public void removeEntityByRuntimeId(final long runtimeId) {
        if (this.entities.remove(runtimeId) == null) {
            return;
        }
        this.uniqueIdToRuntimeId.values().removeIf(value -> value.longValue() == runtimeId);
    }

    public EntityCache getEntity(final long runtimeId) {
        return this.entities.get(runtimeId);
    }

    public Long getRuntimeIdByUniqueId(final long uniqueId) {
        return this.uniqueIdToRuntimeId.get(uniqueId);
    }

    public Long getUniqueIdByRuntimeId(final long runtimeId) {
        for (final Map.Entry<Long, Long> entry : this.uniqueIdToRuntimeId.entrySet()) {
            if (entry.getValue().longValue() == runtimeId) {
                return entry.getKey();
            }
        }
        return null;
    }

    public EntityCache addToCache(final long runtimeId,
                                  final long uniqueId,
                                  final int entityType,
                                  final String explicitIdentifier,
                                  final EntityMetadata spawnMetadata,
                                  final boolean playerEntity) {
        if (runtimeId == this.player.runtimeEntityId) {
            return null;
        }

        final String identifier;
        if (playerEntity) {
            identifier = "minecraft:player";
        } else if (explicitIdentifier != null && !explicitIdentifier.isBlank()) {
            identifier = explicitIdentifier;
        } else {
            final Object resolved = Entity.getIdentifier(entityType);
            if (resolved == null) {
                return null;
            }
            identifier = resolved.toString();
        }

        final Float packetWidth = ac.ghost.anticheat.util.EntityMetadataUtil.getFloat(
                spawnMetadata, Entity.DATA_BOUNDING_BOX_WIDTH);
        final Float packetHeight = ac.ghost.anticheat.util.EntityMetadataUtil.getFloat(
                spawnMetadata, Entity.DATA_BOUNDING_BOX_HEIGHT);
        final boolean boat = "minecraft:boat".equalsIgnoreCase(identifier)
                || "minecraft:chest_boat".equalsIgnoreCase(identifier);

        if (!playerEntity && !boat && (packetWidth == null || packetHeight == null)) {
            return null;
        }

        final float definitionWidth = playerEntity ? 0.6F : boat ? 1.4F : packetWidth;
        final float definitionHeight = playerEntity ? 1.8F : boat ? 0.455F : packetHeight;
        final float definitionOffset = playerEntity ? 1.62001F : boat ? 0.375F : 0F;
        final boolean affectedByOffset = playerEntity || boat;

        this.player.latencyAdapter.sendLatencyStack();
        final EntityCache cache = new EntityCache(
                this.player, entityType, identifier, runtimeId,
                this.player.packetVisibleChunkCache.getDimension(),
                definitionWidth, definitionHeight, definitionOffset, affectedByOffset);
        this.entities.put(runtimeId, cache);
        this.uniqueIdToRuntimeId.put(uniqueId, runtimeId);
        return cache;
    }
}
