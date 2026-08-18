package ac.ghost.anticheat.compensated.cache.entity;

import ac.ghost.anticheat.compensated.cache.entity.state.CachedEntityState;
import ac.ghost.anticheat.data.EntityDimensions;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.AABBShapeComponent;
import ac.ghost.anticheat.prediction.bds.component.CollidableMobFlagComponent;
import ac.ghost.anticheat.prediction.bds.component.FallingBlockFlagComponent;
import ac.ghost.anticheat.util.EntityMetadataUtil;
import ac.ghost.anticheat.util.math.Vec3;
import ac.ghost.anticheat.util.reach.PositionInterpolator;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.entity.item.EntityChestBoat;
import cn.nukkit.entity.item.EntityMinecartChest;
import cn.nukkit.entity.item.EntityMinecartCommandBlock;
import cn.nukkit.entity.item.EntityMinecartEmpty;
import cn.nukkit.entity.item.EntityMinecartHopper;
import cn.nukkit.entity.item.EntityMinecartTNT;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public final class EntityCache {
    private final GhostPlayer player;
    private final int entityType;
    private final String identifier;
    private final long runtimeId;
    private final int dimension;

    
    private final float definitionWidth;
    private final float definitionHeight;
    private final float definitionOffset;

    private EntityDimensions dimensions;
    private Vec3 serverPosition = Vec3.ZERO;
    private boolean inVehicle;
    private EntityMetadata metadata = new EntityMetadata();

    private CachedEntityState past;
    private CachedEntityState current;

    private final AABBShapeComponent aabbShapeComponent = new AABBShapeComponent();
    private final CollidableMobFlagComponent collidableMobFlagComponent =
            new CollidableMobFlagComponent();
    private final FallingBlockFlagComponent fallingBlockFlagComponent =
            new FallingBlockFlagComponent();

    





    private long queuedPositionUpdateSequence;
    private long appliedPositionUpdateSequence;

    public boolean affectedByOffset;

    public EntityCache(final GhostPlayer player,
                       final int entityType,
                       final String identifier,
                       final long runtimeId,
                       final int dimension,
                       final float definitionWidth,
                       final float definitionHeight,
                       final float definitionOffset,
                       final boolean affectedByOffset) {
        this.player = player;
        this.entityType = entityType;
        this.identifier = identifier;
        this.runtimeId = runtimeId;
        this.dimension = dimension;
        this.definitionWidth = definitionWidth;
        this.definitionHeight = definitionHeight;
        this.definitionOffset = definitionOffset;
        this.affectedByOffset = affectedByOffset;
        this.dimensions = EntityDimensions.fixed(definitionWidth, definitionHeight);
        this.fallingBlockFlagComponent.setPresent(
                "minecraft:falling_block".equalsIgnoreCase(identifier));
    }

    




    public void applyMetadata(final EntityMetadata metadata) {
        this.metadata = metadata == null ? new EntityMetadata() : metadata;

        final Float width = EntityMetadataUtil.getFloat(this.metadata, Entity.DATA_BOUNDING_BOX_WIDTH);
        final Float height = EntityMetadataUtil.getFloat(this.metadata, Entity.DATA_BOUNDING_BOX_HEIGHT);
        final Float scale = EntityMetadataUtil.getFloat(this.metadata, Entity.DATA_SCALE);

        if (width != null) {
            this.dimensions = EntityDimensions.fixed(width, this.definitionHeight);
        }
        if (height != null) {
            this.dimensions = EntityDimensions.fixed(this.definitionWidth, height);
        }

        
        
        
        
        if (this.isBoat()) {
            this.dimensions = EntityDimensions.fixed(this.definitionWidth, this.definitionHeight);
        }

        if (scale != null) {
            this.dimensions = this.dimensions.hardScaled(scale);
        }

        this.collidableMobFlagComponent.setPresent(this.hasCollidableActorDataFlag());
        this.refreshAABBShapeComponent();
    }

    private boolean hasCollidableActorDataFlag() {
        return (this.metadata.exists(Entity.DATA_FLAGS)
                || this.metadata.exists(Entity.DATA_FLAGS_EXTENDED))
                && Entity.DATA_FLAG_COLLIDABLE >= 0
                && EntityMetadataUtil.hasFlag(
                        this.metadata, Entity.DATA_FLAG_COLLIDABLE);
    }

    public float getYOffset() {
        return this.affectedByOffset ? this.definitionOffset : 0F;
    }

    public boolean isPlayerEntity() {
        return "minecraft:player".equalsIgnoreCase(this.identifier);
    }

    public boolean isBoat() {
        return this.entityType == EntityBoat.NETWORK_ID
                || this.entityType == EntityChestBoat.NETWORK_ID
                || "minecraft:boat".equalsIgnoreCase(this.identifier)
                || "minecraft:chest_boat".equalsIgnoreCase(this.identifier);
    }

    public boolean isMinecart() {
        return this.entityType == EntityMinecartEmpty.NETWORK_ID
                || this.entityType == EntityMinecartChest.NETWORK_ID
                || this.entityType == EntityMinecartHopper.NETWORK_ID
                || this.entityType == EntityMinecartTNT.NETWORK_ID
                || this.entityType == EntityMinecartCommandBlock.NETWORK_ID;
    }

    public void init() {
        this.current = new CachedEntityState(this.player, this);
        this.refreshAABBShapeComponent();
    }

    public void interpolate(final Vec3 pos, final boolean lerp) {
        this.past = this.current.clone();

        if (!lerp) {
            this.current.setTeleportPos(pos);
        } else {
            final PositionInterpolator interpolator = this.current.getInterpolator();
            if (interpolator != null) {
                interpolator.refreshPositionAndAngles(pos);
            }
        }
        this.refreshAABBShapeComponent();
    }

    public int dimension() {
        return this.dimension;
    }

    public CachedEntityState currentState() {
        return this.current;
    }

    public AABBShapeComponent aabbShapeComponent() {
        return this.aabbShapeComponent;
    }

    public CollidableMobFlagComponent collidableMobFlagComponent() {
        return this.collidableMobFlagComponent;
    }

    public FallingBlockFlagComponent fallingBlockFlagComponent() {
        return this.fallingBlockFlagComponent;
    }

    public void refreshAABBShapeComponent() {
        if (this.current == null) {
            this.aabbShapeComponent.clear();
            return;
        }
        this.aabbShapeComponent.setAABB(this.current.getBoundingBox());
    }

    public long nextPositionUpdateSequence() {
        return ++this.queuedPositionUpdateSequence;
    }

    public void interpolate(final Vec3 pos, final boolean lerp, final long sequence) {
        this.interpolate(pos, lerp);
        this.appliedPositionUpdateSequence = sequence;
    }

    




    public boolean clearPastForPositionUpdate(final long sequence) {
        if (this.appliedPositionUpdateSequence != sequence) {
            return false;
        }

        this.past = null;
        return true;
    }
}
