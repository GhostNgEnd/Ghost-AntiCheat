package ac.ghost.anticheat.data.vanilla;

import java.util.LinkedHashMap;
import java.util.Map;








public final class AttributeInstance implements Cloneable {
    private float baseMinimum;
    private float baseMaximum;
    private float baseValue;

    private float minimum;
    private float maximum;
    private float value;
    private boolean dirty = true;

    
    private final Map<String, AttributeModifierData> modifiers = new LinkedHashMap<>();

    public AttributeInstance(final float baseValue) {
        this(-Float.MAX_VALUE, Float.MAX_VALUE, baseValue);
    }

    public AttributeInstance(final float baseMinimum,
                             final float baseMaximum,
                             final float baseValue) {
        this.baseMinimum = baseMinimum;
        this.baseMaximum = baseMaximum;
        this.baseValue = baseValue;
        this.minimum = baseMinimum;
        this.maximum = baseMaximum;
    }

    public float getBaseMinimum() {
        return baseMinimum;
    }

    public float getBaseMaximum() {
        return baseMaximum;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public float getMinimum() {
        ensureUpdated();
        return minimum;
    }

    public float getMaximum() {
        ensureUpdated();
        return maximum;
    }

    public float getValue() {
        ensureUpdated();
        return value;
    }

    public boolean isDirty() {
        return dirty;
    }

    public Map<String, AttributeModifierData> getModifiers() {
        return Map.copyOf(modifiers);
    }

    public AttributeModifierData getModifier(final String id) {
        return modifiers.get(id);
    }

    public void setBaseRange(final float minimum, final float maximum) {
        if (this.baseMinimum == minimum && this.baseMaximum == maximum) {
            return;
        }
        this.baseMinimum = minimum;
        this.baseMaximum = maximum;
        setDirty();
    }

    public void setBaseValue(final float baseValue) {
        if (this.baseValue == baseValue) {
            return;
        }
        this.baseValue = baseValue;
        setDirty();
    }

    



    public void setValue(final float value) {
        this.value = value;
        this.dirty = false;
    }

    public void clearModifiers() {
        if (this.modifiers.isEmpty()) {
            return;
        }
        this.modifiers.clear();
        update();
    }

    public void removeModifier(final String id) {
        if (this.modifiers.remove(id) != null) {
            update();
        }
    }

    public void addTemporaryModifier(final AttributeModifierData modifier) {
        addModifier(modifier);
    }

    private void addModifier(final AttributeModifierData modifier) {
        if (this.modifiers.putIfAbsent(modifier.id(), modifier) == null) {
            update();
        }
    }

    public void setDirty() {
        this.dirty = true;
    }

    private void ensureUpdated() {
        if (this.dirty) {
            update();
        }
    }

    private void update() {
        final ComputedValues computed = computeValues();
        this.minimum = computed.minimum();
        this.maximum = computed.maximum();
        this.value = computed.current();
        this.dirty = false;
    }

    private ComputedValues computeValues() {
        final float[] base = {baseMinimum, baseMaximum, baseValue};

        
        for (final AttributeModifierData modifier : modifiers.values()) {
            if (modifier.operation() == AttributeOperation.ADDITION) {
                base[modifier.operand().nativeIndex()] += modifier.amount();
            }
        }

        final float[] computed = {base[0], base[1], base[2]};

        
        for (final AttributeModifierData modifier : modifiers.values()) {
            if (modifier.operation() == AttributeOperation.MULTIPLY_BASE) {
                final int operand = modifier.operand().nativeIndex();
                computed[operand] += base[operand] * modifier.amount();
            }
        }

        
        for (final AttributeModifierData modifier : modifiers.values()) {
            if (modifier.operation() == AttributeOperation.MULTIPLY_TOTAL) {
                final int operand = modifier.operand().nativeIndex();
                computed[operand] *= 1.0F + modifier.amount();
            }
        }

        
        float upper = computed[AttributeOperand.MAXIMUM.nativeIndex()];
        for (final AttributeModifierData modifier : modifiers.values()) {
            if (modifier.operation() == AttributeOperation.CAP) {
                upper = Math.min(upper, modifier.amount());
            }
        }

        final float candidate = computed[AttributeOperand.CURRENT.nativeIndex()];
        final float lower = computed[AttributeOperand.MINIMUM.nativeIndex()];
        final float current = upper < candidate ? upper : Math.max(candidate, lower);
        return new ComputedValues(computed[0], computed[1], current);
    }

    @Override
    public AttributeInstance clone() {
        final AttributeInstance instance = new AttributeInstance(
                baseMinimum, baseMaximum, baseValue);
        instance.minimum = minimum;
        instance.maximum = maximum;
        instance.value = value;
        instance.dirty = dirty;
        instance.modifiers.putAll(modifiers);
        return instance;
    }

    public enum AttributeOperand {
        MINIMUM(0),
        MAXIMUM(1),
        CURRENT(2);

        private final int nativeIndex;

        AttributeOperand(final int nativeIndex) {
            this.nativeIndex = nativeIndex;
        }

        public int nativeIndex() {
            return nativeIndex;
        }
    }

    public enum AttributeOperation {
        ADDITION(0),
        MULTIPLY_BASE(1),
        MULTIPLY_TOTAL(2),
        CAP(3);

        private final int nativeId;

        AttributeOperation(final int nativeId) {
            this.nativeId = nativeId;
        }

        public int nativeId() {
            return nativeId;
        }
    }

    public record AttributeModifierData(String id,
                                        AttributeOperation operation,
                                        AttributeOperand operand,
                                        float amount) {
        public AttributeModifierData(final String id,
                                     final AttributeOperation operation,
                                     final float amount) {
            this(id, operation, AttributeOperand.CURRENT, amount);
        }
    }

    private record ComputedValues(float minimum, float maximum, float current) {
    }
}
