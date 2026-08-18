package ac.ghost.anticheat.compensated;

import ac.ghost.anticheat.compensated.cache.container.ContainerCache;
import ac.ghost.anticheat.compensated.cache.container.impl.ArmorContainerCache;
import ac.ghost.anticheat.compensated.cache.container.impl.PlayerContainerCache;
import ac.ghost.anticheat.data.inventory.ItemCache;
import ac.ghost.anticheat.data.inventory.PotionMixData;
import ac.ghost.anticheat.player.GhostPlayer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import cn.nukkit.inventory.Recipe;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CompensatedInventory {
    @Getter
    private final GhostPlayer player;

    @Getter
    @Setter
    private Map<Integer, Recipe> craftingData = new HashMap<>();
    @Getter
    @Setter
    private Map<Integer, Item> creativeData = new HashMap<>();
    @Getter
    @Setter
    private List<PotionMixData> potionMixData = new ObjectArrayList<>();

    public int heldItemSlot;

    public final PlayerContainerCache inventoryContainer = new PlayerContainerCache(this);
    public final ContainerCache offhandContainer = new ContainerCache(this, ContainerCache.OFFHAND, cn.nukkit.inventory.InventoryType.PLAYER, null, -1L);
    public final ContainerCache armorContainer = new ArmorContainerCache(this);
    public final ContainerCache hudContainer = new ContainerCache(this, ContainerCache.UI, cn.nukkit.inventory.InventoryType.PLAYER, null, -1L);

    public ContainerCache openContainer = null;

    @Getter
    private final Map<Integer, ItemCache> bundleCache = new HashMap<>();


    public ContainerCache getContainer(byte id) {
        if (id == inventoryContainer.getId()) {
            return inventoryContainer;
        } else if (id == offhandContainer.getId()) {
            return offhandContainer;
        } else if (id == armorContainer.getId()) {
            return armorContainer;
        } else if (id == hudContainer.getId()) {
            return hudContainer;
        } else if (openContainer != null && id == openContainer.getId()) {
            return openContainer;
        }

        return null;
    }

    @NonNull
    public static Map<Enchantment, Integer> getEnchantments(final Item data) {
        if (data == null) {
            return Map.of();
        }

        CompoundTag tag = data.getNamedTag();
        if (tag == null || !tag.contains("ench")) {
            return Map.of();
        }

        final Map<Enchantment, Integer> enchantmentMap = new EnchantmentIdMap();
        ListTag<CompoundTag> enchantments = tag.getList("ench", CompoundTag.class);

        for (CompoundTag nbtMap : enchantments.getAll()) {
            if (!nbtMap.contains("id") || !nbtMap.contains("lvl")) {
                continue;
            }

            int id = nbtMap.getShort("id");
            int lvl = nbtMap.getShort("lvl");
            Enchantment enchantment = Enchantment.getEnchantment(id);
            if (enchantment != null) {
                enchantmentMap.put(enchantment, lvl);
            }
        }

        return enchantmentMap;
    }

    public static int getEnchantmentLevel(final Item item, final int enchantmentId) {
        if (item == null || item.isNull()) {
            return 0;
        }

        
        
        
        try {
            final int nativeLevel = item.getEnchantmentLevel(enchantmentId);
            if (nativeLevel > 0) {
                return nativeLevel;
            }
        } catch (RuntimeException ignored) {
            
        }

        final Enchantment enchantment = Enchantment.getEnchantment(enchantmentId);
        if (enchantment == null) {
            return 0;
        }

        return getEnchantments(item).getOrDefault(enchantment, 0);
    }

    






    private static final class EnchantmentIdMap extends HashMap<Enchantment, Integer> {
        @Override
        public boolean containsKey(Object key) {
            if (key instanceof Enchantment enchantment) {
                return this.containsEnchantmentId(enchantment.getId());
            }
            return super.containsKey(key);
        }

        @Override
        public Integer get(Object key) {
            if (key instanceof Enchantment enchantment) {
                return this.getByEnchantmentId(enchantment.getId());
            }
            return super.get(key);
        }

        private boolean containsEnchantmentId(int id) {
            for (Enchantment enchantment : this.keySet()) {
                if (enchantment != null && enchantment.getId() == id) {
                    return true;
                }
            }
            return false;
        }

        private Integer getByEnchantmentId(int id) {
            for (Map.Entry<Enchantment, Integer> entry : this.entrySet()) {
                Enchantment enchantment = entry.getKey();
                if (enchantment != null && enchantment.getId() == id) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

}
