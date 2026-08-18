package ac.ghost.anticheat.check.api.holder;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.Check;
import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.check.impl.badpackets.BadPacketA;
import ac.ghost.anticheat.check.impl.badpackets.BadPacketB;
import ac.ghost.anticheat.check.impl.elytra.*;
import ac.ghost.anticheat.check.impl.breaking.*;
import ac.ghost.anticheat.check.impl.hitboxes.Hitboxes;
import ac.ghost.anticheat.check.impl.movement.NoSlow;
import ac.ghost.anticheat.check.impl.multiactions.MultiActionsA;
import ac.ghost.anticheat.check.impl.multiactions.MultiActionsB;
import ac.ghost.anticheat.check.impl.multiactions.MultiActionsC;
import ac.ghost.anticheat.check.impl.multiactions.MultiActionsD;
import ac.ghost.anticheat.check.impl.multiactions.MultiActionsE;
import ac.ghost.anticheat.check.impl.prediction.AntiKB;
import ac.ghost.anticheat.check.impl.prediction.Collisions;
import ac.ghost.anticheat.check.impl.prediction.DebugOffsetA;
import ac.ghost.anticheat.check.impl.prediction.Phase;
import ac.ghost.anticheat.check.impl.prediction.Prediction;
import ac.ghost.anticheat.check.impl.reach.Reach;
import ac.ghost.anticheat.check.impl.scaffolding.FarPlace;
import ac.ghost.anticheat.check.impl.timer.Timer;
import ac.ghost.anticheat.player.GhostPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class CheckHolder extends LinkedHashMap<Class<?>, Check> {
    private final GhostPlayer player;

    public CheckHolder(final GhostPlayer player) {
        this.player = player;
        reload();
    }

    





    public void reload() {
        
        
        player.entityContext.serverPlayerMovementSyncComponent.finishCorrectionTick();
        player.entityContext.serverPlayerMovementSyncComponent.clearCorrectionState();
        clear();

        register(Reach.class, () -> new Reach(player));
        register(Hitboxes.class, () -> new Hitboxes(player));
        register(Timer.class, () -> new Timer(player));

        register(DebugOffsetA.class, () -> new DebugOffsetA(player));
        register(Phase.class, () -> new Phase(player));
        register(Collisions.class, () -> new Collisions(player));
        register(NoSlow.class, () -> new NoSlow(player));
        register(Prediction.class, () -> new Prediction(player));
        register(AntiKB.class, () -> new AntiKB(player));

        register(BadPacketA.class, () -> new BadPacketA(player));
        register(BadPacketB.class, () -> new BadPacketB(player));
        
        
        register(InvalidBreak.class, () -> new InvalidBreak(player));
        register(AirLiquidBreak.class, () -> new AirLiquidBreak(player));
        register(FarBreak.class, () -> new FarBreak(player));
        register(PositionBreak.class, () -> new PositionBreak(player));
        register(FarPlace.class, () -> new FarPlace(player));

        register(ElytraA.class, () -> new ElytraA(player));
        register(ElytraB.class, () -> new ElytraB(player));
        register(ElytraC.class, () -> new ElytraC(player));
        register(ElytraD.class, () -> new ElytraD(player));
        register(ElytraE.class, () -> new ElytraE(player));
        register(ElytraF.class, () -> new ElytraF(player));
        register(ElytraG.class, () -> new ElytraG(player));

        register(MultiActionsA.class, () -> new MultiActionsA(player));
        register(MultiActionsB.class, () -> new MultiActionsB(player));
        register(MultiActionsC.class, () -> new MultiActionsC(player));
        register(MultiActionsD.class, () -> new MultiActionsD(player));
        register(MultiActionsE.class, () -> new MultiActionsE(player));

        
        
        register(WrongBreak.class, () -> new WrongBreak(player));
    }

    private <T extends Check> void register(
            final Class<T> key,
            final Supplier<? extends T> factory) {
        if (isDisabled(key)) {
            return;
        }
        super.put(key, factory.get());
    }

    



    public boolean isDisabled(final Class<? extends Check> key) {
        final CheckInfo info = key.getDeclaredAnnotation(CheckInfo.class);
        final String name = info == null ? key.getSimpleName() : info.name();
        final String type = info == null ? "" : info.type();
        final String configuredName = type.isEmpty() ? name : name + "-" + type;

        final List<String> candidates = new ArrayList<>();
        candidates.add(configuredName);
        candidates.add(key.getSimpleName());

        
        if (key == NoSlow.class) {
            candidates.add("NoSlow");
            candidates.add("Noslow (Prediction)");
        } else if (key == AntiKB.class) {
            candidates.add("AntiKB");
            candidates.add("Knockback");
        }

        for (final String configured : Ghost.getConfig().disabledChecks()) {
            final String normalizedConfigured = normalize(configured);
            if (normalizedConfigured.isEmpty()) {
                continue;
            }

            for (final String candidate : candidates) {
                if (normalizedConfigured.equals(normalize(candidate))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String normalize(final String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public void manuallyFail(Class<?> klass) {
        this.manuallyFail(klass, "");
    }

    public void manuallyFail(Class<?> klass, String verbose) {
        Check check = this.get(klass);
        if (check != null) {
            check.fail(verbose);
        }
    }

    public List<PacketCheck> packetChecks() {
        final List<PacketCheck> checks = new ArrayList<>();
        for (Check check : this.values()) {
            if (check instanceof PacketCheck packetCheck) {
                checks.add(packetCheck);
            }
        }
        return checks;
    }

    



    @Override
    public Check put(final Class<?> key, final Check value) {
        if (key != null
                && Check.class.isAssignableFrom(key)
                && isDisabled(key.asSubclass(Check.class))) {
            return null;
        }
        return super.put(key, value);
    }
}
