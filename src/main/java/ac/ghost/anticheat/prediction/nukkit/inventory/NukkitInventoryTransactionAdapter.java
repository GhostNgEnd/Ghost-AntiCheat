package ac.ghost.anticheat.prediction.nukkit.inventory;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.data.block.NetworkBlockState;
import ac.ghost.anticheat.data.block.NetworkBlockStateRegistry;
import ac.ghost.anticheat.data.inventory.ItemCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.nukkit.NukkitMovementEffectAdapter;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;
import ac.ghost.anticheat.util.ItemUtil;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.block.BlockUtil;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAnvil;
import cn.nukkit.block.BlockBarrel;
import cn.nukkit.block.BlockBeacon;
import cn.nukkit.block.BlockBed;
import cn.nukkit.block.BlockBell;
import cn.nukkit.block.BlockBrewingStand;
import cn.nukkit.block.BlockButton;
import cn.nukkit.block.BlockCandle;
import cn.nukkit.block.BlockCauldron;
import cn.nukkit.block.BlockChest;
import cn.nukkit.block.BlockLectern;
import cn.nukkit.block.BlockLever;
import cn.nukkit.block.BlockNoteblock;
import cn.nukkit.block.BlockRespawnAnchor;
import cn.nukkit.block.BlockSignPost;
import cn.nukkit.block.BlockTNT;
import cn.nukkit.block.BlockWallSign;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockItemFrame;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.block.BlockFenceGate;
import cn.nukkit.block.BlockTrapdoor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBucket;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemElytra;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.types.GameType;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.inventory.transaction.data.UseItemData;








public final class NukkitInventoryTransactionAdapter {
    private static final int CLIENT_PREDICTION_FAILURE = 0;

    private final GhostPlayer player;

    public NukkitInventoryTransactionAdapter(final GhostPlayer player) {
        this.player = player;
    }


    public boolean handleNormal(final InventoryTransactionPacket packet) {
        final NetworkInventoryAction[] actions = packet.actions;
        if (actions == null || actions.length != 2) {
            return false;
        }

        
        
        
        
        NetworkInventoryAction world = null;
        NetworkInventoryAction container = null;
        for (final NetworkInventoryAction action : actions) {
            if (action == null) {
                return false;
            }

            if (action.sourceType == NetworkInventoryAction.SOURCE_WORLD) {
                if (world != null) {
                    return false;
                }
                world = action;
            } else if (action.sourceType == NetworkInventoryAction.SOURCE_CONTAINER) {
                if (container != null) {
                    return false;
                }
                container = action;
            } else {
                return false;
            }
        }

        if (world == null || container == null) {
            return false;
        }

        
        
        
        if (world.inventorySlot != InventoryTransactionPacket.ACTION_MAGIC_SLOT_DROP_ITEM) {
            return false;
        }

        if (container.windowId != this.player.compensatedInventory.inventoryContainer.getId()) {
            return false;
        }

        return handleHotbarDrop(world, container);
    }

    private boolean handleHotbarDrop(final NetworkInventoryAction world, final NetworkInventoryAction containerAction) {
        final int slot = containerAction.inventorySlot;
        if (slot < 0 || slot > 8) {
            return false;
        }

        final CompensatedInventory inventory = this.player.compensatedInventory;
        final Item slotData = inventory.inventoryContainer.getItemFromSlot(slot).getData();
        final Item claimedDrop = world.newItem;
        if (claimedDrop == null || claimedDrop.isNull()) {
            return false;
        }

        final int dropCount = claimedDrop.getCount();
        if (dropCount < 1 || dropCount > slotData.getCount()
                || !validate(this.player, slotData, claimedDrop)) {
            return false;
        }

        
        
        
        if (dropCount == slotData.getCount()) {
            inventory.inventoryContainer.set(slot, ItemCache.AIR);
        } else {
            final Item remaining = slotData.clone();
            remaining.setCount(Math.max(0, slotData.getCount() - dropCount));
            inventory.inventoryContainer.set(slot, remaining);
        }

        
        
        
        
        if (slot == inventory.heldItemSlot) {
            NukkitItemUseStateSystem.onHeldItemChanged(this.player,
                    inventory.inventoryContainer.getHeldItemData());
        }
        return true;
    }








    public boolean handleClickAir(final UseItemData data) {
        final CompensatedInventory inventory = this.player.compensatedInventory;
        final Item held = inventory.inventoryContainer.getHeldItemData();

        if (data.itemInHand == null || !validate(this.player, held, data.itemInHand)) {
            return true;
        }

        
        
        
        
        
        
        if (held instanceof ItemFirework firework) {
            if (this.player.getSession().isGliding()
                    && this.player.getSession().getInventory().getChestplateFast() instanceof ItemElytra) {
                NukkitMovementEffectAdapter.beginGlideBoost(this.player, firework);
            }
            return true;
        }

        
        
        
        if (this.player.entityContext.itemInUseComponent.isPresent()) {
            return true;
        }

        
        
        
        
        if (NukkitItemUseStateSystem.resumeLatchedUse(this.player, held)) {
            return true;
        }

        NukkitItemUseStateSystem.beginFromInventoryTransaction(this.player, held);
        return true;
    }

    public boolean handleClickBlock(final UseItemData data) {
        if (data.blockPos == null) {
            return false;
        }

        final CompensatedInventory inventory = this.player.compensatedInventory;
        final Item held = inventory.inventoryContainer.getHeldItemData();
        if (data.itemInHand == null || !validate(this.player, held, data.itemInHand)) {
            return true;
        }

        final BlockVector3 position = data.blockPos;
        if (data.clientInteractPrediction == CLIENT_PREDICTION_FAILURE) {
            return true;
        }
        if (data.face == null) {
            return false;
        }
        final int face = faceIndex(data.face);
        if (face < 0 || face > 5) {
            return false;
        }

        final BlockLegacy clickedState = this.player.entityContext.blockSource.getBlockState(position, 0);
        final Block clickedBlock = clickedState.getBlock();
        if (isItemFrame(clickedBlock, data.blockRuntimeId)) {
            return true;
        }
        final BlockVector3 newBlockPos = BlockUtil.getBlockPosition(position, face);
        final boolean heldItemExists = held != null && !held.isNull();
        final boolean doingSecondaryAction = heldItemExists
                && this.player.entityContext.playerActionComponent.actions().contains(AuthInputAction.SNEAKING);

        if (!doingSecondaryAction) {
            if (handleUseItemOn(position, clickedBlock, held, data.face)) {
                return true;
            }
            if (clickedBlock instanceof BlockBell bell && data.clickPos != null
                    && isProperHit(bell, data.face, data.clickPos.getY())) {
                return true;
            }
            if (canUseWithoutItem(clickedBlock)) {
                predictActivatedBlock(position, clickedBlock);
                return true;
            }
        }

        if (isScaffoldingPlacement(held, clickedBlock, newBlockPos)) {
            return true;
        }

        if (clickedState.isAir()) {
            BlockUtil.resendBlocksAroundArea(this.player.getSession(), position, face);
            return false;
        }

        if (handleBucketUse(inventory, held, position, newBlockPos, clickedBlock)) {
            return true;
        }

        if (held.canBePlaced() && held.getBlockId() != BlockID.AIR) {
            handleBlockPlacement(inventory, held, newBlockPos);
        }
        return true;
    }


    public void handleLegacyArmorEquip(final InventoryTransactionPacket packet, final UseItemData data) {
        final CompensatedInventory inventory = this.player.compensatedInventory;

        if (packet.actions == null || packet.actions.length != 1
                || packet.legacySlots == null || packet.legacySlots.isEmpty()) {
            return;
        }

        if (data.hotbarSlot != inventory.heldItemSlot) {
            return;
        }

        final InventoryTransactionPacket.LegacySetItemSlotData slotData = packet.legacySlots.get(0);
        if (slotData == null || slotData.slots == null || slotData.slots.length == 0) {
            return;
        }

        final int actualSlot = Byte.toUnsignedInt(slotData.slots[0]);
        if (actualSlot < 0 || actualSlot >= inventory.armorContainer.getContainerSize()) {
            return;
        }

        
        
        
        if (slotData.containerId == 6) {
            final ItemCache oldHotbar = inventory.inventoryContainer.getHeldItemCache();
            inventory.inventoryContainer.set(data.hotbarSlot, inventory.armorContainer.get(actualSlot));
            inventory.armorContainer.set(actualSlot, oldHotbar);
        }
    }

    private boolean tooFar(final BlockVector3 position) {
        final Vec3 blockPos = new Vec3(position);
        return this.player.entityContext.stateVectorComponent.getPosition().squaredDistanceTo(blockPos) > 12F * 12F
                && position.getX() + position.getY() + position.getZ() != 0;
    }

    private static int faceIndex(final BlockFace face) {
        return switch (face) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
            default -> -1;
        };
    }


    private boolean isScaffoldingPlacement(final Item held, final Block clickedBlock, final BlockVector3 target) {
        if (held == null || held.getBlockId() != BlockID.SCAFFOLDING) {
            return false;
        }

        if (clickedBlock != null && clickedBlock.getId() == BlockID.SCAFFOLDING) {
            return true;
        }

        if (target == null) {
            return false;
        }

        final Block targetBlock = this.player.entityContext.blockSource.getBlockState(target, 0).getBlock();
        return targetBlock != null && targetBlock.getId() == BlockID.SCAFFOLDING;
    }



    private boolean handleBucketUse(final CompensatedInventory inventory, final Item held,
                                    final BlockVector3 clicked, final BlockVector3 target,
                                    final Block clickedBlock) {
        if (held.getId() != ItemID.BUCKET) {
            return false;
        }

        final int bucketDamage = held.getDamage();
        if (bucketDamage == ItemBucket.WATER_BUCKET) {
            this.player.entityContext.blockSource.updateLegacyBlock(target, 0, fullId(BlockID.WATER, 0));
            setHeldBucket(inventory, ItemBucket.EMPTY_BUCKET);
            return true;
        } else if (bucketDamage == ItemBucket.LAVA_BUCKET) {
            this.player.entityContext.blockSource.updateLegacyBlock(target, 0, fullId(BlockID.LAVA, 0));
            setHeldBucket(inventory, ItemBucket.EMPTY_BUCKET);
            return true;
        } else if (bucketDamage == ItemBucket.POWDER_SNOW_BUCKET) {
            this.player.entityContext.blockSource.updateLegacyBlock(target, 0, fullId(BlockID.POWDER_SNOW, 0));
            setHeldBucket(inventory, ItemBucket.EMPTY_BUCKET);
            return true;
        } else if (bucketDamage != ItemBucket.EMPTY_BUCKET) {
            return false;
        }

        int layer = 0;
        int resultBucket = -1;
        if (clickedBlock.isWater()) {
            resultBucket = ItemBucket.WATER_BUCKET;
        } else if (this.player.entityContext.blockSource.getBlockState(clicked, 1).getBlock().isWater()) {
            layer = 1;
            resultBucket = ItemBucket.WATER_BUCKET;
        } else if (Block.isLava(clickedBlock.getId())) {
            resultBucket = ItemBucket.LAVA_BUCKET;
        } else if (clickedBlock.getId() == BlockID.POWDER_SNOW) {
            resultBucket = ItemBucket.POWDER_SNOW_BUCKET;
        }

        if (resultBucket == -1) {
            return true;
        }
        
        this.player.entityContext.blockSource.updateLegacyBlock(target, layer, 0);
        setHeldBucket(inventory, resultBucket);
        return true;
    }

    private void handleBlockPlacement(final CompensatedInventory inventory, final Item held,
                                      final BlockVector3 target) {
        final Block mapped = held.getBlockUnsafe();
        if (mapped != null && mapped.getId() != BlockID.AIR) {
            final Block defaultState = Block.get(mapped.getId(), 0);
            this.player.entityContext.blockSource.updateLegacyBlock(target, 0, defaultState.getFullId());
        }

        if (this.player.entityContext.actorGameTypeComponent.value != GameType.CREATIVE) {
            final ItemCache heldCache = inventory.inventoryContainer.getHeldItemCache();
            heldCache.count(heldCache.count() - 1);
            if (heldCache.count() <= 0) {
                inventory.inventoryContainer.set(inventory.heldItemSlot, ItemCache.AIR);
            }
        }
    }


    private boolean handleUseItemOn(final BlockVector3 position, final Block block,
                                    final Item held, final BlockFace face) {
        if (block == null || held == null) {
            return false;
        }

        final int heldId = held.getId();
        final int blockId = block.getId();
        if (block instanceof BlockCauldron
                && (isWaterBucket(held) || isLavaBucket(held) || isPowderSnowBucket(held))) {
            return true;
        }
        if (blockId == BlockID.CAKE_BLOCK
                && (ItemUtil.hasTag(this.player, held, "minecraft:candles") || block.getDamage() == 0)) {
            return true;
        }
        if (block instanceof BlockCandle candle && held.isNull() && candle.isLit()) {
            return true;
        }
        if (block instanceof BlockLectern && held.isNull() && !lecternHasBook(position)) {
            return true;
        }
        if (block instanceof BlockNoteblock && face == BlockFace.UP
                && ItemUtil.isNoteblockTopInstrument(this.player, held)) {
            return true;
        }
        if (blockId == BlockID.PUMPKIN || blockId == BlockID.REDSTONE_ORE) {
            return true;
        }
        if (block instanceof BlockRespawnAnchor anchor
                && held.getBlockId() == 89 && anchor.getCharge() < 4) {
            return true;
        }
        if (isSign(block)) {
            return true;
        }
        if (blockId == BlockID.SWEET_BERRY_BUSH && block.getDamage() != 3 && isBoneMeal(held)) {
            return true;
        }
        if (block instanceof BlockTNT
                && (heldId == ItemID.FLINT_AND_STEEL || heldId == ItemID.FLINT_STEEL
                || heldId == ItemID.FIRE_CHARGE)) {
            return true;
        }
        return blockId == BlockID.VAULT && (held.isNull() || !isVaultActive(position, block));
    }

    private boolean lecternHasBook(final BlockVector3 position) {
        final CompoundTag tag = this.player.entityContext.blockSource.getBlockEntityTag(
                position.getX(), position.getY(), position.getZ());
        return tag != null && (tag.getBoolean("hasBook") || tag.containsCompound("book"));
    }

    private boolean isVaultActive(final BlockVector3 position, final Block block) {
        try {
            final Object value = block.getClass().getMethod("getVaultState").invoke(block);
            if (value != null) {
                return "active".equalsIgnoreCase(value.toString());
            }
        } catch (Exception ignored) {
        }
        final CompoundTag tag = this.player.entityContext.blockSource.getBlockEntityTag(
                position.getX(), position.getY(), position.getZ());
        if (tag != null) {
            final String state = tag.getString("vault_state");
            if (!state.isEmpty()) {
                return "active".equalsIgnoreCase(state);
            }
            final String fallback = tag.getString("state");
            if (!fallback.isEmpty()) {
                return "active".equalsIgnoreCase(fallback);
            }
        }
        return false;
    }

    private void predictActivatedBlock(final BlockVector3 position, final Block block) {
        if (position == null || block == null) {
            return;
        }

        
        
        
        
        
        
        if (block instanceof BlockTrapdoor) {
            this.player.entityContext.blockSource.updateLegacyBlock(
                    position, 0, fullId(block.getId(), block.getDamage() ^ BlockTrapdoor.TRAPDOOR_OPEN_BIT)
            );
            return;
        }

        if (block instanceof BlockDoor door) {
            predictDoorToggle(position, door);
            return;
        }

        if (block instanceof BlockFenceGate) {
            this.player.entityContext.blockSource.updateLegacyBlock(
                    position, 0, fullId(block.getId(), block.getDamage() ^ BlockFenceGate.OPEN_BIT)
            );
        }
    }

    private void predictDoorToggle(final BlockVector3 position, final BlockDoor door) {
        final boolean top = (door.getDamage() & BlockDoor.DOOR_TOP_BIT) != 0;
        final BlockVector3 lowerPos = top
                ? new BlockVector3(position.getX(), position.getY() - 1, position.getZ())
                : position;

        final Block lower = this.player.entityContext.blockSource.getBlockState(lowerPos, 0).getBlock();
        if (!(lower instanceof BlockDoor lowerDoor) || lowerDoor.getId() != door.getId()) {
            return;
        }

        this.player.entityContext.blockSource.updateLegacyBlock(
                lowerPos, 0, fullId(lowerDoor.getId(), lowerDoor.getDamage() ^ BlockDoor.DOOR_OPEN_BIT)
        );
    }

    private static boolean canUseWithoutItem(final Block block) {
        if (block == null) {
            return false;
        }
        final String className = block.getClass().getSimpleName();
        final boolean furnace = className.equals("BlockFurnace") || className.equals("BlockFurnaceBurning")
                || className.equals("BlockBlastFurnace") || className.equals("BlockBlastFurnaceBurning");
        return furnace
                || block instanceof BlockAnvil
                || block instanceof BlockBarrel
                || block instanceof BlockBeacon
                || block instanceof BlockBed
                || block instanceof BlockChest
                || block instanceof BlockBrewingStand
                || block instanceof BlockButton
                || block instanceof BlockLever
                || block instanceof BlockDoor
                || block instanceof BlockTrapdoor
                || block instanceof BlockFenceGate;
    }

    private static boolean isProperHit(final BlockBell bell, final BlockFace direction, final float clickY) {
        if (direction.getAxis() == BlockFace.Axis.Y || clickY > 0.8124F) {
            return false;
        }
        final BlockFace.Axis bellAxis = bell.getBlockFace().getAxis();
        return switch (bell.getAttachmentType()) {
            case BlockBell.TYPE_ATTACHMENT_STANDING -> bellAxis == direction.getAxis();
            case BlockBell.TYPE_ATTACHMENT_SIDE, BlockBell.TYPE_ATTACHMENT_MULTIPLE -> bellAxis != direction.getAxis();
            case BlockBell.TYPE_ATTACHMENT_HANGING -> true;
            default -> false;
        };
    }

    private static boolean isWaterBucket(final Item item) {
        return item.getId() == ItemID.BUCKET && item.getDamage() == ItemBucket.WATER_BUCKET;
    }

    private static boolean isLavaBucket(final Item item) {
        return item.getId() == ItemID.BUCKET && item.getDamage() == ItemBucket.LAVA_BUCKET;
    }

    private static boolean isPowderSnowBucket(final Item item) {
        return item.getId() == ItemID.BUCKET && item.getDamage() == ItemBucket.POWDER_SNOW_BUCKET;
    }

    private static boolean isBoneMeal(final Item item) {
        return item.getId() == ItemID.DYE && item.getDamage() == 15;
    }

    private static boolean isCake(final Block block) {
        final int id = block.getId();
        return id == BlockID.CAKE_BLOCK
                || id == BlockID.CANDLE_CAKE
                || id == BlockID.WHITE_CANDLE_CAKE
                || id == BlockID.ORANGE_CANDLE_CAKE
                || id == BlockID.MAGENTA_CANDLE_CAKE
                || id == BlockID.LIGHT_BLUE_CANDLE_CAKE
                || id == BlockID.YELLOW_CANDLE_CAKE
                || id == BlockID.LIME_CANDLE_CAKE
                || id == BlockID.PINK_CANDLE_CAKE
                || id == BlockID.GRAY_CANDLE_CAKE
                || id == BlockID.LIGHT_GRAY_CANDLE_CAKE
                || id == BlockID.CYAN_CANDLE_CAKE
                || id == BlockID.PURPLE_CANDLE_CAKE
                || id == BlockID.BLUE_CANDLE_CAKE
                || id == BlockID.BROWN_CANDLE_CAKE
                || id == BlockID.GREEN_CANDLE_CAKE
                || id == BlockID.RED_CANDLE_CAKE
                || id == BlockID.BLACK_CANDLE_CAKE;
    }

    private static boolean isCandleBlock(final Block block) {
        final int id = block.getId();
        return id == BlockID.CANDLE
                || id == BlockID.WHITE_CANDLE
                || id == BlockID.ORANGE_CANDLE
                || id == BlockID.MAGENTA_CANDLE
                || id == BlockID.LIGHT_BLUE_CANDLE
                || id == BlockID.YELLOW_CANDLE
                || id == BlockID.LIME_CANDLE
                || id == BlockID.PINK_CANDLE
                || id == BlockID.GRAY_CANDLE
                || id == BlockID.LIGHT_GRAY_CANDLE
                || id == BlockID.CYAN_CANDLE
                || id == BlockID.PURPLE_CANDLE
                || id == BlockID.BLUE_CANDLE
                || id == BlockID.BROWN_CANDLE
                || id == BlockID.GREEN_CANDLE
                || id == BlockID.RED_CANDLE
                || id == BlockID.BLACK_CANDLE;
    }

    private static boolean isCandleItem(final Item item) {
        final Block block = item.getBlockUnsafe();
        return block != null && isCandleBlock(block);
    }

    private static boolean isSign(final Block block) {
        if (block instanceof BlockSignPost || block instanceof BlockWallSign) {
            return true;
        }
        return block.getClass().getSimpleName().contains("Sign");
    }

    private static int fullId(final int blockId, final int blockData) {
        return blockId << Block.DATA_BITS | blockData & Block.DATA_MASK;
    }

    private void setHeldBucket(final CompensatedInventory inventory, final int bucketDamage) {
        inventory.inventoryContainer.set(
                inventory.heldItemSlot,
                Item.get(ItemID.BUCKET, bucketDamage, 1)
        );
    }



    private boolean isItemFrame(final Block block, final int runtimeId) {
        if (block instanceof BlockItemFrame) {
            return true;
        }

        final NetworkBlockState networkState =
                NetworkBlockStateRegistry.tryResolve(this.player, runtimeId);
        return networkState != null
                && (networkState.is("minecraft:frame")
                || networkState.is("minecraft:glow_frame")
                || networkState.identifierContains("item_frame"));
    }

    private static boolean isEmpty(final Item item) {
        return item == null || item.isNull() || item.getCount() <= 0;
    }

    public static boolean validate(final GhostPlayer player, final Item predicted, final Item claimed) {
        return ItemUtil.sameDefinition(player, predicted, claimed);
    }

    




}
