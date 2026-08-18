package ac.ghost.anticheat.prediction.bds.entity;

import ac.ghost.anticheat.collision.bds.system.BlockCollisionsSystem;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.*;
import ac.ghost.anticheat.prediction.bds.world.BlockSource;










public final class EntityContext {
    private final EntityRegistry registry;

    public final BlockSource blockSource;
    public final BlockSourceComponent blockSourceComponent;
    public final LocalConstBlockSourceFactoryComponent localConstBlockSourceFactoryComponent;

    EntityContext(final EntityRegistry registry,
                  final GhostPlayer externalPlayer,
                  final BlockSource blockSource) {
        this.registry = registry;
        this.blockSource = blockSource;
        this.blockSourceComponent = new BlockSourceComponent(blockSource);
        this.localConstBlockSourceFactoryComponent =
                new LocalConstBlockSourceFactoryComponent(blockSource);
        this.externalDataComponent.bind(externalPlayer);
    }

    public EntityRegistry registry() {
        return this.registry;
    }

    public final AttributesComponent attributesComponent = new AttributesComponent();
    public final MobEffectsComponent mobEffectsComponent = new MobEffectsComponent();
    public final PlayerActionComponent playerActionComponent = new PlayerActionComponent();
    public final PlayerDestroyProgressCacheComponent playerDestroyProgressCacheComponent =
            new PlayerDestroyProgressCacheComponent();
    public final PlayerLoadingScreenComponent playerLoadingScreenComponent =
            new PlayerLoadingScreenComponent();
    public final UnloadedChunkTimerComponent unloadedChunkTimerComponent =
            new UnloadedChunkTimerComponent();
    public final ActorGameTypeComponent actorGameTypeComponent =
            new ActorGameTypeComponent();
    public final MovementEffectsComponent movementEffectsComponent =
            new MovementEffectsComponent();
    public final ShouldBeSimulatedComponent shouldBeSimulatedComponent =
            new ShouldBeSimulatedComponent();
    public final PushableByBlockComponent pushableByBlockComponent =
            new PushableByBlockComponent();
    public final MovementInterpolatorComponent movementInterpolatorComponent =
            new MovementInterpolatorComponent();
    public final SwiftSneakEnchantComponent swiftSneakEnchantComponent =
            new SwiftSneakEnchantComponent();
    public final SwimSpeedMultiplierComponent swimSpeedMultiplierComponent =
            new SwimSpeedMultiplierComponent();
    public final WaterMovementComponent waterMovementComponent =
            new WaterMovementComponent();
    public final ServerPlayerInventoryTransactionComponent
            serverPlayerInventoryTransactionComponent =
            new ServerPlayerInventoryTransactionComponent();
    public final VehicleComponent vehicleComponent = new VehicleComponent();
    public final MoveInputComponent moveInputComponent = new MoveInputComponent();
    public final ClientInputLockComponent clientInputLockComponent = new ClientInputLockComponent();
    public final ActorDataFlagComponent actorDataFlagComponent =
            new ActorDataFlagComponent();
    public final ActorDataDirtyFlagsComponent actorDataDirtyFlagsComponent =
            new ActorDataDirtyFlagsComponent();
    public final ActorDataHorseFlagComponent actorDataHorseFlagComponent =
            new ActorDataHorseFlagComponent();
    public final ActorDataJumpDurationComponent actorDataJumpDurationComponent =
            new ActorDataJumpDurationComponent();
    public final ActorDataBoundingBoxComponent actorDataBoundingBoxComponent =
            new ActorDataBoundingBoxComponent();
    public final ActorDataSeatOffsetComponent actorDataSeatOffsetComponent =
            new ActorDataSeatOffsetComponent();
    public final SynchedActorDataComponent synchedActorDataComponent =
            new SynchedActorDataComponent();
    public final ItemInUseComponent itemInUseComponent =
            new ItemInUseComponent();
    public final ItemInUseTicksDuringMovementComponent
            itemInUseTicksDuringMovementComponent =
            new ItemInUseTicksDuringMovementComponent();
    public final ItemUseSlowdownModifierComponent
            itemUseSlowdownModifierComponent =
            new ItemUseSlowdownModifierComponent();
    public final IsHorizontalPoseFlagComponent isHorizontalPoseFlagComponent =
            new IsHorizontalPoseFlagComponent();
    public final AbilitiesComponent abilitiesComponent = new AbilitiesComponent();
    public final AbilitiesRequestComponent abilitiesRequestComponent =
            new AbilitiesRequestComponent();
    public final PermissionFlyFlagComponent permissionFlyFlagComponent =
            new PermissionFlyFlagComponent();
    public final MovementAbilitiesComponent movementAbilitiesComponent = new MovementAbilitiesComponent();
    public final MovementAttributesComponent movementAttributesComponent =
            new MovementAttributesComponent();
    public final MovementSpeedComponent movementSpeedComponent = new MovementSpeedComponent();
    public final MobTravelComponent mobTravelComponent = new MobTravelComponent();
    public final PlayerFlyingTravelComponent playerFlyingTravelComponent =
            new PlayerFlyingTravelComponent();
    public final PlayerInputRequestComponent playerInputRequestComponent =
            new PlayerInputRequestComponent();
    public final PlayerInputModeComponent playerInputModeComponent =
            new PlayerInputModeComponent();
    public final PlayerPositionModeComponent playerPositionModeComponent =
            new PlayerPositionModeComponent();
    public final ServerPlayerTeleportingFlagComponent
            serverPlayerTeleportingFlagComponent =
            new ServerPlayerTeleportingFlagComponent();
    public final HasTeleportedFlagComponent hasTeleportedFlagComponent =
            new HasTeleportedFlagComponent();
    public final IsBeingTeleportedFlagComponent isBeingTeleportedFlagComponent =
            new IsBeingTeleportedFlagComponent();
    public final VanillaClientGameplayComponent vanillaClientGameplayComponent =
            new VanillaClientGameplayComponent();
    public final ActorRotationComponent actorRotationComponent = new ActorRotationComponent();
    public final StateVectorComponent stateVectorComponent = new StateVectorComponent();
    public final AABBShapeComponent aabbShapeComponent = new AABBShapeComponent();
    public final CollidableMobNearFlagComponent collidableMobNearFlagComponent =
            new CollidableMobNearFlagComponent();
    public final FallDistanceComponent fallDistanceComponent = new FallDistanceComponent();
    public final BlockMovementSlowdownMultiplierComponent blockMovementSlowdownMultiplierComponent =
            new BlockMovementSlowdownMultiplierComponent();
    public final BlockMovementSlowdownAppliedComponent blockMovementSlowdownAppliedComponent =
            new BlockMovementSlowdownAppliedComponent();
    public final ExternalDataComponent externalDataComponent = new ExternalDataComponent();
    public final BlockPosTrackerComponent blockPosTrackerComponent =
            new BlockPosTrackerComponent();
    public final CurrentlyStandingOnBlockComponent currentlyStandingOnBlockComponent =
            new CurrentlyStandingOnBlockComponent();
    public final StandOnSpeedAlteringBlockFlagComponent
            standOnSpeedAlteringBlockFlagComponent =
            new StandOnSpeedAlteringBlockFlagComponent();
    public final StandOnOtherBlockFlagComponent standOnOtherBlockFlagComponent =
            new StandOnOtherBlockFlagComponent();
    public final MoveRequestComponent moveRequestComponent = new MoveRequestComponent();
    public ApplyRestitutionComponent applyRestitutionComponent;
    public RewindCollisionShapesComponent rewindCollisionShapesComponent;
    public final MaxAutoStepComponent maxAutoStepComponent = new MaxAutoStepComponent();
    public final CanAlwaysAutoStepFlagComponent canAlwaysAutoStepFlagComponent =
            new CanAlwaysAutoStepFlagComponent();
    public final AutoStepRequestFlagComponent autoStepRequestFlagComponent =
            new AutoStepRequestFlagComponent();
    public final HasAutoSteppedComponent hasAutoSteppedComponent =
            new HasAutoSteppedComponent();
    public final CollisionFlagComponent collisionFlagComponent =
            new CollisionFlagComponent();
    public final VerticalCollisionFlagComponent verticalCollisionFlagComponent =
            new VerticalCollisionFlagComponent();
    public final DepenetrationComponent depenetrationComponent = new DepenetrationComponent();
    public final CustomDepenetrationMagnitudeComponent customDepenetrationMagnitudeComponent =
            new CustomDepenetrationMagnitudeComponent();
    public final MoveTowardsClosestSpaceFlagComponent moveTowardsClosestSpaceFlagComponent =
            new MoveTowardsClosestSpaceFlagComponent();
    public final BlockCollisionEvaluationQueueComponent blockCollisionEvaluationQueueComponent =
            new BlockCollisionEvaluationQueueComponent();
    public final BlockCollisionsSystem.BlockCollisionResolutionVectorComponent
            blockCollisionResolutionVectorComponent =
            new BlockCollisionsSystem.BlockCollisionResolutionVectorComponent();
    public final ActorSetPositionRequestComponent actorSetPositionRequestComponent =
            new ActorSetPositionRequestComponent();
    public final ReplayStateComponent replayStateComponent = new ReplayStateComponent();
    public final ReplayStateTrackerComponent replayStateTrackerComponent =
            new ReplayStateTrackerComponent();
    public final ApplyReplayStateTrackerRequestComponent
            applyReplayStateTrackerRequestComponent =
            new ApplyReplayStateTrackerRequestComponent();
    public final ServerPlayerCurrentMovementComponent serverPlayerCurrentMovementComponent =
            new ServerPlayerCurrentMovementComponent();
    public final InsideBubbleColumnBlockComponent insideBubbleColumnBlockComponent =
            new InsideBubbleColumnBlockComponent();
    public final InsideBlockWithPosAndBlockComponent<HoneyBlockFlag> insideHoneyBlockComponent =
            new InsideBlockWithPosAndBlockComponent<>();
    public final InsideBlockWithPosAndBlockComponent<SweetBerryBushBlockFlag>
            insideSweetBerryBushBlockComponent = new InsideBlockWithPosAndBlockComponent<>();
    public final InsideSlowingSweetBerryBushBlockComponent
            insideSlowingSweetBerryBushBlockComponent =
            new InsideSlowingSweetBerryBushBlockComponent();
    public final InsideWebBlockComponent insideWebBlockComponent =
            new InsideWebBlockComponent();
    public final InsideOnewayBlockComponent insideOnewayBlockComponent =
            new InsideOnewayBlockComponent();
    public final InsideBlockWithPosAndBlockComponent<PowderSnowBlockFlag>
            insidePowderSnowBlockComponent = new InsideBlockWithPosAndBlockComponent<>();
    public final CanStandOnSnowFlagComponent canStandOnSnowFlagComponent =
            new CanStandOnSnowFlagComponent();
    public final HasLightweightFamilyFlagComponent hasLightweightFamilyFlagComponent =
            new HasLightweightFamilyFlagComponent();
    public final AutoClimbTravelFlagComponent autoClimbTravelFlagComponent =
            new AutoClimbTravelFlagComponent();
    public final HorizontalCollisionFlagComponent horizontalCollisionFlagComponent =
            new HorizontalCollisionFlagComponent();
    public final MobIsJumpingFlagComponent mobIsJumpingFlagComponent =
            new MobIsJumpingFlagComponent();
    public final MobJumpComponent mobJumpComponent = new MobJumpComponent();
    public final SneakingComponent sneakingComponent = new SneakingComponent();
    public final RiptideTridentSpinAttackComponent riptideTridentSpinAttackComponent =
            new RiptideTridentSpinAttackComponent();
    public final SpinAttackResultsComponent spinAttackResultsComponent =
            new SpinAttackResultsComponent();
    public final AntiCheatRewindFlagComponent antiCheatRewindFlagComponent =
            new AntiCheatRewindFlagComponent();
    public final OnGroundFlagComponent onGroundFlagComponent =
            new OnGroundFlagComponent();
    public final WasInWaterFlagComponent wasInWaterFlagComponent =
            new WasInWaterFlagComponent();
    public final WasInLavaFlagComponent wasInLavaFlagComponent =
            new WasInLavaFlagComponent();
    public final WaterTravelFlagComponent waterTravelFlagComponent =
            new WaterTravelFlagComponent();
    public final LavaTravelFlagComponent lavaTravelFlagComponent =
            new LavaTravelFlagComponent();
    public final ActorHeadInWaterFlagComponent actorHeadInWaterFlagComponent =
            new ActorHeadInWaterFlagComponent();
    public final ActorHeadWasInWaterFlagComponent actorHeadWasInWaterFlagComponent =
            new ActorHeadWasInWaterFlagComponent();
    public final ShouldUpdateBoundingBoxRequestComponent shouldUpdateBoundingBoxRequestComponent =
            new ShouldUpdateBoundingBoxRequestComponent();
    public final SwimAmountComponent swimAmountComponent =
            new SwimAmountComponent();
    public final ActorMovementTickNeededComponent actorMovementTickNeededComponent =
            new ActorMovementTickNeededComponent();
    public final ServerPlayerMovementComponent serverPlayerMovementComponent =
            new ServerPlayerMovementComponent();
    public final ServerPlayerMovementSyncComponent serverPlayerMovementSyncComponent =
            new ServerPlayerMovementSyncComponent();
    public final ClientAcceptanceThresholdsComponent clientAcceptanceThresholdsComponent =
            new ClientAcceptanceThresholdsComponent();
    public final PlayerMovementSettingsComponent playerMovementSettingsComponent =
            new PlayerMovementSettingsComponent();
    public final ForceSendMotionPacketComponent forceSendMotionPacketComponent =
            new ForceSendMotionPacketComponent();
    public final PlayerTickStartVelocityComponent playerTickStartVelocityComponent =
            new PlayerTickStartVelocityComponent();
    public BounceGravityCorrectionComponent bounceGravityCorrection;
}
