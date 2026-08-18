package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.prediction.bds.world.BlockSource;
import ac.ghost.anticheat.prediction.bds.world.LocalConstBlockSource;


public final class LocalConstBlockSourceFactoryComponent {
    private final LocalConstBlockSource localConstBlockSource;

    public LocalConstBlockSourceFactoryComponent(final BlockSource blockSource) {
        this.localConstBlockSource = new LocalConstBlockSource(blockSource);
    }

    public LocalConstBlockSource create() {
        return this.localConstBlockSource;
    }
}
