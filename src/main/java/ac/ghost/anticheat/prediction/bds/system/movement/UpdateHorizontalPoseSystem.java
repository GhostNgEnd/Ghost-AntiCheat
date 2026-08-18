package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.prediction.bds.entity.EntityContext;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import cn.nukkit.entity.Entity;
import cn.nukkit.potion.Effect;


public final class UpdateHorizontalPoseSystem {
    private UpdateHorizontalPoseSystem() {
    }

    public static void tick(final EntityContext entity) {
        final boolean horizontal = entity.actorDataFlagComponent
                .has(ActorDataFlag.GLIDING)
                || entity.actorDataFlagComponent
                .has(Entity.DATA_FLAG_SPIN_ATTACK)
                || entity.actorDataFlagComponent
                .has(ActorDataFlag.SWIMMING)
                || entity.actorDataFlagComponent
                .has(ActorDataFlag.CRAWLING);
        entity.isHorizontalPoseFlagComponent.setPresent(horizontal);
    }

    public static void filterInvalidGlidingState(final EntityContext entity) {
        if (entity.actorDataFlagComponent.has(ActorDataFlag.GLIDING)
                && (entity.onGroundFlagComponent.isPresent()
                || entity.vehicleComponent.value != null
                || entity.mobEffectsComponent.has(Effect.LEVITATION))) {
            entity.actorDataFlagComponent.set(ActorDataFlag.GLIDING, false);
        }
        tick(entity);
    }
}
