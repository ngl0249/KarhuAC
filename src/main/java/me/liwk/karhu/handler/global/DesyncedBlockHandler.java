package me.liwk.karhu.handler.global;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.setback.Setbacks;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import me.liwk.karhu.util.KarhuStream;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import me.liwk.karhu.util.pending.BlockPlacePending;
import me.liwk.karhu.util.player.PlayerUtil;
import me.liwk.karhu.util.task.Tasker;
import me.liwk.karhu.world.nms.FrictionLookup;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static me.liwk.karhu.util.player.BlockUtil.getTileEntitiesSync;

@RequiredArgsConstructor
public class DesyncedBlockHandler {
    private static final double WALL_COLLISION_THRESHOLD = 1E-6;
    private static final int MAX_VELOCITY_ABUSE_TICKS = 20;
    private static final int MIN_VELOCITY_ABUSE_TICKS = 5;
    private static final int ABOVE_DESYNC_THRESHOLD_TICKS = 3;
    private static final int LIQUID_DESYNC_TICKS_WATER = 3;
    private static final int LIQUID_DESYNC_TICKS_LAVA = 1;
    private static final double LIQUID_MOTION_THRESHOLD = 1E-4;

    private final KarhuPlayer data;
    @Getter
    @Setter
    private Location noFakeWaterLocation;

    public double desyncTicksUncertain, lavaDesyncTicks,
            waterDesyncTicks, wallTicks, uncertainTicks, aboveTicks,
            velocityAbuse, placeAbuse, uncertain2Ticks;

    private int lastCollidesClientBlock;
    private boolean setbacked, setbackEnabled;

    @Getter
    private final Set<BlockPlacePending> clientSideBlocks = ConcurrentHashMap.newKeySet();
    public int invalidPlaces;

    public void handleFlying(boolean moved, boolean clientCollide, boolean moveEvent) {
        if (!Karhu.getInstance().getConfigManager().isGhostBlock()) return;

        if (!clientSideBlocks.isEmpty()) {
            double downLookup = data.getClientVersion().isOlderThan(ClientVersion.V_1_15) ? 1 : 0.5000001;

            final Vector moveBlockLoc = new Vector(Math.floor(data.getLastLocation().getX()),
                    Math.floor(data.getLastLocation().getY() - downLookup),
                    Math.floor(data.getLastLocation().getZ()));

            for (BlockPlacePending bpp : clientSideBlocks) {

                AxisAlignedBB blockAABB = new AxisAlignedBB(bpp.getBlockPosition());

                if (intersectsWith(blockAABB, moveBlockLoc)) {

                    Set<Material> list = new KarhuStream<>(FrictionLookup.FRICTION_CACHE.keySet()).find(s -> s.contains(bpp.getItem()));

                    if (list != null) data.setFrictionUncertain(data.getTotalTicks());
                }
            }
        }


        if (!data.getCollisionHandler().hasCached()) return;

        if (!data.extraCache && Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_13)) {
            Tasker.run(() -> {
                data.setExtraCache(true);
                data.getCollisionHandler().cacheBlocks();
            });
            return;
        }

        setbackEnabled = Karhu.getInstance().getConfigManager().isGbLagback();

        final boolean serverAir = calculateServerAirStatus();
        final boolean serverCollideHuge = data.isGroundNearBox();
        final boolean mathCollide = MathUtil.onGround(Math.abs(data.getLocation().y)) || data.getMoveTicks() <= 1;
        final boolean unloadedChunk = data.elapsed(data.getLastInUnloadedChunk()) <= 3;

        final boolean clientBlockCollide = collidesWithClientBlock(true) || checkClientSideBlock(data.getLocation().toVector(), 2D);

        data.setOnGhostBlock(clientCollide && mathCollide && serverAir);

        handleBelowDesyncScenarios(clientCollide, mathCollide, serverAir, clientBlockCollide, serverCollideHuge, moveEvent, unloadedChunk);
        handleAboveDesyncScenarios(clientCollide, serverAir, mathCollide, unloadedChunk);
        handleLiquidDesyncScenarios(clientCollide, unloadedChunk);
        handleHorizontalCollisionScenarios(clientCollide);
        handleWallClimb(clientCollide, mathCollide, clientBlockCollide, data.isGroundNearBoxBelow());

        lastCollidesClientBlock = clientBlockCollide ? data.getTotalTicks() : lastCollidesClientBlock;
        setbacked = false;
    }

    private boolean calculateServerAirStatus() {
        return data.getAirTicks() >= 3 + Math.min(10, MathUtil.getPingInTicks((data.getTransactionPing() + data.getLastTransactionPing()) / 2));
    }

    private void handleBelowDesyncScenarios(boolean clientCollide, boolean mathCollide, boolean serverAir,
                                            boolean clientBlockCollide, boolean serverCollideHuge,
                                            boolean moveEvent, boolean unloadedChunk) {
        if (clientCollide && mathCollide && serverAir && !clientBlockCollide
                && data.elapsed(lastCollidesClientBlock) > 1 && !unloadedChunk) {
            handleBelowDesync(serverCollideHuge, moveEvent);
            data.setOnGhostBlock(true);

            if (!data.isAboveButNotInWater() && !data.isOnLiquid()) {
                if (setbacked || !setbackEnabled) {
                    data.setAirTicks(0);
                }
            }
        } else {
            desyncTicksUncertain = Math.max(desyncTicksUncertain - 0.0075, 0);
        }

        handleUncertainDesyncScenarios(clientCollide, serverAir, mathCollide, clientBlockCollide);
        handlePlaceAbuseScenarios(clientCollide, serverAir, clientBlockCollide);
    }

    private void handleWallClimb(boolean clientCollide, boolean mathCollide, boolean clientBlockCollide,
                                            boolean serverCollideHuge) {
        if (clientCollide && data.isCollidedHorizontally()
                && !data.isOnGroundServer() && !data.isLastOnGroundServer()
                && mathCollide && !serverCollideHuge && data.getTeleportManager().teleportTicks > 3) {
            if (++uncertain2Ticks >= (clientBlockCollide ? 10 : 1.5)) {
                performSetback("ghost tp NW SG");
            }
        } else {
            uncertain2Ticks = Math.max(uncertain2Ticks - 0.025, 0);
        }
    }

    private void handleUncertainDesyncScenarios(boolean clientCollide, boolean serverAir, boolean mathCollide, boolean clientBlockCollide) {
        if (clientCollide && serverAir && !mathCollide) {
            if (++uncertainTicks >= 2) {
                if (setbackEnabled && data.getMoveTicks() > 1) {
                    performSetback("ghost tp BELOW (UCV2)");
                }
            }
        } else {
            uncertainTicks = Math.max(uncertainTicks - 0.025, 0);
        }
    }

    private void handlePlaceAbuseScenarios(boolean clientCollide, boolean serverAir, boolean clientBlockCollide) {
        int airTicksNeeded = clientBlockCollide ? 40 : 10;

        if (clientCollide && data.getAirTicks() > airTicksNeeded && data.elapsed(data.getLastOnDripLeaf()) > 5) {
            if (++placeAbuse >= 5) {
                if (setbackEnabled) {
                    performSetback("ghost tp BELOW (DESYNC TICKS)");
                    placeAbuse = 3;
                }
            }
        } else {
            placeAbuse = Math.max(placeAbuse - 0.1D, 0);
        }

        if (clientCollide && serverAir && invalidPlaces > 3) {
            if (setbackEnabled) {
                performSetback("ghost tp BELOW (PLACE)");
                --invalidPlaces;
            }
        }
    }

    private void handleAboveDesyncScenarios(boolean clientCollide, boolean serverAir, boolean mathCollide, boolean unloadedChunk) {
        if (!unloadedChunk) {
            if (!clientCollide) {
                handleAboveDesync();
            }
        }
    }

    private void handleLiquidDesyncScenarios(boolean clientCollide, boolean unloadedChunk) {
        if (!unloadedChunk) {
            handleLiquidDesync(clientCollide);
        }
    }

    private void handleHorizontalCollisionScenarios(boolean serverCollide) {
        handleHorizontal(data.isFinalCollidedH());
    }

    public boolean checkInsidePlace() {
        return collidesWithClientBlock(false) && !data.isOnGroundServer() && !data.isOnGroundPacket();
    }

    public boolean checkBelowPlace() {
        return collidesWithClientBlock(true) && !data.isOnGroundServer();
    }

    private void handleBelowDesync(boolean serverCollideHuge, boolean moveEvent) {
        if (!serverCollideHuge) {
            if (setbackEnabled) {
                if (data.getTeleportManager().teleportTicks > 2 && data.elapsed(data.getLastOnDripLeaf()) > 10
                        && !data.isOnClimbable() && !data.couldBeUnloadedClient()
                        && data.elapsed(data.getLastFlyTick()) > 30) {

                    CustomLocation location = Setbacks.forgeToRotatedLocation(data.getSafeGroundSetback(), data);
                    double deltaY = data.deltas.motionY;

                    double horizontal = data.getLocation().horizontal(location);
                    double vertical = data.getLocation().vertical(location);

                    if (horizontal > 0 && vertical > 0) {

                        setbacked = true;

                        Tasker.run(() -> {
                            data.teleport(location);
                            if (deltaY < -0.1) {
                                data.getBukkitPlayer().damage(1D);
                            }
                        });

                        data.setDidFlagMovement(true);
                        data.setLastMovementFlag(data.getTotalTicks());

                        if (Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_13)) {
                            data.setCancelHitsTick(data.getTotalTicks());

                            Karhu.getInstance().printCool("&b> &fKarhu is cancelling hits for 20 ticks USER: " + data.getBukkitPlayer().getName());
                        }

                        MiscellaneousAlertPoster.postMitigation(data, 0, "DesyncBlock",
                                "* Desynced block (CE)" +
                                        "\n §f* h §b" + String.format("%.2f", horizontal) +
                                        "\n §f* v §b" + String.format("%.2f", vertical) +
                                        "\n §f* tp §b" + data.getTeleportManager().teleportTicks);
                    }
                }
            }

            this.updateBlocksServerside();
        } else {

            if(!moveEvent) {
                if (++desyncTicksUncertain > 15 && data.elapsed(data.getLastOnDripLeaf()) > 5) {

                    if (setbackEnabled) {

                        CustomLocation location = Setbacks.forgeToRotatedLocation(data.getSafeGroundSetback(), data);

                        if (!data.isPossiblyTeleporting() && !data.couldBeUnloadedClient() && data.elapsed(data.getLastFlyTick()) > 30) {

                            setbacked = true;

                            Tasker.run(() -> {
                                data.teleport(location);
                            });
                        }

                        data.setDidFlagMovement(true);
                        data.setLastMovementFlag(data.getTotalTicks());

                        MiscellaneousAlertPoster.postMitigation(data, 0, "DesyncBlock",
                                "* Desynced block (UN)" +
                                        "\n §f* air §b" + data.getAirTicks());

                        MiscellaneousAlertPoster.postSetback(data.getName() + " desync §eBELOW (UN) " + data.getAirTicks());
                    }

                    desyncTicksUncertain = 0;

                    this.updateBlocksServerside();
                }
            }
        }
    }

    private void handleHorizontal(boolean serverCollide) {
        boolean glitchWall = false;
        if (!serverCollide) {
            if (!data.isPossiblyTeleporting() && !data.couldBeUnloadedClient() && data.elapsed(data.getLastFlyTick()) > 30) {

                CustomLocation location = data.getLocation();

                final double distanceX = MathUtil.distanceToHorizontalCollision(location.x);
                final double distanceZ = MathUtil.distanceToHorizontalCollision(location.z);

                if (distanceX <= WALL_COLLISION_THRESHOLD || distanceZ <= WALL_COLLISION_THRESHOLD) {

                    if (wallTicks > 0) {
                        data.setCollidedHorizontally(true);
                        data.setLastCollided(data.getTotalTicks());
                        data.setLastCollidedH(data.getTotalTicks());
                    }

                    glitchWall = true;

                    if (data.elapsed(data.getLastVelocityTaken()) <= 2
                            && (data.getHoldVelo().getX() != 0.0 && data.getHoldVelo().getZ() != 0.0)) {
                        // might cause small bypasses near the world border, shouldn't be of any impact though
                        if (!data.isNearWorldBorder() && ++velocityAbuse >= MIN_VELOCITY_ABUSE_TICKS) {
                            data.setAbusingVelocity(true);
                            data.setLastAbusingVelocity(data.getTotalTicks());

                            Vector3d collisionDirection = new Vector3d(
                                    data.getHoldVelo().getX(),
                                    data.getHoldVelo().getY(),
                                    data.getHoldVelo().getZ()
                            );

                            data.setArtificialKbVector(collisionDirection);

                            if (velocityAbuse >= MAX_VELOCITY_ABUSE_TICKS) {
                                velocityAbuse = 3;
                            }
                        } else {
                            data.setAbusingVelocity(false);
                        }
                    }

                    data.setLastCollidedGhost(data.getTotalTicks());

                    if (++wallTicks > 10) {
                        //this.updateBlocksServerside();
                        wallTicks = 0;
                    }
                } else {
                    wallTicks = Math.max(wallTicks - 2, 0);
                }


            }
        } else {
            wallTicks = Math.max(wallTicks - 0.1, 0);
        }

        if (!glitchWall) {
            if (data.elapsed(data.getLastVelocityTaken()) >= 0 && data.elapsed(data.getLastVelocityTaken()) <= 5) {
                velocityAbuse = Math.max(velocityAbuse - 0.025, 0);
            }
        }
    }

    private void handleAboveDesync() {
        if (!data.isPossiblyTeleporting()) {

            double motionY = data.deltas.motionY, lMotionY = data.deltas.lastMotionY;

            double clamp = data.getClientVersion().getProtocolVersion() > 47 ? 0.003D : 0.005D;

            double prediction = (lMotionY - 0.08D) * 0.98F;

            if (prediction < clamp) prediction = 0.0D;

            final double jumpHeight = PlayerUtil.getJumpHeight(data);

            if (data.isLastOnGroundPacket() && !data.isOnGroundPacket() && data.deltas.motionY >= jumpHeight - 0.03125) {
                prediction = Math.min(this.data.deltas.motionY, jumpHeight); //nonsense, revise code.
            }

            double diff = Math.abs(motionY - prediction);
            boolean slabHit = Math.abs(diff - 0.05) <= clamp;
            boolean higher = data.getLocation().getY() > data.getLastLocation().getY();

            boolean desyncedAbove = diff > 0.02 //0.03
                    && ((higher && motionY < 0.42f) || (lMotionY >= 0.42f && slabHit))
                    && (data.elapsed(data.getLastCollidedV()) > 2 || !data.isUnderBlockStrict())
                    && data.getClientAirTicks() <= 4
                    && data.elapsed(data.getLastVelocityTaken()) > 3
                    && data.getTickedVelocity() == null
                    && data.elapsed(data.getLastInBerry()) > 2
                    && !data.isOnClimbable()
                    && !data.isOnSoulsand()
                    && !data.isOnSlime()
                    && !data.isConfirmingVelocity()
                    && !data.isInWeb()
                    && data.elapsed(data.getLastOnBed()) > 3
                    && data.getLevitationLevel() == 0
                    && !data.isWasInWeb()
                    && !data.isWasOnClimbable()
                    && data.elapsed(data.getLastInPowder()) > 3
                    && data.elapsed(data.getLastInLiquidOffset()) > 3
                    && data.elapsed(data.getLastInLiquid()) > 3;

            if (desyncedAbove) {
                if ((!data.isWasOnHoney() && !data.isOnHoney()) || data.deltas.motionY > 0.21F) {
                    if (data.getTotalTicks() > 40 && data.elapsed(data.getLastFlyTick()) > 30) {

                        data.setUnderGhostBlock(true);
                        data.setLastCollidedVGhost(data.getTotalTicks());

                        if (Karhu.getInstance().getConfigManager().isGbLagback() && !data.isOnBoat() && data.elapsed(data.getLastGlide()) > 20) {

                            if (++aboveTicks > 3) {

                                CustomLocation location = Setbacks.forgeToRotatedLocation(data.getSafeGroundSetback(), data);

                                setbacked = true;

                                MiscellaneousAlertPoster.postMitigation(data, 0, "DesyncBlock",
                                        "* Desynced block (above)" +
                                                "\n §f* diff §b" + diff +
                                                "\n §f* vel §b" + data.elapsed(data.getLastVelocityTaken())
                                        );
                                MiscellaneousAlertPoster.postSetback(data.getName() + " desync §4ABOVE diff " + diff + " | " + data.elapsed(data.getLastVelocityTaken()));
                                Tasker.run(() -> {
                                    data.teleport(location);
                                });

                                data.setDidFlagMovement(true);
                                data.setLastMovementFlag(data.getTotalTicks());
                                data.setCancelHitsTick(data.getTotalTicks());

                                Karhu.getInstance().printCool("&b> &fKarhu is cancelling hits for 5 ticks USER: " + data.getBukkitPlayer().getName());

                            }

                        }
                        this.updateBlocksServerside();
                    }
                }

            } else {
                data.setUnderGhostBlock(false);
                aboveTicks = Math.max(0, aboveTicks - 0.075);
            }
        } else {
            data.setUnderGhostBlock(false);
            aboveTicks = Math.max(0, aboveTicks - 0.2);
        }
    }

    private void handleLiquidDesync(boolean collided) {
        if(!collided && !data.isLastOnGroundPacket() && !data.isPossiblyTeleporting() && !data.isTakingVertical() && data.elapsed(data.getLastVelocityTaken()) > 1) {
            double fixedLastMotion = data.deltas.lastMotionY > 0
                    ? data.deltas.lastLastMotionY += 0.03999999910593033D
                    : data.deltas.lastLastMotionY;

            double predictionLava = !data.isCollidedHorizontalClient()
                    ? (fixedLastMotion * 0.5D) - 0.02D
                    : 0.30000001192092896D;
            double predictionWater = !data.isCollidedHorizontalClient()
                    ? (fixedLastMotion * 0.800000011920929D) - 0.02D
                    : 0.30000001192092896D;

            double differenceLava = Math.abs(data.deltas.lastMotionY - predictionLava);
            double differenceWater = Math.abs(data.deltas.lastMotionY - predictionWater);

            if (differenceLava <= 1E-4
                    && data.elapsed(data.getLastInLiquid()) > 4
                    && data.elapsed(data.getLastInWeb()) >= 2
                    && data.elapsed(data.getLastFlyTick()) > 30) {

                if (Karhu.getInstance().getConfigManager().isLiquidDetect()) {
                    if (Karhu.getInstance().getConfigManager().isGbLagback() && data.elapsed(data.getLastGlide()) > 10) {
                        if (++lavaDesyncTicks >= 1 && noFakeWaterLocation != null) {
                            Location location = Setbacks.forgeToRotatedLocation(noFakeWaterLocation.clone(), data);

                            setbacked = true;

                            MiscellaneousAlertPoster.postMitigation(data, 0, "DesyncBlock",
                                    "* Desynced block (lava) " + data.elapsed(data.getLastInLiquid()) + " | " + data.deltas.lastMotionY
                            );

                            Tasker.run(() -> {
                                data.teleport(location);
                                MiscellaneousAlertPoster.postSetback(data.getName() + " desync §cLAVA");
                            });

                            data.setDidFlagMovement(true);
                            data.setLastMovementFlag(data.getTotalTicks());
                        }
                    }
                }

                data.setLastInLiquid(data.getTotalTicks());
                data.setLastInGhostLiquid(data.getTotalTicks());
                data.setOnLava(true);

                if (Karhu.getInstance().getConfigManager().isLiquidDetect()) {
                    this.updateBlocksServerside();
                }
            } else {
                lavaDesyncTicks = Math.max(lavaDesyncTicks - 0.01, 0);
            }

            if (differenceWater <= 1E-4
                    && data.elapsed(data.getLastInLiquid()) > 4
                    && !data.isWasWasOnClimbable()
                    && !data.isOnClimbable()
                    && data.elapsed(data.getLastFlyTick()) > 30) {

                if (Karhu.getInstance().getConfigManager().isGbLagback()) {
                    if(++waterDesyncTicks >= 3 && noFakeWaterLocation != null) {
                        CustomLocation location = Setbacks.forgeToRotatedLocation(data.getSafeGroundSetback(), data);

                        setbacked = true;

                        Tasker.run(() -> {
                            data.teleport(location);
                            MiscellaneousAlertPoster.postMitigation(data, 0, "DesyncBlock",
                                    "* Desynced block (water)"
                            );
                            MiscellaneousAlertPoster.postSetback(data.getName() + " desync §bWATER");
                        });

                        data.setDidFlagMovement(true);
                        data.setLastMovementFlag(data.getTotalTicks());
                    }
                }

                data.setLastInLiquid(data.getTotalTicks());
                data.setLastInGhostLiquid(data.getTotalTicks());
                data.setOnWater(true);

                this.updateBlocksServerside();
            } else {
                waterDesyncTicks = Math.max(waterDesyncTicks - 0.01, 0);
            }
        }

        if(!data.isPossiblyTeleporting()) {
            if(checkClientSideBlock(2D, MaterialChecks.LIQUID_BUCKETS)) {
                data.setLastInLiquid(data.getTotalTicks());
                data.setOnWater(true);
                data.setOnLava(true);
            }

            if(checkClientSideBlock(2D, MaterialChecks.WEB)) {
                data.setLastInWeb(data.getTotalTicks());
                data.setOnWeb(true);
                data.setInWeb(true);
            }
        }

        if(!data.isOnWater() && !data.isOnLava() && data.elapsed(data.getLastInGhostLiquid()) > 3) {
            noFakeWaterLocation = data.getLocation().toLocation(data.getWorld());
        }

    }

    private void updateBlocksServerside() {
        if (Karhu.getInstance().getConfigManager().isGbUpdate() && !collidesWithClientBlock(false) && data.isInitialized()) {
            getTileEntitiesSync(data.getBoundingBox().clone().expand(1, 1.5, 1), blocks -> {
                for (final Block block : blocks) {
                    if (block != null) {
                        Location blockLoc = block.getLocation();

                        if(!checkClientSideBlock(blockLoc.toVector(), 2)) {
                            Vector3i vector3i
                                    = new Vector3i(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
                            PlayerUtil.sendPacket(data.getBukkitPlayer(), new WrapperPlayServerBlockChange(vector3i,
                                    SpigotConversionUtil.fromBukkitMaterialData(new MaterialData(block.getType())).getGlobalId()
                            ));
                        }
                    }
                }
            });
        }
    }


    private void performSetback(String alertMessage) {
        CustomLocation location = Setbacks.forgeToRotatedLocation(data.getSafeGroundSetback(), data);

        if (!data.isPossiblyTeleporting() && !data.couldBeUnloadedClient() && data.elapsed(data.getLastFlyTick()) > 30) {
            setbacked = true;

            Tasker.run(() -> {
                if (!data.isPossiblyTeleporting() && !data.couldBeUnloadedClient() && data.getTotalTicks() > 20) {
                    data.teleport(location);
                }
            });

            data.setDidFlagMovement(true);
            data.setLastMovementFlag(data.getTotalTicks());
            data.setCancelHitsTick(data.getTotalTicks());

            MiscellaneousAlertPoster.postMitigation(data, 0, "DesyncBlock",
                    "* " + alertMessage
            );

            Karhu.getInstance().printCool("&b> &fKarhu is cancelling hits for 20 ticks USER: " + data.getBukkitPlayer().getName());
            MiscellaneousAlertPoster.postMiscPrivate(alertMessage);
        }
    }

    public boolean checkClientSideBlock(Vector vector, double radius) {
        for (BlockPlacePending block : this.getClientSideBlocks()) {
            Vector position = block.getBlockPosition();

            double distance = position.distance(vector);

            if (distance <= radius) return true;
        }

        return false;
    }

    public boolean checkClientSideBlock(double radius, Set<Material> checks) {
        for (BlockPlacePending block : this.getClientSideBlocks()) {

            if(!checks.contains(block.getItem())) continue;

            Vector position = block.getBlockPosition();

            double distance = position.distance(data.getLocation().toVector());

            if (distance <= radius) return true;
        }

        return false;
    }

    public boolean collidesWithClientBlock(boolean lenient) {

        double expand = lenient ? 3 : 0.5;

        for (BlockPlacePending block : this.getClientSideBlocks()) {

            Vector position = block.getBlockPosition();
            AxisAlignedBB blockAABB = new AxisAlignedBB(position, position, true).addCoord(1F, 1F, 1F);
            AxisAlignedBB playerAABB = data.getBoundingBox().toBB().expand(expand, expand, expand);

            if (playerAABB.intersectsWith(blockAABB)) return true;
        }

        return false;
    }

    public boolean collidesWithClientBlock(double expand) {

        for (BlockPlacePending block : this.getClientSideBlocks()) {

            Vector position = block.getBlockPosition();
            AxisAlignedBB blockAABB = new AxisAlignedBB(position, position, true).addCoord(1F, 1F, 1F);
            AxisAlignedBB playerAABB = data.getBoundingBox().toBB().expand(expand, expand, expand);

            if (playerAABB.intersectsWith(blockAABB)) return true;
        }

        return false;
    }


    public boolean collidesWithClientBlock(boolean lenient, Set<Material> checks) {

        double expand = lenient ? 2 : 0;

        for (BlockPlacePending block : this.getClientSideBlocks()) {

            if(!checks.contains(block.getItem())) continue;

            Vector position = block.getBlockPosition();
            AxisAlignedBB blockAABB = new AxisAlignedBB(position, position, true).addCoord(1F, 1F, 1F);
            AxisAlignedBB playerAABB = data.getBoundingBox().toBB().expand(expand, expand, expand);

            if (playerAABB.intersectsWith(blockAABB)) return true;
        }

        return false;
    }

    public boolean intersectsWith(AxisAlignedBB other, Vector vector)
    {
        return other.maxX >= vector.getBlockX() && other.minX <= vector.getBlockX()
                && other.maxY >= vector.getY() && other.minY <= vector.getY()
                && other.maxZ >= vector.getBlockZ() && other.minZ <= vector.getBlockZ();
    }
}
