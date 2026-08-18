package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.data.vanilla.AttributeInstance;
import cn.nukkit.Player;
import cn.nukkit.entity.Attribute;

import java.util.HashMap;
import java.util.Map;






public final class AttributesComponent {
    public static final String SPRINTING_SPEED_BOOST_ID =
            "D208FC00-42AA-4AAD-9276-D5446530DE43";

    private static final float ATTRIBUTE_MAX_VALUE =
            Float.intBitsToFloat(0x7F7FFFFF);
    private static final AttributeInstance.AttributeModifierData SPRINTING_SPEED_BOOST =
            new AttributeInstance.AttributeModifierData(
                    SPRINTING_SPEED_BOOST_ID,
                    AttributeInstance.AttributeOperation.MULTIPLY_TOTAL,
                    AttributeInstance.AttributeOperand.CURRENT,
                    Float.intBitsToFloat(0x3E99999A));

    private final Map<String, AttributeInstance> baseAttributeMap = new HashMap<>();

    public AttributesComponent() {
        initializePlayerMovementAttributes();
    }

    public Map<String, AttributeInstance> baseAttributeMap() {
        return this.baseAttributeMap;
    }

    public Map<String, AttributeInstance> copyBaseAttributeMap() {
        final Map<String, AttributeInstance> copy = new HashMap<>();
        this.baseAttributeMap.forEach((name, value) -> copy.put(name, value.clone()));
        return copy;
    }

    public AttributeInstance get(final String name) {
        return this.baseAttributeMap.get(name);
    }

    public void set(final String name, final float defaultValue, final float value) {
        final AttributeInstance instance = this.baseAttributeMap.computeIfAbsent(
                name, ignored -> new AttributeInstance(defaultValue));
        instance.clearModifiers();
        instance.setBaseValue(defaultValue);
        instance.setValue(value);
    }

    public void initializePlayerMovementAttributes() {
        this.baseAttributeMap.put(movementSpeedName(), new AttributeInstance(
                0.0F, ATTRIBUTE_MAX_VALUE, Player.DEFAULT_SPEED));
        this.baseAttributeMap.put(underwaterMovementName(), new AttributeInstance(0.02F));
        this.baseAttributeMap.put(lavaMovementName(), new AttributeInstance(0.02F));
    }

    public float movementSpeed() {
        return require(movementSpeedName()).getValue();
    }

    public float underwaterMovementSpeed() {
        return require(underwaterMovementName()).getValue();
    }

    public float lavaMovementSpeed() {
        return require(lavaMovementName()).getValue();
    }

    public AttributeInstance movementSpeedAttribute() {
        return require(movementSpeedName());
    }

    public void applySprintingModifier(final boolean sprinting) {
        final AttributeInstance movementSpeed = movementSpeedAttribute();
        final AttributeInstance.AttributeModifierData current =
                movementSpeed.getModifier(SPRINTING_SPEED_BOOST_ID);
        if (sprinting) {
            if (!SPRINTING_SPEED_BOOST.equals(current)) {
                movementSpeed.removeModifier(SPRINTING_SPEED_BOOST_ID);
                movementSpeed.addTemporaryModifier(SPRINTING_SPEED_BOOST);
            }
        } else if (current != null) {
            movementSpeed.removeModifier(SPRINTING_SPEED_BOOST_ID);
        }
    }

    private AttributeInstance require(final String name) {
        final AttributeInstance value = this.baseAttributeMap.get(name);
        if (value == null) {
            throw new IllegalStateException("Missing BDS attribute: " + name);
        }
        return value;
    }

    private static String movementSpeedName() {
        return Attribute.getAttribute(Attribute.MOVEMENT_SPEED).getName();
    }

    private static String underwaterMovementName() {
        return Attribute.getAttribute(Attribute.UNDERWATER_MOVEMENT).getName();
    }

    private static String lavaMovementName() {
        return Attribute.getAttribute(Attribute.LAVA_MOVEMENT).getName();
    }
}
