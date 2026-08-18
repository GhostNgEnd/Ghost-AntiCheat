package ac.ghost.anticheat.data;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.types.PlayerActionType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class BreakingData {
    private PlayerActionType state;
    private BlockVector3 position;
    private int face;

    private float breakingProcess;

    public BreakingData(PlayerActionType state, BlockVector3 position, int face) {
        this.state = state;
        this.position = position;
        this.face = face;
    }
}
