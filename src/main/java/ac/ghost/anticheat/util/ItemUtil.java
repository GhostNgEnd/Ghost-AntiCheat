package ac.ghost.anticheat.util;

import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.inventory.ItemTag;
import cn.nukkit.item.Item;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ItemUtil {
    




    private static final Map<String, Set<String>> LOCAL_ITEM_TAGS = Map.of(
            "minecraft:bundles", Set.of(
                    "minecraft:bundle",
                    "minecraft:white_bundle", "minecraft:orange_bundle", "minecraft:magenta_bundle",
                    "minecraft:light_blue_bundle", "minecraft:yellow_bundle", "minecraft:lime_bundle",
                    "minecraft:pink_bundle", "minecraft:gray_bundle", "minecraft:light_gray_bundle",
                    "minecraft:cyan_bundle", "minecraft:purple_bundle", "minecraft:blue_bundle",
                    "minecraft:brown_bundle", "minecraft:green_bundle", "minecraft:red_bundle",
                    "minecraft:black_bundle"
            ),
            "minecraft:candles", Set.of(
                    "minecraft:candle",
                    "minecraft:white_candle", "minecraft:orange_candle", "minecraft:magenta_candle",
                    "minecraft:light_blue_candle", "minecraft:yellow_candle", "minecraft:lime_candle",
                    "minecraft:pink_candle", "minecraft:gray_candle", "minecraft:light_gray_candle",
                    "minecraft:cyan_candle", "minecraft:purple_candle", "minecraft:blue_candle",
                    "minecraft:brown_candle", "minecraft:green_candle", "minecraft:red_candle",
                    "minecraft:black_candle"
            ),
            "minecraft:noteblock_top_instruments", Set.of(
                    "minecraft:skeleton_skull", "minecraft:wither_skeleton_skull",
                    "minecraft:zombie_head", "minecraft:player_head", "minecraft:creeper_head",
                    "minecraft:dragon_head", "minecraft:piglin_head"
            )
    );

    private ItemUtil() {
    }

    public static String identifier(final GhostPlayer player, final Item item) {
        if (item == null) {
            return null;
        }
        try {
            return item.getNamespaceId(player.getSession().getGameVersion());
        } catch (Exception ignored) {
            return null;
        }
    }

    public static int runtimeId(final GhostPlayer player, final Item item) {
        if (item == null) {
            return Integer.MIN_VALUE;
        }
        try {
            return item.getNetworkId(player.getSession().getGameVersion());
        } catch (Exception ignored) {
            return Integer.MIN_VALUE;
        }
    }

    public static boolean sameDefinition(final GhostPlayer player,
                                         final Item predicted,
                                         final Item claimed) {
        final boolean predictedEmpty = predicted == null || predicted.isNull();
        final boolean claimedEmpty = claimed == null || claimed.isNull();
        if (predictedEmpty || claimedEmpty) {
            return predictedEmpty == claimedEmpty;
        }

        final String predictedId = identifier(player, predicted);
        final String claimedId = identifier(player, claimed);
        if (predictedId == null || claimedId == null) {
            
            
            
            return predicted.equals(claimed, false, false);
        }
        if (!StringUtil.sanitizePrefix(predictedId)
                .equalsIgnoreCase(StringUtil.sanitizePrefix(claimedId))) {
            return false;
        }

        final int predictedRuntimeId = runtimeId(player, predicted);
        final int claimedRuntimeId = runtimeId(player, claimed);
        return predictedRuntimeId == Integer.MIN_VALUE
                || claimedRuntimeId == Integer.MIN_VALUE
                || predictedRuntimeId == claimedRuntimeId;
    }

    public static boolean hasUseDurationComponent(final GhostPlayer player, final Item item) {
        final String identifier = identifier(player, item);
        if (identifier == null) {
            return false;
        }

        try {
            final CustomItemDefinition definition = customItemDefinition(identifier);
            if (definition == null) {
                return false;
            }
            final CompoundTag root = definition.getNbt(player.getSession().protocol);
            if (root == null) {
                return false;
            }
            if (!root.containsCompound("components")) {
                return false;
            }
            final CompoundTag components = root.getCompound("components");
            if (components.contains("minecraft:use_duration")) {
                return true;
            }
            if (!components.containsCompound("item_properties")) {
                return false;
            }
            return components.getCompound("item_properties").contains("use_duration");
        } catch (Exception ignored) {
            return false;
        }
    }


    public static int useDurationTicks(final GhostPlayer player, final Item item) {
        if (item == null || item.isNull()) {
            return 0;
        }
        try {
            final int duration = item.getUseDuration();
            if (duration > 0) {
                return duration;
            }
        } catch (RuntimeException ignored) {
        }

        final String identifier = identifier(player, item);
        if (identifier == null) {
            return 0;
        }
        try {
            final CustomItemDefinition definition = customItemDefinition(identifier);
            if (definition == null) {
                return 0;
            }
            final CompoundTag root = definition.getNbt(player.getSession().protocol);
            if (root == null || !root.containsCompound("components")) {
                return 0;
            }
            final CompoundTag components = root.getCompound("components");
            if (components.containsCompound("minecraft:use_modifiers")) {
                return Math.max(0, components.getCompound("minecraft:use_modifiers")
                        .getInt("use_duration"));
            }
            if (components.containsCompound("item_properties")) {
                return Math.max(0, components.getCompound("item_properties")
                        .getInt("use_duration"));
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    




    public static float itemUseSlowdownModifier(final GhostPlayer player,
                                                final Item item) {
        if (item == null || item.isNull()) {
            return 1.0F;
        }
        final String identifier = identifier(player, item);
        if (identifier != null) {
            try {
                final CustomItemDefinition definition = customItemDefinition(identifier);
                if (definition != null) {
                    final CompoundTag root = definition.getNbt(player.getSession().protocol);
                    if (root != null && root.containsCompound("components")) {
                        final CompoundTag components = root.getCompound("components");
                        if (components.containsCompound("minecraft:use_modifiers")) {
                            final CompoundTag modifiers = components
                                    .getCompound("minecraft:use_modifiers");
                            if (modifiers.contains("movement_modifier")) {
                                return modifiers.getFloat("movement_modifier");
                            }
                            return 1.0F;
                        }
                        if (components.containsCompound("item_properties")) {
                            final CompoundTag properties = components
                                    .getCompound("item_properties");
                            if (properties.contains("movement_modifier")) {
                                return properties.getFloat("movement_modifier");
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        
        
        return isSpear(player, item) ? 1.0F : 0.35F;
    }

    public static boolean isSpear(final GhostPlayer player, final Item item) {
        if (item == null || item.isNull()) {
            return false;
        }
        if (item.getId() == cn.nukkit.item.ItemID.TRIDENT) {
            return true;
        }
        final String identifier = identifier(player, item);
        if (identifier == null) {
            return false;
        }
        final String normalized = identifier.toLowerCase(Locale.ROOT);
        return normalized.equals("minecraft:wooden_spear")
                || normalized.equals("minecraft:stone_spear")
                || normalized.equals("minecraft:iron_spear")
                || normalized.equals("minecraft:golden_spear")
                || normalized.equals("minecraft:diamond_spear")
                || normalized.equals("minecraft:netherite_spear");
    }

    private static CustomItemDefinition customItemDefinition(final String identifier) {
        return Item.getCustomItemDefinition(identifier);
    }

    public static boolean hasTag(final GhostPlayer player, final Item item, final String tag) {
        final String identifier = identifier(player, item);
        if (identifier == null) {
            return false;
        }
        final String normalizedIdentifier = identifier.toLowerCase(Locale.ROOT);
        final Set<String> items = ItemTag.getItemSet(tag);
        if (items != null && items.stream().anyMatch(value -> value.equalsIgnoreCase(normalizedIdentifier))) {
            return true;
        }
        return LOCAL_ITEM_TAGS.getOrDefault(tag.toLowerCase(Locale.ROOT), Set.of())
                .contains(normalizedIdentifier);
    }

    public static boolean isBundle(final GhostPlayer player, final Item item) {
        return hasTag(player, item, "minecraft:bundles");
    }

    public static boolean isNoteblockTopInstrument(final GhostPlayer player, final Item item) {
        return hasTag(player, item, "minecraft:noteblock_top_instruments");
    }

    public static boolean identifierEquals(final GhostPlayer player, final Item item, final String expected) {
        final String identifier = identifier(player, item);
        return identifier != null && identifier.equalsIgnoreCase(expected);
    }
}
