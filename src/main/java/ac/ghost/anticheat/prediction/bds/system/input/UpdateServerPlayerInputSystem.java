package ac.ghost.anticheat.prediction.bds.system.input;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;
import ac.ghost.anticheat.prediction.nukkit.data.ReplayableActorInput;
import ac.ghost.anticheat.prediction.bds.system.player.FlyTriggerActionSystem;
import ac.ghost.anticheat.prediction.bds.system.player.FlyTriggerIntentSystem;
import ac.ghost.anticheat.prediction.bds.system.player.ActionSprintTriggerSystem;
import ac.ghost.anticheat.prediction.bds.system.player.IntentSprintTriggerSystem;
import ac.ghost.anticheat.prediction.bds.system.player.ProcessRequestAbilitiesSystem;
import ac.ghost.anticheat.prediction.bds.system.player.RemovePermissionFlyFlagSystem;
import ac.ghost.anticheat.prediction.bds.system.player.SetRequestSprintTriggerSystem;
import ac.ghost.anticheat.prediction.bds.system.player.UpdateAbilitiesSystem;
import ac.ghost.anticheat.prediction.bds.system.item.ItemUseSlowdownApplySystem;
import ac.ghost.anticheat.prediction.bds.system.item.ItemUseSlowdownClearSystem;
import ac.ghost.anticheat.prediction.bds.system.item.ItemUseSlowdownSystem;
import ac.ghost.anticheat.prediction.bds.system.item.ItemUseTickDurationMovementSystem;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitPlayerActionDispatchSystem;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitSneakInputNormalizationSystem;
import ac.ghost.anticheat.util.InputUtil;
import ac.ghost.anticheat.util.ClientDeviceUtil;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.AuthInputAction;

import java.util.HashSet;
import java.util.Set;




public final class UpdateServerPlayerInputSystem {
    private UpdateServerPlayerInputSystem() {
    }

    public static void tick(final GhostPlayer player,
                            final ReplayableActorInput replayableInput,
                            final PlayerAuthInputPacket networkPacket) {
        final PlayerAuthInputPacket packet = replayableInput.packet();

        
        player.ghostMovementBridgeState.wasPredictionSwimming = player.entityContext.actorDataFlagComponent.has(ActorDataFlag.SWIMMING);
        player.ghostMovementBridgeState.wasPredictionCrawling = player.entityContext.actorDataFlagComponent
                .has(ActorDataFlag.CRAWLING);

        
        
        
        
        
        player.entityContext.playerFlyingTravelComponent.setPresent(
                player.entityContext.movementAbilitiesComponent.isFlying());

        player.entityContext.playerActionComponent.setActions(new HashSet<>(packet.getInputData()));

        
        
        
        
        final boolean startFlying = player.entityContext.playerActionComponent.actions()
                .contains(AuthInputAction.START_FLYING);
        final boolean stopFlying = player.entityContext.playerActionComponent.actions()
                .contains(AuthInputAction.STOP_FLYING);
        FlyTriggerIntentSystem.tick(player);
        FlyTriggerActionSystem.tick(player, startFlying, stopFlying);
        RemovePermissionFlyFlagSystem.tick(player);
        ProcessRequestAbilitiesSystem.tick(player);
        UpdateAbilitiesSystem.tick(player);

        
        
        NukkitSneakInputNormalizationSystem.tick(player, packet.getInputMode());
        updateMoveInput(player, packet);

        player.entityContext.actorRotationComponent.set(
                packet.getPitch(), packet.getYaw(), packet.getHeadYaw());

        final Vector2f interactRotation = InputUtil.hasInteractRotation(player,
                packet) ? packet.getInteractRotation() : null;
        player.entityContext.playerActionComponent.setInteractRotation(interactRotation);
        player.entityContext.playerInputModeComponent.set(packet.getInputMode());

        SetRequestSprintTriggerSystem.tick(player);
        IntentSprintTriggerSystem.tick(player);
        ActionSprintTriggerSystem.tick(player);
        NukkitPlayerActionDispatchSystem.tick(player);

        NukkitItemUseStateSystem.snapshotPredictionUseState(player);
        ItemUseSlowdownClearSystem.tick(player);
        ItemUseSlowdownSystem.tick(player);
        ItemUseSlowdownApplySystem.tick(player);
        ItemUseTickDurationMovementSystem.tick(player);

        
        
        replayableInput.writeInputDataTo(networkPacket, player.entityContext.playerActionComponent.actions());
    }

    private static boolean updateMoveInput(final GhostPlayer player,
                                           final PlayerAuthInputPacket packet) {
        final Vector2f raw = getRawMoveVector(player, packet);
        final MoveInputComponent moveInput = player.entityContext.moveInputComponent;
        moveInput.clearForPacket();
        encodeActions(packet.getInputData(), moveInput);

        if (raw == null) {
            







            final boolean mouse = packet.getInputMode()
                    == cn.nukkit.network.protocol.types.InputMode.MOUSE
                    && ClientDeviceUtil.canTrustMouseAsDigital(player);
            final boolean inputModeMissing = packet.getInputMode() == null
                    || packet.getInputMode()
                    == cn.nukkit.network.protocol.types.InputMode.UNDEFINED;
            final boolean hasDigitalDirection =
                    (moveInput.getFlags() & digitalDirectionMask()) != 0;
            if (player.ghostMovementBridgeState
                    .predictionHasDigitalDirectionState
                    && (mouse
                    || inputModeMissing && hasDigitalDirection
                    && ClientDeviceUtil.canTrustMouseAsDigital(player))) {
                player.entityContext.clientInputLockComponent.applyTo(moveInput);
                player.entityContext.mobTravelComponent.setInput(
                        moveInput.resolveMovementVector());
                SneakingSystem.tick(player);
                ServerMoveInputHandlerSystemUtils
                        ._tickServerMoveInputHandler(player);
                StorePreviousClientInputSystem.tick(moveInput);
                return true;
            }

            player.entityContext.mobTravelComponent.setInput(Vec3.ZERO.clone());
            return false;
        }

        final float[] normalized = normalizePacketAxis(raw.getX(), raw.getY());
        moveInput.setAxisX(normalized[0]);
        moveInput.setAxisY(normalized[1]);

        player.entityContext.clientInputLockComponent.applyTo(moveInput);
        player.entityContext.mobTravelComponent.setInput(player.entityContext.moveInputComponent.resolveMovementVector());

        
        
        
        SneakingSystem.tick(player);
        ServerMoveInputHandlerSystemUtils._tickServerMoveInputHandler(player);
        StorePreviousClientInputSystem.tick(moveInput);
        return true;
    }

    private static int digitalDirectionMask() {
        return MoveInputComponent.UP
                | MoveInputComponent.DOWN
                | MoveInputComponent.LEFT
                | MoveInputComponent.RIGHT
                | MoveInputComponent.UP_LEFT
                | MoveInputComponent.UP_RIGHT
                | MoveInputComponent.DOWN_LEFT
                | MoveInputComponent.DOWN_RIGHT;
    }

    private static Vector2f getRawMoveVector(final GhostPlayer player,
                                             final PlayerAuthInputPacket packet) {
        if (player.getSession().protocol < ProtocolInfo.v1_21_50) {
            return null;
        }
        return packet.getRawMoveVector();
    }

    private static float[] normalizePacketAxis(float x, float y) {
        if (Float.isNaN(x) || Float.isNaN(y)) {
            return new float[]{0.0F, 0.0F};
        }
        final float len2 = x * x + y * y;
        if (len2 > 1.0F) {
            final float inv = 1.0F / (float) Math.sqrt(len2);
            x *= inv;
            y *= inv;
        }
        return new float[]{x, y};
    }

    private static void encodeActions(final Set<AuthInputAction> actions,
                                      final MoveInputComponent input) {
        add(actions, input, "ASCEND", MoveInputComponent.ASCEND);
        add(actions, input, "DESCEND", MoveInputComponent.DESCEND);
        add(actions, input, "JUMP_DOWN", MoveInputComponent.JUMP_DOWN);
        add(actions, input, "SPRINT_DOWN", MoveInputComponent.SPRINT_DOWN);
        add(actions, input, "CHANGE_HEIGHT", MoveInputComponent.CHANGE_HEIGHT);
        add(actions, input, "SNEAK_DOWN", MoveInputComponent.SNEAK_DOWN);
        add(actions, input, "UP", MoveInputComponent.UP);
        add(actions, input, "DOWN", MoveInputComponent.DOWN);
        add(actions, input, "LEFT", MoveInputComponent.LEFT);
        add(actions, input, "RIGHT", MoveInputComponent.RIGHT);
        add(actions, input, "UP_LEFT", MoveInputComponent.UP_LEFT);
        add(actions, input, "UP_RIGHT", MoveInputComponent.UP_RIGHT);
        add(actions, input, "WANT_DOWN_SLOW", MoveInputComponent.WANT_DOWN_SLOW);
        add(actions, input, "WANT_UP_SLOW", MoveInputComponent.WANT_UP_SLOW);
        add(actions, input, "ASCEND_BLOCK", MoveInputComponent.ASCEND_BLOCK);
        add(actions, input, "DESCEND_BLOCK", MoveInputComponent.DESCEND_BLOCK);
        add(actions, input, "SNEAK_TOGGLE_DOWN", MoveInputComponent.SNEAK_TOGGLE_DOWN);
        add(actions, input, "DOWN_LEFT", MoveInputComponent.DOWN_LEFT);
        add(actions, input, "DOWN_RIGHT", MoveInputComponent.DOWN_RIGHT);

        addState(actions, input, "SNEAKING", MoveInputComponent.STATE_SNEAKING);
        addState(actions, input, "SPRINTING", MoveInputComponent.STATE_SPRINTING);
        addState(actions, input, "WANT_UP", MoveInputComponent.STATE_WANT_UP);
        addState(actions, input, "WANT_DOWN", MoveInputComponent.STATE_WANT_DOWN);
        addState(actions, input, "JUMPING", MoveInputComponent.STATE_JUMPING);
        addState(actions, input, "AUTO_JUMPING_IN_WATER",
                MoveInputComponent.STATE_AUTO_JUMPING_IN_WATER);
        addState(actions, input, "PERSIST_SNEAK", MoveInputComponent.STATE_PERSIST_SNEAK);

        input.setPaddlingLeft(hasAction(actions, "PADDLING_LEFT"));
        input.setPaddlingRight(hasAction(actions, "PADDLING_RIGHT"));
    }

    private static void add(final Set<AuthInputAction> actions,
                            final MoveInputComponent input,
                            final String name,
                            final int flag) {
        if (hasAction(actions, name)) {
            input.addFlag(flag);
        }
    }

    private static void addState(final Set<AuthInputAction> actions,
                                 final MoveInputComponent input,
                                 final String name,
                                 final int flag) {
        if (hasAction(actions, name)) {
            input.addStateFlag(flag);
        }
    }

    private static boolean hasAction(final Set<AuthInputAction> actions,
                                     final String name) {
        for (final AuthInputAction action : actions) {
            if (action != null && action.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
