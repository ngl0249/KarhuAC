package me.liwk.karhu.handler.collision;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.handler.collision.enums.Boxes;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.handler.interfaces.KarhuHandler;
import me.liwk.karhu.util.KarhuStream;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.benchmark.Benchmark;
import me.liwk.karhu.util.benchmark.BenchmarkType;
import me.liwk.karhu.util.benchmark.KarhuBenchmarker;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import me.liwk.karhu.util.mc.boundingbox.BoundingBox;
import me.liwk.karhu.world.CachedBlock;
import me.liwk.karhu.world.nms.MainSupportingBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public final class CollisionHandler implements KarhuHandler {

    private final KarhuPlayer data;

    @Getter
    private boolean hasCachedOnce = false;

    private List<CachedBlock> cache1_13 = new CopyOnWriteArrayList<>();

    public void handleLastTicks() {
        this.data.setWasOnBed(this.data.isOnBed());
        this.data.setWasOnDoor(this.data.isOnDoor());
        this.data.setWasWasInWeb(this.data.isWasInWeb());
        this.data.setWasInWeb(this.data.isInWeb());
        this.data.setWasWasOnSlime(this.data.isWasOnSlime());
        this.data.setWasOnSlime(this.data.isOnSlime());
        this.data.setWasSlimeLand(this.data.isSlimeLand());
        this.data.setWasOnSlab(this.data.isOnStairs());
        this.data.setWasOnSlab(this.data.isOnSlab());
        this.data.setWasOnHoney(this.data.isOnHoney());
        this.data.setWasOnSoulSand(this.data.isOnSoulsand());
        this.data.setWasUnderBlock(this.data.isUnderBlock());
        this.data.setWasOnFence(this.data.isOnFence());
        this.data.setWasWasOnWater(this.data.isWasOnWater());
        this.data.setWasOnWater(this.data.isOnWater());
        this.data.setWasOnLava(this.data.isOnLava());
        this.data.setWasWasOnClimbable(this.data.isWasOnClimbable());
        this.data.setWasOnClimbable(this.data.isOnClimbable());
        this.data.setWasFullyInsideBlock(data.isFullyInsideBlock());
        this.data.setWasOnComparator(this.data.isOnComparator());
        this.data.setWasOnGroundServer(this.data.isOnGroundServer());
    }

    public void cacheBlocks() {

        final double areaSize =
                data.elapsed(data.getUnderPlaceTicks()) <= (3 + MathUtil.getPingInTicks(data.getTransactionPing()))
                        ? 2D : Math.min(3, data.deltas.deltaXZ + 1.13);

        BoundingBox searchBox = data.getBoundingBox().clone()
                .expand(areaSize, 2, areaSize);

        if (data.isNewerThan12()) {
            cache1_13 = searchBox.getCollidingBlocksModern(data);
        } else {
            cache1_13 = searchBox.getCollidingBlocks();
        }
    }

    public boolean hasCached() {
        return hasCachedOnce;
    }

    public void handle(boolean moved) {
        long nanoStart = System.nanoTime();

        boolean newVer = Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_13);

        final double areaSize =
                data.elapsed(data.getUnderPlaceTicks()) <= (3 + MathUtil.getPingInTicks(data.getTransactionPing()))
                        ? 2D : Math.min(3, data.deltas.deltaXZ + 1.13);

        List<CachedBlock> tickCache = newVer
                ? cache1_13
                : data.isNewerThan12() ? data.getBoundingBox().clone()
                .expand(areaSize, 2, areaSize)
                .getCollidingBlocksModern(data) : data.getBoundingBox().clone()
                .expand(areaSize, 2, areaSize)
                .getCollidingBlocks();

        hasCachedOnce = !tickCache.isEmpty();

        long startBBCreate = System.nanoTime();

        double substract = data.deltas.motionY == 0 && data.deltas.lastMotionY < -0.1
                ? 0.6
                : 0.03125;

        final BoundingBox below = data.getBoundingBox().clone()
                .subtractMin(0, substract, 0)
                .subtractMax(0,1.8f,0),
                belowHugeBox = data.getBoundingBox().clone()
                        .expand(areaSize, 0, areaSize)
                        .subtractMin(0, 3, 0)
                        .subtractMax(0, 1.805, 0),
                belowHugeBox2 = data.getBoundingBox().clone()
                        .expandMin(0.3, -1, 0.3)
                        .subtractMax(0.3, 1.805, 0.3),
                inside = data.getBoundingBox().clone()
                        .expandMin(0.01, 0.8, 0.01)
                        .subtractMax(0.01, 0.8, 0.01),
                mcpWaterBox = data.getMcpCollision().clone()
                        .expand(0, !data.isNewerThan12() ? -0.4F : 0, 0),
                mcpLavaBox = data.getBoundingBox().clone()
                        .expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D),
                mcpLavaBoxModern = data.getBoundingBox().clone()
                        .expand(-0.001),
                mcpAnyLiquidBox = data.getBoundingBox().clone()
                        .translate(data.deltas.deltaX,
                                (data.deltas.lastMotionY * 0.800000011920929D) + 0.6000000238418579D,
                                data.deltas.deltaZ);

        //Aids fix for plugins that do carpets with packets.
        if (this.data.deltas.motionY == 0
                && data.isOnGroundPacket()
                && data.getLocation().getY() - MathHelper.floor_double(data.getLocation().getY()) == 0.0625) {
            below.subtractMin(0, 0.05, 0);
        }


        final BoundingBox above = data.getBoundingBox().clone()
                .expandMin(0, 1.4 ,0) //1.4 so it will still work with swimming pose
                .expand(new Vector(-data.deltas.deltaX, 0, -data.deltas.deltaZ))
                .expandMax(0, 0.4, 0)
                .expand(0.005, 0, 0.005);

        final BoundingBox aboveStrict = data.getBoundingBox().clone()
                .expandMin(0, 1.795 ,0)
                .expandMax(0, 0.3, 0)
                .expand(0.001, 0.001, 0.001);

        final BoundingBox lqBelow = data.getBoundingBox().clone()
                .subtractMax(0, 1.8, 0)
                .subtractMin(0, 0.0425, 0);

        final BoundingBox horiBox = this.data.getBoundingBox().clone()
                .expandMin(0, 0.001, 0)
                .expand(0.01, 0.0, 0.01);

        final BoundingBox horiBoxLatest = this.data.getBoundingBox().clone()
                .expand(0.3, 0, 0.3)
                .translate(-data.deltas.deltaX, 0.0, -data.deltas.deltaZ);

        double moveY = data.deltas.lastMotionY - 0.10005;
        final BoundingBox goingToGroundBox = this.data.getLastBoundingBox().clone()
                .subtractMax(0, 1.7, 0)
                .expandMin(0, moveY, 0)
                .expand(Math.abs(data.deltas.lastDX), 0, Math.abs(data.deltas.lastDZ));

        //long nanoStopBox = System.nanoTime();

        /*
        * Stream responsible for the blocks below the player
        */
        final List<Material> blocksBelow = below.getCachedCollidingBlocks(tickCache);
        final List<Material> blocksNearHugeBelow = belowHugeBox.getCachedCollidingBlocks(tickCache);
        final KarhuStream<Material> blocksNearHugeBelow2 = new KarhuStream<>(belowHugeBox2.getCachedCollidingBlocks(tickCache));
        final KarhuStream<Material> blocksInsideMiddleBox = new KarhuStream<>(inside.getCachedCollidingBlocks(tickCache));
        final List<Material> normalBox = this.data.getBoundingBox().clone()
                .expandMin(0, 0.001, 0)
                .expand(data.deltas.deltaX, 0, data.deltas.deltaZ).getCachedCollidingBlocks(tickCache);
        final KarhuStream<Material> lqStream = new KarhuStream<>(lqBelow.getCachedCollidingBlocks(tickCache));
        final KarhuStream<Material> mcpLandStream = new KarhuStream<>(data.getBoundingBox().clone().getCollidingOnLanded(tickCache,
                MathHelper.floor_double(data.getLocation().getX()),
                MathHelper.floor_double(data.getLocation().getY() - 0.20000000298023224D),
                MathHelper.floor_double(data.getLocation().getZ()))
        );
        final KarhuStream<Material> mcpWaterStream = new KarhuStream<>(mcpWaterBox.getCollidingMaterialAccel(tickCache));
        final KarhuStream<Material> mcpLavaStream = !data.isNewerThan12() ?
                new KarhuStream<>(mcpLavaBox.getCollidingMaterialAccel(tickCache))
                : new KarhuStream<>(mcpLavaBoxModern.getCollidingMaterialAccel(tickCache));
        final KarhuStream<Material> nearFlowing = new KarhuStream<>(data.getBoundingBox().clone().expand(1, 0, 1)
                .getCollidingMaterialAccel(tickCache));

        final KarhuStream<Material> goingToGround = new KarhuStream<>(goingToGroundBox.getCachedCollidingBlocks(tickCache));

        /*
         * Stream responsible for the blocks slightly outside the player box
         */
        final List<Material> blocksHori = horiBox.getCachedCollidingBlocks(tickCache);
        final KarhuStream<Material> blocksHoriLatest = new KarhuStream<>(horiBoxLatest.getCachedCollidingBlocks(tickCache));

        /*
         * Stream responsible for the blocks above player box
         */
        final KarhuStream<Material> blocksAbove = new KarhuStream<>(above.getCachedCollidingBlocks(tickCache));
        final KarhuStream<Material> blocksAboveStrict = new KarhuStream<>(aboveStrict.getCachedCollidingBlocks(tickCache));

        /*
         * Stream responsible for entities
         */
        KarhuStream<EntityType> entities =  new KarhuStream<>(this.data.getBoundingBox().clone()
                .expand(0.3, 0.6, 0.3)
                .getCollidingEntitiesNew());

       KarhuStream<EntityType> entitiesUp = new KarhuStream<>(this.data.getBoundingBox().clone()
                .expandMin(0, 1.5, 0)
                .expandMax(0, 0.7, 0)
                .expand(1, 0, 1)
                .getCollidingEntitiesNew());

        KarhuStream<Material> climbBlock = new KarhuStream<>(getCachedCollidingMatsLocation(tickCache, onClimbable()));

        final double lastX = data.getLastLocation().getX(),
                lastY = data.getLastLocation().getY(),
                lastZ = data.getLastLocation().getZ();

        final double floorX = Math.floor(lastX), floorZ = Math.floor(lastZ);

        KarhuStream<Material> sneakBlock = new KarhuStream<>(getCachedCollidingMatsLocation(tickCache, new Location(data.getWorld(),
                floorX,
                Math.floor(lastY - 0.2),
                floorZ)));

        Location groundBridgeLoc = data.getLocation()
                .clone()
                .subtract(0, 2, 0)
                .toLocation(data.getWorld());

        Material groundBridge = getCachedCollidingBlocksLocation(tickCache, groundBridgeLoc);

        final Location stepLoc = new Location(data.getWorld(),
                floorX,
                Math.floor(data.getLastLocation().getY() - 0.2f),
                floorZ);

        KarhuStream<Material> steppedOn = data.getClientVersion().getProtocolVersion() >= 47
                ? new KarhuStream<>(getCachedCollidingMatsLocation(tickCache, stepLoc))
                : null;

        float downLookup = data.getClientVersion().isOlderThan(ClientVersion.V_1_15) ? 1 : 0.5000001f;

        final int floorY = MathHelper.floor(lastY - downLookup);

        final Location moveBlockLoc = new Location(data.getWorld(),
                floorX,
                floorY,
                floorZ);

        Material moveBlock = getCachedCollidingBlocksLocation(tickCache, moveBlockLoc);

        data.setMovementBlock(moveBlock);

        long nanoStartCollision = System.nanoTime();

        if (data.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20)) {

            if (data.getMainSupportingBlock() != null && data.getMainSupportingBlock().getBlockPos() != null) {
                Vector3i blockIsAt = data.getMainSupportingBlock().getBlockPos().withY(MathHelper.floorBasic(lastY - (double) 0.5000001f));

                data.setAirMovementBlock(getCachedCollidingBlocksVec(tickCache, blockIsAt));
            } else {
                final Vector3i blockIsAt = new Vector3i(
                        MathHelper.floorBasic(data.getLastLocation().x),
                        MathHelper.floorBasic(data.getLastLocation().y - 0.5000001f),
                        MathHelper.floorBasic(data.getLastLocation().z));
                data.setAirMovementBlock(getCachedCollidingBlocksVec(tickCache, blockIsAt));
            }

            data.setLastMainSupportingBlock(data.getMainSupportingBlock());
            data.setMainSupportingBlock(findMainSupportingBlockPos(data.getLastMainSupportingBlock(), data.isOnGroundPacket(), tickCache));
        }

        handleHoriBoxColl(blocksHori);
        handleBlocksBelow(blocksBelow, data.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20) ? data.getAirMovementBlock() : moveBlock);

        boolean entity = entities.any(e -> EntityTypes.isTypeInstanceOf(e, EntityTypes.BOAT)
                || EntityTypes.isTypeInstanceOf(e, EntityTypes.CHEST_BOAT)
                || EntityTypes.isTypeInstanceOf(e, EntityTypes.HAPPY_GHAST)
                || EntityTypes.isTypeInstanceOf(e, EntityTypes.SHULKER));
        boolean livingEntity = entities.any((e ->
                EntityTypes
                        .isTypeInstanceOf(e,
                                EntityTypes.LIVINGENTITY)
        ));
        //final boolean livingEntity = entities.any(e -> e instanceof LivingEntity);

        this.data.setCollidedWithLivingEntity(livingEntity);

        this.data.setOnBoat(entity);

        if (data.getVehicle() != null) {

            Location underBoatLoc = new CustomLocation(data.getVehicleX(), data.getVehicleY(), data.getVehicleZ())
                            .toLocation(data.getWorld());

            KarhuStream<Material> waterUnderBoat = new KarhuStream<>(getCachedCollidingMatsLocation(tickCache, underBoatLoc));


            data.setWaterUnderBoat(waterUnderBoat.any(material -> MaterialChecks.WATER.contains(material) || MaterialChecks.SEASHIT.contains(material)));
        }

        data.setNotGroundBridging(groundBridge == Material.AIR);

        handleBlocksBelowHugeColl(blocksNearHugeBelow);

        this.data.setLastOnGroundServer(data.isOnGroundServer());
        this.data.setOnGroundServer(entity || data.isOnFence()
                || data.isBelowSolid());
        this.data.setGroundNearBox(entity
                || data.isOnFence()
                || data.isNearHugeBelowSolid());
        this.data.setGroundNearBoxBelow(entity
                || data.isOnFence()
                || blocksNearHugeBelow2.any(m -> m.isSolid() || MaterialChecks.WEIRD_SOLID.contains(m)));


        if (data.isOnScaffolding()) {
            this.data.setLastOnScaffolding(data.getTotalTicks());
        }

        if (!this.data.isInUnloadedChunk()) {
            this.data.setOnWater(mcpWaterStream.any(b -> MaterialChecks.WATER.contains(b) || MaterialChecks.SEASHIT.contains(b)));
            this.data.setLastPushedByWater(nearFlowing.any(b -> MaterialChecks.AIR.contains(b)) ? data.getTotalTicks() : data.getLastPushedByWater());
            this.data.setOnLava(mcpLavaStream.any(b -> MaterialChecks.LAVA.contains(b)));
            this.data.setInsideWater(blocksInsideMiddleBox.any(b -> MaterialChecks.WATER.contains(b) || MaterialChecks.SEASHIT.contains(b)));



            this.data.setLastOnWaterOffset(data.isOnWaterOffset());
            this.data.setOnWaterOffset(mcpAnyLiquidBox.getAnyLiquid(tickCache));
        }

        if (data.isNewerThan13()) {

            CustomLocation location = data.getLastLocation();

            float width = Boxes.CROUCH.getWidth(), height = Boxes.CROUCH.getHeight();

            BoundingBox bbCrouch = new BoundingBox(data,
                    location.x - width,
                    location.y,
                    location.z - width,
                    location.x + width,
                    location.y + height,
                    location.z + width
            );

            //TODO FIX THIS CODE
            data.setCrouching(bbCrouch.getCachedCollidingBlocks(tickCache).isEmpty()
                    && (data.isWasSneaking() || !data.getLastBoundingBox().getCollidingBlocks().isEmpty()));
        }

        handleNormalBoxColl(normalBox);

        boolean bedHori = blocksHoriLatest.any(b -> MaterialChecks.BED.contains(b));
        boolean fenceHori = blocksHoriLatest.any(b -> MaterialChecks.FENCES.contains(b));

        this.data.setCollidedWithFence(fenceHori);
        this.data.setOnBed(data.isBedBelowHuge() || bedHori);

        if (Karhu.SERVER_VERSION.getProtocolVersion() >= 47) {
            this.data.setSlimeLand(mcpLandStream.any(b -> MaterialChecks.SLIME.contains(b)));
        }

        data.setNextTickOnGround(goingToGround.any(b -> b.isSolid() || MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(b)));


        boolean water = lqStream.any(material -> (MaterialChecks.WATER.contains(material) || MaterialChecks.SEASHIT.contains(material)));
        boolean lily = lqStream.any(material -> MaterialChecks.LILY.contains(material));

        boolean waterBelow = water && !lily;

        if (!this.data.isInUnloadedChunk()) {
            this.data.setAboveButNotInWater(waterBelow);
        } else {
            this.data.setOnWater(false);
            this.data.setOnLava(false);
            this.data.setOnLiquid(false);
            this.data.setInsideWater(false);
        }

        this.data.setOnLiquid(this.data.isWasOnWater() || this.data.isOnLava());

        boolean isRightBlock = sneakBlock.any(material -> !material.isSolid() && !MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(material));

        this.data.setLastLadder(data.isOnLadder());

        this.data.setOnLadder(climbBlock.any(material -> MaterialChecks.CLIMBABLE.contains(material)));

        if (steppedOn != null) {
            boolean isOnSlime = steppedOn.any(b -> MaterialChecks.SLIME.contains(b));

            data.setSteppedOnSlime(isOnSlime);

            if (isOnSlime) {
                data.setLastSteppedOnSlime(data.getTotalTicks());
            }
        }

        this.data.setSneakEdge((isRightBlock || sneakBlock.isEmpty()) && data.isOnGroundPacket());

        this.data.setLastBlockSneak(isRightBlock);

        boolean boatsUp = entitiesUp.any(e -> EntityTypes.BOAT.equals(e) || EntityTypes.SHULKER.equals(e) || EntityTypes.CHEST_BOAT.equals(e));

        this.data.setUnderBlock(blocksAbove.any(b -> (b.isSolid() || MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(b)))
                || boatsUp);

        this.data.setUnderBlockStrict(blocksAboveStrict.any(b -> (b.isSolid()
                || MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(b)))
                || boatsUp);

        this.data.setUnderWeb(blocksAbove.any(b -> MaterialChecks.WEB.contains(b)));

        this.data.setInsideBlock(blocksInsideMiddleBox.any(b -> (b.isSolid() || MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(b))
                && !MaterialChecks.SIGNS.contains(b)
                && !MaterialChecks.CLIMBABLE.contains(b))
        );

        data.setAbleToStep(canStep());

        //tickCache.clear();

        long nanoStop = System.nanoTime();

        Benchmark cacheData = KarhuBenchmarker.getProfileData(BenchmarkType.BLOCK_CACHE);
        cacheData.insertResult(nanoStart, startBBCreate);

        Benchmark bbData = KarhuBenchmarker.getProfileData(BenchmarkType.BB_CREATE);
        bbData.insertResult(startBBCreate, nanoStartCollision);

        Benchmark profileData = KarhuBenchmarker.getProfileData(BenchmarkType.BLOCK_COLLISION);
        profileData.insertResult(nanoStartCollision, nanoStop);
    }

    public void handleTicks() {
        final boolean half = data.isOnStairs()
                || data.isOnSlab()
                || data.isOnBed()
                || data.isInsideTrapdoor()
                || data.isOnFence();

        this.data.setLastCollided(this.data.isCollidedHorizontally() || this.data.isUnderBlock() ? this.data.getTotalTicks() : this.data.getLastCollided());
        this.data.setLastCollidedH(this.data.isCollidedHorizontally() ? this.data.getTotalTicks() : this.data.getLastCollidedH());
        this.data.setLastCollidedV(this.data.isUnderBlock() ? data.getTotalTicks() : this.data.getLastCollidedV());
        this.data.setLastInLiquidOffset(this.data.isOnWaterOffset() ? this.data.getTotalTicks() : this.data.getLastInLiquidOffset());
        this.data.setLastInLiquid(this.data.isOnLiquid() ? this.data.getTotalTicks() : this.data.getLastInLiquid());
        this.data.setLastOnSlime(this.data.isOnSlime() ? this.data.getTotalTicks() : this.data.getLastOnSlime());
        this.data.setLastOnSoul(this.data.isOnSoulsand() ? this.data.getTotalTicks() : this.data.getLastOnSoul());
        this.data.setLastOnIce(this.data.isOnIce() ? this.data.getTotalTicks() : this.data.getLastOnIce());
        this.data.setLastOnClimbable(this.data.isOnClimbable() ? this.data.getTotalTicks() : this.data.getLastOnClimbable());
        this.data.setLastOnBed(this.data.isOnBed() ? this.data.getTotalTicks() : this.data.getLastOnBed());
        this.data.setLastOnHalfBlock(half ? this.data.getTotalTicks() : this.data.getLastOnHalfBlock());
        this.data.setLastFence(data.isOnFence() ? this.data.getTotalTicks() : this.data.getLastFence());
        this.data.setLastPortal(data.isOnPortal() ? this.data.getTotalTicks() : this.data.getLastFence());
        this.data.setLastInWeb(data.isOnWeb() || data.isInWeb() ? this.data.getTotalTicks() : this.data.getLastInWeb());
        this.data.setLastInBerry(data.isOnSweet() ? this.data.getTotalTicks() : this.data.getLastInBerry());
        this.data.setLastInPowder(data.isInPowder() ? this.data.getTotalTicks() : this.data.getLastInPowder());

        if (data.isSneakEdge()) this.data.setLastSneakEdge(data.getTotalTicks());

        if (Karhu.SERVER_VERSION.getProtocolVersion() > 47) {
            this.data.setLastCollidedWithEntity(data.isCollidedWithLivingEntity() ? data.getTotalTicks() : this.data.getLastCollidedWithEntity());
        }

        this.data.setLastOnBoat(data.isOnBoat() ? data.getTotalTicks() : this.data.getLastOnBoat());

        this.data.setServerGroundTicks(this.data.isOnGroundServer() ? data.getServerGroundTicks() + 1 : 0);
        this.data.setClientGroundTicks(this.data.isOnGroundPacket() ? data.getClientGroundTicks() + 1 : 0);
    }

    private Location onClimbable() {

        final int i = MathHelper.floor_double(this.data.getLocation().getX());
        final int j = MathHelper.floor_double(this.data.getLocation().getY());
        final int k = MathHelper.floor_double(this.data.getLocation().getZ());

        return new Location(data.getWorld(), i, j, k);
    }

    public MainSupportingBlockData findMainSupportingBlockPos(MainSupportingBlockData lastSupportingBlock, boolean isOnGround, List<CachedBlock> cachedBlocks) {
        if (!isOnGround) {
            return new MainSupportingBlockData(null, false);
        }

        boolean onGroundNoBlock;

        if (lastSupportingBlock == null) {
            onGroundNoBlock = true;
        } else {
            onGroundNoBlock = lastSupportingBlock.lastOnGroundAndNoBlock();
        }

        BoundingBox playerBox = data.getBoundingBox().clone();

        BoundingBox slightlyBelowPlayer = new BoundingBox(data, playerBox.minX, playerBox.minY - 1.0E-6D, playerBox.minZ,
                playerBox.maxX, playerBox.minY, playerBox.maxZ);

        Optional<Vector3i> supportingBlock = findSupportingBlock(slightlyBelowPlayer, cachedBlocks);
        if (!supportingBlock.isPresent() && !onGroundNoBlock) {
            BoundingBox aabb2 = slightlyBelowPlayer.translate(-data.deltas.lastDX, 0.0D, -data.deltas.lastDZ);
            supportingBlock = findSupportingBlock(aabb2, cachedBlocks);

            return new MainSupportingBlockData(supportingBlock.orElse(null), true);
        } else {
            return new MainSupportingBlockData(supportingBlock.orElse(null), true);
        }
    }

    public void handleNormalBoxColl(List<Material> normalBox) {
        boolean solid = false, wsnl = false, berry = false, climbable = false, sign = false,
                web = false, powder = false;

        for (Material material : normalBox) {
            if (material.isSolid()) solid = true;
            if (MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(material)) wsnl = true;
            if (MaterialChecks.BERRIES.contains(material)) berry = true;
            if (MaterialChecks.CLIMBABLE.contains(material)) climbable = true;
            if (MaterialChecks.SIGNS.contains(material)) sign = true;
            if (MaterialChecks.WEB.contains(material)) web = true;
            if (MaterialChecks.POWDERSNOW.contains(material)) powder = true;

        }

        this.data.setLastCollideH(data.isCollidedHorizontalClient());
        this.data.setCollidedHorizontalClient(solid || wsnl);

        this.data.setOnSweet(berry);
        this.data.setOnClimbable(climbable);

        this.data.setFullyInsideBlock((solid && !sign) || this.data.isOnClimbable());
        this.data.setInWeb(web);
        this.data.setInPowder(powder);
    }

    public void handleHoriBoxColl(List<Material> normalBox) {
        boolean solid = false, wsnl = false, cactus = false, pane = false,
                stairs = false, doors = false, halfs = false, honey = false;

        for (Material material : normalBox) {
            if (material.isSolid()) solid = true;
            if (MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(material)) wsnl = true;
            if (material == Material.CACTUS) cactus = true;
            if (MaterialChecks.PANES.contains(material)) pane = true;
            if (MaterialChecks.STAIRS.contains(material)) stairs = true;
            if (MaterialChecks.DOORS.contains(material)) doors = true;
            if (MaterialChecks.HALFS.contains(material)) halfs = true;
            if (MaterialChecks.HONEY.contains(material)) honey = true;

        }


        this.data.setCollidedWithPane(pane);

        boolean hori = solid
                || wsnl
                || this.data.isOnClimbable();

        this.data.setCollidedHorizontally(hori);
        this.data.setCollidedWithCactus(cactus);

        this.data.setFinalCollidedH(hori);

        this.data.setHoriStairs(stairs);
        this.data.setHoriDoors(doors);
        this.data.setHoriHalfs(halfs);

        if (honey) {
            this.data.setLastOnHoneySide(data.getTotalTicks());
            this.data.setOnHoneySide(true);
        } else {
            this.data.setOnHoneySide(false);
        }
    }

    public void handleBlocksBelow(List<Material> blocksBelow, Material moveBlock) {
        boolean solid = false, wsnl = false, fence = false, piston = false, dripLeaf = false,
                soul = false, stairs = false, doors = false, halfs = false, web = false,
                carpets = false, redstone = false, honey = false, scaffolding = false, trap = false, portal = false;

        for (Material material : blocksBelow) {
            if (material.isSolid()) solid = true;
            if (MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(material)) wsnl = true;
            if (MaterialChecks.FENCES.contains(material) || MaterialChecks.MOVABLE.contains(material)) fence = true;
            if (MaterialChecks.MOVABLE.contains(material)) piston = true;
            if (MaterialChecks.DRIP_LEAF.contains(material)) dripLeaf = true;
            if (MaterialChecks.SOUL.contains(material)) soul = true;
            if (MaterialChecks.STAIRS.contains(material)) stairs = true;
            if (MaterialChecks.DOORS.contains(material)) doors = true;
            if (MaterialChecks.HALFS.contains(material)) halfs = true;
            if (MaterialChecks.WEB.contains(material)) web = true;
            if (MaterialChecks.CARPETS.contains(material)) carpets = true;
            if (MaterialChecks.REDSTONE.contains(material)) redstone = true;
            if (MaterialChecks.HONEY.contains(material) || MaterialChecks.HONEY.contains(moveBlock)) honey = true;
            if (MaterialChecks.SCAFFOLD.contains(material)) scaffolding = true;
            if (MaterialChecks.TRAPS.contains(material)) trap = true;
            if (MaterialChecks.PORTAL.contains(material)) portal = true;


        }

        this.data.setBelowSolid(solid || wsnl);

        this.data.setOnPiston(piston);
        this.data.setOnFence(fence);

        if (dripLeaf) this.data.setLastOnDripLeaf(data.getTotalTicks());

        this.data.setOnSoulsand(soul);

        this.data.setOnStairs(stairs || data.isHoriStairs());
        this.data.setOnDoor(doors || data.isHoriDoors());
        this.data.setOnSlab(halfs || data.isHoriHalfs());
        this.data.setOnWeb(web);
        this.data.setOnCarpet(carpets);
        this.data.setOnComparator(redstone);

        this.data.setOnPortal(portal);

        //Higher ver
        this.data.setOnHoney(honey);
        this.data.setOnScaffolding(scaffolding);

        this.data.setInsideTrapdoor(trap);
    }

    public void handleBlocksBelowHugeColl(List<Material> blocksBelowHuge) {
        boolean solid = false, wsnl = false, door = false, climbable = false, retard = false,
                ice = false, slime = false, bed = false;

        for (Material material : blocksBelowHuge) {
            if (material.isSolid()) solid = true;
            if (MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(material)) wsnl = true;
            if (MaterialChecks.DOORS.contains(material)) door = true;
            if (MaterialChecks.CLIMBABLE.contains(material)) climbable = true;
            if (MaterialChecks.RETARD_FACE.contains(material)) retard = true;
            if (MaterialChecks.ICE.contains(material)) ice = true;
            if (MaterialChecks.SLIME.contains(material)) slime = true;
            if (MaterialChecks.BED.contains(material)) bed = true;

        }

        this.data.setNearHugeBelowSolid(solid || wsnl);
        this.data.setNearDoor(door);

        this.data.setAtButton(retard);
        this.data.setAtSign(retard);

        this.data.setNearClimbable(climbable);

        this.data.setBedBelowHuge(bed);

        this.data.setOnIce(ice);
        if (Karhu.SERVER_VERSION.getProtocolVersion() >= 47) this.data.setOnSlime(slime);
    }

    private Optional<Vector3i> findSupportingBlock(BoundingBox searchBox, List<CachedBlock> cachedBlocks) {
        AtomicReference<Vector3i> bestBlockPos = new AtomicReference<>();
        AtomicDouble blockDist = new AtomicDouble(Double.MAX_VALUE);


        searchBox.getCachedCollidingBlocks2(cachedBlocks).forEach(block -> {
            Location blockLocation = block.getPosition();
            Vector3i blockPos = new Vector3i(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());

            Vector3d blockPosAsVector3d = new Vector3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
            double distance = data.getLocation().distanceSquared(blockPosAsVector3d);

            if (distance < blockDist.get()
                    || (distance == blockDist.get() && (bestBlockPos.get() == null
                    || getClosestBlock(blockPos, bestBlockPos.get())))) {
                bestBlockPos.set(blockPos);
                blockDist.set(distance);
            }
        });

        return Optional.ofNullable(bestBlockPos.get());
    }

    private boolean getClosestBlock(Vector3i first, Vector3i second) {
        if (first.getY() < second.getY()) return true;

        double sumX = second.getX() - first.getX();
        double sumY = second.getZ() - first.getZ();

        double horizontalSumTotal = sumX + sumY;
        if (horizontalSumTotal == 0) {
            return sumX < 0;
        }

        return horizontalSumTotal < 0;
    }

    private boolean canStep() {
        if (!data.isOnGroundPacket() && !data.isLastOnGroundPacket()) return false;
        if (data.isPossiblyTeleporting()) return true;
        if (data.deltas.motionY > 0.6F + 1e-3) return false;
        if (data.isOnBoat()) return true;

        /*Bukkit.broadcastMessage("NTG " + data.isNextTickOnGround()
                + " ATS " + data.isAbleToStep()
                + " GS " + data.isOnGroundServer()
                + " LCH " + data.isLastCollideH());*/

        return (data.isNextTickOnGround() || data.isAbleToStep())
                && data.isOnGroundServer()
                && data.isLastCollideH()
                && data.deltas.motionY > 0;
    }

    public Material getCachedCollidingBlocksLocation(List<CachedBlock> blocks, Location location) {

        Material blockWanted = Material.AIR;

        for (CachedBlock cachedBlock : blocks) {

            Material blockMat = cachedBlock.getMaterial();

            AxisAlignedBB blockAABB = new AxisAlignedBB(cachedBlock.getPosition(), 0, 0);

            if (intersectsWith(blockAABB, location)) {

                if (MaterialChecks.AIR.contains(blockMat)) {
                    continue;
                }

                blockWanted = blockMat;
            }

        }


        return blockWanted;
    }

    public Material getCachedCollidingBlocksVec(List<CachedBlock> blocks, Vector3i vector3i) {

        Material blockWanted = Material.AIR;

        for (CachedBlock cachedBlock : blocks) {

            Material blockMat = cachedBlock.getMaterial();

            AxisAlignedBB blockAABB = new AxisAlignedBB(cachedBlock.getPosition(), 0, 0);

            if (intersectsWith(blockAABB, vector3i)) {

                if (MaterialChecks.AIR.contains(blockMat)) {
                    continue;
                }

                blockWanted = blockMat;
            }

        }


        return blockWanted;
    }

    public List<Material> getCachedCollidingMatsLocation(List<CachedBlock> blocks, Location location) {

        final List<Material> blocksWanted = new ArrayList<>();

        for (CachedBlock cachedBlock : blocks) {

            Material blockMat = cachedBlock.getMaterial();

            AxisAlignedBB blockAABB = new AxisAlignedBB(cachedBlock.getPosition(), 0, 0);

            if (intersectsWith(blockAABB, location)) {

                blocksWanted.add(blockMat);
            }

        }


        return blocksWanted;
    }

    public boolean intersectsWith(AxisAlignedBB other, Location location)
    {
        return other.maxX >= location.getBlockX() && other.minX <= location.getBlockX()
                && other.maxY >= location.getY() && other.minY <= location.getY()
                && other.maxZ >= location.getBlockZ() && other.minZ <= location.getBlockZ();
    }

    public boolean intersectsWith(AxisAlignedBB other, Vector3i vector3i)
    {
        return other.maxX >= vector3i.getX() && other.minX <= vector3i.getX()
                && other.maxY >= vector3i.getY() && other.minY <= vector3i.getY()
                && other.maxZ >= vector3i.getZ() && other.minZ <= vector3i.getZ();
    }

}
