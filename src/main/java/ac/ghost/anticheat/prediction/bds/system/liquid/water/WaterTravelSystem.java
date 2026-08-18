package ac.ghost.anticheat.prediction.bds.system.liquid.water;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;

import java.util.Map;


public final class WaterTravelSystem {
    private static final int MAX_DEPTH_STRIDER_LEVEL = 3;

    private WaterTravelSystem() {
    }

    public static Result tick(final GhostPlayer player, final Vec3 velocity) {
        final Item boots = player.compensatedInventory.armorContainer.get(3).getData();
        final Map<Enchantment, Integer> enchantments = CompensatedInventory.getEnchantments(boots);
        final Enchantment depthType = Enchantment.getEnchantment(Enchantment.ID_WATER_WALKER);
        final int level = depthType == null ? 0 : Math.max(0, enchantments.getOrDefault(depthType, 0));

        float ratio = Math.min(level, MAX_DEPTH_STRIDER_LEVEL)
                / (float) MAX_DEPTH_STRIDER_LEVEL;
        final float underwaterSpeed = player.entityContext.attributesComponent.underwaterMovementSpeed();
        final float strength;
        if (player.entityContext.swimSpeedMultiplierComponent.getValue() > 1.0F) {
            strength = underwaterSpeed * player.entityContext.swimSpeedMultiplierComponent.getValue()
                    * (0.7F + 0.3F * ratio);
        } else {
            if (!player.entityContext.onGroundFlagComponent.isPresent()) {
                ratio *= 0.5F;
            }
            strength = underwaterSpeed
                    + (player.entityContext.attributesComponent.movementSpeed() - underwaterSpeed) * ratio;
        }

        return new Result(WaterMoveSystem.tick(player, velocity, strength), ratio);
    }

    public record Result(Vec3 velocity, float depthStriderRatio) {
    }
}
