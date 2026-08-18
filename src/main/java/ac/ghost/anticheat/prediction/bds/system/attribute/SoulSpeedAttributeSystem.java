package ac.ghost.anticheat.prediction.bds.system.attribute;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.entity.EntityContext;
import cn.nukkit.item.enchantment.Enchantment;


public final class SoulSpeedAttributeSystem {
    private SoulSpeedAttributeSystem() {
    }

    public static boolean hasSoulSpeed(final EntityContext entity) {
        final GhostPlayer player = entity.externalDataComponent.player();
        final Object boots = player.compensatedInventory.armorContainer
                .get(3).getData();
        if (!(boots instanceof cn.nukkit.item.Item item)) {
            return false;
        }
        final Enchantment soulSpeed = Enchantment.getEnchantment(
                Enchantment.ID_SOUL_SPEED);
        return soulSpeed != null
                && CompensatedInventory.getEnchantments(item)
                .containsKey(soulSpeed);
    }
}
