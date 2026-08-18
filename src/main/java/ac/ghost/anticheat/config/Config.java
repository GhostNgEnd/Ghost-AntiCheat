package ac.ghost.anticheat.config;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


@ToString
public final class Config {
    public static final Config DEFAULT_CONFIG = new Config();

    private String prefix = "&bGhost &7> ";
    private transient String formattedPrefix;

    
    private int rewindHistory = 40;
    private int rewindMinCorrectionDelayTicks = 5;
    private float playerPositionAcceptanceThreshold = 0.5F;
    private float movementActionDirectionThreshold = 0.85F;
    private boolean serverAuthoritativeMovementStrict;
    private boolean serverAuthoritativeEntityInteractionsStrict;
    private boolean useCorrectPlayerMovePredictionPacket = true;

    
    private float predictionThreshold = 1.0E-4F;
    private int phaseSetbackVl = 1;
    private int collisionsSetbackVl = 1;
    private float knockbackThreshold = 1.0E-4F;
    private int knockbackSetbackVl = 1;
    private float noSlowThreshold = 1.0E-4F;
    private int noSlowSetbackVl = 1;
    private float toleranceReach = 3.005F;

    private List<String> disabledChecks = new ArrayList<>();
    private boolean ignoreGhostBlock;
    private long maxLatencyWait = 15000L;
    private long maxBalanceAdvantage = 8500L;
    private boolean debugMode;

    static Config fromNukkit(final cn.nukkit.utils.Config source) {
        final Config result = new Config();
        result.prefix = source.getString("prefix", result.prefix);

        
        
        result.rewindHistory = source.getInt(
                "player-rewind-history-size-ticks", result.rewindHistory);
        result.rewindMinCorrectionDelayTicks = source.getInt(
                "player-rewind-min-correction-delay-ticks",
                result.rewindMinCorrectionDelayTicks);
        result.playerPositionAcceptanceThreshold = (float) source.getDouble(
                "player-position-acceptance-threshold",
                result.playerPositionAcceptanceThreshold);
        result.movementActionDirectionThreshold = (float) source.getDouble(
                "player-movement-action-direction-threshold",
                result.movementActionDirectionThreshold);
        result.serverAuthoritativeMovementStrict = source.getBoolean(
                "server-authoritative-movement-strict",
                result.serverAuthoritativeMovementStrict);
        result.serverAuthoritativeEntityInteractionsStrict = source.getBoolean(
                "server-authoritative-entity-interactions-strict",
                result.serverAuthoritativeEntityInteractionsStrict);
        result.useCorrectPlayerMovePredictionPacket = source.getBoolean(
                "use-correct-player-move-prediction-packet",
                result.useCorrectPlayerMovePredictionPacket);

        result.predictionThreshold = getFloat(
                source, "Prediction.threshold", result.predictionThreshold);
        result.phaseSetbackVl = source.getInt(
                "Phase.setbackvl", result.phaseSetbackVl);
        result.collisionsSetbackVl = source.getInt(
                "Collisions.setbackvl", result.collisionsSetbackVl);
        result.knockbackThreshold = getFloat(
                source, "Knockback.threshold", result.knockbackThreshold);
        result.knockbackSetbackVl = source.getInt(
                "Knockback.setbackvl", result.knockbackSetbackVl);
        result.noSlowThreshold = getFloat(
                source, "NoSlow.threshold", result.noSlowThreshold);
        result.noSlowSetbackVl = source.getInt(
                "NoSlow.setbackvl", result.noSlowSetbackVl);
        result.toleranceReach = getFloat(
                source,
                "Reach.max-tolerance-compensated-reach",
                result.toleranceReach);

        result.disabledChecks = new ArrayList<>(source.getStringList("disabled-checks"));
        result.ignoreGhostBlock = source.getBoolean(
                "ignore-ghost-block", result.ignoreGhostBlock);
        result.maxLatencyWait = source.getLong(
                "max-latency-wait", result.maxLatencyWait);
        result.maxBalanceAdvantage = source.getLong(
                "max-balance-advantage", result.maxBalanceAdvantage);
        result.debugMode = source.getBoolean("debug-mode", result.debugMode);

        return result;
    }

    private static float getFloat(
            final cn.nukkit.utils.Config source,
            final String key,
            final float fallback) {
        if (!source.exists(key)) {
            return fallback;
        }

        final Object value = source.get(key);
        if (value instanceof Number number) {
            return number.floatValue();
        }
        if (value instanceof String text) {
            try {
                return Float.parseFloat(text.trim());
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public String prefix() {
        return prefix;
    }

    public String formattedPrefix() {
        if (formattedPrefix == null) {
            formattedPrefix = prefix == null ? "" : prefix.replace('&', '§');
        }
        return formattedPrefix;
    }

    public int rewindHistory() {
        return Math.max(1, rewindHistory);
    }

    public int rewindMinCorrectionDelayTicks() {
        return Math.max(0, Math.min(255, rewindMinCorrectionDelayTicks));
    }

    public float playerPositionAcceptanceThreshold() {
        return finiteNonNegative(playerPositionAcceptanceThreshold, 0.5F);
    }

    public float movementActionDirectionThreshold() {
        if (!Float.isFinite(movementActionDirectionThreshold)) {
            return 0.85F;
        }
        return Math.max(0.0F, Math.min(1.0F, movementActionDirectionThreshold));
    }

    public boolean serverAuthoritativeMovementStrict() {
        return serverAuthoritativeMovementStrict;
    }

    public boolean serverAuthoritativeEntityInteractionsStrict() {
        return serverAuthoritativeEntityInteractionsStrict;
    }

    public boolean useCorrectPlayerMovePredictionPacket() {
        return useCorrectPlayerMovePredictionPacket;
    }

    public float predictionThreshold() {
        return finiteNonNegative(predictionThreshold, 1.0E-4F);
    }


    public int phaseSetbackVl() {
        return positiveVl(phaseSetbackVl);
    }

    public int collisionsSetbackVl() {
        return positiveVl(collisionsSetbackVl);
    }

    public float knockbackThreshold() {
        return finiteNonNegative(knockbackThreshold, 1.0E-4F);
    }

    public int knockbackSetbackVl() {
        return positiveVl(knockbackSetbackVl);
    }

    public float noSlowThreshold() {
        return finiteNonNegative(noSlowThreshold, 1.0E-4F);
    }

    public int noSlowSetbackVl() {
        return positiveVl(noSlowSetbackVl);
    }

    public float toleranceReach() {
        if (!Float.isFinite(toleranceReach)) {
            return 3.005F;
        }
        return Math.max(3.0001F, toleranceReach);
    }

    public List<String> disabledChecks() {
        return disabledChecks;
    }

    public boolean ignoreGhostBlock() {
        return ignoreGhostBlock;
    }

    public long maxLatencyWait() {
        return Math.max(0L, maxLatencyWait);
    }

    public long maxBalanceAdvantage() {
        return maxBalanceAdvantage;
    }

    public boolean debugMode() {
        return debugMode;
    }

    private static float finiteNonNegative(final float value, final float fallback) {
        return Float.isFinite(value) ? Math.max(0.0F, value) : fallback;
    }

    private static int positiveVl(final int value) {
        return Math.max(1, value);
    }
}
