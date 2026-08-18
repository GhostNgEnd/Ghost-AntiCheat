package ac.ghost.anticheat.compensated.cache.container.impl;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.compensated.cache.container.ContainerCache;
import ac.ghost.anticheat.data.inventory.ItemCache;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.enchantment.Enchantment;

import java.util.Map;

public class ArmorContainerCache extends ContainerCache {
    public static final byte ARMOR_ID = 120; 

    public ArmorContainerCache(CompensatedInventory inventory) {
        super(inventory, ARMOR_ID, InventoryType.PLAYER, null, -1L);
    }

    @Override
    public void set(int slot, ItemCache cache, boolean offset) {
        if (slot == 2) { 
            Map<Enchantment, Integer> enchantments =
                    CompensatedInventory.getEnchantments(cache.getData());
            Enchantment swiftSneak =
                    Enchantment.getEnchantment(Enchantment.ID_SWIFT_SNEAK);
            Integer level = swiftSneak == null ? null : enchantments.get(swiftSneak);
            inventory.getPlayer().entityContext.swiftSneakEnchantComponent.setMovementScaleModifier(
                    level == null ? 0.0F : 0.15F * level);
        }

        super.set(slot, cache, offset);
    }
}
