package ac.ghost.anticheat.prediction.bds.component;


public final class ItemUseSlowdownModifierComponent {
    private boolean present;
    private float value = 1.0F;

    public boolean isPresent() {
        return this.present;
    }

    public float getValue() {
        return this.value;
    }

    public void set(final float value) {
        this.present = true;
        this.value = value;
    }

    public void clear() {
        this.present = false;
        this.value = 1.0F;
    }
}
