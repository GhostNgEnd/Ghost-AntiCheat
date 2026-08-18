package ac.ghost.anticheat.compensated.cache.container.impl;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.compensated.cache.container.ContainerCache;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.nbt.tag.CompoundTag;
import lombok.Getter;

@Getter
public class TradeContainerCache extends ContainerCache {
    private final CompoundTag offers;

    public TradeContainerCache(CompensatedInventory inventory, CompoundTag offers, byte id, InventoryType type, BlockVector3 blockPosition, long uniqueEntityId) {
        super(inventory, id, type, blockPosition, uniqueEntityId);
        this.offers = offers;
    }
}
