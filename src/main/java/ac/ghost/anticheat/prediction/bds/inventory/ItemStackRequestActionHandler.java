package ac.ghost.anticheat.prediction.bds.inventory;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.compensated.cache.container.ContainerCache;
import ac.ghost.anticheat.data.inventory.ItemCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.nukkit.inventory.NukkitBundleRequestAdapter;
import ac.ghost.anticheat.prediction.nukkit.inventory.NukkitItemStackRequestAdapter;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;
import ac.ghost.anticheat.util.ItemUtil;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.inventory.Recipe;
import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.inventory.ShapelessRecipe;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.GameType;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequest;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequestSlotData;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ConsumeAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.CraftCreativeAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.CraftRecipeAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.CraftResultsDeprecatedAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.DestroyAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.DropAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.SwapAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.TransferItemStackRequestAction;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;








public final class ItemStackRequestActionHandler {
    private static final int PLAYER_CRAFTING_GRID_OFFSET = 28;
    private static final int CREATED_OUTPUT_LEGACY_SLOT = 50;

    private final GhostPlayer player;
    private final CompensatedInventory inventory;
    private final List<ItemCache> authoritativeCraftResults = new ArrayList<>();

    private boolean craftActionSeen;
    private ItemCache createdOutput = ItemCache.AIR;

    public ItemStackRequestActionHandler(final GhostPlayer player) {
        this.player = player;
        this.inventory = player.compensatedInventory;
    }

    public ItemStackResponseStatus handleRequest(final ItemStackRequest request) {
        if (request == null || request.getActions() == null
                || request.getActions().length == 0) {
            return ItemStackResponseStatus.OK;
        }

        final RequestSnapshot snapshot = RequestSnapshot.capture(this.inventory);
        this.craftActionSeen = false;
        this.authoritativeCraftResults.clear();
        this.createdOutput = ItemCache.AIR;

        for (final ItemStackRequestAction action : request.getActions()) {
            if (action == null) {
                snapshot.restore(this.inventory);
                return ItemStackResponseStatus.ERROR;
            }
            final ItemStackResponseStatus allowed = isRequestActionAllowed(action);
            if (!allowed.isSuccess()) {
                snapshot.restore(this.inventory);
                return allowed;
            }

            final ItemStackResponseStatus result;
            try {
                result = dispatch(action);
            } catch (RuntimeException ignored) {
                snapshot.restore(this.inventory);
                return ItemStackResponseStatus.ERROR;
            }
            if (!result.isSuccess()) {
                snapshot.restore(this.inventory);
                return result;
            }
        }

        NukkitItemUseStateSystem.onHeldItemChanged(this.player,
                this.inventory.inventoryContainer.getHeldItemData());
        return ItemStackResponseStatus.OK;
    }

    private ItemStackResponseStatus isRequestActionAllowed(
            final ItemStackRequestAction action) {
        final String type = action.getType().name();
        return switch (type) {
            case "TAKE", "PLACE", "DROP", "DESTROY", "LAB_TABLE_COMBINE",
                    "BEACON_PAYMENT", "MINE_BLOCK" -> ItemStackResponseStatus.OK;
            case "SWAP" -> this.craftActionSeen
                    ? ItemStackResponseStatus.INVALID_CRAFT_REQUEST
                    : ItemStackResponseStatus.OK;
            case "CONSUME", "CREATE", "CRAFT_RESULTS_DEPRECATED" ->
                    this.craftActionSeen
                            ? ItemStackResponseStatus.OK
                            : ItemStackResponseStatus.INVALID_CRAFT_REQUEST;
            case "PLACE_IN_CONTAINER", "TAKE_OUT_CONTAINER" ->
                    ItemStackResponseStatus.ERROR;
            case "CRAFT_RECIPE", "CRAFT_RECIPE_AUTO", "CRAFT_CREATIVE",
                    "CRAFT_RECIPE_OPTIONAL", "CRAFT_GRINDSTONE", "CRAFT_LOOM",
                    "CRAFT_NON_IMPLEMENTED_DEPRECATED" -> {
                if (this.craftActionSeen) {
                    yield ItemStackResponseStatus.REQUEST_ALREADY_IN_PROGRESS;
                }
                this.craftActionSeen = true;
                yield ItemStackResponseStatus.OK;
            }
            default -> ItemStackResponseStatus.ERROR;
        };
    }

    private ItemStackResponseStatus dispatch(final ItemStackRequestAction action) {
        return switch (action.getType().name()) {
            case "TAKE" -> handleTransfer((TransferItemStackRequestAction) action,
                    TransferMode.TAKE);
            case "PLACE" -> handleTransfer((TransferItemStackRequestAction) action,
                    TransferMode.PLACE);
            case "SWAP" -> handleSwap((SwapAction) action);
            case "DROP" -> handleRemove(((DropAction) action).getSource(),
                    ((DropAction) action).getCount(), RemoveMode.DROP,
                    NukkitItemStackRequestAdapter.dropRandomly(action));
            case "DESTROY" -> handleRemove(((DestroyAction) action).getSource(),
                    ((DestroyAction) action).getCount(), RemoveMode.DESTROY,
                    false);
            case "CONSUME" -> handleConsume((ConsumeAction) action);
            case "CREATE" -> handleCreate(
                    NukkitItemStackRequestAdapter.createResultIndex(action));
            case "CRAFT_CREATIVE" -> handleCraftCreative((CraftCreativeAction) action);
            case "CRAFT_RECIPE" -> handleCraftRecipe((CraftRecipeAction) action, false);
            case "CRAFT_RECIPE_AUTO" -> handleCraftRecipeAuto(action);
            case "CRAFT_RECIPE_OPTIONAL" -> handleCraftRecipeOptional(action);
            case "CRAFT_GRINDSTONE" -> handleScreenCraft(InventoryType.GRINDSTONE);
            case "CRAFT_LOOM" -> handleScreenCraft(InventoryType.LOOM);
            case "CRAFT_RESULTS_DEPRECATED" ->
                    handleCraftResultsDeprecated((CraftResultsDeprecatedAction) action);
            case "LAB_TABLE_COMBINE", "BEACON_PAYMENT", "MINE_BLOCK" ->
                    handleNukkitScreenAction(action.getType().name());
            case "CRAFT_NON_IMPLEMENTED_DEPRECATED" ->
                    ItemStackResponseStatus.INVALID_CRAFT_REQUEST;
            default -> ItemStackResponseStatus.ERROR;
        };
    }

    private ItemStackResponseStatus handleTransfer(
            final TransferItemStackRequestAction action,
            final TransferMode mode) {
        final NukkitBundleRequestAdapter.BundleResponse bundle =
                NukkitBundleRequestAdapter.process(this.inventory, action);
        if (bundle.bundle()) {
            return bundle.valid() ? ItemStackResponseStatus.OK
                    : ItemStackResponseStatus.FAILED_TO_VALIDATE_SRC_SLOT;
        }

        final ItemStackRequestSlotData sourceData = action.getSource();
        final ItemStackRequestSlotData destinationData = action.getDestination();
        if (destinationData.getContainer() == ContainerSlotType.CREATED_OUTPUT) {
            return ItemStackResponseStatus.DST_CONTAINER_EQUAL_TO_CREATED_OUTPUT_CONTAINER;
        }
        if (sameLogicalSlot(sourceData, destinationData)) {
            return ItemStackResponseStatus
                    .DST_CONTAINER_AND_SLOT_EQUAL_TO_SRC_CONTAINER_AND_SLOT;
        }

        final ResolvedSlot source = resolveSource(sourceData);
        if (source == null) {
            return ItemStackResponseStatus.FAILED_TO_VALIDATE_SRC_SLOT;
        }
        final int amount = action.getCount();
        if (amount <= 0 || amount > source.item().count()) {
            return ItemStackResponseStatus.INVALID_ADJUSTED_AMOUNT;
        }

        final ResolvedSlot destination = resolveDestination(destinationData);
        if (destination == null) {
            return ItemStackResponseStatus.FAILED_TO_VALIDATE_DST_SLOT;
        }

        final ItemCache sourceItem = source.item();
        final ItemCache destinationItem = destination.item();
        if (!destinationItem.isEmpty()
                && !ItemUtil.sameDefinition(this.player,
                sourceItem.getData(), destinationItem.getData())) {
            return ItemStackResponseStatus.CANNOT_PLACE_ITEM;
        }

        if (!destinationItem.isEmpty()) {
            final int maxStack = Math.max(1, destinationItem.getData().getMaxStackSize());
            if (destinationItem.count() + amount > maxStack) {
                return ItemStackResponseStatus.CANNOT_PLACE_ITEM;
            }
        }

        remove(source, amount);
        if (destinationItem.isEmpty()) {
            destination.container().set(destination.slot(),
                    sourceItem.clone().count(amount));
        } else {
            destinationItem.count(destinationItem.count() + amount);
        }
        if (source.createdOutput()) {
            this.createdOutput = source.itemAfterMutation();
        }
        return ItemStackResponseStatus.OK;
    }

    private ItemStackResponseStatus handleSwap(final SwapAction action) {
        final ItemStackRequestSlotData sourceData = action.getSource();
        final ItemStackRequestSlotData destinationData = action.getDestination();
        if (destinationData.getContainer() == ContainerSlotType.CREATED_OUTPUT) {
            return ItemStackResponseStatus.DST_CONTAINER_EQUAL_TO_CREATED_OUTPUT_CONTAINER;
        }
        if (sameLogicalSlot(sourceData, destinationData)) {
            return ItemStackResponseStatus
                    .DST_CONTAINER_AND_SLOT_EQUAL_TO_SRC_CONTAINER_AND_SLOT;
        }
        final ResolvedSlot source = resolveOrdinarySlot(sourceData, true);
        if (source == null) {
            return ItemStackResponseStatus.FAILED_TO_VALIDATE_SRC_SLOT;
        }
        final ResolvedSlot destination = resolveOrdinarySlot(destinationData, false);
        if (destination == null) {
            return ItemStackResponseStatus.FAILED_TO_VALIDATE_DST_SLOT;
        }
        final ItemCache sourceItem = source.item();
        final ItemCache destinationItem = destination.item();
        source.container().set(source.slot(), destinationItem.clone());
        destination.container().set(destination.slot(), sourceItem.clone());
        return ItemStackResponseStatus.OK;
    }

    private ItemStackResponseStatus handleRemove(
            final ItemStackRequestSlotData sourceData,
            final int amount,
            final RemoveMode mode,
            final boolean randomly) {
        final ResolvedSlot source = resolveOrdinarySlot(sourceData, true);
        if (source == null) {
            return ItemStackResponseStatus.FAILED_TO_VALIDATE_SRC_SLOT;
        }
        if (amount <= 0 || amount > source.item().count()) {
            return ItemStackResponseStatus.INVALID_ADJUSTED_AMOUNT;
        }
        remove(source, amount);
        
        
        
        if (mode == RemoveMode.DROP && randomly) {
            return ItemStackResponseStatus.OK;
        }
        return ItemStackResponseStatus.OK;
    }

    private ItemStackResponseStatus handleConsume(final ConsumeAction action) {
        if (!this.craftActionSeen) {
            return ItemStackResponseStatus.INVALID_CRAFT_REQUEST;
        }
        final ResolvedSlot source = resolveOrdinarySlot(action.getSource(), true);
        if (source == null) {
            return ItemStackResponseStatus.FAILED_TO_VALIDATE_SRC_SLOT;
        }
        final Integer itemNetId = NukkitItemStackRequestAdapter
                .itemStackNetworkId(source.item().getData());
        if (itemNetId != null && itemNetId < 0) {
            return ItemStackResponseStatus.INVALID_ITEM_NET_ID;
        }
        if (action.getCount() <= 0 || action.getCount() > source.item().count()) {
            return ItemStackResponseStatus.INVALID_ADJUSTED_AMOUNT;
        }
        remove(source, action.getCount());
        return ItemStackResponseStatus.OK;
    }

    private ItemStackResponseStatus handleCreate(final int resultIndex) {
        if (resultIndex < 0 || resultIndex >= this.authoritativeCraftResults.size()) {
            return ItemStackResponseStatus.INVALID_CRAFT_RESULT_INDEX;
        }
        final ItemCache result = this.authoritativeCraftResults.get(resultIndex);
        if (result == null || result.isEmpty()) {
            return ItemStackResponseStatus.INVALID_CRAFT_RESULT_ITEM;
        }
        if (!this.createdOutput.isEmpty()) {
            return ItemStackResponseStatus.FAILED_TO_SET_CREATED_ITEM_OUTPUT_SLOT;
        }
        final int maxStack = Math.max(1, result.getData().getMaxStackSize());
        if (result.count() <= 0 || result.count() > maxStack) {
            return ItemStackResponseStatus.INVALID_CRAFT_RESULT_STACK_SIZE;
        }
        this.createdOutput = result.clone();
        return ItemStackResponseStatus.OK;
    }

    private ItemStackResponseStatus handleCraftCreative(
            final CraftCreativeAction action) {
        if (this.player.entityContext.actorGameTypeComponent.value != GameType.CREATIVE) {
            return ItemStackResponseStatus.PLAYER_NOT_IN_CREATIVE_MODE;
        }
        final Item raw = this.inventory.getCreativeData()
                .get(action.getCreativeItemNetworkId());
        if (raw == null || raw.isNull()) {
            return ItemStackResponseStatus.INVALID_CRAFT_RESULT_ITEM;
        }
        final Item item = raw.clone();
        item.setCount(Math.max(1, item.getMaxStackSize()));
        this.authoritativeCraftResults.clear();
        this.authoritativeCraftResults.add(ItemCache.build(this.inventory, item));
        return ItemStackResponseStatus.OK;
    }

    private ItemStackResponseStatus handleCraftRecipe(
            final CraftRecipeAction action,
            final boolean automatic) {
        return handleRecipeNetworkId(action.getRecipeNetworkId(), automatic);
    }

    private ItemStackResponseStatus handleCraftRecipeAuto(
            final ItemStackRequestAction action) {
        final Integer recipeId = reflectInt(action, "getRecipeNetworkId");
        if (recipeId == null) {
            return ItemStackResponseStatus.INVALID_CRAFT_REQUEST;
        }
        return handleRecipeNetworkId(recipeId, true);
    }

    private ItemStackResponseStatus handleRecipeNetworkId(final int recipeId,
                                                          final boolean automatic) {
        if (recipeId == 0) {
            return ItemStackResponseStatus.INVALID_ITEM_NET_ID;
        }
        final Recipe recipe = this.inventory.getCraftingData().get(recipeId);
        if (recipe == null) {
            return ItemStackResponseStatus.RECIPE_NOT_FOUND;
        }

        final ContainerCache grid = craftingGrid();
        final boolean largeGrid = grid != null
                && grid.getType() == InventoryType.WORKBENCH;
        if (recipe instanceof ShapedRecipe shaped
                && shaped.getIngredientList().size() > 4 && !largeGrid) {
            return ItemStackResponseStatus.RECIPE_REQUIRES_CRAFTING_TABLE;
        }
        if (grid == null) {
            return ItemStackResponseStatus.INVALID_CRAFT_REQUEST_SCREEN;
        }

        final List<Item> gridItems = craftingGridItems(grid, largeGrid ? 9 : 4);
        if (!matchesRecipe(recipe, gridItems)) {
            return ItemStackResponseStatus.INVALID_CRAFT_RESULT;
        }

        this.authoritativeCraftResults.clear();
        if (recipe instanceof ShapedRecipe shaped) {
            for (final Item result : shaped.getAllResults()) {
                addAuthoritativeResult(result, automatic ? maxCrafts(recipe, gridItems) : 1);
            }
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            addAuthoritativeResult(shapeless.getResult(),
                    automatic ? maxCrafts(recipe, gridItems) : 1);
        } else {
            return ItemStackResponseStatus.INVALID_CRAFT_REQUEST;
        }
        return this.authoritativeCraftResults.isEmpty()
                ? ItemStackResponseStatus.INVALID_CRAFT_RESULT
                : ItemStackResponseStatus.OK;
    }

    private ItemStackResponseStatus handleCraftRecipeOptional(
            final ItemStackRequestAction action) {
        final int index = NukkitItemStackRequestAdapter.optionalRecipeIndex(action);
        if (index < 0) {
            return ItemStackResponseStatus.INVALID_CRAFT_REQUEST;
        }
        
        
        
        return handleRecipeNetworkId(index, false);
    }

    private ItemStackResponseStatus handleScreenCraft(final InventoryType expected) {
        final ContainerCache open = this.inventory.openContainer;
        return open != null && open.getType() == expected
                ? ItemStackResponseStatus.OK
                : ItemStackResponseStatus.INVALID_CRAFT_REQUEST_SCREEN;
    }

    private ItemStackResponseStatus handleNukkitScreenAction(final String type) {
        final ContainerCache open = this.inventory.openContainer;
        if (open == null) {
            return ItemStackResponseStatus.INVALID_CRAFT_REQUEST_SCREEN;
        }
        return switch (type) {
            case "BEACON_PAYMENT" -> open.getType() == InventoryType.BEACON
                    ? ItemStackResponseStatus.OK
                    : ItemStackResponseStatus.INVALID_CRAFT_REQUEST_SCREEN;
            case "LAB_TABLE_COMBINE", "MINE_BLOCK" -> ItemStackResponseStatus.OK;
            default -> ItemStackResponseStatus.INVALID_CRAFT_REQUEST;
        };
    }

    private ItemStackResponseStatus handleCraftResultsDeprecated(
            final CraftResultsDeprecatedAction action) {
        if (!this.craftActionSeen) {
            return ItemStackResponseStatus.INVALID_CRAFT_REQUEST;
        }
        final List<Item> claimedResults = normalizeItems(action.getResultItems());
        if (claimedResults.isEmpty()) {
            return ItemStackResponseStatus.INVALID_CRAFT_RESULT;
        }
        if (this.authoritativeCraftResults.isEmpty()) {
            return ItemStackResponseStatus.INVALID_CRAFT_RESULT;
        }

        final boolean[] matched = new boolean[this.authoritativeCraftResults.size()];
        for (final Item claimed : claimedResults) {
            if (claimed == null || claimed.isNull() || claimed.getCount() <= 0) {
                return ItemStackResponseStatus.INVALID_CRAFT_RESULT_ITEM;
            }
            boolean found = false;
            for (int i = 0; i < this.authoritativeCraftResults.size(); i++) {
                if (matched[i]) {
                    continue;
                }
                final ItemCache authoritative = this.authoritativeCraftResults.get(i);
                if (ItemUtil.sameDefinition(this.player, claimed,
                        authoritative.getData())
                        && (claimed.getCount() == authoritative.count()
                        || this.player.entityContext.actorGameTypeComponent.value
                        == GameType.CREATIVE)) {
                    matched[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return ItemStackResponseStatus.INVALID_CRAFT_RESULT;
            }
        }

        if (this.authoritativeCraftResults.size() == 1) {
            return handleCreate(0);
        }
        return ItemStackResponseStatus.OK;
    }

    private ResolvedSlot resolveSource(final ItemStackRequestSlotData slot) {
        if (slot.getContainer() == ContainerSlotType.CREATED_OUTPUT) {
            if (this.createdOutput.isEmpty() && !this.authoritativeCraftResults.isEmpty()) {
                final ItemStackResponseStatus create = handleCreate(0);
                if (!create.isSuccess()) {
                    return null;
                }
            }
            if (this.createdOutput.isEmpty()) {
                return null;
            }
            return ResolvedSlot.createdOutput(this.createdOutput);
        }
        return resolveOrdinarySlot(slot, true);
    }

    private ResolvedSlot resolveDestination(final ItemStackRequestSlotData slot) {
        return resolveOrdinarySlot(slot, false);
    }

    private ResolvedSlot resolveOrdinarySlot(final ItemStackRequestSlotData request,
                                             final boolean strictExisting) {
        if (request == null) {
            return null;
        }
        final ContainerCache container = findContainer(this.inventory,
                request.getContainer());
        if (container == null) {
            return null;
        }
        final int slot = request.getSlot();
        if (slot < container.getOffset() || slot >= container.getContainerSize()) {
            return null;
        }
        final ItemCache item = container.get(slot);
        if (strictExisting && item.isEmpty()) {
            return null;
        }

        final Integer requestedNetId = NukkitItemStackRequestAdapter
                .requestedStackNetworkId(request);
        if (requestedNetId != null && requestedNetId > 0) {
            if (item.isEmpty()) {
                return null;
            }
            final Integer existingNetId = NukkitItemStackRequestAdapter
                    .itemStackNetworkId(item.getData());
            if (existingNetId != null && existingNetId > 0
                    && !requestedNetId.equals(existingNetId)) {
                return null;
            }
        }
        return new ResolvedSlot(container, slot, item, false);
    }

    private boolean sameLogicalSlot(final ItemStackRequestSlotData a,
                                    final ItemStackRequestSlotData b) {
        if (a == null || b == null || a.getSlot() != b.getSlot()) {
            return false;
        }
        if (a.getContainer() == b.getContainer()) {
            return sameDynamicContainer(a, b);
        }
        return isPlayerInventoryAlias(a.getContainer())
                && isPlayerInventoryAlias(b.getContainer());
    }

    private static boolean sameDynamicContainer(
            final ItemStackRequestSlotData a,
            final ItemStackRequestSlotData b) {
        if (a.getContainer() != ContainerSlotType.DYNAMIC_CONTAINER) {
            return true;
        }
        try {
            return a.getContainerName().getDynamicId()
                    == b.getContainerName().getDynamicId();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean isPlayerInventoryAlias(final ContainerSlotType type) {
        return type == ContainerSlotType.INVENTORY
                || type == ContainerSlotType.HOTBAR
                || type == ContainerSlotType.HOTBAR_AND_INVENTORY;
    }

    private void remove(final ResolvedSlot source, final int amount) {
        final ItemCache item = source.item();
        if (source.createdOutput()) {
            if (amount >= item.count()) {
                this.createdOutput = ItemCache.AIR;
            } else {
                this.createdOutput = item.clone().count(item.count() - amount);
            }
            source.setItemAfterMutation(this.createdOutput);
            return;
        }
        if (amount >= item.count()) {
            source.container().set(source.slot(), ItemCache.AIR);
        } else {
            source.container().set(source.slot(),
                    item.clone().count(item.count() - amount));
        }
    }

    private ContainerCache craftingGrid() {
        if (this.inventory.openContainer != null
                && this.inventory.openContainer.getType() == InventoryType.WORKBENCH) {
            return this.inventory.openContainer;
        }
        return this.inventory.hudContainer;
    }

    private List<Item> craftingGridItems(final ContainerCache grid,
                                         final int slots) {
        final int offset = grid.getType() == InventoryType.WORKBENCH
                ? grid.getOffset() : PLAYER_CRAFTING_GRID_OFFSET;
        final List<Item> items = new ArrayList<>(slots);
        for (int i = 0; i < slots; i++) {
            items.add(grid.get(offset + i).getData());
        }
        return items;
    }

    private boolean matchesRecipe(final Recipe recipe,
                                  final List<Item> gridItems) {
        if (recipe instanceof ShapedRecipe shaped) {
            final List<Item> ingredients = shaped.getIngredientList();
            if (ingredients.size() > gridItems.size()) {
                return false;
            }
            for (int i = 0; i < ingredients.size(); i++) {
                if (!ItemUtil.sameDefinition(this.player,
                        ingredients.get(i), gridItems.get(i))) {
                    return false;
                }
            }
            return true;
        }
        if (recipe instanceof ShapelessRecipe shapeless) {
            final List<Item> available = new ArrayList<>(gridItems);
            for (final Item ingredient : shapeless.getIngredientList()) {
                int match = -1;
                for (int i = 0; i < available.size(); i++) {
                    if (ItemUtil.sameDefinition(this.player,
                            ingredient, available.get(i))) {
                        match = i;
                        break;
                    }
                }
                if (match < 0) {
                    return false;
                }
                available.remove(match);
            }
            return true;
        }
        return false;
    }

    private int maxCrafts(final Recipe recipe, final List<Item> gridItems) {
        final List<Item> ingredients;
        if (recipe instanceof ShapedRecipe shaped) {
            ingredients = shaped.getIngredientList();
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            ingredients = shapeless.getIngredientList();
        } else {
            return 1;
        }
        final Map<String, Integer> available = countDefinitions(gridItems);
        final Map<String, Integer> required = countDefinitions(ingredients);
        int max = Integer.MAX_VALUE;
        for (final Map.Entry<String, Integer> entry : required.entrySet()) {
            final int have = available.getOrDefault(entry.getKey(), 0);
            max = Math.min(max, have / Math.max(1, entry.getValue()));
        }
        return max == Integer.MAX_VALUE ? 1 : Math.max(1, max);
    }

    private Map<String, Integer> countDefinitions(final List<Item> items) {
        final Map<String, Integer> counts = new HashMap<>();
        for (final Item item : items) {
            if (item == null || item.isNull()) {
                continue;
            }
            final String identifier = ItemUtil.identifier(this.player, item);
            final String key = identifier == null
                    ? item.getId() + ":" + item.getDamage()
                    : identifier.toLowerCase();
            counts.merge(key, Math.max(1, item.getCount()), Integer::sum);
        }
        return counts;
    }

    private void addAuthoritativeResult(final Item raw, final int crafts) {
        if (raw == null || raw.isNull()) {
            return;
        }
        final Item result = raw.clone();
        final long count = (long) Math.max(1, result.getCount())
                * Math.max(1, crafts);
        result.setCount((int) Math.min(Integer.MAX_VALUE, count));
        this.authoritativeCraftResults.add(ItemCache.build(this.inventory, result));
    }

    private static List<Item> normalizeItems(final Object rawItems) {
        if (rawItems == null) {
            return List.of();
        }
        final List<Item> items = new ArrayList<>();
        if (rawItems instanceof Iterable<?> iterable) {
            for (final Object value : iterable) {
                if (value instanceof Item item) {
                    items.add(item);
                }
            }
            return items;
        }
        if (rawItems.getClass().isArray()) {
            final int length = Array.getLength(rawItems);
            for (int index = 0; index < length; index++) {
                final Object value = Array.get(rawItems, index);
                if (value instanceof Item item) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    private static Integer reflectInt(final Object object, final String methodName) {
        try {
            final Method method = object.getClass().getMethod(methodName);
            final Object value = method.invoke(object);
            return value instanceof Number number ? number.intValue() : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public static ContainerCache findContainer(final CompensatedInventory inventory,
                                               final ContainerSlotType type) {
        if (inventory == null || type == null) {
            return null;
        }
        return switch (type) {
            case CURSOR -> inventory.hudContainer;
            case ARMOR -> inventory.armorContainer;
            case OFFHAND -> inventory.offhandContainer;
            case INVENTORY, HOTBAR, HOTBAR_AND_INVENTORY ->
                    inventory.inventoryContainer;
            case CREATED_OUTPUT -> null;
            default -> inventory.openContainer;
        };
    }

    private enum TransferMode {
        TAKE,
        PLACE
    }

    private enum RemoveMode {
        DROP,
        DESTROY
    }

    private static final class ResolvedSlot {
        private final ContainerCache container;
        private final int slot;
        private final ItemCache item;
        private final boolean createdOutput;
        private ItemCache itemAfterMutation;

        private ResolvedSlot(final ContainerCache container,
                             final int slot,
                             final ItemCache item,
                             final boolean createdOutput) {
            this.container = container;
            this.slot = slot;
            this.item = item == null ? ItemCache.AIR : item;
            this.createdOutput = createdOutput;
            this.itemAfterMutation = this.item;
        }

        static ResolvedSlot createdOutput(final ItemCache item) {
            return new ResolvedSlot(null, CREATED_OUTPUT_LEGACY_SLOT,
                    item, true);
        }

        ContainerCache container() {
            return this.container;
        }

        int slot() {
            return this.slot;
        }

        ItemCache item() {
            return this.item;
        }

        boolean createdOutput() {
            return this.createdOutput;
        }

        ItemCache itemAfterMutation() {
            return this.itemAfterMutation;
        }

        void setItemAfterMutation(final ItemCache itemAfterMutation) {
            this.itemAfterMutation = itemAfterMutation;
        }
    }

    private record RequestSnapshot(
            ItemCache[] inventory,
            ItemCache[] offhand,
            ItemCache[] armor,
            ItemCache[] hud,
            ItemCache[] open) {

        static RequestSnapshot capture(final CompensatedInventory inventory) {
            return new RequestSnapshot(
                    inventory.inventoryContainer.snapshotContents(),
                    inventory.offhandContainer.snapshotContents(),
                    inventory.armorContainer.snapshotContents(),
                    inventory.hudContainer.snapshotContents(),
                    inventory.openContainer == null ? null
                            : inventory.openContainer.snapshotContents());
        }

        void restore(final CompensatedInventory inventory) {
            inventory.inventoryContainer.restoreContents(this.inventory);
            inventory.offhandContainer.restoreContents(this.offhand);
            inventory.armorContainer.restoreContents(this.armor);
            inventory.hudContainer.restoreContents(this.hud);
            if (inventory.openContainer != null && this.open != null) {
                inventory.openContainer.restoreContents(this.open);
            }
        }
    }
}
