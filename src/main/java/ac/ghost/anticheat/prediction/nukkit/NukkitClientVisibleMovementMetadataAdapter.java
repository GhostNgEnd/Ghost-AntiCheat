package ac.ghost.anticheat.prediction.nukkit;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.PlayerBoundingBoxStateUpdateSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.UpdateHorizontalPoseSystem;
import ac.ghost.anticheat.prediction.bds.system.player.StartGlidingActionServerSystem;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.network.protocol.SetEntityDataPacket;

import java.util.Set;







public final class NukkitClientVisibleMovementMetadataAdapter {
    private NukkitClientVisibleMovementMetadataAdapter() {
    }

    public static void apply(final GhostPlayer player,
                             final Float width,
                             final Float height,
                             final Float scale,
                             final Set<Integer> flags) {
        if (flags != null) {
            final boolean metadataSneaking =
                    flags.contains(Entity.DATA_FLAG_SNEAKING);
            final boolean metadataSwimming =
                    flags.contains(Entity.DATA_FLAG_SWIMMING);
            final boolean metadataCrawling =
                    flags.contains(Entity.DATA_FLAG_CRAWLING);
            final boolean metadataSprinting =
                    flags.contains(Entity.DATA_FLAG_SPRINTING);
            final boolean metadataGliding =
                    flags.contains(Entity.DATA_FLAG_GLIDING);
            final boolean movementGliding =
                    metadataGliding
                            && StartGlidingActionServerSystem
                            .hasElytraEquipped(player.entityContext);
            final boolean canClimb = player.entityContext.actorDataFlagComponent
                    .has(Entity.DATA_FLAG_CAN_CLIMB);

            
            
            
            
            
            player.entityContext.synchedActorDataComponent.setFlags(flags);
            player.entityContext.actorDataFlagComponent.replace(flags);
            player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_CAN_CLIMB,
                    canClimb);

            NukkitItemUseStateSystem.applyAcknowledgedMetadata(player,
                    flags.contains(Entity.DATA_FLAG_ACTION));

            
            
            
            
            player.entityContext.synchedActorDataComponent.setFlag(
                    Entity.DATA_FLAG_SNEAKING, metadataSneaking);
            player.entityContext.synchedActorDataComponent.setFlag(
                    Entity.DATA_FLAG_CRAWLING, metadataCrawling);
            player.entityContext.synchedActorDataComponent.setFlag(
                    Entity.DATA_FLAG_SPRINTING, metadataSprinting);
            player.entityContext.synchedActorDataComponent.setFlag(
                    Entity.DATA_FLAG_SWIMMING, metadataSwimming);
            player.entityContext.synchedActorDataComponent.setFlag(
                    Entity.DATA_FLAG_GLIDING, metadataGliding);

            player.entityContext.actorDataFlagComponent.set(
                    Entity.DATA_FLAG_SNEAKING, metadataSneaking);
            player.entityContext.actorDataFlagComponent.set(
                    Entity.DATA_FLAG_SWIMMING, metadataSwimming);
            player.entityContext.actorDataFlagComponent.set(
                    Entity.DATA_FLAG_CRAWLING, metadataCrawling);
            player.entityContext.actorDataFlagComponent.set(
                    Entity.DATA_FLAG_SPRINTING, metadataSprinting);
            player.entityContext.actorDataFlagComponent.set(
                    Entity.DATA_FLAG_GLIDING, movementGliding);
            UpdateHorizontalPoseSystem.tick(player.entityContext);
            PlayerBoundingBoxStateUpdateSystem.tick(player.entityContext);
        }

        PlayerBoundingBoxStateUpdateSystem.applyClientVisibleDimensions(
                player.entityContext, width, height, scale);
    }

    public static void resyncDimensionsToClient(final GhostPlayer player) {
        final SetEntityDataPacket packet = new SetEntityDataPacket();
        packet.eid = player.runtimeEntityId;
        packet.metadata = new EntityMetadata()
                .putFloat(Entity.DATA_BOUNDING_BOX_WIDTH,
                        player.entityContext.aabbShapeComponent.getDimensions().width())
                .putFloat(Entity.DATA_BOUNDING_BOX_HEIGHT,
                        player.entityContext.aabbShapeComponent.getDimensions().height());
        player.getSession().dataPacket(packet);
    }
}
