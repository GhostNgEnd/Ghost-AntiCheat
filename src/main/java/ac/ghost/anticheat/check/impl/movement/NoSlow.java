package ac.ghost.anticheat.check.impl.movement;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.impl.prediction.PredictionOffsetCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;
import ac.ghost.anticheat.util.ItemUtil;
import cn.nukkit.item.Item;


@CheckInfo(name = "Noslow (Prediction)")
public final class NoSlow extends PredictionOffsetCheck {
    public NoSlow(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final float offset) {
        if (isServerVelocityTick()
                || !isNoSlowSample(player)
                || !canCheck(offset, Ghost.getConfig().noSlowThreshold())) {
            return;
        }

        
        
        
        
        NukkitItemUseStateSystem.armNoSlowConsumeRollback(player);

        failPredictionWithSetback(
                "Noslow (Prediction)",
                offset,
                Ghost.getConfig().noSlowSetbackVl());
    }

    





    public static boolean isNoSlowSample(final GhostPlayer player) {
        if (!NukkitItemUseStateSystem.isPredictionUsingItem(player)
                || !player.entityContext.itemInUseComponent.isPresent()) {
            return false;
        }

        final Item item = player.entityContext.itemInUseComponent.getItem();
        final float modifier = ItemUtil.itemUseSlowdownModifier(player, item);
        return Float.isFinite(modifier) && modifier < 1.0F;
    }
}
