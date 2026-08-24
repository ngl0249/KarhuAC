package me.liwk.karhu.data;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.check.api.CheckManager;
import me.liwk.karhu.data.combat.CombatData;
import me.liwk.karhu.event.AttackEvent;
import me.liwk.karhu.event.SwingEvent;
import me.liwk.karhu.handler.AbilityManager;
import me.liwk.karhu.handler.MovementHandler;
import me.liwk.karhu.handler.SimulationHandler;
import me.liwk.karhu.handler.VehicleHandler;
import me.liwk.karhu.handler.collision.CollisionHandler;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.handler.crash.CrashHandler;
import me.liwk.karhu.handler.global.DesyncedBlockHandler;
import me.liwk.karhu.handler.global.EffectManager;
import me.liwk.karhu.handler.global.TeleportManager;
import me.liwk.karhu.handler.global.world.PacketWorldManager;
import me.liwk.karhu.handler.interfaces.ICrashHandler;
import me.liwk.karhu.handler.interfaces.IVehicleHandler;
import me.liwk.karhu.handler.interfaces.KarhuHandler;
import me.liwk.karhu.handler.net.NetHandler;
import me.liwk.karhu.handler.net.TaskData;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import me.liwk.karhu.util.APICaller;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.gui.Callback;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import me.liwk.karhu.util.mc.boundingbox.BoundingBox;
import me.liwk.karhu.util.mc.vec.Vec3;
import me.liwk.karhu.util.pair.AttackSwingPair;
import me.liwk.karhu.util.pair.Pair;
import me.liwk.karhu.util.pending.VelocityPending;
import me.liwk.karhu.util.player.PlayerUtil;
import me.liwk.karhu.util.task.Tasker;
import me.liwk.karhu.util.thread.Thread;
import me.liwk.karhu.world.nms.MainSupportingBlockData;
import me.liwk.karhu.world.nms.wrap.WrappedEntityPlayer;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getLogger;

@Getter
@Setter
public final class KarhuPlayer {

    private boolean objectLoaded, removingObject = false;
    private boolean configChecked;
    private IVehicleHandler vehicleHandler;
    private UUID uuid;

    private boolean kicked;

    /*
     * Location
     */
    private CustomLocation location, lastLocation, lastLastLocation, lastLastLastLocation;
    private boolean forceRunCollisions;
    private boolean placedInside, placedCancel;
    private int lastPlacedInside;
    private int predictionTicks;
    private int moveTicks, noMoveTicks, yawFucked;

    private final Map<Integer, TaskData> tasks = new LinkedHashMap<>();

    /*
     * Checks
     */
    private CheckManager checkManager;
    private Map<Check, Set<Long>> checkViolationTimes;
    private Map<Check, Double> checkVlMap;

    private boolean cancelNextHitR, cancelNextHitH,
            reduceNextDamage, cancelBreak,
            cancelLagAbuseHits, reduceDamageRefill;
    private boolean forceCancelReach, cancelTripleHit;
    private boolean abusingVelocity, mitigatingVelocity;
    private int lastAbusingVelocity;
    private int entityIdCancel;

    private Vector3d artificialKbVector;

    private int cancelHitsTick, hitsToCancel;
    public double suspiciousActionsVl;

    /*
     * NMS Values
     */
    private float jumpMovementFactor = 0.02F, speedInAir = 0.02F, lastJumpMovementFactor;
    private float attributeSpeed = 0.1F, lastAttributeSpeed;
    private float walkSpeed = 0.1F;
    private double walkSpeedDouble = 0.1F;
    private float jumpFactor;
    private boolean jumpedCurrentTick, jumpedLastTick, jumped;
    private String pressedKey;
    private Block blockBelow, blockInside, lastBlockInside;
    private boolean exitingVehicle;
    private String keyCombo;

    /*
     * Version
     */
    private ClientVersion clientVersion;
    private boolean isLegacy, isNewerThan8, isNewerThan12, isNewerThan13, isNewerThan16, isNewerThan1_21_4;
    private boolean confirmedVersion, confirmedVersion2;

    private boolean isViaMCP;

    /*
     * Handlers
     */
    private KarhuHandler collisionHandler;
    private DesyncedBlockHandler desyncedBlockHandler;
    private MovementHandler movementHandler;

    private SimulationHandler simulationHandler;
    private NetHandler netHandler;
    private ICrashHandler crashHandler;

    public boolean extraCache = false;

    //private final InvalidMoveHandler invalidMoveHandler;

    /*
     * Wrapped stuff
     */
    private WrappedEntityPlayer wrappedEntity;

    /*
     * Transaction
     */

    //Transaction
    private boolean hasReceivedTransaction = false, hasReceivedKeepalive = false, isHasTeleportedOnce = false;

    public int sentConfirms, receivedConfirms;

    public Map<Short, ObjectArrayList<Consumer<Short>>> waitingConfirms = new HashMap<>();

    private short transactionId, confirmId, tickTransactionId;

    private short firstTransaction = 996;

    private int tickFirstConfirmationUid = -1, tickSecondConfirmationUid = -2;
    private int lastTickFirstConfirmationUid = -1, lastTickSecondConfirmationUid = -2;

    public boolean hasSentTickFirst = false, sendingPledgePackets;
    public boolean hasSentFirst;

    private final TaskManager<KarhuPlayer> tasksManager = new TaskManager<>();

    public short lastPostId = -100;

    public List<Integer> rodPulls = new ArrayList<>();
    public AxisAlignedBB rodPullBox;
    public double rodPullLeniencyXZ, rodPullLeniencyY,
            lastRodPullLeniencyXZ, lastRodPullLeniencyY,
            lastLastRodPullLeniencyXZ, lastLastRodPullLeniencyY;
    public int lastRodPullTick;

    /*
     * Velocity
     */
    public final Map<Integer, ConcurrentLinkedDeque<VelocityPending>> velocityPending = new HashMap<>();
    private boolean takingVertical, confirmingVelocity, needExplosionAdditions;
    private int lastVelocityYReset, lastVelocityXZReset, lastVelocityTaken,
            velocityYTicks, maxVelocityXZTicks, maxVelocityYTicks,
            lastRelativeVelo;
    private double velocityX, velocityY, velocityZ, velocityHorizontal, confirmingY;
    private Vector tickedVelocity = null, holdVelo = null;

    /*
     * Ticks & booleans
     */
    private int totalTicks, lastSprintTick, lastSneakTick, airTicks,
            digTicks, fastDigTicks, digStopTicks, placeTicks, underPlaceTicks, bucketTicks,
            useTicks, lClientAirTicks, clientAirTicks, lastFlyTick, lastAllowFlyTick, elapsedOnLiquid,
            elapsedUnderBlock, elapsedSinceBridgePlace, serverGroundTicks, clientGroundTicks, weirdTicks, lastInBerry,
            lastInUnloadedChunk, lastInLiquid, lastOnSlime, lastOnSoul, lastOnIce, lastOnClimbable, lastOnBed,
            lastInWeb, lastCollided, lastCollidedWithEntity, lastOnHalfBlock, lastCollidedV, lastCollidedVGhost,
            lastOnBoat, lastInPowder, lastPossibleInUnloadedChunk, lastCollidedGhost, positionPackets,
            lastInLiquidOffset, lastConfirmingState, lastCollidedH, lastSneakEdge, lastFence, lastPortal, lastOnDripLeaf,
            lastOnScaffolding, lastSteppedOnSlime, timeInLiquid, frictionUncertain, lastOnHoneySide;

    private boolean digging, diggingBasic, traceDigging, placing, wasPlacing, skipNextSwing, noMoveNextFlying,
            collidedHorizontally, wasCollidedHorizontally, collidedHorizontalClient, collidedWithFence,
            edgeOfFence, collidedWithPane, collidedWithCactus, insideTrapdoor, insideBlock, wasFullyInsideBlock,
            fullyInsideBlock, inWeb, isWasInWeb, isWasWasInWeb, blocking, shit, bowing, finalCollidedH, lastCollideH,
            horiStairs, horiDoors, horiHalfs, flyingBeforeTickEnd;

    private Vector lastAbortLoc;
    private int lastPistonPush, lastSlimePistonPush;

    public int moveCalls;

    private long serverTick, createdOnTick;

    private Long pendingSwingTime = null;

    /*
     * BoundingBox
     */
    private BoundingBox boundingBox, mcpCollision, lastBoundingBox;

    private double vehicleX, vehicleY, vehicleZ;

    public int ticksOnGhostBlock, ticksOnBlockHandlerNotEnabled, updateBuf;

    /*
     * Attack
     */
    private int lastTarget;
    private List<Integer> lastTargets = new ArrayList<>();
    public int lastAttackTick = 99, attacks;
    private long lastAttackPacket;
    public boolean attackedSinceVelocity;
    private List<CombatData> combatDataClose = new ArrayList<>();
    private List<CombatData> combatDataFar = new ArrayList<>();
    private boolean reachBypass;
    private double interactionRange = 3;

    /*
     * State
     */
    private boolean sprinting, wasSprinting, wasWasSprinting, sneaking, wasSneaking, wasWasSneaking,
            usingItem, lastUsingItem, eating, lastEating, recorrectingSprint, desyncSprint = true, inventoryOpen,
            crouching, inBed, lastInBed, sprintAttribute, resettingSprint, invalidSprint, metadataSprint, settingMetadataSprint;

    private int bedTicks, sprintAttributeTick;

    private Vec3 bedPos;

    private int invStamp, slotSwitchTick, lastWorldChange;

    public boolean cinematic;
    public int lastCinematic;

    public final Deltas deltas = new Deltas();

    public float fallDistance, lFallDistance;

    /*
     * Reach
     */
    public ConcurrentHashMap<Integer, EntityData> entityData = new ConcurrentHashMap<>();

    public int lastPos;
    public double attackerX, attackerY, attackerZ;
    public float attackerYaw, attackerPitch;
    public int teleports, addedTeleports;

    private List<Double> eyesFourteen = Arrays.asList(0.4, 1.27, 1.62);
    private List<Double> eyesNine = Arrays.asList(0.4, 1.54, 1.62);
    private List<Double> eyesLegacy = Arrays.asList((double)1.54F, (double)1.62F);

    /*
     * Collisions
     */
    private boolean onGroundServer, onBoat, groundNearBox, groundNearBoxBelow, wasOnGroundServer, onWater, wasOnWater, wasWasOnWater,
            aboveButNotInWater, waterAlmostOnFeet, onLava, wasOnLava, onIce, onLiquid, onSlab, wasOnSlab, onDoor, wasOnDoor,
            onFence, wasOnFence, onPortal, onStairs, wasOnStairs, onBed, wasOnBed, underBlock, underBlockStrict, wasUnderBlock, underWeb, onWeb,
            onSoulsand, wasOnSoulSand, wasSlimeLand, slimeLand, onSlime, wasOnSlime, wasWasOnSlime, onCarpet,
            onComparator, wasOnComparator, onClimbable, wasOnClimbable, wasWasOnClimbable, onLadder, lastLadder, nearClimbable,
            onHoney, onSweet, wasOnHoney, onScaffolding, onPiston, onTopGhostBlock, atButton, onWaterOffset, lastOnWaterOffset,
            inPowder, sneakEdge, lastBlockSneak, atSign, lastOnGroundServer, nearDoor, steppedOnSlime, ableToStep, nextTickOnGround,
            insideWater, nearHugeBelowSolid, bedBelowHuge, belowSolid, onHoneySide;

    private Material movementBlock, airMovementBlock;

    private boolean isNotGroundBridging, waterUnderBoat;

    private boolean collidedWithLivingEntity;

    private float currentFriction, lastTickFriction;
    private MainSupportingBlockData mainSupportingBlock, lastMainSupportingBlock;

    private boolean onGroundPacket, lastOnGroundPacket, lastLastOnGroundPacket;
    private boolean onGroundMath, lastOnGroundMath, lastLastOnGroundMath;

    private boolean onGhostBlock, underGhostBlock, isWasUnderGhostBlock;
    private int lastInGhostLiquid, lastPushedByWater;
    public double ghostBlockSetbacks;

    /*
     * Ping
     */
    private long ping, lastPingTime, transactionPing, lastTransactionPing;
    public final Map<Short, Long> transactionTime = new ConcurrentHashMap<>();
    public short timerTransactionSent;
    private boolean inUnloadedChunk, wasInUnloadedChunk, wasWasInUnloadedChunk;
    private long lastTransaction, lastTransactionPingUpdate;
    private boolean readyToAccept = false;

    public int badPingTicks;

    public int pingInTicks;

    //private PacketFrame currentPacketFrame;

    /*
     * Teleport
     */
    private TeleportManager teleportManager;
    private boolean possiblyTeleporting, seventeenPlacing;
    private int lastTeleport;
    private long lastTeleportPacket;
    private CustomLocation firstChunkMove;

    private boolean joining, fuckedTeleport;

    /*
     * Game session
     */
    private boolean banned;
    private Player bukkitPlayer;
    private User user;
    private int entityId = -1;

    private PacketWorldManager packetWorldManager;

    private String brand = "vanilla (not set)", cleanBrand = "vanilla (not set)";

    private boolean brandPosted = false;

    private EffectManager effectManager;
    public int jumpBoost, cacheBoost, speedBoost, slowness, haste, fatigue;
    public int lastJumpBoostChange;

    public GameMode gameMode;

    public boolean allowFlying, flyingS, flyingC, flying, wasFlyingC, initedFlying, confirmingFlying, correctedFly;

    private AbilityManager abilityManager;

    public int lastServerSlot;

    public long lastFlying, flyingTime, lastJoinTime;
    public int lastFlyingTicks, velocityXZTicks, lastTransactionTick, trackCount;

    public int currentServerTransaction = -1, currentClientTransaction = -1, lastClientTransaction, lastLastClientTransaction;

    private boolean firstTransactionSent;

    public int lastDroppedPackets, lastPacketDrop, hurtTicks;
    private long lastFast;

    private boolean movementDesynced, riding, brokenVehicle, ridingUncertain;
    private int vehicleId;
    private EntityType vehicle;
    private int lastUnmount;

    private final AtomicInteger packetSequence = new AtomicInteger(0);

    public final List<Pair<Integer, Integer>> exemptMap = new ArrayList<>();
    public final Map<SubCategory, Pair<Integer, Integer>> exemptCategoryMap = new HashMap<>();

    private double cps, lastCps, highestCps, highestReach;

    private boolean didFlagMovement;
    private int lastMovementFlag;
    private CustomLocation safeSetback, safeGroundSetback, flyCancel, teleportLocation;

    private long lastLocationUpdate;
    public int invalidMovementTicks;

    /*
     * Mouse
     */

    private int sensitivity = -1, inputX, inputY;
    private float sensitivityY = -1, sensitivityX = -1, smallestRotationGCD;
    private float pitchGCD = 9999, yawGCD;

    private float predictPitch, predictYaw;

    /*
     * Shit
     */

    public Vec3 eyeLocation;
    public Vec3 look, lookMouseDelayFix;
    public boolean locationInited, boundingBoxInited;
    public int locationInitedAt;
    public long createdAt, transactionClock;
    private int currentSlot;
    private boolean pendingBackSwitch;
    public final Map<Integer, Deque<Integer>> backSwitchSlots = new HashMap<>();

    private boolean timerKicked = false;

    private Thread thread;

    /*
     * 1.9+ / other versions
     */
    private boolean gliding = false, riptiding = false, spectating = false;
    private int lastGlide = 0, lastRiptide = 0;
    public int dolphinLevel = 0, soulSpeedLevel = 0, depthStriderLevel = 0, slowFallingLevel = 0, levitationLevel = 0;

    /*
    Click record
     */
    private String recordingName;

    private boolean recording, replaying;

    private final Queue<AttackSwingPair> recordingSamples = new LinkedList<>();

    public KarhuPlayer(User user, Karhu karhu, long now) {
        this.createdAt = now;
        this.uuid = user.getUUID();

        this.forceRunCollisions = true;

        //this.thread = Karhu.getInstance().getThreadManager().generate();

        this.teleportManager = new TeleportManager(this);
        this.abilityManager = new AbilityManager(this);
        this.packetWorldManager = new PacketWorldManager(this);

        if (uuid != null) {
            this.bukkitPlayer = Bukkit.getPlayer(uuid);
        }
        if (bukkitPlayer != null) this.entityId = this.bukkitPlayer.getEntityId();

        this.transactionId = Short.MIN_VALUE;

        this.checkViolationTimes = new HashMap<>();
        this.checkVlMap = new HashMap<>();

        this.location = new CustomLocation(0,0,0,0,0);
        this.lastLocation = this.location;
        this.lastLastLocation = this.lastLocation;
        this.lastLastLastLocation = this.lastLastLocation;

        this.simulationHandler = new SimulationHandler(this);

        this.checkManager = new CheckManager(this, karhu);

        if (bukkitPlayer != null) {
            this.gameMode = GameMode.getById(bukkitPlayer.getGameMode().getValue());
        }

        updateClientVersion(user.getClientVersion());
        this.user = user;

        this.collisionHandler = new CollisionHandler(this);
        this.desyncedBlockHandler = new DesyncedBlockHandler(this);
        this.movementHandler = new MovementHandler(this);
        this.netHandler = new NetHandler(this);
        this.crashHandler = new CrashHandler(this);
        this.vehicleHandler = new VehicleHandler(this);
        this.wrappedEntity = new WrappedEntityPlayer(this);

        this.teleportManager = new TeleportManager(this);
        this.effectManager = new EffectManager(this);

        this.inUnloadedChunk = true;

        BoundingBox BB = new BoundingBox(this,
                this.location.x - 0.3, this.location.y, this.location.z - 0.3,
                this.location.x + 0.3, this.location.y + 1.8, this.location.z + 0.3
        );

        this.boundingBox = BB.clone();
        this.lastBoundingBox = BB.clone();
        this.mcpCollision = BB.clone();

        this.confirmedVersion = false;
        this.locationInited = false;
        this.boundingBoxInited = false;

        this.lastJoinTime = System.currentTimeMillis();
        this.serverTick = Karhu.getInstance().getServerTick();
        this.createdOnTick = Karhu.getInstance().getServerTick();
        this.objectLoaded = true;

        this.collisionHandler.cacheBlocks();

        if (bukkitPlayer != null) {
            updateState(bukkitPlayer);
        }
    }

    public void tick() {
        //todo - here we can handle certain things like forcing a player to release their item or
        // force close inventory ect.
        this.tasksManager.doTasks();
    }

    public int getViolations(Check<?> check, Long time) {
        final Set<Long> timestamps = this.checkViolationTimes.get(check);
        final long sys = System.currentTimeMillis();

        int vl = 0;

        if (timestamps != null) {
            for (Long man : timestamps) {
                if (sys - man <= time) {
                    ++vl;
                } else {
                    timestamps.remove(man);
                }
            }
            return vl;
        }
        return 0;
    }

    public void addViolation(Check<?> check) {
        Set<Long> timestamps = this.checkViolationTimes.get(check);
        if (timestamps == null) {
            timestamps = ConcurrentHashMap.newKeySet();
        }
        timestamps.add(System.currentTimeMillis());
        this.checkViolationTimes.put(check, timestamps);
    }

    public void addViolations(Check<?> check, int vl) {
        final long sys = System.currentTimeMillis();
        for (int i = 0; i < vl; i++) {
            Set<Long> timestamps = this.checkViolationTimes.get(check);
            if (timestamps == null) {
                timestamps = ConcurrentHashMap.newKeySet();
            }
            timestamps.add(sys + i);
            this.checkViolationTimes.put(check, timestamps);
        }
    }

    public double getCheckVl(Check<?> check) {
        if (!this.checkVlMap.containsKey(check)) {
            this.checkVlMap.put(check, 0.0);
        }
        return this.checkVlMap.get(check);
    }

    public void setCheckVl(double vl, Check<?> check) {
        if (vl < 0.0) {
            vl = 0.0;
        }
        this.checkVlMap.put(check, vl);
    }

    public short getNextTransactionId() {
        ++transactionId;

        if (transactionId > -20001)
            transactionId = Short.MIN_VALUE;

        return transactionId;
    }

    public short getNextTransactionIdSilent() {
        short predict = transactionId;

        ++predict;

        if (predict > -20001)
            predict = Short.MIN_VALUE;

        return predict;
    }

    public void sendTransaction() {
        if (user.getConnectionState() != ConnectionState.PLAY) return;
        short id = getNextTransactionId();

        if (user != null) {
            PlayerUtil.sendPacket(user, id);
        } else if (bukkitPlayer != null) {
            PlayerUtil.sendPacket(bukkitPlayer, id);
        }
    }

    public void sendTransactionLogin(User user) {
        if (user.getConnectionState() != ConnectionState.PLAY) return;
        short id = getNextTransactionId();

        firstTransaction = id;

        PlayerUtil.sendPacket(user, id);
    }

    public void useOldTransaction(Consumer<Short> callback, short uid) {
        ObjectArrayList<Consumer<Short>> map = waitingConfirms.computeIfAbsent(uid, k -> new ObjectArrayList<>());
        map.add(callback);
        waitingConfirms.put(uid, map);
    }

    public void queueToPrePing(Callback<Integer> callback) {
        this.netHandler.queueToPrePing(callback);
    }

    public void queueToPostPing(Callback<Integer> callback) {
        this.netHandler.queueToPostPing(callback);
    }

    public void queueToFlying(int delay, Callback<Integer> callback) {
        int key = totalTicks + delay;
        if (tasks.containsKey(key)) {
            tasks.get(key).addTask(callback);
        } else {
            tasks.put(key, new TaskData(key, callback));
        }
    }

    public int mostRecentPing() {
        return this.netHandler.mostRecentPing();
    }

    public boolean hasExempt() {
        return !exemptMap.isEmpty() || !exemptCategoryMap.isEmpty();
    }

    public void updateClientVersion(ClientVersion version) {
        if(version == null) version = ClientVersion.getById(Karhu.SERVER_VERSION.getProtocolVersion());

        this.clientVersion = version;
        this.isLegacy = version.isOlderThanOrEquals(ClientVersion.V_1_7_10);
        this.isNewerThan8 = version.isNewerThan(ClientVersion.V_1_8);
        this.isNewerThan12 = version.isNewerThan(ClientVersion.V_1_12_2);
        this.isNewerThan13 = version.isNewerThan(ClientVersion.V_1_13_2);
        this.isNewerThan16 = version.isNewerThan(ClientVersion.V_1_16_4);
        this.isNewerThan1_21_4 = version.isNewerThan(ClientVersion.V_1_21_4);
    }

    public ClientVersion getClientVersion() {
        return this.clientVersion == null
                ? ClientVersion.getById(Karhu.SERVER_VERSION.getProtocolVersion())
                : clientVersion;
    }

    public boolean isBadClientVersion() {
        return this.clientVersion == null || this.clientVersion == ClientVersion.UNKNOWN;
    }

    public boolean legacyTeleports() {
        return clientVersion.isOlderThanOrEquals(ClientVersion.V_1_8)
                || Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_8_8);
    }

    public boolean canGlide() {
        if (clientVersion.isNewerThanOrEquals(ClientVersion.V_1_9)) {
            ItemStack chestPlate = bukkitPlayer.getInventory().getChestplate();
            return chestPlate != null && MaterialChecks.ELYTRA.contains(chestPlate.getType());
        }
        return false;
    }

    public void handleKickAlert(String type) {
        if (!kicked) {
            Tasker.run(() -> bukkitPlayer.kickPlayer(Karhu.getInstance().getConfigManager().getAnticrashKickMsg()));
            MiscellaneousAlertPoster.postMisc(Karhu.getInstance().getConfigManager().getAntiCrashMessage().replaceAll("%debug%", type).replaceAll("%player%", bukkitPlayer.getName()), this, "Crash");
            Karhu.getInstance().getLogger().warning("-----------------Karhu Anticrash-----------------");
            Karhu.getInstance().getLogger().warning(bukkitPlayer.getName() + " was kicked for suspicious packets (" + type + ")");
            Karhu.getInstance().getLogger().warning("Keep an eye on the player!");
            Karhu.getInstance().getLogger().warning("-----------------Karhu Anticrash-----------------");
            //PacketEvents.getAPI().getPlayerUtils().ejectPlayer(bukkitPlayer);
            kicked = true;
        }
    }

    @Deprecated
    public boolean recentlyTeleported(int ticks) {
        return this.totalTicks - this.lastTeleport <= ticks;
    }


    @Deprecated
    public boolean couldBeTeleporting(int ticks) {
        return this.totalTicks - this.lastTeleport <= ticks || this.isPossiblyTeleporting();
    }

    @Deprecated
    public boolean couldBeUnloadedClient() {
        return elapsed(getLastInUnloadedChunk()) <= MathUtil.getPingInTicks(getTransactionPing()) + 2
                || elapsed(getLastWorldChange()) <= MathUtil.getPingInTicks(getTransactionPing()) + 10
                || elapsed(getLastPossibleInUnloadedChunk()) <= 2;
    }

    public boolean hasFast() {
        return this.lastFlying != 0L && this.lastFast != 0L && ((this.lastFlying - this.lastFast) / 1E6) < 100L;
    }

    public boolean isLagging(int ticks) {
        return ticks - this.lastDroppedPackets < 2;
    }

    public boolean isLagging(int ticks, int time) {
        return ticks - this.lastDroppedPackets < time;
    }

    public int elapsed(int i) {
        if (totalTicks - i == totalTicks) return 1000;
        return totalTicks - i;
    }

    public boolean isHasDig() {
        return elapsed(digStopTicks) <= 8 || elapsed(digTicks) <= 3 || digging;
    }

    public boolean isHasDig2() {
        return elapsed(digStopTicks) <= 8 || digging;
    }

    public long elapsedMS(long i) {
        return System.currentTimeMillis() - i;
    }

    public long elapsedMS(long now, long time) {
        return (long) ((now - time) / 1E6);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void velocityTick(VelocityPending velocityPending, boolean artificial) {
        setLastVelocityTaken(getTotalTicks());
        setVelocityXZTicks(0);
        setVelocityYTicks(0);

        Vector velocityVector = velocityPending.getVelocity();

        if (velocityPending.isRelative()) {
            setLastRelativeVelo(getTotalTicks());
            if (!artificial) {
                deltas.deltaXKb += velocityVector.getX();
                deltas.deltaYKb += velocityVector.getY();
                deltas.deltaZKb += velocityVector.getZ();
                setVelocityX(deltas.deltaXKb);
                setVelocityY(deltas.deltaYKb);
                setVelocityZ(deltas.deltaZKb);

                velocityVector.setX(getVelocityX());
                velocityVector.setY(getVelocityY());
                velocityVector.setZ(getVelocityZ());
                //Bukkit.broadcastMessage("GRRPAU Not artificial EXPLOSION Velocity ticked " + totalTicks);
            } else {
                deltas.lastDeltaXKb += velocityVector.getX();
                deltas.lastDeltaYKb += velocityVector.getY();
                deltas.lastDeltaZKb += velocityVector.getZ();
                setVelocityX(deltas.lastDeltaXKb);
                setVelocityY(deltas.lastDeltaYKb);
                setVelocityZ(deltas.lastDeltaZKb);

                velocityVector.setX(getVelocityX());
                velocityVector.setY(getVelocityY());
                velocityVector.setZ(getVelocityZ());
                //Bukkit.broadcastMessage(ChatColor.RED + "FAFAFA Artificially ticked EXPLOSION Velocity " + totalTicks);
            }
        } else {
            //Bukkit.broadcastMessage("FFFRRRRRAH Normal velocity " + totalTicks);
            deltas.deltaXKb = velocityVector.getX();
            deltas.deltaYKb = velocityVector.getY();
            deltas.deltaZKb = velocityVector.getZ();
            setVelocityX(velocityVector.getX());
            setVelocityY(velocityVector.getY());
            setVelocityZ(velocityVector.getZ());
        }

        final int velocityH = (int) Math.ceil((Math.abs(velocityVector.getX())
                + Math.abs(velocityVector.getZ())) / 2D + 2D) * 4;
        final int velocityV = (int) Math.ceil(FastMath.pow(Math.abs(velocityVector.getY()) + 2, 2)) * 2;

        setMaxVelocityXZTicks((velocityH + velocityV) + 5);
        setMaxVelocityYTicks(velocityV);

        setTakingVertical(true);

        setVelocityHorizontal(MathUtil.hypot(velocityVector.getX(), velocityVector.getZ()));

        setTickedVelocity(velocityVector.clone());
        setHoldVelo(velocityVector.clone());

        setConfirmingVelocity(false);
        setNeedExplosionAdditions(false);
    }

    public void checkVelocity() {
        double lastDX = deltas.lastDX;
        double lastMotionY = deltas.lastMotionY;
        double lastDZ = deltas.lastDZ;

        for (Map.Entry<Integer, ConcurrentLinkedDeque<VelocityPending>> pendingEntry : velocityPending.entrySet()) {
            ConcurrentLinkedDeque<VelocityPending> velocities = pendingEntry.getValue();
            processVelocities(velocities, lastDX, lastMotionY, lastDZ);
        }
    }

    private void processVelocities(ConcurrentLinkedDeque<VelocityPending> velocities, double lastDX, double lastMotionY, double lastDZ) {
        // Sort by sequence to process in arrival order
        List<VelocityPending> sorted = new ArrayList<>(velocities);
        Collections.sort(sorted, new Comparator<VelocityPending>() {
            public int compare(VelocityPending a, VelocityPending b) {
                return Integer.compare(a.getSequence(), b.getSequence());
            }
        });

        for (VelocityPending velocityCheck : sorted) {
            if (velocityCheck.isMarkedSent()) {
                continue;
            }

            Vector velocity = velocityCheck.getVelocity();
            double ogHz = MathUtil.hypot(velocity.getX(), velocity.getZ());


            if (velocityCheck.isRelative()) {
                lastDX += velocity.getX();
                lastMotionY += velocity.getY();
                lastDZ += velocity.getZ();
            } else {
                lastDX = velocity.getX();
                lastMotionY = velocity.getY();
                lastDZ = velocity.getZ();
            }

            double kbToUseX = velocityCheck.isRelative() ? lastDX : velocity.getX();
            double kbToUseY = velocityCheck.isRelative() ? lastMotionY : velocity.getY();
            double kbToUseZ = velocityCheck.isRelative() ? lastDZ : velocity.getZ();

            simulationHandler.simulateMovement(kbToUseX, kbToUseZ, true);

            double kbY = Math.abs(kbToUseY) < clamp() ? 0 : kbToUseY;

            // Keep Vector objects for distance calculations
            Vector knockbackVector = new Vector(simulationHandler.getTestOutputX(), kbY, simulationHandler.getTestOutputZ());
            Vector playerVector = new Vector(deltas.deltaX, deltas.motionY, deltas.deltaZ);

            double horizontalDistance = MathUtil.horizontalDistance(knockbackVector, playerVector);
            double verticalDistance = MathUtil.verticalDistance(knockbackVector, playerVector);

            double precisionY = calculatePrecisionY();
            double precisionH = calculatePrecisionH(ogHz, horizontalDistance);

            if (horizontalDistance <= precisionH && verticalDistance <= precisionY) {
                MiscellaneousAlertPoster.postMiscPrivate(
                        bukkitPlayer.getName() + " split velocity found 2nd " + velocityCheck.getId()
                );
                velocityTick(velocityCheck, true);
                velocityCheck.markSent();
            }
        }
    }

    private double calculatePrecisionY() {
        if (elapsed(lastCollidedV) <= 2) {
            return 0.205;
        }
        return (moveTicks <= 2 || elapsed(predictionTicks) <= 1) ? offsetMove() : 0.001;
    }

    private double calculatePrecisionH(double ogHz, double horizontalDistance) {
        if (elapsed(lastCollidedH) <= 2) {
            return Math.max(ogHz, horizontalDistance);
        }
        return (moveTicks <= 2 || elapsed(predictionTicks) <= 1) ? offsetMove() : 0.001;
    }


    public double offsetMove() {
        return clientVersion.isNewerThanOrEquals(ClientVersion.V_1_18_2) ? 0.0002 : 0.03;
    }
    public double clamp() {
        return clientVersion.getProtocolVersion() > 47 ? 0.003D : 0.005D;
    }

    public ConcurrentLinkedDeque<VelocityPending> getTickVelocities(int transactionId) {
        return this.velocityPending.get(transactionId);
    }

    public ItemStack getStackInHand() {
        if(bukkitPlayer == null) return new ItemStack(Material.AIR);
        ItemStack stack = bukkitPlayer.getInventory().getItem(this.currentSlot);
        return stack == null ? new ItemStack(Material.AIR) : stack;
    }

    public boolean isInitialized() {
        return bukkitPlayer != null;
    }

    public String getName() {
        if(bukkitPlayer == null) return "null_player";
        return bukkitPlayer.getName();
    }

    public boolean isFlyingBukkit() {
        if(bukkitPlayer == null) return false;
        return bukkitPlayer.isFlying();
    }

    public boolean isAllowFlyingBukkit() {
        if(bukkitPlayer == null) return false;
        return bukkitPlayer.getAllowFlight();
    }

    public World getWorld() {
        if(bukkitPlayer == null) return findWorld();
        return bukkitPlayer.getWorld();
    }

    public void teleport(Location location) {
        if(bukkitPlayer == null) return;
        if(getWorld() != location.getWorld()) return;

        invalidMovementTicks = 0;

        bukkitPlayer.teleport(location);
    }

    public void closeInventory() {
        if(bukkitPlayer == null) return;
        bukkitPlayer.closeInventory();
        queueToPrePing((task) -> inventoryOpen = false);
    }

    public void teleport(CustomLocation location) {
        if(bukkitPlayer == null) return;

        invalidMovementTicks = 0;

        bukkitPlayer.teleport(location.toLocation(getWorld()));
    }

    private World findWorld() {
        if(entityId != -1) {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : SpigotReflectionUtil.getEntityList(world)) {
                    if (entity.getEntityId() == entityId) {
                        return world;
                    }
                }
            }
        }
        return Bukkit.getWorld(Bukkit.getWorlds().get(0).getUID());
    }

    public boolean isNearWorldBorder(double leniency) {
        WorldBorder border = getWorld().getWorldBorder();
        Location center = border.getCenter();

        double radius = border.getSize() / 2 - leniency;

        double minX = center.getX() - radius;
        double minZ = center.getZ() - radius;

        double maxX = center.getX() + radius;
        double maxZ = center.getZ() + radius;

        return location.x < minX
                || location.x > maxX
                || location.z < minZ
                || location.z > maxZ;
    }

    public boolean isNearWorldBorder() {
        return isNearWorldBorder(0.5);
    }

    public boolean rodPullAffecting() {
        return elapsed(lastRodPullTick) <= 3;
    }

    public List<Double> getEyePositions() {
        if (this.isNewerThan13) {
            return eyesFourteen;
        } else if (this.isNewerThan8) {
            return eyesNine;
        } else {
            return eyesLegacy;
        }
    }

    private void handleRecord() {
        File dataFolder = Karhu.getInstance().getDataFolder();
        File replaysFolder = new File(dataFolder, "replays");

        if (!replaysFolder.exists()) {
            replaysFolder.mkdirs();
        }

        File file = new File(replaysFolder, recordingName + ".txt");

        try {
            Files.write(file.toPath(),
                    recordingSamples.stream()
                            .map(String::valueOf)
                            .collect(Collectors.toList()),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE);

            int lineNumber = Files.readAllLines(Paths.get(file.toURI())).size();

            System.out.println("Recorded " + lineNumber + " clicks ...");
        } catch (IOException e) {
            getLogger().severe("Error writing to file: " + e.getMessage());
        }
    }

    public void updateState(Player player) {
        if (Karhu.getInstance().getConfigManager().isGeyserSupport()) {
            if (PlayerUtil.isGeyserPlayer(player.getUniqueId())) {
                Karhu.getInstance().getDataManager().remove(player.getUniqueId());
                return;
            }
        }

        if (Karhu.getInstance().getConfigManager().isGeyserPrefixCheck()) {
            if (player.getName().contains(Karhu.getInstance().getConfigManager().getGeyserPrefix())) {
                Karhu.getInstance().getDataManager().remove(player.getUniqueId());
                return;
            }
        }

        setBukkitPlayer(player);
        getCollisionHandler().cacheBlocks();

        if (Karhu.isAPIAvailable()) {
            APICaller.callRegister(player);
        }

        final boolean permission = player.hasPermission("karhu.alerts");

        if (player.hasPermission("karhu.mitigations")) {
            Karhu.getInstance().getAlertsManager().setMitigations(player, true);
        }

        Tasker.taskAsync(() -> {

            if (permission) {
                if (Karhu.getInstance().getConfigManager().isCrackedServer()) {
                    Karhu.getInstance().getAlertsManager().setReceiveAlerts(player,
                            Karhu.getStorage().getAlerts(player.getName()));
                } else {
                    Karhu.getInstance().getAlertsManager().setReceiveAlerts(player,
                            Karhu.getStorage().getAlerts(player.getUniqueId().toString()));
                }
            }

            if (!Karhu.getInstance().getConfigManager().isLogSync()) {
                if (!Karhu.getInstance().getConfigManager().isCrackedServer()) {
                    Karhu.getStorage().loadActiveViolations(player.getUniqueId().toString(), this);
                } else {
                    Karhu.getStorage().loadActiveViolations(player.getName(), this);
                }
            }

        });
    }

    public void handleClick(AttackSwingPair sample) {

        Long attackTime = sample.getAttackTime();
        Long swingTime = sample.getSwingTime();

        if (attackTime == null) {
            checkManager.runChecks(checkManager.getPacketChecks(), new SwingEvent(0, 0, Math.toIntExact(swingTime)), null);
        } else {
            checkManager.runChecks(checkManager.getPacketChecks(), new SwingEvent(0, 0, Math.toIntExact(swingTime)), null);
            checkManager.runChecks(checkManager.getPacketChecks(), new AttackEvent(-69, true, 0, 0, Math.toIntExact(attackTime)), null);
        }
    }

    public int getNextSequence() {
        int seq = packetSequence.incrementAndGet();
        if (seq < 0) { // Overflowed
            packetSequence.set(0);
            return 0;
        }
        return seq;
    }


}
