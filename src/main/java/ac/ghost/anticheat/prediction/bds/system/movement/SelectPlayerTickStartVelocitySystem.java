package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.collision.bds.system.MovementCollisionPreviewSystem;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MobTravelComponent;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;
import ac.ghost.anticheat.prediction.bds.component.MovementSpeedComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerFlyingTravelComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerTickStartVelocityComponent;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.prediction.bds.player.PlayerMovement;
import ac.ghost.anticheat.prediction.bds.system.block.ScaffoldingActionSystem;
import ac.ghost.anticheat.prediction.bds.system.glide.GlideMoveSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.common.LiquidPhysicsSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.lava.LavaTravelSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.water.SwimControlSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.water.WaterSinkInputSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.water.WaterTravelSystem;
import ac.ghost.anticheat.prediction.bds.system.travel.DefaultMoveSystems;
import ac.ghost.anticheat.prediction.bds.system.travel.FlyingPlayerStuckOnGroundWorkaroundSystem;
import ac.ghost.anticheat.prediction.bds.system.travel.GroundTravelTypeSystem;
import ac.ghost.anticheat.prediction.bds.system.travel.MobJumpSystem;
import ac.ghost.anticheat.prediction.bds.system.travel.MobTravelIntentSystem;
import ac.ghost.anticheat.prediction.bds.system.travel.MobTravelUpdateSpeedsSystem;
import ac.ghost.anticheat.prediction.bds.system.travel.PlayerFlyingMoveSpeed;
import ac.ghost.anticheat.prediction.bds.system.travel.VerticalFlySpeedControlSystem;
import ac.ghost.anticheat.util.math.Vec3;
import ac.ghost.anticheat.util.ClientDeviceUtil;
import cn.nukkit.entity.Entity;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.types.InputMode;

import java.util.ArrayList;
import java.util.List;












public final class SelectPlayerTickStartVelocitySystem {
    private static final float DIAGONAL = 0.70710677F;
    private static final float INPUT_LIMIT_SQUARED = 1.0F;
    private static final float SOLVER_EPSILON = 1.0E-8F;
    private static final float SOLVED_POSITION_EPSILON_SQUARED = 1.0E-10F;
    private static final float[] ANALOG_REFINEMENT_STEPS = {
            0.5F, 0.25F, 0.125F, 0.0625F, 0.03125F
    };

    private static final List<Vec3> DIGITAL_INPUTS = List.of(
            new Vec3(0.0F, 0.0F, 0.0F),
            new Vec3(1.0F, 0.0F, 0.0F),
            new Vec3(-1.0F, 0.0F, 0.0F),
            new Vec3(0.0F, 0.0F, 1.0F),
            new Vec3(0.0F, 0.0F, -1.0F),
            new Vec3(DIAGONAL, 0.0F, DIAGONAL),
            new Vec3(DIAGONAL, 0.0F, -DIAGONAL),
            new Vec3(-DIAGONAL, 0.0F, DIAGONAL),
            new Vec3(-DIAGONAL, 0.0F, -DIAGONAL));

    private SelectPlayerTickStartVelocitySystem() {
    }

    public static void tick(
            final GhostPlayer player,
            final PlayerTickStartVelocityComponent.Candidates candidates) {
        final InputSearch inputSearch = inputSearch(player);
        if (inputSearch == InputSearch.EXACT && !candidates.ambiguous()) {
            return;
        }

        final Vec3 ordinaryPrepared =
                player.entityContext.stateVectorComponent.getDelta().clone();
        final BranchPreview ordinary = bestInputForVelocity(
                player, candidates.ordinary(), ordinaryPrepared, inputSearch);

        final BranchPreview uncertain;
        if (candidates.uncertain() == null) {
            uncertain = null;
        } else {
            final Vec3 uncertainPrepared = prepareUncertain(
                    player, candidates.uncertain().velocity());
            uncertain = bestInputForVelocity(
                    player, candidates.uncertain(), uncertainPrepared,
                    inputSearch);
            player.entityContext.playerTickStartVelocityComponent
                    .recordComparison(uncertain.preview().distanceSquared(),
                            ordinary.preview().distanceSquared());
        }

        final BranchPreview selected = uncertain != null
                && compare(uncertain.preview(), ordinary.preview()) < 0
                ? uncertain : ordinary;

        player.entityContext.playerTickStartVelocityComponent.finish(
                candidates, selected.preview().candidate());
        player.entityContext.stateVectorComponent.setDelta(
                selected.preview().preparedVelocity().clone());
        applySelectedInput(player, selected.input(), inputSearch);
    }

    private static InputSearch inputSearch(final GhostPlayer player) {
        if (player.ghostMovementBridgeState.predictionHasRawMoveVector) {
            return InputSearch.EXACT;
        }

        final InputMode mode = player.entityContext.playerInputModeComponent
                .getProtocolValue();
        final boolean hasDigitalState = player.ghostMovementBridgeState
                .predictionHasDigitalDirectionState;
        final boolean hasPressedDigitalDirection =
                (player.entityContext.moveInputComponent.getFlags()
                        & digitalDirectionMask()) != 0;
        final boolean trustedDigitalDevice =
                ClientDeviceUtil.canTrustMouseAsDigital(player);

        
        
        
        if (hasDigitalState
                && trustedDigitalDevice
                && (mode == InputMode.MOUSE
                || mode == InputMode.UNDEFINED
                && hasPressedDigitalDirection)) {
            return InputSearch.EXACT;
        }

        if (mode == InputMode.MOUSE) {
            return trustedDigitalDevice
                    ? InputSearch.DIGITAL : InputSearch.ANALOG;
        }
        if (mode == InputMode.TOUCH
                || mode == InputMode.GAME_PAD
                || mode == InputMode.MOTION_CONTROLLER) {
            return InputSearch.ANALOG;
        }

        
        
        return InputSearch.ANALOG;
    }

    private static BranchPreview bestInputForVelocity(
            final GhostPlayer player,
            final PlayerTickStartVelocityComponent.Candidate candidate,
            final Vec3 preparedVelocity,
            final InputSearch inputSearch) {
        if (inputSearch == InputSearch.EXACT) {
            final Vec3 current = player.entityContext.mobTravelComponent
                    .getInput().clone();
            final InputCandidate input = new InputCandidate(
                    new Vec3(player.entityContext.moveInputComponent.getAxisX(),
                            0.0F,
                            player.entityContext.moveInputComponent.getAxisY()),
                    current);
            return evaluate(player, candidate, preparedVelocity, input);
        }

        final List<BranchPreview> initial = new ArrayList<>(
                DIGITAL_INPUTS.size());
        BranchPreview best = null;
        for (final Vec3 raw : DIGITAL_INPUTS) {
            final BranchPreview evaluated = evaluate(player, candidate,
                    preparedVelocity, candidateInput(player, raw));
            initial.add(evaluated);
            best = better(best, evaluated);
        }

        if (inputSearch == InputSearch.DIGITAL) {
            return best;
        }
        if (best.preview().distanceSquared()
                <= SOLVED_POSITION_EPSILON_SQUARED) {
            return best;
        }

        
        
        
        
        final Vec3 solved = solveLinearInput(player, initial);
        if (solved != null) {
            best = better(best, evaluate(player, candidate, preparedVelocity,
                    candidateInput(player, solved)));
        }

        for (final float step : ANALOG_REFINEMENT_STEPS) {
            if (best.preview().distanceSquared()
                    <= SOLVED_POSITION_EPSILON_SQUARED) {
                break;
            }
            final Vec3 centre = best.input().raw();
            BranchPreview refined = best;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }
                    final Vec3 raw = projectToUnitDisc(new Vec3(
                            centre.x + x * step, 0.0F,
                            centre.z + z * step));
                    refined = better(refined, evaluate(player, candidate,
                            preparedVelocity, candidateInput(player, raw)));
                }
            }
            best = refined;
        }
        return best;
    }

    private static Vec3 solveLinearInput(
            final GhostPlayer player,
            final List<BranchPreview> initial) {
        if (initial.size() < 4) {
            return null;
        }

        final Vec3 p0 = initial.get(0).preview().predictedPosition();
        final Vec3 px = initial.get(1).preview().predictedPosition();
        final Vec3 pz = initial.get(3).preview().predictedPosition();
        final Vec3 target = player.entityContext
                .serverPlayerCurrentMovementComponent
                .getUnvalidatedPosition();

        final float ax = px.x - p0.x;
        final float az = px.z - p0.z;
        final float bx = pz.x - p0.x;
        final float bz = pz.z - p0.z;
        final float tx = target.x - p0.x;
        final float tz = target.z - p0.z;
        final float determinant = ax * bz - az * bx;
        if (!Float.isFinite(determinant)
                || Math.abs(determinant) <= SOLVER_EPSILON) {
            return null;
        }

        final float x = (tx * bz - tz * bx) / determinant;
        final float z = (ax * tz - az * tx) / determinant;
        if (!Float.isFinite(x) || !Float.isFinite(z)) {
            return null;
        }
        return projectToUnitDisc(new Vec3(x, 0.0F, z));
    }

    private static InputCandidate candidateInput(final GhostPlayer player,
                                                   final Vec3 requestedRaw) {
        final Vec3 raw = applyInputLocks(player,
                projectToUnitDisc(requestedRaw));
        final Vec3 effective = PlayerMovement.calculateMoveVector(
                raw,
                player.entityContext.moveInputComponent,
                player.entityContext.movementAbilitiesComponent.isFlying(),
                player.entityContext.actorDataFlagComponent,
                player.ghostMovementBridgeState.waterSample.touching(),
                player.entityContext.sneakingComponent);
        return new InputCandidate(raw, effective);
    }

    private static Vec3 applyInputLocks(final GhostPlayer player,
                                         final Vec3 input) {
        float x = input.x;
        float z = input.z;
        final int mask = player.entityContext.clientInputLockComponent
                .getMask();
        if ((mask & ac.ghost.anticheat.prediction.bds.component
                .ClientInputLockComponent.BLOCK_POSITIVE_X) != 0) {
            x = Math.min(x, 0.0F);
        }
        if ((mask & ac.ghost.anticheat.prediction.bds.component
                .ClientInputLockComponent.BLOCK_NEGATIVE_X) != 0) {
            x = Math.max(x, 0.0F);
        }
        if ((mask & ac.ghost.anticheat.prediction.bds.component
                .ClientInputLockComponent.BLOCK_POSITIVE_Y) != 0) {
            z = Math.min(z, 0.0F);
        }
        if ((mask & ac.ghost.anticheat.prediction.bds.component
                .ClientInputLockComponent.BLOCK_NEGATIVE_Y) != 0) {
            z = Math.max(z, 0.0F);
        }
        return new Vec3(x, 0.0F, z);
    }

    private static Vec3 projectToUnitDisc(final Vec3 input) {
        final float lengthSquared = input.x * input.x + input.z * input.z;
        if (lengthSquared <= INPUT_LIMIT_SQUARED) {
            return new Vec3(input.x, 0.0F, input.z);
        }
        final float scale = 1.0F / (float) Math.sqrt(lengthSquared);
        return new Vec3(input.x * scale, 0.0F, input.z * scale);
    }

    private static void applySelectedInput(final GhostPlayer player,
                                           final InputCandidate selected,
                                           final InputSearch inputSearch) {
        player.entityContext.mobTravelComponent.setInput(
                selected.effective().clone());
        if (inputSearch == InputSearch.EXACT) {
            return;
        }
        player.entityContext.moveInputComponent.setAxisX(selected.raw().x);
        player.entityContext.moveInputComponent.setAxisY(selected.raw().z);
        player.entityContext.moveInputComponent.setEffective(
                selected.effective().x, selected.effective().z);
    }

    



    private static Vec3 prepareUncertain(final GhostPlayer player,
                                         final Vec3 startVelocity) {
        Vec3 velocity = startVelocity.add(
                LiquidPhysicsSystem.peekSelectedPush(player));
        velocity = WaterSinkInputSystem.apply(player, velocity);
        velocity = MobTravelIntentSystem.sanitizeVelocity(velocity);
        velocity = ScaffoldingActionSystem.applyAcceptedDescend(
                player, velocity);

        final StateVectorComponent temporary = temporaryState(player, velocity);
        FlyingPlayerStuckOnGroundWorkaroundSystem.tick(player, temporary);

        final int noJumpDelay =
                player.entityContext.mobJumpComponent.getNoJumpDelay();
        try {
            return MobJumpSystem.apply(
                    player.entityContext, temporary.getDelta()).clone();
        } finally {
            player.entityContext.mobJumpComponent.setNoJumpDelay(noJumpDelay);
        }
    }

    private static BranchPreview evaluate(
            final GhostPlayer player,
            final PlayerTickStartVelocityComponent.Candidate candidate,
            final Vec3 preparedVelocity,
            final InputCandidate input) {
        final Vec3 previousInput = player.entityContext.mobTravelComponent
                .getInput().clone();
        try {
            player.entityContext.mobTravelComponent.setInput(
                    input.effective().clone());
            final Preview preview = preview(
                    player, candidate, preparedVelocity);
            return new BranchPreview(preview, input);
        } finally {
            player.entityContext.mobTravelComponent.setInput(previousInput);
        }
    }

    private static Preview preview(
            final GhostPlayer player,
            final PlayerTickStartVelocityComponent.Candidate candidate,
            final Vec3 preparedVelocity) {
        final Vec3 travelVelocity = travel(player, preparedVelocity);
        final MovementCollisionPreviewSystem.Result collision =
                MovementCollisionPreviewSystem.tick(player, travelVelocity);
        final Vec3 predictedPosition =
                player.entityContext.stateVectorComponent.getPosition()
                        .add(collision.resolved());
        final float distanceSquared = predictedPosition.squaredDistanceTo(
                player.entityContext.serverPlayerCurrentMovementComponent
                        .getUnvalidatedPosition());

        int collisionMismatches = 0;
        if (player.ghostMovementBridgeState
                .predictionHasDigitalDirectionState
                && player.getSession().protocol >= ProtocolInfo.v1_21_50) {
            final boolean clientHorizontal =
                    player.entityContext.playerActionComponent.actions()
                            .contains(AuthInputAction.HORIZONTAL_COLLISION);
            final boolean clientVertical =
                    player.entityContext.playerActionComponent.actions()
                            .contains(AuthInputAction.VERTICAL_COLLISION);
            collisionMismatches =
                    (collision.horizontalCollision() == clientHorizontal
                            ? 0 : 1)
                            + (collision.verticalCollision() == clientVertical
                            ? 0 : 1);
        }
        return new Preview(candidate, preparedVelocity, predictedPosition,
                distanceSquared, collisionMismatches);
    }

    private static Vec3 travel(final GhostPlayer player,
                               final Vec3 preparedVelocity) {
        Vec3 velocity = SwimControlSystem.apply(player, preparedVelocity);
        final StateVectorComponent state = temporaryState(player, velocity);
        VerticalFlySpeedControlSystem.tick(player, state);

        if (player.entityContext.playerFlyingTravelComponent.isPresent()) {
            final MovementSpeedComponent speed = baseMovementSpeed(player);
            final PlayerFlyingTravelComponent flying =
                    new PlayerFlyingTravelComponent();
            flying.setPresent(true);
            PlayerFlyingMoveSpeed.tick(player, speed, flying);
            final MobTravelComponent travel = new MobTravelComponent();
            travel.setInput(player.entityContext.mobTravelComponent
                    .getInput().clone());
            DefaultMoveSystems.tickFlyingPlayer(
                    player, state, speed, travel);
            return state.getDelta().clone();
        }
        if ((player.entityContext.serverPlayerMovementComponent
                .getCurrentInputTick() != 1L
                && player.ghostMovementBridgeState.lavaSample.touching())
                || player.ghostMovementBridgeState.waterSample.touching()) {
            if (player.entityContext.serverPlayerMovementComponent
                    .getCurrentInputTick() != 1L
                    && player.ghostMovementBridgeState.lavaSample.touching()) {
                return LavaTravelSystem.tick(player, state.getDelta());
            }
            return WaterTravelSystem.tick(player, state.getDelta()).velocity();
        }
        if (player.entityContext.actorDataFlagComponent.has(
                Entity.DATA_FLAG_GLIDING)) {
            return GlideMoveSystem.tick(player, state.getDelta());
        }

        final MovementSpeedComponent speed = baseMovementSpeed(player);
        if (player.entityContext.onGroundFlagComponent.isPresent()) {
            GroundTravelTypeSystem.tick(player, speed);
        } else {
            MobTravelUpdateSpeedsSystem.tickAir(player, speed);
        }
        final MobTravelComponent travel = new MobTravelComponent();
        travel.setInput(player.entityContext.mobTravelComponent
                .getInput().clone());
        DefaultMoveSystems.tickGroundOrAir(player, state, speed, travel);
        return state.getDelta().clone();
    }

    private static MovementSpeedComponent baseMovementSpeed(
            final GhostPlayer player) {
        final MovementSpeedComponent result = new MovementSpeedComponent();
        result.setValue(
                player.entityContext.attributesComponent.movementSpeed());
        return result;
    }

    private static StateVectorComponent temporaryState(
            final GhostPlayer player,
            final Vec3 velocity) {
        final StateVectorComponent result = new StateVectorComponent();
        result.initialize(
                player.entityContext.stateVectorComponent.getPosition().clone());
        result.setPreviousPosition(
                player.entityContext.stateVectorComponent
                        .getPreviousPosition().clone());
        result.setDelta(velocity.clone());
        return result;
    }

    private static BranchPreview better(final BranchPreview first,
                                         final BranchPreview second) {
        if (first == null) {
            return second;
        }
        return compare(second.preview(), first.preview()) < 0
                ? second : first;
    }

    
    private static int compare(final Preview first, final Preview second) {
        final int distance = Float.compare(
                first.distanceSquared(), second.distanceSquared());
        if (distance != 0) {
            return distance;
        }
        return Integer.compare(
                first.collisionMismatches(), second.collisionMismatches());
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

    private enum InputSearch {
        EXACT,
        DIGITAL,
        ANALOG
    }

    private record InputCandidate(Vec3 raw, Vec3 effective) {
        InputCandidate {
            raw = raw.clone();
            effective = effective.clone();
        }

        @Override
        public Vec3 raw() {
            return raw.clone();
        }

        @Override
        public Vec3 effective() {
            return effective.clone();
        }
    }

    private record BranchPreview(Preview preview, InputCandidate input) {
    }

    private record Preview(
            PlayerTickStartVelocityComponent.Candidate candidate,
            Vec3 preparedVelocity,
            Vec3 predictedPosition,
            float distanceSquared,
            int collisionMismatches) {
        Preview {
            preparedVelocity = preparedVelocity.clone();
            predictedPosition = predictedPosition.clone();
        }

        @Override
        public Vec3 preparedVelocity() {
            return preparedVelocity.clone();
        }

        @Override
        public Vec3 predictedPosition() {
            return predictedPosition.clone();
        }
    }
}
