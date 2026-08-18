package ac.ghost.anticheat.data.vanilla;

import cn.nukkit.potion.Effect;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StatusEffect {
    private final Effect effect;
    private final int amplifier;
    private int duration;

    public void tick() {
        if (duration > 0) {
            duration--;
        }
    }
}
