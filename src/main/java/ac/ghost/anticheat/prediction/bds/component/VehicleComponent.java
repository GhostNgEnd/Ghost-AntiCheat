package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.player.data.VehicleData;


public final class VehicleComponent {
    public VehicleData value;
    public boolean isPresent() { return value != null; }
}
