package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.data.vanilla.StatusEffect;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.potion.Effect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class MobEffectsComponent {
    private final Map<Integer, StatusEffect> effects = new ConcurrentHashMap<>();

    public boolean has(final int id) {
        return this.effects.containsKey(id);
    }

    public StatusEffect get(final int id) {
        return this.effects.get(id);
    }

    public Map<Integer, StatusEffect> view() {
        return this.effects;
    }

    public float effectiveGravity(final Vec3 velocity) {
        final float defaultGravity = 0.08F;
        return velocity.y < 0.0F && has(Effect.SLOW_FALLING)
                ? Math.min(defaultGravity, 0.01F)
                : defaultGravity;
    }

    








    public void addOrUpdate(final int effectId,
                            final int amplifier,
                            final int duration) {
        final Effect effect = Effect.getEffect(effectId);
        if (effect == null) {
            return;
        }
        this.effects.put(effectId, new StatusEffect(effect, amplifier, duration));
    }

    public void remove(final int effectId) {
        this.effects.remove(effectId);
    }
}
