package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;


public final class UpdateDepenetration {
    private static final int ACTOR_DATA_FLAG_BYTE_D_BIT_20 = 109;
    private UpdateDepenetration() {}
    public static void run(final GhostPlayer player) {
        int flags = player.entityContext.depenetrationComponent.flags();
        if (player.entityContext.actorDataFlagComponent.has(ACTOR_DATA_FLAG_BYTE_D_BIT_20)) flags |= 0x10;
        else flags &= ~0x10;

        if (player.entityContext.moveRequestComponent.collisionResponse()) {
            if ((flags & 0x02) == 0) flags |= 0x02;
            else flags |= 0x04;
        } else {
            flags &= 0x19;
        }
        player.entityContext.depenetrationComponent.setFlags(flags);
        player.entityContext.depenetrationComponent.clearCustomMagnitude();
        player.entityContext.customDepenetrationMagnitudeComponent.clear();
    }
}
