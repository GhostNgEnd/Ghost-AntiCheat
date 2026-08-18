package ac.ghost.anticheat.packets.player;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.ack.InventoryAcknowledgmentHandler;
import ac.ghost.anticheat.ack.types.InventoryContentAck;
import ac.ghost.anticheat.ack.types.InventorySlotAck;
import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.compensated.cache.container.ContainerCache;
import ac.ghost.anticheat.compensated.cache.container.impl.TradeContainerCache;
import ac.ghost.anticheat.data.inventory.PotionMixData;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.inventory.ItemStackNetManagerServer;
import ac.ghost.anticheat.prediction.bds.system.inventory.ServerPlayerInventoryTransactionSystem;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.inventory.BrewingRecipe;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.inventory.Recipe;
import cn.nukkit.inventory.RecipeType;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ContainerClosePacket;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.network.protocol.CreativeContentPacket;
import cn.nukkit.network.protocol.InventoryContentPacket;
import cn.nukkit.network.protocol.InventorySlotPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.ItemStackRequestPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.network.protocol.PlayerHotbarPacket;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.UpdateTradePacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemData;
import cn.nukkit.network.protocol.v113.ContainerSetContentPacket_v113;
import cn.nukkit.network.protocol.v113.ContainerSetSlotPacket_v113;
import cn.nukkit.network.protocol.v113.UseItemPacket_v113;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PlayerInventoryPackets implements Listener {
    






    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void finishRawAuthInput(final DataPacketReceiveEvent event) {
        if (!(event.getPacket() instanceof PlayerAuthInputPacket)) {
            return;
        }
        final GhostPlayer player = Ghost.getInstance().getPlayerManager()
                .get(event.getPlayer());
        if (!NukkitItemUseStateSystem.shouldRollbackNoSlowConsume(player)) {
            return;
        }
        NukkitItemUseStateSystem.keepUsingAfterNoSlowConsume(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(event.getPlayer());
        if (player == null) {
            return;
        }
        final CompensatedInventory inventory = player.compensatedInventory;

        if (event.getPacket() instanceof UseItemPacket_v113 packet
                && BedrockProtocolCapabilities.usesLegacyUseItem(
                        player.getSession().protocol)
                && packet.face == -1) {
            
            
            
            NukkitItemUseStateSystem.beginFromInventoryTransaction(
                    player, packet.item);
        }

        if (event.getPacket() instanceof PlayerActionPacket packet
                && BedrockProtocolCapabilities.usesLegacyPlayerActionIds(
                        player.getSession().protocol)
                && packet.action == PlayerActionPacket.ACTION_START_SLEEPING) {
            
            
            NukkitItemUseStateSystem.release(player);
        }

        if (event.getPacket() instanceof InventoryTransactionPacket packet) {
            
            
            
            if (packet.transactionType == InventoryTransactionPacket.TYPE_RELEASE_ITEM
                    && packet.transactionData instanceof ReleaseItemData data
                    && data.actionType == InventoryTransactionPacket.RELEASE_ITEM_ACTION_CONSUME
                    && NukkitItemUseStateSystem.shouldRollbackNoSlowConsume(player)) {
                event.setCancelled(true);
                NukkitItemUseStateSystem.keepUsingAfterNoSlowConsume(player);
                final var liveInventory = player.getSession().getInventory();
                liveInventory.sendSlot(liveInventory.getHeldItemIndex(), player.getSession());
                return;
            }

            
            
            
            
            
            
            
            new ServerPlayerInventoryTransactionSystem(player.entityContext).handle(packet);
        }

        if (event.getPacket() instanceof PlayerAuthInputPacket packet) {
            if (packet.getItemUseTransaction() != null) {
                new ServerPlayerInventoryTransactionSystem(player.entityContext)
                        .handle(packet.getItemUseTransaction());
            }
            if (packet.getItemStackRequest() != null) {
                final ItemStackRequestPacket requestPacket =
                        new ItemStackRequestPacket();
                requestPacket.getRequests().add(packet.getItemStackRequest());
                new ItemStackNetManagerServer(player.entityContext)
                        .handle(requestPacket);
            }
        }

        if (event.getPacket() instanceof ItemStackRequestPacket packet) {
            
            
            
            
            
            new ItemStackNetManagerServer(player.entityContext).handle(packet);
        }

        if (event.getPacket() instanceof ContainerClosePacket packet) {
            if (inventory.openContainer == null) {
                return;
            }
            if (packet.windowId != inventory.openContainer.getId() && packet.windowId != -1) {
                return;
            }
            inventory.openContainer = null;
        }

        if (event.getPacket() instanceof MobEquipmentPacket packet) {
            final int newSlot = packet.hotbarSlot;
            if (packet.eid != player.runtimeEntityId) {
                return;
            }
            if (newSlot < 0 || newSlot > 8 || packet.windowId != ContainerIds.INVENTORY
                    || inventory.heldItemSlot == newSlot) {
                return;
            }
            inventory.heldItemSlot = newSlot;
            NukkitItemUseStateSystem.onHeldItemChanged(player,
                    inventory.inventoryContainer.getHeldItemData());
        }
    }

    @EventHandler
    public void onPacketSend(final DataPacketSendEvent event) {
        final Player nukkitPlayer = event.getPlayer();
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
        if (player == null) {
            return;
        }
        final CompensatedInventory inventory = player.compensatedInventory;

        if (event.getPacket() instanceof CreativeContentPacket packet) {
            player.latencyAdapter.latencyUtil().queue(() -> {
                inventory.getCreativeData().clear();
                putCreativeEntries(inventory, packet);
            });
        }

        if (event.getPacket() instanceof CraftingDataPacket packet) {
            player.latencyAdapter.latencyUtil().queue(() -> {
                inventory.getCraftingData().clear();
                putCraftingEntries(inventory, packet);
                inventory.setPotionMixData(readBrewingEntries(player, packet));
            });
        }

        if (event.getPacket() instanceof ContainerOpenPacket packet) {
            final InventoryType type = InventoryType.from(packet.type);
            if (type == null) {
                return;
            }
            player.latencyAdapter.sendLatencyStack(() -> {
                final ContainerCache container = inventory.getContainer((byte) packet.windowId);
                inventory.openContainer = Objects.requireNonNullElseGet(container, () -> new ContainerCache(
                        inventory, (byte) packet.windowId, type,
                        new BlockVector3(packet.x, packet.y, packet.z), packet.entityId
                ));
            });
        }

        if (event.getPacket() instanceof UpdateTradePacket packet) {
            final InventoryType type = InventoryType.from(packet.windowType);
            if (packet.playerUniqueEntityId != player.runtimeEntityId || type != InventoryType.TRADING) {
                return;
            }
            final CompoundTag offers = decodeNetworkTag(packet.offers);
            player.latencyAdapter.sendLatencyStack(() -> {
                try {
                    inventory.openContainer = new TradeContainerCache(
                            inventory, offers, packet.windowId, type,
                            new BlockVector3(0, 0, 0), packet.traderUniqueEntityId
                    );
                } catch (Exception ignored) {
                }
            });
        }

        if (event.getPacket() instanceof InventorySlotPacket packet) {
            
            
            
            final InventorySlotAck acknowledgment = InventorySlotAck.capture(
                    packet.inventoryId, packet.slot, packet.item, packet.storageItem);
            player.latencyAdapter.sendLatencyStack(() ->
                    InventoryAcknowledgmentHandler.handle(inventory, acknowledgment));
        }

        if (event.getPacket() instanceof InventoryContentPacket packet) {
            
            
            final InventoryContentAck acknowledgment = InventoryContentAck.capture(
                    packet.inventoryId, packet.slots, packet.storageItem);
            if (packet.inventoryId == ContainerIds.CREATIVE) {
                player.latencyAdapter.sendLatencyStack(() -> {
                    inventory.getCreativeData().clear();
                    putCreativeEntries(inventory, acknowledgment);
                });
            } else {
                player.latencyAdapter.sendLatencyStack(() ->
                        InventoryAcknowledgmentHandler.handle(inventory, acknowledgment));
            }
        }

        if (event.getPacket() instanceof ContainerSetSlotPacket_v113 packet) {
            final InventorySlotAck acknowledgment = InventorySlotAck.capture(
                    packet.windowid, packet.slot, packet.item, null);
            player.latencyAdapter.sendLatencyStackAfterOutbound(() ->
                    InventoryAcknowledgmentHandler.handle(inventory, acknowledgment));
        }

        if (event.getPacket() instanceof ContainerSetContentPacket_v113 packet) {
            final InventoryContentAck acknowledgment = InventoryContentAck.capture(
                    (int) packet.windowid, packet.slots, null);
            if (packet.windowid == ContainerSetContentPacket_v113.SPECIAL_CREATIVE) {
                player.latencyAdapter.sendLatencyStackAfterOutbound(() -> {
                    inventory.getCreativeData().clear();
                    putCreativeEntries(inventory, acknowledgment);
                });
            } else {
                player.latencyAdapter.sendLatencyStackAfterOutbound(() ->
                        InventoryAcknowledgmentHandler.handle(inventory, acknowledgment));
            }
        }

        if (event.getPacket() instanceof PlayerHotbarPacket packet) {
            if (packet.windowId != inventory.inventoryContainer.getId() || !packet.selectHotbarSlot) {
                return;
            }
            final int slot = packet.selectedHotbarSlot;
            if (slot >= 0 && slot < 9) {
                player.latencyAdapter.sendLatencyStack(() -> inventory.heldItemSlot = slot);
            }
        }
    }


    private static void putCreativeEntries(final CompensatedInventory inventory, final CreativeContentPacket packet) {
        if (packet.creativeItems != null) {
            try {
                final List<CreativeItemData> data = packet.creativeItems.getCreativeItemDatas();
                for (final CreativeItemData entry : data) {
                    inventory.getCreativeData().put(entry.getNetId(), entry.getItem());
                }
                if (!inventory.getCreativeData().isEmpty()) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        if (packet.entries != null) {
            for (int i = 0; i < packet.entries.length; i++) {
                inventory.getCreativeData().put(i + 1, packet.entries[i]);
            }
        }
    }

    private static void putCreativeEntries(final CompensatedInventory inventory,
                                           final InventoryContentAck acknowledgment) {
        for (int i = 0; i < acknowledgment.contents().size(); i++) {
            inventory.getCreativeData().put(i + 1,
                    acknowledgment.contents().get(i).materialize());
        }
    }

    private static void putCraftingEntries(final CompensatedInventory inventory, final CraftingDataPacket packet) {
        putCraftingCollection(inventory, readField(packet, "entries"));
        
        
        putCraftingCollection(inventory, readField(packet, "stonecutterEntries"));
    }

    private static void putCraftingCollection(final CompensatedInventory inventory, final Object raw) {
        if (!(raw instanceof Collection<?> collection)) {
            return;
        }
        for (final Object value : collection) {
            if (!(value instanceof Recipe recipe)) {
                continue;
            }
            final RecipeType type = recipe.getType();
            if (type != RecipeType.MULTI && type != RecipeType.SHAPED && type != RecipeType.SHAPELESS
                    && type != RecipeType.SMITHING_TRANSFORM && type != RecipeType.SMITHING_TRIM) {
                continue;
            }
            final int networkId = recipeNetworkId(recipe);
            if (networkId != Integer.MIN_VALUE) {
                inventory.getCraftingData().put(networkId, recipe);
            }
        }
    }

    private static List<PotionMixData> readBrewingEntries(final GhostPlayer player,
                                                           final CraftingDataPacket packet) {
        final List<PotionMixData> result = new ArrayList<>();
        final Object raw = readField(packet, "brewingEntries");
        if (raw instanceof Collection<?> collection) {
            for (final Object value : collection) {
                if (!(value instanceof BrewingRecipe recipe)) {
                    continue;
                }
                final Item input = recipe.getInput();
                final Item reagent = recipe.getIngredient();
                final Item output = recipe.getResult();
                result.add(new PotionMixData(
                        input.getNetworkId(player.getSession().getGameVersion()), input.getDamage(),
                        reagent.getNetworkId(player.getSession().getGameVersion()), reagent.getDamage(),
                        output.getNetworkId(player.getSession().getGameVersion()), output.getDamage()
                ));
            }
        }
        return result;
    }

    private static Object readField(final Object instance, final String name) {
        try {
            final Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int recipeNetworkId(final Recipe recipe) {
        try {
            final Object value = recipe.getClass().getMethod("getNetworkId").invoke(recipe);
            return value instanceof Number number ? number.intValue() : Integer.MIN_VALUE;
        } catch (Exception ignored) {
            return Integer.MIN_VALUE;
        }
    }

    private static CompoundTag decodeNetworkTag(final byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            return NBTIO.read(data, ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException ignored) {
            return null;
        }
    }
}
