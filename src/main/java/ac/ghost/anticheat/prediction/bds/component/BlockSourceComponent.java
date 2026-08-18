package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.prediction.bds.world.BlockSource;


public record BlockSourceComponent(BlockSource value) {
    public BlockSourceComponent {
        if (value == null) {
            throw new IllegalArgumentException("BlockSource cannot be null");
        }
    }
}
