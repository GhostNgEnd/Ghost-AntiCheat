package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.prediction.bds.entity.EntityContext;








public final class ApplyJumpModifierSystem {
    private static final float BASE_JUMP_POWER = 0.42F;

    private ApplyJumpModifierSystem() {
    }

    public static float getJumpPower(final EntityContext entity) {
        return BASE_JUMP_POWER * getBlockJumpFactor(entity)
                + getJumpBoostPower(entity);
    }

    public static float getJumpBoostPower(final EntityContext entity) {
        return entity.mobEffectsComponent.has(cn.nukkit.potion.Effect.JUMP_BOOST)
                ? 0.1F * (entity.mobEffectsComponent.view()
                .get(cn.nukkit.potion.Effect.JUMP_BOOST).getAmplifier() + 1.0F)
                : 0.0F;
    }

    public static float getBlockJumpFactor(final EntityContext entity) {
        final float current = entity.blockSource.getBlockState(
                entity.stateVectorComponent.getPosition().toBlockVector3(), 0)
                .getJumpFactor();
        final float below = entity.blockSource.getBlockState(
                entity.stateVectorComponent.getPosition().down(0.1F).toBlockVector3(), 0)
                .getJumpFactor();
        return current == 1.0 ? below : current;
    }
}
