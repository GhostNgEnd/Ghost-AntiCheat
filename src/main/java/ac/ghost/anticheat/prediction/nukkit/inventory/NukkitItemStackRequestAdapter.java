package ac.ghost.anticheat.prediction.nukkit.inventory;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequestSlotData;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.ItemStackRequestAction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public final class NukkitItemStackRequestAdapter {
    private NukkitItemStackRequestAdapter() {
    }

    public static Integer requestedStackNetworkId(final ItemStackRequestSlotData slot) {
        return number(slot, "getStackNetworkId", "getStackId", "getNetId");
    }

    public static Integer itemStackNetworkId(final Item item) {
        return number(item, "getStackNetworkId", "getNetId", "getNetworkId");
    }

    public static int createResultIndex(final ItemStackRequestAction action) {
        final Integer value = number(action, "getResultIndex", "getSlot", "getIndex");
        return value == null ? -1 : value;
    }

    public static boolean dropRandomly(final ItemStackRequestAction action) {
        final Object value = invoke(action, "isRandomly", "getRandomly", "isRandom");
        return value instanceof Boolean bool && bool;
    }

    public static int optionalRecipeIndex(final ItemStackRequestAction action) {
        final Integer value = number(action, "getRecipeNetworkId", "getFilteredStringIndex", "getIndex");
        return value == null ? -1 : value;
    }

    private static Integer number(final Object instance, final String... names) {
        final Object value = invoke(instance, names);
        return value instanceof Number number ? number.intValue() : null;
    }

    private static Object invoke(final Object instance, final String... names) {
        if (instance == null) {
            return null;
        }
        for (final String name : names) {
            try {
                final Method method = instance.getClass().getMethod(name);
                method.setAccessible(true);
                return method.invoke(instance);
            } catch (ReflectiveOperationException ignored) {
            }
            try {
                final Field field = instance.getClass().getDeclaredField(fieldName(name));
                field.setAccessible(true);
                return field.get(instance);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private static String fieldName(final String accessor) {
        String name = accessor;
        if (name.startsWith("get") && name.length() > 3) {
            name = name.substring(3);
        } else if (name.startsWith("is") && name.length() > 2) {
            name = name.substring(2);
        }
        if (name.isEmpty()) {
            return accessor;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
