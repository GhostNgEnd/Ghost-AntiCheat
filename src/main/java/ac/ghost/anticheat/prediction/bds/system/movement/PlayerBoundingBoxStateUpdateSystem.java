package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.data.EntityDimensions;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.entity.EntityContext;








public final class PlayerBoundingBoxStateUpdateSystem {
    private PlayerBoundingBoxStateUpdateSystem() {
    }

    public static void initialize(final EntityContext entity,
                                  final float width,
                                  final float height) {
        updateDimensions(entity, width, height);
    }

    public static void applyClientVisibleDimensions(final EntityContext entity,
                                                    final Float width,
                                                    final Float height,
                                                    final Float scale) {
        if (width != null) {
            updateWidth(entity, width);
        }
        if (height != null) {
            
            
            updateHeight(entity, height);
        }
        if (scale != null) {
            updateScale(entity, scale);
        }
    }

    
    public static void tick(final EntityContext entity) {
        final GhostPlayer player = entity.externalDataComponent.player();
        final boolean horizontal = entity.isHorizontalPoseFlagComponent.isPresent();
        if (horizontal) {
            if (!player.ghostMovementBridgeState.authSwimmingPoseApplied) {
                player.ghostMovementBridgeState.dimensionsBeforeAuthSwimming =
                        entity.aabbShapeComponent.getDimensions();
                player.ghostMovementBridgeState.authSwimmingPoseApplied = true;
            }

            entity.aabbShapeComponent.setDimensions(
                    EntityDimensions.fixed(
                                    entity.aabbShapeComponent.getDimensions().width(),
                                    0.6F)
                            .withEyeHeight(0.42F),
                    entity.stateVectorComponent.getPosition());
            return;
        }

        if (!player.ghostMovementBridgeState.authSwimmingPoseApplied) {
            return;
        }

        final EntityDimensions previous =
                player.ghostMovementBridgeState.dimensionsBeforeAuthSwimming;
        final float restoredHeight = previous == null ? 1.8F : previous.height();
        final float restoredEyeHeight = previous == null ? 1.62F : previous.eyeHeight();

        
        entity.aabbShapeComponent.setDimensions(
                EntityDimensions.fixed(
                                entity.aabbShapeComponent.getDimensions().width(),
                                restoredHeight)
                        .withEyeHeight(restoredEyeHeight),
                entity.stateVectorComponent.getPosition());

        player.ghostMovementBridgeState.dimensionsBeforeAuthSwimming = null;
        player.ghostMovementBridgeState.authSwimmingPoseApplied = false;
    }

    
    public static void restoreDimensions(final EntityContext entity,
                                         final EntityDimensions snapshot) {
        if (snapshot == null
                || !Float.isFinite(snapshot.width()) || snapshot.width() <= 0F
                || !Float.isFinite(snapshot.height()) || snapshot.height() <= 0F) {
            return;
        }

        entity.aabbShapeComponent.setDimensions(
                snapshot,
                entity.stateVectorComponent.getPosition());
    }

    private static void updateDimensions(final EntityContext entity,
                                         final float width,
                                         final float height) {
        if (!Float.isFinite(width) || !Float.isFinite(height)
                || width <= 0F || height <= 0F) {
            return;
        }

        final float eyeHeight = eyeHeightFor(height);
        entity.aabbShapeComponent.setDimensions(
                EntityDimensions.fixed(width, height).withEyeHeight(eyeHeight),
                entity.stateVectorComponent.getPosition());
    }

    private static void updateWidth(final EntityContext entity,
                                    final float width) {
        if (!Float.isFinite(width) || width <= 0F) {
            return;
        }

        entity.aabbShapeComponent.setDimensions(
                EntityDimensions.fixed(
                                width,
                                entity.aabbShapeComponent.getDimensions().height())
                        .withEyeHeight(
                                entity.aabbShapeComponent.getDimensions().eyeHeight()),
                entity.stateVectorComponent.getPosition());
    }

    private static void updateHeight(final EntityContext entity,
                                     final float height) {
        if (!Float.isFinite(height) || height <= 0F) {
            return;
        }

        entity.aabbShapeComponent.setDimensions(
                EntityDimensions.fixed(
                                entity.aabbShapeComponent.getDimensions().width(),
                                height)
                        .withEyeHeight(eyeHeightFor(height)),
                entity.stateVectorComponent.getPosition());
    }

    private static void updateScale(final EntityContext entity,
                                    final float scale) {
        if (!Float.isFinite(scale) || scale <= 0F) {
            return;
        }

        entity.aabbShapeComponent.setDimensions(
                entity.aabbShapeComponent.getDimensions().hardScaled(scale),
                entity.stateVectorComponent.getPosition());
    }

    private static float eyeHeightFor(final float height) {
        
        
        
        
        if (Math.abs(height - 0.2F) <= 1.0E-3F) {
            return 0.2F;
        }
        if (Math.abs(height - 0.625F) <= 1.0E-3F) {
            return 0.42F;
        }
        if (Math.abs(height - 0.6F) <= 1.0E-3F) {
            return 0.42F;
        }
        if (Math.abs(height - 1.49F) <= 1.0E-3F
                || Math.abs(height - 1.65F) <= 1.0E-3F) {
            return 1.26F;
        }
        if (Math.abs(height - 1.5F) <= 1.0E-3F) {
            return 1.27F;
        }
        return 1.62F;
    }
}
