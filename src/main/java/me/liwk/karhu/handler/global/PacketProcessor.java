package me.liwk.karhu.handler.global;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketLoginSendEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.check.impl.movement.fly.FlyA;
import me.liwk.karhu.check.setback.KnockbackUtil;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.data.combat.CombatData;
import me.liwk.karhu.data.potion.PotionEffect;
import me.liwk.karhu.event.*;
import me.liwk.karhu.handler.PlayerHandler;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.TeleportPosition;
import me.liwk.karhu.util.VersionBridgeHelper;
import me.liwk.karhu.util.benchmark.Benchmark;
import me.liwk.karhu.util.benchmark.BenchmarkType;
import me.liwk.karhu.util.benchmark.KarhuBenchmarker;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.pair.AttackSwingPair;
import me.liwk.karhu.util.pending.BlockPlacePending;
import me.liwk.karhu.util.player.BlockUtil;
import me.liwk.karhu.util.task.Tasker;
import me.liwk.karhu.util.update.MovementUpdate;
import me.liwk.karhu.world.nms.NMSValueParser;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Objects;

import static com.github.retrooper.packetevents.protocol.player.DiggingAction.RELEASE_USE_ITEM;
import static com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT;
import static com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction.*;

public final class PacketProcessor extends SimplePacketListenerAbstract {


    private final Karhu plugin;

    public PacketProcessor(Karhu plugin) {
        super(PacketListenerPriority.MONITOR);
        this.plugin = plugin;
    }


    @Override
    public void onPacketLoginSend(PacketLoginSendEvent event) {
        User user = event.getUser();
        long now = System.nanoTime();
        if (event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS) {
            event.getTasksAfterSend().add(() -> {
                Karhu.getInstance().getDataManager().add(user, now);
            });
        }
    }

    public void onPacketPlayReceive(PacketPlayReceiveEvent e) {
        long nano = System.nanoTime();
        long nowTimeMillis = System.currentTimeMillis();

        PacketPlayReceiveEvent cloned = null;

        if (e.getUser() == null || e.getUser().getUUID() == null) {
            Karhu.getInstance().printCool("&b> &cPacket received from an user that is defined as null in PacketEvents " + e.getPacketName());
            return;
        }

        KarhuPlayer data = this.plugin.getDataManager().getPlayerData(e.getUser());
        final PacketType.Play.Client type = e.getPacketType();

        boolean handleOthers = true;

        if (data == null) {
            return;
        }

        if (data.getBukkitPlayer() == null && e.getPlayer() != null) {
            data.updateState(e.getPlayer());
        }

        if (!data.isReadyToAccept()) {
            if (type != PacketType.Play.Client.WINDOW_CONFIRMATION
                    && type != PacketType.Play.Client.PONG
                    && type != PacketType.Play.Client.PLUGIN_MESSAGE
                    && type != PacketType.Play.Client.KEEP_ALIVE
                    && type != PacketType.Play.Client.CLIENT_SETTINGS) {
                e.setCancelled(true);
                return;
            }
        }

        if (WrapperPlayClientPlayerFlying.isFlying(type)) {

            //cloned = e.clone();

            final WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(e);

            com.github.retrooper.packetevents.protocol.world.Location location = packet.getLocation();

            if (Karhu.getInstance().getConfigManager().isAnticrash()
                    && Karhu.getInstance().getConfigManager().isLargeMove()) {
                if (Math.abs(location.getX()) > 3.0E+7
                        || Math.abs(location.getY()) > 3.0E+7
                        || Math.abs(location.getZ()) > 3.0E+7
                        || Math.abs(location.getPitch()) >= 1.0E+3
                        || Math.abs(location.getYaw()) >= Float.MAX_VALUE) {

                    e.setCancelled(true);
                    data.handleKickAlert("Invalid position");

                    handleOthers = false;
                }
            }
        /*} else if (type == PacketType.Play.Client.TAB_COMPLETE) {
            handleOthers = false;
            final WrapperPlayClientTabComplete packet = new WrapperPlayClientTabComplete(e);
            final String tb = packet.getText();
            if (tb.contains("to for(") && tb.contains("++")) {
                data.handleKickAlert("FAWE exploit");
                e.setCancelled(true);
            }*/
        } else if (type == PacketType.Play.Client.CHAT_MESSAGE) {
            handleOthers = false;
            if (Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_19)) {
                final WrapperPlayClientChatMessage packet = new WrapperPlayClientChatMessage(e);
                final String chat = packet.getMessage();

                if (chat != null) {
                    if (chat.toLowerCase().contains("${")) {
                        e.setCancelled(true);
                    }
                }
            }
        } else if (type == PacketType.Play.Client.PLUGIN_MESSAGE) {

            //cloned = e.clone();

            final WrapperPlayClientPluginMessage packet = new WrapperPlayClientPluginMessage(e);


            String channelName;
            Object channelObject = packet.getChannelName();
            if (channelObject != null) {
                channelName = (String) channelObject;
            } else {
                ResourceLocation resourceLocation = (ResourceLocation) channelObject;
                channelName = resourceLocation.getNamespace() + ":" + resourceLocation.getKey();
            }

            if (packet.getChannelName().equals("MC|Brand") || channelName.equals("minecraft:brand")) {

                byte[] dataBytes = packet.getData();

                String brand = new String(Arrays.copyOfRange(dataBytes, 1, dataBytes.length));

                data.setBrand(brand);

                brand = brand.replaceAll("[^a-zA-Z0-9_-]", "");

                brand = brand.replaceAll("Velocity", "");

                if (brand.equalsIgnoreCase("Cave Client")) brand = "Cave Client";
                else if (brand.contains("lunarclient")) brand = "Lunar";
                else if (brand.contains("PLC18")) brand = "PvPLounge";
                else if (brand.contains("fmlforge")) brand = "Forge";
                else if (brand.contains("salwyrr")) brand = "Salwyrr";

                String brand2;

                try {
                    brand2 = brand.toUpperCase().charAt(0) + brand.substring(1);
                } catch (StringIndexOutOfBoundsException esd) {
                    brand2 = brand;
                }

                if (brand2.length() > 30) {
                    brand2 = "INVALID_BRAND";
                }

                data.setCleanBrand(brand2);

                if (!brand.equalsIgnoreCase("vanilla") && !data.isBrandPosted() && Karhu.getInstance().getConfigManager().isClientCheck()) {
                    data.setBrandPosted(true);
                    MiscellaneousAlertPoster.postMisc(Karhu.getInstance().getConfigManager().getClientCheckMessage()
                            .replace("%player%", data.getName())
                            .replace("%brand%", brand2), data, "Brand");
                }

                String unallowed = Karhu.getInstance().getConfigManager().getConfig().getString("unallowed-brands.brands", "Vivecraft");

                if (unallowed.contains(brand)) {
                    Tasker.run(() -> data.getBukkitPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', Karhu.getInstance().getConfigManager().getConfig().getString("unallowed-brands.kick-msg"))));
                }
            }

        }


        if (handleOthers) {
            if (!data.isRemovingObject()) {
                //PacketPlayReceiveEvent finalCloned = cloned == null ? e.clone() : cloned;

                //data.getThread().getExecutorService().execute(() -> {
                    handlePlayReceive(e, data, nano, nowTimeMillis);
                    //finalCloned.cleanUp();
                    long stopTime = System.nanoTime();
                    Benchmark cacheData = KarhuBenchmarker.getProfileData(BenchmarkType.PLAY_RECEIVE);
                    cacheData.insertResult(nano, stopTime);

                //});
            } else if (cloned != null) {
                cloned.cleanUp();
                Karhu.getInstance().printCool("&b> &cRemovingobject");
            }
        } else if (cloned != null) cloned.cleanUp();
    }

    public void onPacketPlaySend(PacketPlaySendEvent e) {
        long nanoTime = System.nanoTime();

        PacketPlaySendEvent cloned = null;

        if (e.getUser() == null || e.getUser().getUUID() == null) {
            Karhu.getInstance().printCool("&b> &cPacket sent to an user that is defined as null in PacketEvents " + e.getPacketName());
            return;
        }

        KarhuPlayer data = this.plugin.getDataManager().getPlayerData(e.getUser());

        if (data == null) {
            return;
        }

        final PacketType.Play.Server packetID = e.getPacketType();

        boolean handleAsync = true;

        if (data.getBukkitPlayer() == null && e.getPlayer() != null) {
            data.updateState(e.getPlayer());
        }

        if (!data.isRemovingObject()) {
            switch (packetID) {
                case ENTITY_EFFECT: {
                    //cloned = e.clone();

                    final WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect(e);

                    boolean via = Karhu.getInstance().isViaVersion();

                    PotionType type = packet.getPotionType();

                    int typeId = type.getId(data.getClientVersion());

                    //S/O GRIM

                    // ViaVersion tries faking levitation effects and fails badly lol, flagging the anticheat
                    // Block other effects just in case ViaVersion gets any ideas
                    //
                    // Set to 24 so ViaVersion blocks it
                    // 24 is the levitation effect
                    if (data.getClientVersion().getProtocolVersion() <= 47 && via && typeId > 23) {
                        e.setCancelled(true);
                        handleAsync = false;
                        break;
                    }

                    // ViaVersion dolphin's grace also messes us up, set it to a potion effect that doesn't exist on 1.12
                    // Effect 31 is bad omen
                    if (!data.isNewerThan12() && via && typeId == 30) {
                        e.setCancelled(true);
                        handleAsync = false;
                        break;
                    }

                    break;
                }
                case PING: {
                    final WrapperPlayServerPing ping = new WrapperPlayServerPing(e);

                    if (ping.getId() > Short.MAX_VALUE || ping.getId() < Short.MIN_VALUE) {
                        return;
                    }

                    final short id = (short) ping.getId();

                   // data.getThread().getExecutorService().execute(() ->
                            Karhu.getInstance().getTransactionHandler()
                                    .handleTransaction(id, nanoTime, data);
                    //);

                    handleAsync = false;
                    break;
                }
                case WINDOW_CONFIRMATION: {
                    final WrapperPlayServerWindowConfirmation transaction = new WrapperPlayServerWindowConfirmation(e);

                    final short tid = transaction.getActionId();

                    if (!transaction.isAccepted()) {
                        //data.getThread().getExecutorService().execute(() ->
                                Karhu.getInstance().getTransactionHandler()
                                        .handleTransaction(tid, nanoTime, data);
                        //);
                    }

                    handleAsync = false;
                    break;
                }

                case JOIN_GAME:
                    e.getTasksAfterSend().add(() -> data.sendTransactionLogin(e.getUser()));
                    break;
            }

            if (handleAsync) {
                //PacketPlaySendEvent finalCloned = cloned == null ? e.clone() : cloned;

                //data.getThread().getExecutorService().execute(() -> {
                    //long startTime = System.nanoTime();
                    handlePacketPlaySend(e, data, nanoTime);
                    //finalCloned.cleanUp();
                    long stopTime = System.nanoTime();
                    Benchmark cacheData = KarhuBenchmarker.getProfileData(BenchmarkType.PLAY_SEND);
                    cacheData.insertResult(nanoTime, stopTime);
                //});
            } else if (cloned != null) cloned.cleanUp();
        }
    }

    public void handlePostPlayReceive(WrapperPlayClientPlayerFlying packet, KarhuPlayer data) {
        long now = System.currentTimeMillis();
        if (!packet.hasPositionChanged() && !packet.hasRotationChanged()
                && packet.isOnGround() == data.isLastOnGroundPacket()
                && !data.recentlyTeleported(2)
                && !data.isPossiblyTeleporting()
                && Karhu.getInstance().getServerTick() - data.getCreatedOnTick() > 80
                && !data.isViaMCP()
                && !data.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)
                && data.isNewerThan8()) {
            Tasker.run(() -> Karhu.getInstance().getAlertsManager().getAlertsToggled().stream().map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(staff -> staff.sendMessage("§7[§c!§7] §c" + data.getBukkitPlayer().getName() + " §7is on a protocol that is related to hacked clients")));

            data.setViaMCP(true);
        }


        data.setRiptiding(false);

        data.setLastInBed(data.isInBed());

        data.wasFlyingC = data.flyingC;

        if (!data.isCorrectedFly() && data.isInitialized()) {
            Tasker.run(() -> {
                if (Karhu.getInstance().getServerTick() - data.getServerTick() > 40) {
                    if (!data.isAllowFlying() && data.getBukkitPlayer().getAllowFlight()) {
                        data.getBukkitPlayer().setAllowFlight(true);
                        data.setCorrectedFly(true);
                    }
                }
            });
        }

        VelocityHandler.handle(data);
        EntityLocationHandler.updateFlyingLocations(data, packet);

        if (data.getTeleportManager().teleportTicks != 0 && !data.isSeventeenPlacing()) {
            EntityLocationHandler.updateEntityLocations(data);
        }

        if (data.getTasks().containsKey(data.getTotalTicks())) {
            data.getTasks().remove(data.getTotalTicks()).consumeTask();
        }

        data.getTeleportManager().handlePostFlying();

        data.getLastTargets().clear();

        /*
        Remove after 3 ticks, because our call is async it could be called before the tick update
        */
        data.getDesyncedBlockHandler()
                .getClientSideBlocks()
                .removeIf(block -> data.getServerTick() - block.getServerTick() > 3);

        if (data.getTickedVelocity() != null) {
            data.setTickedVelocity(null);
        }

        if (!data.isReadyToAccept() && data.getTotalTicks() > 600) {
            Tasker.run(() -> data.getBukkitPlayer().kickPlayer("Timed out"));
        }

        data.setAttacks(0);

        if (!data.isDidFlagMovement()) {

            boolean mathGround = MathUtil.onGround(Math.abs(data.getLocation().y));

            double distance = data.getLastLocation().distance(packet.getLocation());
            boolean ground = data.isOnGroundServer() && packet.isOnGround()
                    && (mathGround || data.getMoveTicks() <= 1);
            boolean inside = data.isInsideBlock();
            boolean teleport = data.isPossiblyTeleporting() || !data.getTeleportManager().locations.isEmpty();

            if (data.isAbusingVelocity() && !data.isMitigatingVelocity()) {

                Vector3d artificialKbVector = data.getArtificialKbVector();

                if (artificialKbVector != null) {
                    int steps = 7; // Number of packets for smooth knockback

                    data.setMitigatingVelocity(true);
                    KnockbackUtil.applyKnockbackWithAbsoluteTeleports(data, artificialKbVector, steps);
                    MiscellaneousAlertPoster.postMitigation(data, data.getDesyncedBlockHandler().velocityAbuse,
                            "Velocity",
                            "* Artificial velocity applied" +
                                    "\n §f* X: §b" + artificialKbVector.getX() + " | Z: " + artificialKbVector.getZ()

                    );

                    data.setArtificialKbVector(null);
                }
            }

            long timeSinceLocationUpdate = now - data.getLastLocationUpdate();

            if (ground && !inside && !teleport && distance <= 10 && timeSinceLocationUpdate > 40L) {
                if (++data.invalidMovementTicks > 10) {
                    CustomLocation groundSet = data.getLocation().clone();
                    CustomLocation safeSet = data.getLocation().clone();

                    if (mathGround) {
                        groundSet.setY(groundSet.getY() + 0.1);
                        safeSet.setY(safeSet.getY() + 0.1);
                    }

                    if (data.invalidMovementTicks > 100) {
                        groundSet.setY(groundSet.getY() - 0.1);
                        safeSet.setY(groundSet.getY() - 0.1);
                        data.invalidMovementTicks = 0;
                    }

                    data.setSafeGroundSetback(groundSet);
                    data.setSafeSetback(safeSet);
                }
                data.setLastLocationUpdate(now);
            }

            data.getLocation().setCheats(false);
            data.getLocation().setTeleport(false);
        }

        data.setDidFlagMovement(false);
    }

    public void handlePlayReceive(PacketPlayReceiveEvent e, KarhuPlayer data, long nanoTime, long timeMillis) {

        final PacketType.Play.Client type = e.getPacketType();

        if (data != null) {

            final boolean isFlying = WrapperPlayClientPlayerFlying.isFlying(type);

            WrapperPlayClientPlayerFlying packet = null;

            Event callEvent = null;

            /*
            Handle all transaction stuff
             */
            Karhu.getInstance().getTransactionHandler().handlePlayReceive(e, nanoTime, data);

            if (isFlying) {

                packet = new WrapperPlayClientPlayerFlying(e);

                com.github.retrooper.packetevents.protocol.world.Location location = packet.getLocation();

                data.setSeventeenPlacing(false);

                if (data.isBadClientVersion() && data.getBukkitPlayer() != null) {
                    User userCheck = PacketEvents.getAPI().getPlayerManager().getUser(data.getBukkitPlayer());
                    if(userCheck != null) {
                        data.updateClientVersion(userCheck.getClientVersion());
                    }
                }

                data.getAbilityManager().onFlying();

                if (data.legacyTeleports()) {
                    data.getTeleportManager().handlePreFlying(packet);
                } else {
                    data.getTeleportManager().findTeleport(packet);
                }

                if (data.isInBed() != data.isLastInBed()) {
                    data.getTeleportManager().setTeleporting();
                }

                if (data.getTotalTicks() <= 3 && data.getTeleportManager().teleportAmount == 0) {
                    data.setFuckedTeleport(true);
                }

                data.setTotalTicks(
                        data.getTeleportManager().teleportTicks != 0
                        ? data.getTotalTicks() + 1
                        : data.getTotalTicks()
                );

                if (data.getTeleportManager().teleportTicks > 0) {
                    data.setFlyingBeforeTickEnd(true);
                }

                final long packetDiff = (long) ((nanoTime - data.getLastFlying()) / 1E6);
                final int nowTicks = data.getTotalTicks();

                data.lastPacketDrop = (packetDiff <= 2L || packetDiff >= 90L) ? nowTicks : data.lastPacketDrop;

                data.setFlyingTime(packetDiff);

                data.setLastFlying(nanoTime);
                data.setLastFlyingTicks(nowTicks);

                PlayerHandler.checkConditions(data);

                double x, y, z;
                float yaw = location.getYaw(), pitch = location.getPitch();
                final boolean position = packet.hasPositionChanged(), look = packet.hasRotationChanged(), ground = packet.isOnGround();

                if (position) {
                    x = location.getX();
                    y = location.getY();
                    z = location.getZ();
                    data.setMoveTicks(data.getMoveTicks() + 1);
                    data.setNoMoveTicks(0);
                } else {
                    x = data.getLocation().getX();
                    y = data.getLocation().getY();
                    z = data.getLocation().getZ();
                    data.setMoveTicks(0);
                    data.setNoMoveTicks(data.getNoMoveTicks() - 1);
                }

                if (!look) {
                    yaw = data.getLocation().getYaw();
                    pitch = data.getLocation().getPitch();
                }

                if (look) {
                    if (Math.abs(yaw) > 100000) {
                        float finalPitch = pitch;
                        float finalYaw = yaw % 360;
                        data.setYawFucked(data.getTotalTicks());

                        Tasker.run(() -> {
                            data.getBukkitPlayer().teleport(new Location(data.getWorld(), x, y, z, finalYaw, finalPitch));
                        });
                    }
                }

                final ItemStack stack8 = data.getStackInHand();

                data.setLastUsingItem(data.isUsingItem());
                data.setLastEating(data.isEating());

                if (data.getClientVersion().getProtocolVersion() <= 47 && stack8.getType().isBlock()) {
                    data.setUsingItem(false);
                    data.setEating(false);
                }

                if (packetDiff < 25L) {
                    data.setLastFast(nanoTime);
                }

                if (data.isAllowFlying() || data.isFlying() || data.isFlyingBukkit() || data.isAllowFlyingBukkit()) {
                    data.setLastFlyTick(nowTicks);
                }

                if (data.isAllowFlying() || data.isSpectating() || data.isAllowFlyingBukkit()) {
                    data.setLastAllowFlyTick(nowTicks);
                }

                /*
                We are injected sometimes after the teleport packet has been sent
                 */
                //data.setJoining(data.getTotalTicks() < 100 && !data.isHasReceivedTransaction() && !data.isHasReceivedKeepalive());

                if (data.getClientVersion().getProtocolVersion() > 754) {
                    if (position && look && data.isOnGroundPacket() == ground) {

                        double distance = data.getLocation().distance(location);

                        if (data.getPositionPackets() > 0 && distance == 0 && !data.recentlyTeleported(2)) {
                            data.setTotalTicks(Math.max(0, data.getTotalTicks() - 1));
                            data.setSeventeenPlacing(true);
                            return;
                        }
                    }
                }

                if (position) {
                    data.setPositionPackets(data.getPositionPackets() + 1);

                    if (data.getPositionPackets() == 1) {
                        if (data.getSafeGroundSetback() == null) {
                            data.setSafeGroundSetback(data.getLocation().clone());
                        }
                        if (data.getSafeSetback() == null) {
                            data.setSafeSetback(data.getLocation().clone());
                        }
                        if (data.getFlyCancel() == null) {
                            data.setFlyCancel(data.getLocation().clone());
                        }
                    }
                }

                /*
                 * External Processing
                 */
                data.lastAttackTick++;

                data.setLastLastOnGroundPacket(data.isLastOnGroundPacket());
                data.setLastOnGroundPacket(data.isOnGroundPacket());
                data.setOnGroundPacket(ground);

                data.setLastSprintTick(data.isSprinting() ? data.getTotalTicks() : data.getLastSprintTick());
                data.setLastSneakTick(data.isSneaking() ? data.getTotalTicks() : data.getLastSneakTick());

                data.setLastLastLastLocation(data.getLastLastLocation().clone());
                data.setLastLastLocation(data.getLastLocation().clone());
                data.setLastLocation(data.getLocation().clone());

                data.getLocation().setGround(packet.isOnGround());

                data.setWasWasInUnloadedChunk(data.isWasInUnloadedChunk());
                data.setWasInUnloadedChunk(data.isInUnloadedChunk());
                data.setInUnloadedChunk(!BlockUtil.chunkLoaded(data.getLocation().toLocation(data.getWorld())));
                data.setLastInUnloadedChunk(data.isInUnloadedChunk() ? data.getTotalTicks() : data.getLastInUnloadedChunk());

                EffectManager eManager = data.getEffectManager();
                int jumpBoost = eManager.getEffectStrenght(PotionEffect.JUMP_BOOST);
                if (jumpBoost != data.getJumpBoost()) {
                    data.setLastJumpBoostChange(data.getTotalTicks());
                }
                data.jumpBoost = jumpBoost;
                data.speedBoost = eManager.getEffectStrenght(PotionEffect.SPEED);
                data.slowness = eManager.getEffectStrenght(PotionEffect.SLOWNESS);
                data.haste = eManager.getEffectStrenght(PotionEffect.HASTE);
                data.fatigue = eManager.getEffectStrenght(PotionEffect.MINING_FATIGUE);

                //1.9+
                data.dolphinLevel = eManager.getEffectStrenght(PotionEffect.DOLPHIN_GRACE);
                data.slowFallingLevel = eManager.getEffectStrenght(PotionEffect.SLOW_FALLING);
                data.levitationLevel = eManager.getEffectStrenght(PotionEffect.LEVITATION);

                if (position) {
                    data.getLocation().setPosition(location.getX(), location.getY(), location.getZ());

                    if (data.isPossiblyTeleporting()) {
                        data.setLastTeleport(data.getTotalTicks());
                    }

                    if (data.getTeleportManager().teleportTicks == 0) {
                        data.getLastLocation().setPosition(location.getX(), location.getY(), location.getZ());

                        data.setClientAirTicks(0);
                        data.setAirTicks(0);
                    }

                    data.getWrappedEntity().setPosition(x, y, z);

                    if (!data.isLocationInited()) {
                        data.setLocationInited(true);
                        data.setLocationInitedAt(nowTicks);
                    }
                }

                if (look) {
                    data.getLastLocation().setRotation(data.getLocation().yaw, data.getLocation().pitch);
                    data.getLocation().setRotation(yaw, pitch);
                }

                data.getLocation().moved = position;
                data.getLocation().rotated = look;

                data.getMovementHandler().handleMotions(position, look);

                //if (Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_12_2)) {
                    boolean run = data.getLocation().distance(data.getLastLocation()) > 0D
                            || data.getLocation().distance(data.getLastLastLocation()) > 0D;
                    if (run) {
                        data.getCollisionHandler().handleLastTicks();
                        data.getCollisionHandler().handle(position);
                        data.getCollisionHandler().handleTicks();
                    } else if (data.isForceRunCollisions()) {
                        data.getCollisionHandler().handleLastTicks();
                        data.getCollisionHandler().handle(position);
                        data.getCollisionHandler().handleTicks();
                        data.setForceRunCollisions(false);
                    } else {
                        data.getCollisionHandler().handleLastTicks();
                        data.getCollisionHandler().handleTicks();
                    }
                //}


                data.getMovementHandler().handleOther(ground);

                NMSValueParser.parse(data);

                //Try to predict 0.03
                float friction;

                if (data.isLastOnGroundPacket()) {
                    friction = data.getCurrentFriction();
                    if (!data.isNewerThan12()) {
                        friction *= 0.91F;
                    }
                } else {
                    friction = 0.91F;
                }

                double xDiff = data.deltas.lastDX * friction;
                double zDiff = data.deltas.lastDZ * friction;

                final double prediction = (data.deltas.lastMotionY - 0.08D) * 0.98F;

                if (xDiff * xDiff + prediction * prediction + zDiff * zDiff <= 9.0E-4) {
                    data.deltas.predictX = (xDiff * xDiff);
                    data.deltas.predictY = (prediction * prediction);
                    data.deltas.predictZ = (zDiff * zDiff);
                    data.setPredictionTicks(data.getTotalTicks());
                }

                data.getDesyncedBlockHandler().handleFlying(position, ground, false);
                data.setPlacedInside(data.getDesyncedBlockHandler().checkInsidePlace());
                data.setPlacedCancel(data.getDesyncedBlockHandler().checkBelowPlace());

                data.setLastPlacedInside(data.isPlacedInside() ? data.getTotalTicks() : data.getLastPlacedInside());


                double chunkMove = data.getLocation().y > 0.0D ? 0.09800000190735147D : 0;

                //Math.abs(data.getVelocityY() + chunkMove) <= 1E-5;
                boolean exemptable = data.getTickedVelocity() != null;
                boolean unload = Math.abs(data.deltas.motionY + chunkMove) <= 1E-7 && !ground;

                if (unload) {
                    if (data.elapsed(data.getLastInUnloadedChunk()) > MathUtil.getPingInTicks(data.getTransactionPing()) + 8 && !exemptable) {

                        if(!data.isDidFlagMovement() && !data.isPossiblyTeleporting() && data.elapsed(data.getLastFlyTick()) > 30) {
                            Check<?> flyA = data.getCheckManager().getCheck(FlyA.class);

                            if (Karhu.getInstance().getCheckState().isEnabled(flyA.getName())) {
                                flyA.setViolations(flyA.getViolations() + 1);
                                if(flyA.getViolations() >= 3) {
                                    flyA.disallowMove(true);
                                    flyA.setViolations(0);
                                }
                            }
                        }
                    }
                }

                if (data.getLastAttackTick() <= 50 && data.getLastTarget() != -696969) {
                    EntityData entityData = data.getEntityData().get(data.getLastTarget());
                    if (entityData != null) {
                        double nonInterpolatedDist = data.getBoundingBox().distance(entityData.getNewXYZ());
                        double interpolatedDist = data.getBoundingBox().distanceToHitbox(entityData.getEntityBoundingBox());
                        long ping = data.getTransactionPing();

                        double distanceDiff = Math.abs(nonInterpolatedDist - interpolatedDist);

                        if (nonInterpolatedDist > interpolatedDist) {
                            data.getCombatDataClose().add(
                                    new CombatData(nonInterpolatedDist,
                                            interpolatedDist,
                                            ping,
                                            timeMillis
                                    ));
                        } else if (distanceDiff > 0.06125) {
                            data.getCombatDataFar().add(
                                    new CombatData(nonInterpolatedDist,
                                            interpolatedDist,
                                            ping,
                                            timeMillis
                                    ));
                        }
                    }
                }

                data.setLastPossibleInUnloadedChunk(unload
                        ? data.getTotalTicks()
                        : data.getLastPossibleInUnloadedChunk()
                );

                if (look) {
                    data.getCheckManager().runChecks(data.getCheckManager().getRotationChecks(), new MovementUpdate(data.getLastLastLocation(), data.getLastLocation(), data.getLocation(), packet.isOnGround()), null);
                }

                boolean teleport = data.getTeleportManager().teleportTicks == 0;

                callEvent = new FlyingEvent(
                        data.getLocation().getX(), data.getLocation().getY(), data.getLocation().getZ(), yaw, pitch,
                        position,
                        look,
                        ground,
                        teleport,
                        nanoTime,
                        timeMillis);


                /*
                 * External Processing #2
                 */
                data.setElapsedOnLiquid(data.isOnLiquid() ? data.getElapsedOnLiquid() + 1 : 0);
                data.setElapsedUnderBlock(data.isUnderBlock() ? data.getElapsedUnderBlock() + 1 : 0);

                data.setWasPlacing(data.isPlacing());
                data.setPlacing(false);

                data.setWasWasSneaking(data.isWasSneaking());
                data.setWasSneaking(data.isSneaking());

                data.getCrashHandler().handleFlying(position, look, data.getLocation(), data.getLastLocation());
                //data.getPredictionHandler().moveEntityWithHeading();

                data.tick();

                if (!data.getVelocityPending().isEmpty()) data.checkVelocity();

                if (data.isPossiblyTeleporting()) {
                    data.setVelocityX(0);
                    data.setVelocityY(0);
                    data.setVelocityZ(0);

                    data.setVelocityHorizontal(0);
                    data.setTickedVelocity(null);

                    data.setTakingVertical(false);
                }

                data.getSimulationHandler().simulateMovement(0, 0, false);

                if (position) {
                    data.getCheckManager().runChecks(data.getCheckManager().getPositionChecks(), new MovementUpdate(data.getLastLastLocation(), data.getLastLocation(), data.getLocation(), packet.isOnGround()), null);
                }

                data.setWasWasSprinting(data.isWasSprinting());
                data.setWasSprinting(data.isSprinting());

                if (Karhu.getInstance().getConfigManager().isPingKick()) {
                    if (data.getTransactionPing() > Karhu.getInstance().getConfigManager().getPingKickMaxPing()) {
                        if (++data.badPingTicks >= Karhu.getInstance().getConfigManager().getPingKickTicks()) {
                            Tasker.run(() -> data.getBukkitPlayer().kickPlayer(Karhu.getInstance().getConfigManager().getPingKickMsg()));
                        }
                    } else {
                        data.badPingTicks = Math.max(data.getBadPingTicks() - 70, 0);
                    }
                }

            } else {

                switch (type) {

                    case PONG:
                    case WINDOW_CONFIRMATION: {
                        callEvent = new TransactionEvent(nanoTime);
                        break;
                    }

                    case CLIENT_TICK_END: {
                        if (!data.isFlyingBeforeTickEnd() && data.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)) {
                            /*
                            Interpolation bug on modern mc, just keep the old way for now
                             */
                            //EntityLocationHandler.updateEntityLocations(data);
                            data.setAttacks(0);
                            data.setTotalTicks(
                                    data.getTeleportManager().teleportTicks != 0
                                            ? data.getTotalTicks() + 1
                                            : data.getTotalTicks()
                            );

                            if (data.getTasks().containsKey(data.getTotalTicks())) {
                                data.getTasks().remove(data.getTotalTicks()).consumeTask();
                            }

                            callEvent = new TickEndEvent(nanoTime, timeMillis);
                        }
                        data.setFlyingBeforeTickEnd(false);
                        break;
                    }

                    case INTERACT_ENTITY: {

                        final WrapperPlayClientInteractEntity use = new WrapperPlayClientInteractEntity(e);
                        final int id = use.getEntityId();
                        final EntityData eData = data.getEntityData().get(id);

                        if (eData == null) {
                            return;
                        }

                        final EntityType entityType = eData.getType();

                        boolean isPlayer = EntityTypes.PLAYER.equals(entityType);

                        if (use.getAction() == ATTACK) {

                            callEvent = new AttackEvent(id, isPlayer, nanoTime, timeMillis, 0);

                            data.lastAttackTick = 0;
                            ++data.attacks;
                            data.setLastAttackPacket(nanoTime);

                            data.setDigTicks(0);

                            if (isPlayer) {
                                data.setLastTarget(id);
                                data.getLastTargets().add(id);

                                if (data.isRecording()) {
                                    if (data.getPendingSwingTime() != null) {
                                        data.getRecordingSamples().add(new AttackSwingPair(data.getPendingSwingTime(), (long) data.getTotalTicks()));
                                        data.setPendingSwingTime(null);
                                    }
                                }
                            } else {
                                data.setLastTarget(-696969);
                            }

                            if (entityType.equals(EntityTypes.PLAYER)
                                    || EntityTypes.isTypeInstanceOf(entityType, EntityTypes.BOAT)
                                    || EntityTypes.isTypeInstanceOf(entityType, EntityTypes.CHEST_BOAT)
                                    || EntityTypes.isTypeInstanceOf(entityType, EntityTypes.MINECART_ABSTRACT)
                                    || EntityTypes.isTypeInstanceOf(entityType, EntityTypes.CHESTED_MINECART_ABSTRACT)
                                    || entityType.equals(EntityTypes.FIREBALL)
                                    || entityType.equals(EntityTypes.END_CRYSTAL)
                                    || entityType.equals(EntityTypes.ENDER_DRAGON)) {

                                data.setResettingSprint(true);
                            }


                            data.setLastAbortLoc(null);
                            data.setDigging(false);


                            data.setUsingItem(false);
                            data.setEating(false);
                            data.setBlocking(false);

                        } else if (use.getAction() == INTERACT
                                || use.getAction() == INTERACT_AT) {

                            if (isPlayer) {

                                callEvent = new InteractEvent(id, isPlayer,
                                        use.getTarget().isPresent()
                                                ? use.getTarget().get()
                                                : null,
                                        use.getAction() == INTERACT_AT, nanoTime);

                            }

                            //data.getVehicleHandler().handle(en);
                        }

                        break;
                    }

                    case ANIMATION:

                        final Vector aborted = data.getLastAbortLoc();
                        if (aborted != null) {
                            boolean state = data.getLocation()
                                    .distance(new CustomLocation(aborted.getX(), aborted.getY(), aborted.getZ())) < 7;

                            if (data.getLastAttackTick() <= 1) {
                                state = false;
                                data.setLastAbortLoc(null);
                            }

                            if(!state) {
                                if(!data.isPossiblyTeleporting()) {
                                    data.setDigging(state);
                                }
                            } else {
                                data.setDigging(state);
                            }
                        }

                        if (data.isHasDig2()) {
                            data.setDigTicks(data.getTotalTicks());
                        }


                        data.getCrashHandler().handleArm();
                        if (!data.isSkipNextSwing()) {
                            callEvent = new SwingEvent(nanoTime, timeMillis, 0);
                        } else {
                            data.setSkipNextSwing(false);
                        }

                        if (data.isRecording() && data.getLastAttackTick() <= 60) {
                            if (data.getPendingSwingTime() != null) {
                                data.getRecordingSamples().add(new AttackSwingPair((long) -420, data.getPendingSwingTime()));

                            }

                            // Store this swing as pending
                            data.setPendingSwingTime((long) data.getTotalTicks());
                        }
                        break;

                    case STEER_VEHICLE:
                        final WrapperPlayClientSteerVehicle steerVehicle = new WrapperPlayClientSteerVehicle(e);
                        callEvent = new SteerEvent(steerVehicle.isUnmount());
                        break;

                    case CLICK_WINDOW:
                        final WrapperPlayClientClickWindow winClick = new WrapperPlayClientClickWindow(e);

                        data.getCrashHandler().handleWindowClick(winClick.getSlot(), winClick.getStateId().orElse(0),
                                winClick.getWindowId(), winClick.getButton());

                        callEvent = new WindowEvent(nanoTime,
                                winClick.getWindowClickType(),
                                winClick.getSlot(),
                                winClick.getWindowId(),
                                winClick.getButton(),
                                winClick.getCarriedItemStack()
                                );
                        break;

                    case PLAYER_ABILITIES:
                        final WrapperPlayClientPlayerAbilities abilities = new WrapperPlayClientPlayerAbilities(e);

                        data.getAbilityManager().onAbilityClient(abilities);

                        callEvent = new AbilityEvent();
                        break;

                    case CLIENT_STATUS:
                        final WrapperPlayClientClientStatus status = new WrapperPlayClientClientStatus(e);
                        if (status.getAction() == OPEN_INVENTORY_ACHIEVEMENT) {
                            data.setBlocking(false);
                            data.setUsingItem(false);
                            data.setEating(false);
                            data.setInventoryOpen(true);
                            data.setInvStamp(data.getTotalTicks());
                        }
                        callEvent = new ClientCommandEvent(status.getAction());
                        break;

                    case CLOSE_WINDOW:
                        data.setInventoryOpen(false);
                        break;

                    case ENTITY_ACTION: {

                        final WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(e);

                        switch (action.getAction()) {
                            case START_SPRINTING:
                                data.setWasSprinting(data.isSprinting());
                                data.setSprinting(true);
                                data.setDesyncSprint(false);
                                data.setMetadataSprint(false);
                                data.setInvalidSprint(false);
                                data.setSprintAttribute(false);
                                break;
                            case STOP_SPRINTING:
                                data.setWasSprinting(data.isSprinting());
                                data.setSprinting(false);
                                data.setDesyncSprint(false);
                                data.setRecorrectingSprint(false);
                                data.setMetadataSprint(false);
                                data.setInvalidSprint(false);
                                data.setSprintAttribute(false);
                                break;
                            case START_SNEAKING:
                                data.setWasSneaking(data.isSneaking());
                                data.setSneaking(true);
                                break;
                            case STOP_SNEAKING:
                                data.setWasSneaking(data.isSneaking());
                                data.setSneaking(false);
                                break;
                            case START_FLYING_WITH_ELYTRA:
                                if (data.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_14)) {
                                    return;
                                }
                                data.setGliding(data.canGlide());
                                break;
                        }

                        callEvent = new ActionEvent(action.getAction());
                        break;
                    }

                    case KEEP_ALIVE: {
                        data.setPing(MathUtil.toMillis(nanoTime - data.getLastPingTime()));

                        data.getCrashHandler().handleClientKeepAlive();

                        data.setHasReceivedKeepalive(true);
                        callEvent = new ConnectionHeartbeatEvent();
                        break;
                    }

                    case PLAYER_DIGGING: {
                        final WrapperPlayClientPlayerDigging dig = new WrapperPlayClientPlayerDigging(e);

                        data.getPacketWorldManager().handleBlockBreak(dig);

                        final Vector position = new Vector(dig.getBlockPosition().x,
                                dig.getBlockPosition().y,
                                dig.getBlockPosition().z);

                        switch (dig.getAction()) {
                            case START_DIGGING:
                                data.setDigging(true);
                                data.setDiggingBasic(true);

                                data.setDigTicks(data.getTotalTicks());

                                if (Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_12_2)) {
                                    if (!data.isInUnloadedChunk()) {

                                        Location blockIn = new Location(data.getWorld(),
                                                position.getX(), position.getY(), position.getZ());

                                        Block block = Karhu.getInstance().getChunkManager().getChunkBlockAt(blockIn);

                                        if (block != null) {
                                            if (MaterialChecks.ONETAPS.contains(blockIn.getBlock().getType())
                                                    && data.getLocation().toVector().distance(position) <= 2) {
                                                data.setFastDigTicks(data.getTotalTicks());
                                            }
                                        }
                                    }
                                }
                                break;

                            case DROP_ITEM:
                                if (data.getClientVersion().isNewerThan(ClientVersion.V_1_14_4))
                                    data.setSkipNextSwing(true);
                            case RELEASE_USE_ITEM:
                            case SWAP_ITEM_WITH_OFFHAND:
                            case DROP_ITEM_STACK:
                                data.setBlocking(false);
                                data.setUsingItem(false);
                                data.setEating(false);
                                break;
                            case CANCELLED_DIGGING:
                                //data.setDigging(false);
                                data.setDiggingBasic(false);
                                data.setLastAbortLoc(position);
                                break;
                            case FINISHED_DIGGING:
                                data.setDigging(false);
                                data.setDiggingBasic(false);
                                data.setLastAbortLoc(null);
                                data.setDigStopTicks(data.getTotalTicks());
                                break;
                        }

                        if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_13)) {
                            if (dig.getAction() == RELEASE_USE_ITEM && data.getBukkitPlayer() != null) {
                                boolean liquid = data.elapsed(data.getLastInLiquid()) <= 2 && !data.isOnLava();
                                boolean storm = data.getWorld() != null && data.getWorld().hasStorm();
                                boolean rain = data.getBukkitPlayer().getPlayerWeather() == WeatherType.DOWNFALL;
                                if (liquid || storm || rain) {
                                    ItemStack mainHand = data.getBukkitPlayer().getInventory().getItemInMainHand();
                                    ItemStack offHand = data.getBukkitPlayer().getInventory().getItemInOffHand();

                                    if (MaterialChecks.TRIDENT.contains(mainHand.getType())) {
                                        if (mainHand.getEnchantmentLevel(Enchantment.RIPTIDE) > 0) {
                                            data.setRiptiding(true);
                                            data.setLastRiptide(data.getTotalTicks());
                                        }
                                    } else if (MaterialChecks.TRIDENT.contains(offHand.getType())) {
                                        if (offHand.getEnchantmentLevel(Enchantment.RIPTIDE) > 0) {
                                            data.setRiptiding(true);
                                            data.setLastRiptide(data.getTotalTicks());
                                        }
                                    }

                                }
                            }
                        }
                        callEvent = new DigEvent(position, dig.getBlockFace().getFaceValue(), dig.getAction(), nanoTime);
                        break;
                    }

                    case PLAYER_BLOCK_PLACEMENT:
                    case USE_ITEM: {
                        if (type == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT && Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_9)) {
                            final WrapperPlayClientPlayerBlockPlacement place = new WrapperPlayClientPlayerBlockPlacement(e);

                            data.getPacketWorldManager().handleBlockPlacement(place);

                            int x = place.getBlockPosition().getX();
                            int y = place.getBlockPosition().getY();
                            int z = place.getBlockPosition().getZ();

                            final Vector loc = new Vector(x, y, z);

                            final ItemStack stack = VersionBridgeHelper.getStackInHand(data);
                            final ItemStack stack8 = data.getStackInHand();

                            final Material stackType = stack8.getType();

                            final BlockFace face = place.getFace();

                            boolean placed = false;

                            boolean pebug = loc.getY() == 4095;

                            if (loc.getX() == -1 && (loc.getY() == 255 || pebug || loc.getY() == -1) && loc.getZ() == -1 && (face.getFaceValue() == 255 || pebug)) {

                                if (stack8.getDurability() <= 16384) {

                                    boolean eating = false, blocking = false, bowing = false;

                                    if (MaterialChecks.SWORDS.contains(stackType)) {
                                        data.setBlocking(blocking = true);
                                    } else if (MaterialChecks.BOWS.contains(stackType) &&
                                            data.getBukkitPlayer().getInventory().contains(Material.ARROW)) {
                                        data.setBowing(bowing = true);
                                    } else if (stack8.getType().isEdible() &&
                                            (data.getBukkitPlayer().getFoodLevel() < 20
                                                    || MaterialChecks.EDIBLE_WITHOUT_HUNGER.contains(stackType))) {
                                        eating = true;
                                    }

                                    if (blocking || bowing || eating) {
                                        data.setUsingItem(true);
                                        if (eating) {
                                            data.setEating(true);
                                        }
                                        data.setUseTicks(data.getTotalTicks());
                                    }
                                }

                            } else {

                                //Bukkit.broadcastMessage("LOC: " + loc + " FACE: " + face.getFaceValue());
                                boolean bucket = MaterialChecks.LIQUID_BUCKETS.contains(stackType);

                                if (stack.getType().isBlock() || bucket) {
                                    final double diffX = Math.abs(data.getLocation().x - loc.getBlockX());
                                    final double diffZ = Math.abs(data.getLocation().z - loc.getBlockZ());

                                    final double blockY = loc.getBlockY(); //Cache result, prevents multiple floors

                                    final double locationY = data.getLocation().y;

                                    if (diffX <= 4 && blockY >= locationY - 4 && (blockY <= locationY + 4) && diffZ <= 4) {
                                        data.setPlaceTicks(data.getTotalTicks());
                                        if (bucket && data.elapsed(data.getLastInLiquid()) <= data.getPingInTicks() + 1) {
                                            data.setBucketTicks(data.getTotalTicks());
                                        }
                                    }
                                    if (diffX <= 2 && (blockY <= data.getLocation().y + 2) && diffZ <= 2 && !bucket) {
                                        data.setUnderPlaceTicks(data.getTotalTicks());
                                    }
                                    data.setPlacing(true);
                                }

                                placed = true;
                            }

                            switch (face.getFaceValue()) {
                                case 0:
                                    y -= 1;
                                    break;
                                case 1:
                                    y += 1;
                                    break;
                                case 2:
                                    z -= 1;
                                    break;
                                case 3:
                                    z += 1;
                                    break;
                                case 4:
                                    x -= 1;
                                    break;
                                case 5:
                                    x += 1;
                                    break;
                                default:
                                    break;
                            }

                            Vector placedLocation = new Vector(x, y, z);
                            Vector3f cursor = place.getCursorPosition();

                            if (placed) {


                                Location against = new Location(data.getWorld(), loc.getX(), loc.getY(), loc.getZ());
                                Block blockAgainst = Karhu.getInstance().getChunkManager().getChunkBlockAt(against);

                                Material material = blockAgainst == null ? Material.AIR : blockAgainst.getType();

                                DesyncedBlockHandler blockHandler = data.getDesyncedBlockHandler();

                                Material itemInHand = data.getStackInHand().getType();

                                /*
                                We queue valid places to the next server tick, since server also does that
                                The block can't be found before the next tick with #getBlock() etc
                                */

                                boolean invalid = false;

                                if ((MaterialChecks.AIR.contains(material)
                                        || MaterialChecks.LIQUIDS.contains(material))) {
                                    invalid = true;
                                }

                                if (!invalid) {

                                    BlockPlacePending bpp = new BlockPlacePending(
                                            placedLocation,
                                            face.getFaceValue(),
                                            Karhu.getInstance().getServerTick(),
                                            itemInHand
                                    );

                                    blockHandler.getClientSideBlocks().add(bpp);

                                    blockHandler.invalidPlaces = Math.max(blockHandler.invalidPlaces - 1, 0);
                                } else {
                                    ++blockHandler.invalidPlaces;
                                }

                            }

                            callEvent = new BlockPlaceEvent(placedLocation, loc, stack8,
                                    cursor.getX(),
                                    cursor.getY(),
                                    cursor.getZ(),
                                    face,
                                    face.getFaceValue(),
                                    nanoTime, timeMillis,
                                    data.getWorld()
                            );
                            data.getCrashHandler().handlePlace();
                        } else if(type == PacketType.Play.Client.USE_ITEM && Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_8_8)) {
                            //WrapperPlayClientUseItem useItem = new WrapperPlayClientUseItem(e);

                            //?
                        }
                        break;
                    }


                    case HELD_ITEM_CHANGE: {
                        final WrapperPlayClientHeldItemChange held = new WrapperPlayClientHeldItemChange(e);

                        data.setWasPlacing(data.isPlacing());

                        data.setUsingItem(false);
                        data.setEating(false);
                        data.setBlocking(false);
                        data.setPlacing(false);

                        data.setSlotSwitchTick(data.getTotalTicks());
                        data.setCurrentSlot(held.getSlot());

                        callEvent = new HeldItemSlotEvent(held.getSlot());
                        data.getCrashHandler().handleSlot();
                        break;
                    }

                    case PLUGIN_MESSAGE: {
                        data.getCrashHandler().handleCustomPayload();
                        break;
                    }

                    case PLAYER_INPUT: {
                        final WrapperPlayClientPlayerInput input = new WrapperPlayClientPlayerInput(e);

                        int forward = 0;
                        int strafe = 0;

                        if (input.isForward()) {
                            forward++;
                        }

                        if (input.isBackward()) {
                            forward--;
                        }

                        if (input.isLeft()) {
                            strafe++;
                        }

                        if (input.isRight()) {
                            strafe--;
                        }

                        data.getSimulationHandler().setKnownInputF(forward);
                        data.getSimulationHandler().setKnownInputS(strafe);
                        break;
                    }

                    case VEHICLE_MOVE: {
                        final WrapperPlayClientVehicleMove move = new WrapperPlayClientVehicleMove(e);

                        if (data.isRiding()) {
                            data.setVehicleX(move.getPosition().getX());
                            data.setVehicleY(move.getPosition().getY());
                            data.setVehicleZ(move.getPosition().getZ());
                        }

                        callEvent = new VehicleEvent(
                                move.getPosition().getX(),
                                move.getPosition().getY(),
                                move.getPosition().getZ());

                        break;
                    }

                    case TELEPORT_CONFIRM: {
                        WrapperPlayClientTeleportConfirm teleportConfirm = new WrapperPlayClientTeleportConfirm(e);

                        data.getTeleportManager().confirmingTeleport(teleportConfirm);
                        data.getNetHandler().onTeleportConfirmPacket();
                        break;
                    }

                    default:
                        break;

                }

            }

            if (callEvent != null) {
                data.getCheckManager().runChecks(data.getCheckManager().getPacketChecks(), callEvent, null);
            }

            if (isFlying) {
                handlePostPlayReceive(packet, data);
            }

        } else {
            Karhu.getInstance().printCool("&b> &cPacket received from an user that is defined as null in PlayerData 2");
        }
    }


    public void handlePacketPlaySend(PacketPlaySendEvent e, KarhuPlayer data, long nanoTime) {

        final PacketType.Play.Server packetID = e.getPacketType();

        if (data != null) {

            Event callEvent = null;
            TransactionHandler network = Karhu.getInstance().getTransactionHandler();

            /*
            Handle all transaction confirmations
            */
            network.handlePacketPlaySend(e, nanoTime, data);

            if(!data.isObjectLoaded()) return;

            switch (packetID) {

                case ENTITY_VELOCITY: {

                    final WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(e);

                    Vector3d vecVelocity = velocity.getVelocity();

                    if (velocity.getEntityId() == e.getUser().getEntityId()) {
                        callEvent = new VelocityEvent(
                                vecVelocity.getX(), vecVelocity.getY(), vecVelocity.getZ(),
                                velocity.getEntityId()
                        );
                    }
                    break;
                }

                case PLAYER_POSITION_AND_LOOK: {
                    final WrapperPlayServerPlayerPositionAndLook position = new WrapperPlayServerPlayerPositionAndLook(e);

                    Vector3d pos = new Vector3d(position.getX(), position.getY(), position.getZ());


                    boolean checkBB = Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_7_10);

                    final double x = pos.getX(),
                            y = !checkBB
                                    ? pos.getY()
                                    : pos.getY() - (double) 1.62F,
                            z = pos.getZ();


                    TeleportPosition teleport = new TeleportPosition(x, y, z);

                    ++data.getTeleportManager().teleportAmount;

                    data.setSafeGroundSetback(teleport.toCLocation());
                    data.setSafeSetback(teleport.toCLocation());
                    data.setTeleportLocation(teleport.toCLocation());
                    data.setFlyCancel(teleport.toCLocation());

                    data.getDesyncedBlockHandler().setNoFakeWaterLocation(teleport.toLocation(data.getWorld()));

                    callEvent = new PositionEvent(x, y, z, position.getYaw(), position.getPitch());
                    break;
                }

                default:
                    break;

            }

            if (callEvent != null) {
                data.getCheckManager().runChecks(data.getCheckManager().getPacketChecks(), callEvent, null);
            }
        }
    }
}
