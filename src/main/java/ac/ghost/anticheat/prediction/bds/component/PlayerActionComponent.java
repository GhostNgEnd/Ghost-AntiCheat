package ac.ghost.anticheat.prediction.bds.component;

import cn.nukkit.math.Vector2f;
import cn.nukkit.network.protocol.types.AuthInputAction;

import java.util.HashSet;
import java.util.Set;








public final class PlayerActionComponent {
    public static final int START_SPRINTING = 9;
    public static final int STOP_SPRINTING = 10;
    public static final int START_SNEAKING = 11;
    public static final int STOP_SNEAKING = 12;
    public static final int START_GLIDING = 15;
    public static final int STOP_GLIDING = 16;
    public static final int START_SWIMMING = 21;
    public static final int STOP_SWIMMING = 22;
    public static final int START_CRAWLING = 32;
    public static final int STOP_CRAWLING = 33;

    private Set<AuthInputAction> actions = new HashSet<>();
    private long actionBits;
    private Vector2f interactRotation;

    
    public Set<AuthInputAction> actions() {
        return this.actions;
    }

    public void setActions(final Set<AuthInputAction> actions) {
        this.actions = actions == null ? new HashSet<>() : actions;
        rebuildStateActionBits();
    }

    
    public void rebuildStateActionBits() {
        this.actionBits = 0L;
        for (final AuthInputAction action : this.actions) {
            if (action == null) {
                continue;
            }
            final int bdsAction = toBdsStateAction(action.name());
            if (bdsAction >= 0) {
                this.actionBits |= 1L << bdsAction;
            }
        }
    }

    public long bits() {
        return this.actionBits;
    }

    public boolean has(final int action) {
        checkAction(action);
        return (this.actionBits & (1L << action)) != 0L;
    }

    public void set(final int action) {
        checkAction(action);
        this.actionBits |= 1L << action;
    }

    public void set(final int action, final boolean value) {
        if (value) {
            set(action);
        } else {
            clear(action);
        }
    }

    public void clear(final int action) {
        checkAction(action);
        this.actionBits &= ~(1L << action);
    }

    public Vector2f interactRotation() {
        return this.interactRotation == null ? null
                : new Vector2f(this.interactRotation.getX(), this.interactRotation.getY());
    }

    public void setInteractRotation(final Vector2f value) {
        this.interactRotation = value == null ? null
                : new Vector2f(value.getX(), value.getY());
    }

    public void clear() {
        this.actions.clear();
        this.actionBits = 0L;
        this.interactRotation = null;
    }

    private static int toBdsStateAction(final String networkName) {
        return switch (networkName) {
            case "START_SPRINTING" -> START_SPRINTING;
            case "STOP_SPRINTING" -> STOP_SPRINTING;
            case "START_SNEAKING" -> START_SNEAKING;
            case "STOP_SNEAKING" -> STOP_SNEAKING;
            case "START_GLIDING" -> START_GLIDING;
            case "STOP_GLIDING" -> STOP_GLIDING;
            case "START_SWIMMING" -> START_SWIMMING;
            case "STOP_SWIMMING" -> STOP_SWIMMING;
            case "START_CRAWLING" -> START_CRAWLING;
            case "STOP_CRAWLING" -> STOP_CRAWLING;
            default -> -1;
        };
    }

    private static void checkAction(final int action) {
        if (action < 0 || action >= Long.SIZE) {
            throw new IllegalArgumentException("Player action must be in [0, 63]: " + action);
        }
    }
}
