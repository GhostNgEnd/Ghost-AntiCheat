package ac.ghost.anticheat.prediction.nukkit.inventory;

import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.item.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Method;





public final class NukkitInventoryTransactionDataAdapter {
    private NukkitInventoryTransactionDataAdapter() {
    }

    public static int hotbarSlot(final Object data, final int fallback) {
        return number(data, fallback, "getHotbarSlot", "hotbarSlot", "selectedSlot");
    }

    public static Item itemInHand(final Object data) {
        final Object value = value(data, "getItemInHand", "itemInHand", "item");
        return value instanceof Item item ? item : Item.AIR_ITEM;
    }

    public static Vec3 fromPos(final Object data) {
        final Object value = value(data, "getPlayerPos", "getFromPos", "playerPos", "fromPos");
        return vector(value);
    }

    public static Vec3 clickPos(final Object data) {
        final Object value = value(data, "getClickPos", "clickPos");
        return vector(value);
    }

    private static int number(final Object target, final int fallback,
                              final String... names) {
        final Object value = value(target, names);
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static Object value(final Object target, final String... names) {
        if (target == null) {
            return null;
        }
        for (final String name : names) {
            try {
                final Method method = target.getClass().getMethod(name);
                return method.invoke(target);
            } catch (ReflectiveOperationException ignored) {
            }
            try {
                final Field field = target.getClass().getField(name);
                return field.get(target);
            } catch (ReflectiveOperationException ignored) {
            }
            try {
                final Field field = target.getClass().getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private static Vec3 vector(final Object value) {
        if (value == null) {
            return null;
        }
        final Float x = coordinate(value, "getX", "x");
        final Float y = coordinate(value, "getY", "y");
        final Float z = coordinate(value, "getZ", "z");
        if (x == null || y == null || z == null
                || !Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) {
            return null;
        }
        return new Vec3(x, y, z);
    }

    private static Float coordinate(final Object target, final String methodName,
                                    final String fieldName) {
        final Object value = value(target, methodName, fieldName);
        return value instanceof Number number ? number.floatValue() : null;
    }
}
