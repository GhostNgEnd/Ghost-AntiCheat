package ac.ghost.anticheat.data.block;

import cn.nukkit.block.Block;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.NumberTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;








public final class NetworkBlockState {
    private final int networkId;
    private final int legacyFullId;
    private final CompoundTag state;
    private final CompoundTag properties;
    private final String identifier;

    NetworkBlockState(int networkId, int legacyFullId, CompoundTag state) {
        this.networkId = networkId;
        this.legacyFullId = legacyFullId;
        this.state = state == null ? null : state.clone();
        this.properties = this.state != null && this.state.containsCompound("states")
                ? this.state.getCompound("states") : new CompoundTag("states");
        this.identifier = this.state == null ? "" : this.state.getString("name");
    }

    public static NetworkBlockState legacy(int legacyFullId) {
        return new NetworkBlockState(Integer.MIN_VALUE, legacyFullId, null);
    }

    public int networkId() {
        return networkId;
    }

    public int legacyFullId() {
        return legacyFullId;
    }

    public int blockId() {
        return legacyFullId < 0 ? 0 : legacyFullId >> Block.DATA_BITS;
    }

    public int blockData() {
        return legacyFullId < 0 ? 0 : legacyFullId & Block.DATA_MASK;
    }

    public String identifier() {
        return identifier;
    }

    public boolean is(String identifier) {
        return identifier != null && identifier.equals(this.identifier);
    }

    public boolean identifierContains(String value) {
        return value != null && this.identifier.contains(value);
    }

    public CompoundTag stateTag() {
        return state == null ? null : state.clone();
    }

    public boolean hasProperty(String name) {
        return properties.contains(name);
    }

    public int intProperty(String name, int fallback) {
        Tag value = properties.get(name);
        if (value instanceof NumberTag number) {
            return number.getData().intValue();
        }
        if (value instanceof StringTag string) {
            try {
                return Integer.parseInt(string.data);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public boolean booleanProperty(String name, boolean fallback) {
        Tag value = properties.get(name);
        if (value instanceof NumberTag number) {
            return number.getData().intValue() != 0;
        }
        if (value instanceof StringTag string) {
            String text = string.data;
            if ("true".equalsIgnoreCase(text)) {
                return true;
            }
            if ("false".equalsIgnoreCase(text)) {
                return false;
            }
        }
        return fallback;
    }

    public String stringProperty(String name, String fallback) {
        Tag value = properties.get(name);
        if (value instanceof StringTag string) {
            return string.data;
        }
        if (value instanceof NumberTag number) {
            return String.valueOf(number.getData());
        }
        return fallback;
    }
}
