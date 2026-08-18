package ac.ghost.anticheat.util.math;

import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.Pair;
import cn.nukkit.network.protocol.types.InputMode;

import java.util.Locale;








public final class HitboxUtil {
    private static final float ROTATION_SAMPLE_STEP = 0.01F;

    
    private static final float MOT_MIN_FACING_PROJECTION = -0.87F;

    private HitboxUtil() {
    }

    public static boolean isInSight(final GhostPlayer player,
                                    final Pair<Vec3, Vec3> pair,
                                    final EntityCache entity) {
        return evaluate(player, pair, entity, null);
    }

    public static HitboxDebug diagnoseHitboxes(final GhostPlayer player,
                                               final Pair<Vec3, Vec3> pair,
                                               final EntityCache entity) {
        final HitboxDebug debug = new HitboxDebug(
                getEyePosition(player, pair, 0F),
                getEyePosition(player, pair, 1F)
        );
        debug.accepted = evaluate(player, pair, entity, debug);
        return debug;
    }

    private static boolean evaluate(final GhostPlayer player,
                                    final Pair<Vec3, Vec3> pair,
                                    final EntityCache entity,
                                    final HitboxDebug debug) {
        if (entity == null || entity.getCurrent() == null) {
            return false;
        }

        final Box currentBox = entity.getCurrent().calculateBoundingBox();
        final Box pastBox = entity.getPast() == null
                ? null
                : entity.getPast().calculateBoundingBox();

        boolean accepted = false;
        for (float delta = 0F; delta < 1F + 1.0E-3F; delta += ROTATION_SAMPLE_STEP) {
            final float sampleDelta = Math.min(delta, 1F);
            final Vec3 eye = getEyePosition(player, pair, sampleDelta);
            final Vec3 feet = new Vec3(eye.x, eye.y - player.entityContext.aabbShapeComponent.getDimensions().eyeHeight(), eye.z);
            final float yaw = getInteractionYaw(player, sampleDelta);
            final Vec3 horizontalDirection = MathUtil.getRotationVector(0F, yaw);

            final FacingHit currentHit = evaluateMotFacing(currentBox, feet, horizontalDirection);
            final FacingHit pastHit = pastBox == null
                    ? FacingHit.UNAVAILABLE
                    : evaluateMotFacing(pastBox, feet, horizontalDirection);

            accepted |= currentHit.accepted || pastHit.accepted;

            if (debug != null) {
                debug.observe(sampleDelta, yaw, eye, feet, currentHit, pastHit);
            } else if (accepted) {
                break;
            }
        }

        return accepted;
    }

    private static FacingHit evaluateMotFacing(final Box box,
                                               final Vec3 feet,
                                               final Vec3 horizontalDirection) {
        final float directionLengthSquared = horizontalDirection.x * horizontalDirection.x
                + horizontalDirection.z * horizontalDirection.z;
        if (directionLengthSquared <= 1.0E-12F) {
            return FacingHit.NO_DIRECTION;
        }

        final float inverseLength = 1F / (float) Math.sqrt(directionLengthSquared);
        final float directionX = horizontalDirection.x * inverseLength;
        final float directionZ = horizontalDirection.z * inverseLength;

        
        final float targetX = (box.minX + box.maxX) * 0.5F;
        final float targetY = box.minY;
        final float targetZ = (box.minZ + box.maxZ) * 0.5F;

        final float deltaX = targetX - feet.x;
        final float deltaZ = targetZ - feet.z;
        final float facingProjection = directionX * deltaX + directionZ * deltaZ;

        final float deltaY = targetY - feet.y;
        final float anchorDistanceSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
        final boolean accepted = facingProjection >= MOT_MIN_FACING_PROJECTION - Box.EPSILON;

        return new FacingHit(accepted, facingProjection, anchorDistanceSquared, true);
    }

    private static float getInteractionYaw(final GhostPlayer player, final float delta) {
        return player.entityContext.playerInputModeComponent.getProtocolValue() == InputMode.TOUCH
                ? player.entityContext.playerActionComponent.interactRotation().getY()
                : MathUtil.lerp(delta, player.entityContext.actorRotationComponent.getPreviousYaw(), player.entityContext.actorRotationComponent.getYaw());
    }

    private static Vec3 getEyePosition(final GhostPlayer player,
                                       final Pair<Vec3, Vec3> pair,
                                       final float delta) {
        return new Vec3(
                MathUtil.lerp(delta, pair.a().x, pair.b().x),
                MathUtil.lerp(delta, pair.a().y, pair.b().y) + player.entityContext.aabbShapeComponent.getDimensions().eyeHeight(),
                MathUtil.lerp(delta, pair.a().z, pair.b().z)
        );
    }

    public static final class HitboxDebug {
        private final Vec3 startEye;
        private final Vec3 endEye;
        private boolean accepted;
        private int samples;
        private int currentAccepted;
        private int pastAccepted;
        private SampleHit bestCurrent;
        private SampleHit bestPast;
        private final StringBuilder selectedSamples = new StringBuilder();

        private HitboxDebug(final Vec3 startEye, final Vec3 endEye) {
            this.startEye = startEye;
            this.endEye = endEye;
        }

        private void observe(final float delta,
                             final float yaw,
                             final Vec3 eye,
                             final Vec3 feet,
                             final FacingHit current,
                             final FacingHit past) {
            this.samples++;
            if (current.accepted) {
                this.currentAccepted++;
                if (this.bestCurrent == null
                        || current.facingProjection > this.bestCurrent.hit.facingProjection) {
                    this.bestCurrent = new SampleHit(delta, yaw, eye, feet, current);
                }
            }
            if (past.accepted) {
                this.pastAccepted++;
                if (this.bestPast == null
                        || past.facingProjection > this.bestPast.hit.facingProjection) {
                    this.bestPast = new SampleHit(delta, yaw, eye, feet, past);
                }
            }

            final int hundredths = Math.round(delta * 100F);
            if (hundredths == 0 || hundredths == 25 || hundredths == 50
                    || hundredths == 75 || hundredths == 100) {
                if (this.selectedSamples.length() > 0) {
                    this.selectedSamples.append(" | ");
                }
                this.selectedSamples.append(String.format(Locale.ROOT,
                        "d=%.2f yaw=%.6f eye=%s feet=%s current=%s past=%s",
                        delta, yaw, vec(eye), vec(feet), hit(current), hit(past)));
            }
        }

        public String summary() {
            return "model=NUKKIT_MOT_INTERACTION_GATE"
                    + " accepted=" + this.accepted
                    + " samples=" + this.samples
                    + " originSource=boarCompensatedEye"
                    + " originSegment=" + vec(this.startEye) + "->" + vec(this.endEye)
                    + " facingThreshold=" + format(MOT_MIN_FACING_PROJECTION)
                    + " current[accepted=" + this.currentAccepted
                    + ",best=" + sample(this.bestCurrent) + "]"
                    + " past[accepted=" + this.pastAccepted
                    + ",best=" + sample(this.bestPast) + "]"
                    + "\nsamples: " + this.selectedSamples;
        }

        private static String sample(final SampleHit sample) {
            if (sample == null) {
                return "n/a";
            }
            return String.format(Locale.ROOT,
                    "delta=%.2f yaw=%.6f eye=%s feet=%s projection=%.7f anchorDistance=%.7f",
                    sample.delta, sample.yaw, vec(sample.eye), vec(sample.feet),
                    sample.hit.facingProjection,
                    Math.sqrt(sample.hit.anchorDistanceSquared));
        }

        private static String hit(final FacingHit hit) {
            if (!hit.available) {
                return "unavailable";
            }
            return String.format(Locale.ROOT,
                    "%s(projection=%.6f,anchorDistance=%.6f)",
                    hit.accepted ? "accepted" : "rejected",
                    hit.facingProjection,
                    Math.sqrt(hit.anchorDistanceSquared));
        }
    }

    private static String vec(final Vec3 vec) {
        if (vec == null) {
            return "null";
        }
        return String.format(Locale.ROOT, "(%.6f,%.6f,%.6f)", vec.x, vec.y, vec.z);
    }

    private static String format(final float value) {
        return String.format(Locale.ROOT, "%.6f", value);
    }

    private static final class FacingHit {
        private static final FacingHit UNAVAILABLE = new FacingHit(
                false, Float.NaN, Float.NaN, false
        );
        private static final FacingHit NO_DIRECTION = new FacingHit(
                false, Float.NaN, Float.NaN, true
        );

        private final boolean accepted;
        private final float facingProjection;
        private final float anchorDistanceSquared;
        private final boolean available;

        private FacingHit(final boolean accepted,
                          final float facingProjection,
                          final float anchorDistanceSquared,
                          final boolean available) {
            this.accepted = accepted;
            this.facingProjection = facingProjection;
            this.anchorDistanceSquared = anchorDistanceSquared;
            this.available = available;
        }
    }

    private static final class SampleHit {
        private final float delta;
        private final float yaw;
        private final Vec3 eye;
        private final Vec3 feet;
        private final FacingHit hit;

        private SampleHit(final float delta,
                          final float yaw,
                          final Vec3 eye,
                          final Vec3 feet,
                          final FacingHit hit) {
            this.delta = delta;
            this.yaw = yaw;
            this.eye = eye;
            this.feet = feet;
            this.hit = hit;
        }
    }
}
