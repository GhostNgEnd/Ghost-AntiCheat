package ac.ghost.anticheat.prediction.bds.component;

import cn.nukkit.network.protocol.types.PlayerAbility;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


public final class AbilitiesComponent {
    public static final int ABILITY_COUNT = 20;

    public static final int OPERATOR_COMMANDS = 6;
    public static final int FLYING = 9;
    public static final int MAY_FLY = 10;
    public static final int INSTABUILD = 11;
    public static final int FLY_SPEED = 13;
    public static final int WORLD_BUILDER = 16;
    public static final int NO_CLIP = 17;
    public static final int VERTICAL_FLY_SPEED = 19;

    private static final byte UNSET = 1;
    private static final byte BOOLEAN = 2;
    private static final byte FLOAT = 3;

    private final byte[] states = new byte[ABILITY_COUNT];
    private final boolean[] booleanValues = new boolean[ABILITY_COUNT];
    private final float[] floatValues = new float[ABILITY_COUNT];
    private final Set<PlayerAbility> protocolAbilities = new LinkedHashSet<>();

    public AbilitiesComponent() {
        Arrays.fill(this.states, UNSET);
    }

    public Set<PlayerAbility> protocolAbilities() {
        return Collections.unmodifiableSet(this.protocolAbilities);
    }

    public void clearProtocolAbilities() {
        this.protocolAbilities.clear();
    }

    public void setProtocolAbility(final PlayerAbility ability, final boolean enabled) {
        if (enabled) {
            this.protocolAbilities.add(ability);
        } else {
            this.protocolAbilities.remove(ability);
        }
    }

    public float protocolFlySpeed() {
        return this.getFloat(FLY_SPEED);
    }

    public void setProtocolFlySpeed(final float value) {
        this.setFloat(FLY_SPEED, value);
    }

    public float protocolVerticalFlySpeed() {
        return this.getFloat(VERTICAL_FLY_SPEED);
    }

    public void setProtocolVerticalFlySpeed(final float value) {
        this.setFloat(VERTICAL_FLY_SPEED, value);
    }

    public void applyProtocolSnapshot(final Set<PlayerAbility> abilities,
                                      final float flySpeed,
                                      final float verticalFlySpeed) {
        this.protocolAbilities.clear();
        if (abilities != null) {
            this.protocolAbilities.addAll(abilities);
        }
        this.setBoolean(OPERATOR_COMMANDS,
                this.protocolAbilities.contains(PlayerAbility.OPERATOR_COMMANDS));
        this.setBoolean(FLYING, this.protocolAbilities.contains(PlayerAbility.FLYING));
        this.setBoolean(MAY_FLY, this.protocolAbilities.contains(PlayerAbility.MAY_FLY));
        this.setBoolean(INSTABUILD,
                this.protocolAbilities.contains(PlayerAbility.INSTABUILD));
        this.setBoolean(WORLD_BUILDER,
                this.protocolAbilities.contains(PlayerAbility.WORLD_BUILDER));
        this.setBoolean(NO_CLIP, this.protocolAbilities.contains(PlayerAbility.NO_CLIP));
        this.setFloat(FLY_SPEED, flySpeed);
        this.setFloat(VERTICAL_FLY_SPEED, verticalFlySpeed);
    }

    public void resolveProtocolSnapshot() {
        this.applyProtocolSnapshot(
                new LinkedHashSet<>(this.protocolAbilities),
                this.protocolFlySpeed(),
                this.protocolVerticalFlySpeed());
    }

    public void setBoolean(final int ability, final boolean value) {
        this.checkIndex(ability);
        this.states[ability] = BOOLEAN;
        this.booleanValues[ability] = value;
    }

    public void setFloat(final int ability, final float value) {
        this.checkIndex(ability);
        this.states[ability] = FLOAT;
        this.floatValues[ability] = value;
    }

    public boolean getBoolean(final int ability) {
        this.checkIndex(ability);
        return this.booleanValues[ability];
    }

    public float getFloat(final int ability) {
        this.checkIndex(ability);
        return this.floatValues[ability];
    }

    public byte getState(final int ability) {
        this.checkIndex(ability);
        return this.states[ability];
    }

    private void checkIndex(final int ability) {
        if (ability < 0 || ability >= ABILITY_COUNT) {
            throw new IndexOutOfBoundsException("ability=" + ability);
        }
    }
}
