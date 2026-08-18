package ac.ghost.anticheat.check.impl.prediction;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.alert.AlertManager;
import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;

import java.util.ArrayList;

@CheckInfo(name = "DebugOffset")
public class DebugOffsetA extends OffsetHandlerCheck {
    public DebugOffsetA(GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(float offset) {
        if (player.getTrackedDebugPlayers().isEmpty()) {
            return;
        }

        final AlertManager alertManager = Ghost.getInstance().getAlertManager();

        final float maxOffset = ac.ghost.anticheat.Ghost.getConfig().predictionThreshold();
        float eotOffset = player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedTickEnd().distanceTo(player.entityContext.stateVectorComponent.getDelta());

        Vec3 predicted = player.entityContext.stateVectorComponent.getPosition().subtract(player.entityContext.serverPlayerCurrentMovementComponent.getPreviousUnvalidatedPosition());
        Vec3 actual = player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedPosition().subtract(player.entityContext.serverPlayerCurrentMovementComponent.getPreviousUnvalidatedPosition());
        if (actual.length() > 1e-5 || offset > maxOffset || eotOffset > maxOffset) {
            String colorOffset = offset > maxOffset ? "§c" : offset > 1.0E-5 ? "§6" : "§a";
            if (player.entityContext.stateVectorComponent.getPosition().distanceTo(player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedPosition()) > maxOffset && offset < maxOffset) {
                colorOffset = "§7";
            }

            String predDebug = colorOffset + "O:" + offset + ", P: " + predicted.x + "," + predicted.y + "," + predicted.z + ", pos=" + player.entityContext.stateVectorComponent.getPosition();
            alertManager.alertToPlayers(new ArrayList<CommandSender>(player.getTrackedDebugPlayers().values()), predDebug);
            alertManager.alertToPlayers(new ArrayList<CommandSender>(player.getTrackedDebugPlayers().values()), colorOffset + "A: " + actual.x + "," + actual.y + "," + actual.z + ", " + "SPRINTING=" + player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_SPRINTING) + ", SNEAKING=" + player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_SNEAKING) + ", water=" + player.ghostMovementBridgeState.waterSample.touching());
            alertManager.alertToPlayers(new ArrayList<CommandSender>(player.getTrackedDebugPlayers().values()), "A EOT: " + player.entityContext.stateVectorComponent.getDelta().toVector3f().toString());
            alertManager.alertToPlayers(new ArrayList<CommandSender>(player.getTrackedDebugPlayers().values()), "EOT O: " + (eotOffset > 1e-4 ? "§b" : "§a") + eotOffset + "," + player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedTickEnd().toVector3f().toString());
        }
    }
}
