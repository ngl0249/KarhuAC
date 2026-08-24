package me.liwk.karhu.handler.global;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.EntityPositionData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.RespawnEvent;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.Teleport;
import me.liwk.karhu.util.TeleportPosition;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.vec.Vec3;
import me.liwk.karhu.util.pending.VelocityPending;
import me.liwk.karhu.util.player.PlayerUtil;
import me.liwk.karhu.util.task.Tasker;
import org.bukkit.ChatColor;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public class TransactionHandler {

    public void handlePlayReceive(PacketPlayReceiveEvent e, long nanoTime, KarhuPlayer data) {

        final PacketType.Play.Client type = e.getPacketType();

        switch (type) {
            case PONG:
            case WINDOW_CONFIRMATION: {

                short number;

                if (type == PacketType.Play.Client.PONG) {
                    number = (short) new WrapperPlayClientPong(e).getId();
                } else {
                    number = new WrapperPlayClientWindowConfirmation(e).getActionId();
                }

                data.setLastLastClientTransaction(data.getLastClientTransaction());
                data.setLastClientTransaction(data.getCurrentClientTransaction());

                if (number <= -3000) {
                    data.setCurrentClientTransaction(number);
                }

                ObjectArrayList<Consumer<Short>> packetList = data.getWaitingConfirms().get(number);

                if (packetList != null && !packetList.isEmpty()) {
                    for (Consumer<Short> consumer : packetList) {
                        consumer.accept(number);
                    }
                    packetList.clear();
                    data.getWaitingConfirms().remove(number);
                }

                Deque<Integer> slots = data.getBackSwitchSlots().get((int) number);

                if (data.isPendingBackSwitch() && slots != null) {
                    data.setPendingBackSwitch(false);

                    if(slots.peekFirst() != null) {
                        int slot = slots.peekFirst();

                        PlayerUtil.sendPacket(data.getBukkitPlayer(),
                                new WrapperPlayServerHeldItemChange(slot));
                    }

                    slots.remove((int) number);
                }

                if (data.getTransactionTime().containsKey(number)) {

                    long transactionStamp = data.getTransactionTime().get(number);

                    if (!data.isHasReceivedTransaction()) {
                        data.setTransactionClock(transactionStamp);
                    }

                    data.setHasReceivedTransaction(true);

                    //Ping
                    data.setLastTransactionPing(data.getTransactionPing());
                    data.setTransactionPing(TimeUnit.NANOSECONDS.toMillis(nanoTime - transactionStamp));

                    //Remove processed id to prevent weird stuff / mem leaks
                    data.getTransactionTime().remove(number);
                    data.setLastTransactionPingUpdate(transactionStamp);

                    data.setPingInTicks(Math.min(15, MathUtil.getPingInTicks(data.getTransactionPing() + 50L)));
                }

                if (number == data.getFirstTransaction()) {
                    data.setReadyToAccept(true);
                }

                data.getNetHandler().handleClientTransaction(number);

                data.setLastTransaction(nanoTime);
                //callEvent = new TransactionEvent(number, ping);

                break;
            }
        }
    }


    public void handlePacketPlaySend(PacketPlaySendEvent e, long nanoTime, KarhuPlayer data) {

        final PacketType.Play.Server type = e.getPacketType();

        if(!data.isObjectLoaded()) return;

        switch (type) {

            case PLAYER_POSITION_AND_LOOK: {
                final WrapperPlayServerPlayerPositionAndLook position = new WrapperPlayServerPlayerPositionAndLook(e);

                Vector3d pos = new Vector3d(position.getX(), position.getY(), position.getZ());

                CustomLocation locationPlayer = data.getLocation();

                if (!data.isNewerThan8() && Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_9)) {
                    if (position.isRelativeFlag(RelativeFlag.X)) {
                        pos = pos.add(new Vector3d(locationPlayer.x, 0, 0));
                    }

                    if (position.isRelativeFlag(RelativeFlag.Y)) {
                        pos = pos.add(new Vector3d(0, locationPlayer.y, 0));
                    }

                    if (position.isRelativeFlag(RelativeFlag.Z)) {
                        pos = pos.add(new Vector3d(0, 0, locationPlayer.z));
                    }

                    position.setX(pos.getX());
                    position.setY(pos.getY());
                    position.setZ(pos.getZ());
                    position.setRelativeMask((byte) (position.getRelativeFlags().getMask() & 0b11000));
                }

                final double x = pos.getX(),
                        y = pos.getY(),
                        z = pos.getZ();


                Teleport teleport = new Teleport(new TeleportPosition(x, y, z), position.getRelativeFlags(), position.getTeleportId(), nanoTime);

                data.queueToPrePing((uid) -> {
                    //Bukkit.broadcastMessage("Teleporting id: " + uid);
                    data.getTeleportManager().locations.add(teleport);
                    ++data.getTeleportManager().teleportsPending;
                    data.setInventoryOpen(false);
                });

                data.queueToPostPing((uid) -> {
                    data.queueToFlying(1, (tick) -> {
                        data.getTeleportManager().locations.remove(teleport);
                        --data.getTeleportManager().teleportsPending;
                        //Bukkit.broadcastMessage("Done id: " + uid + " | " + data.getTotalTicks() + " tps: " + data.getTeleportManager().locations.size());
                    });

                });

                data.setLastTeleportPacket(data.getServerTick());

                break;
            }


            case ENTITY_VELOCITY: {

                final WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(e);

                final Vector3d vecVelo = velocity.getVelocity();

                if (velocity.getEntityId() == e.getUser().getEntityId()) {

                    final int tickToUse = data.getCurrentServerTransaction();

                    final int sequence = data.getNextSequence();

                    data.queueToPrePing((uid) -> {
                        data.setConfirmingVelocity(true);

                        int uidInt = uid;

                        if (uidInt != tickToUse) {
                            Karhu.getInstance().printCool("&b> &fDEBUG: " + uidInt + "/" + tickToUse);
                        }

                        //Bukkit.broadcastMessage("§aPRE velocity transaction" + tickToUse + " t: " + uid);

                        Vector vector = new Vector(vecVelo.getX(), vecVelo.getY(), vecVelo.getZ());

                        ConcurrentLinkedDeque<VelocityPending> velocities =
                                data.getVelocityPending().getOrDefault(uidInt, new ConcurrentLinkedDeque<>());

                        velocities.add(new VelocityPending((short) tickToUse, vector, false, sequence));

                        data.getVelocityPending().put(uidInt, velocities);
                    });

                    data.queueToPostPing((uid) -> {
                        ConcurrentLinkedDeque<VelocityPending> velos = data.getTickVelocities(tickToUse);

                        if (velos != null) {

                            List<VelocityPending> sorted = new ArrayList<>(velos);
                            Collections.sort(sorted, new Comparator<VelocityPending>() {
                                public int compare(VelocityPending a, VelocityPending b) {
                                    return Integer.compare(a.getSequence(), b.getSequence());
                                }
                            });

                            for (VelocityPending velocityPending : sorted) {
                                if (!velocityPending.isMarkedSent()) {
                                    data.velocityTick(velocityPending, false);
                                }
                                velos.remove(velocityPending);

                                if (velos.isEmpty()) {
                                    data.getVelocityPending().remove(tickToUse);
                                }
                            }
                        }

                        //Bukkit.broadcastMessage(e.getPlayer().getName() + " §cPOST velocity transaction " + tickToUse + " t: " + data.getTotalTicks());
                    });

                }
                break;
            }

            case EXPLOSION: {

                final WrapperPlayServerExplosion explosion = new WrapperPlayServerExplosion(e);
                Vector3d playerMotion = explosion.getKnockback();

                if (playerMotion != null) {

                    boolean invalid = playerMotion.getX() == 0 && playerMotion.getY() == 0 && playerMotion.getZ() == 0;

                    if (!invalid) {
                        final int tickToUse = data.getCurrentServerTransaction();

                        final int sequence = data.getNextSequence();

                        data.queueToPrePing((uid) -> {
                            data.setConfirmingVelocity(true);
                            int uidInt = uid;

                            if (uidInt != tickToUse) {
                                Karhu.getInstance().printCool("&b> &fDEBUG: " + uidInt + "/" + tickToUse);
                            }

                            //Bukkit.broadcastMessage("§aPRE velocity transaction" + tickToUse + " t: " + uid);

                            Vector vector = new Vector(playerMotion.getX(), playerMotion.getY(), playerMotion.getZ());

                            ConcurrentLinkedDeque<VelocityPending> velocities =
                                    data.getVelocityPending().getOrDefault(uidInt, new ConcurrentLinkedDeque<>());

                            velocities.add(new VelocityPending((short) tickToUse, vector, true, sequence));

                            data.getVelocityPending().put(uidInt, velocities);
                        });

                        data.queueToPostPing((uid) -> {
                            ConcurrentLinkedDeque<VelocityPending> velos = data.getTickVelocities(tickToUse);

                            if (velos != null) {

                                List<VelocityPending> sorted = new ArrayList<>(velos);
                                Collections.sort(sorted, new Comparator<VelocityPending>() {
                                    public int compare(VelocityPending a, VelocityPending b) {
                                        return Integer.compare(a.getSequence(), b.getSequence());
                                    }
                                });

                                for (VelocityPending velocityPending : sorted) {
                                    if (!velocityPending.isMarkedSent()) {
                                        data.velocityTick(velocityPending, false);
                                    }
                                    velos.remove(velocityPending);

                                    if (velos.isEmpty()) {
                                        data.getVelocityPending().remove(tickToUse);
                                    }
                                }
                            }
                        });
                    }
                }

                break;
            }

            case ENTITY_ROTATION: {

                final WrapperPlayServerEntityRotation look = new WrapperPlayServerEntityRotation(e);

                data.queueToPrePing((uid) -> EntityLocationHandler.updateEntityLook(data, look.getEntityId()));

                break;
            }

            case ENTITY_POSITION_SYNC: {
                WrapperPlayServerEntityPositionSync entity = new WrapperPlayServerEntityPositionSync(e);
                final EntityPositionData values = entity.getValues();
                final Vector3d move = values.getPosition();

                // NOT working data.getLocation().distanceSquared(move) <= 4096.0

                data.queueToPrePing((uid) -> {
                    EntityLocationHandler.updateEntityTeleport2(data, entity.getId(),
                            move.getX(), move.getY(), move.getZ(),
                            true);
                });

                data.queueToPostPing(uid -> {
                    EntityData edata = data.getEntityData().get(entity.getId());

                    if (edata != null) {
                        edata.postTransaction();
                    }
                });

                break;
            }


            case ENTITY_RELATIVE_MOVE_AND_ROTATION:
            case ENTITY_RELATIVE_MOVE: {
                int entityId;
                double deltaX, deltaY, deltaZ;

                if (type == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
                    WrapperPlayServerEntityRelativeMove entity = new WrapperPlayServerEntityRelativeMove(e);

                    entityId = entity.getEntityId();

                    deltaX = entity.getDeltaX();
                    deltaY = entity.getDeltaY();
                    deltaZ = entity.getDeltaZ();

                } else {
                    WrapperPlayServerEntityRelativeMoveAndRotation entity = new WrapperPlayServerEntityRelativeMoveAndRotation(e);

                    entityId = entity.getEntityId();

                    deltaX = entity.getDeltaX();
                    deltaY = entity.getDeltaY();
                    deltaZ = entity.getDeltaZ();
                }

                data.queueToPrePing((uid) -> {
                    EntityLocationHandler.updateEntityRelMove2(data, entityId,
                            deltaX, deltaY, deltaZ);
                });

                data.queueToPostPing((uid) -> {
                    EntityData edata = data.getEntityData().get(entityId);

                    if (edata != null) {
                        edata.postTransaction();
                    }

                });

                break;
            }

            case SPAWN_LIVING_ENTITY: {
                WrapperPlayServerSpawnLivingEntity wrapper = new WrapperPlayServerSpawnLivingEntity(e);

                double newX = wrapper.getPosition().getX(),
                        newY = wrapper.getPosition().getY(),
                        newZ = wrapper.getPosition().getZ();

                data.queueToPrePing((uid) -> {
                    EntityLocationHandler.addEntity(data, wrapper.getEntityType(), newX, newY, newZ, wrapper.getEntityId());
                });

                break;
            }

            case SPAWN_ENTITY: {
                WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(e);

                double newX = wrapper.getPosition().getX(),
                        newY = wrapper.getPosition().getY(),
                        newZ = wrapper.getPosition().getZ();

                data.queueToPrePing((uid) -> {
                    EntityLocationHandler.addEntity(data, wrapper.getEntityType(), newX, newY, newZ, wrapper.getEntityId(), wrapper.getData());
                });

                break;
            }

            case SPAWN_PLAYER: {

                WrapperPlayServerSpawnPlayer wrapper = new WrapperPlayServerSpawnPlayer(e);

                double newX = wrapper.getPosition().getX(),
                        newY = wrapper.getPosition().getY(),
                        newZ = wrapper.getPosition().getZ();

                data.queueToPrePing((uid) -> {
                    EntityLocationHandler.addEntity(data, EntityTypes.PLAYER, newX, newY, newZ, wrapper.getEntityId());
                });

                break;
            }

            case ENTITY_TELEPORT: {
                final WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport(e);

                Vector3d pos = teleport.getPosition();
                int entityId = teleport.getEntityId();

                data.queueToPrePing(uid -> {
                    EntityData edata = data.getEntityData().get(entityId);

                    if (edata == null) return;


                    if (data.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_16_1)
                            && Math.abs(edata.newX - pos.getX()) < 0.03125D
                            && Math.abs(edata.newY - pos.getY()) < 0.015625D
                            && Math.abs(edata.newZ - pos.getZ()) < 0.03125D) {

                        EntityLocationHandler.updateEntityTeleport2(data, entityId, edata.newX, edata.newY, edata.newZ, true);
                    } else {
                        EntityLocationHandler.updateEntityTeleport2(data, entityId, pos.getX(), pos.getY(), pos.getZ(), true);
                    }
                });

                data.queueToPostPing(uid -> {
                    EntityData edata = data.getEntityData().get(entityId);

                    if (edata != null) {
                        edata.postTransaction();
                    }
                });

                break;
            }

            case DESTROY_ENTITIES: {
                final WrapperPlayServerDestroyEntities wrapper = new WrapperPlayServerDestroyEntities(e);

                int[] entityIds = wrapper.getEntityIds();

                data.queueToPrePing((uid) -> {
                    EntityLocationHandler.destroyEntity(data, entityIds);
                });
                break;
            }

            case USE_BED: {
                WrapperPlayServerUseBed bed = new WrapperPlayServerUseBed(e);

                if (data.getEntityId() == bed.getEntityId()) {

                    data.queueToPrePing((uid) -> {
                        data.setInBed(true);
                        data.setBedPos(new Vec3(bed.getPosition().getX() + 0.5, bed.getPosition().getY(), bed.getPosition().getZ() + 0.5));
                    });
                }
                break;
            }

            case ENTITY_ANIMATION: {
                WrapperPlayServerEntityAnimation animation = new WrapperPlayServerEntityAnimation(e);

                if (data.getEntityId() == animation.getEntityId()
                        && animation.getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP) {
                    data.queueToPrePing((uid) -> {
                        data.setInBed(false);
                        data.setBedTicks(data.getTotalTicks());
                    });
                }
                break;
            }

            case OPEN_WINDOW: {
                data.queueToPrePing((uid) -> {
                    data.setInventoryOpen(true);
                    data.setInvStamp(data.getTotalTicks());
                    data.setUsingItem(false);
                    data.setEating(false);
                });
                break;
            }

            case CLOSE_WINDOW: {
                data.queueToPrePing((uid) -> {
                    data.setInventoryOpen(false);
                    data.setInvStamp(data.getTotalTicks());
                });
                break;
            }

            case ENTITY_EFFECT: {
                WrapperPlayServerEntityEffect wrapper = new WrapperPlayServerEntityEffect(e);

                int entityId = wrapper.getEntityId();

                if (data.getEntityId() != entityId) return;

                int effectId = wrapper.getPotionType().getId(data.getClientVersion());
                int amplifier = wrapper.getEffectAmplifier();

                if (effectId == -1) {
                    Karhu.getInstance().printCool("&b> &fClientVersion can't be grabbed for potions " + data.getClientVersion());
                    return;
                }

                data.queueToPrePing((uid) -> {
                    data.getEffectManager().addPotionEffect(effectId, amplifier);
                });

                break;
            }

            case REMOVE_ENTITY_EFFECT: {
                WrapperPlayServerRemoveEntityEffect wrapper = new WrapperPlayServerRemoveEntityEffect(e);
                if (data.getEntityId() != wrapper.getEntityId()) {
                    return;
                }

                int potionId = wrapper.getPotionType().getId(data.getClientVersion());

                if (potionId == -1) {
                    Karhu.getInstance().printCool("&b> &fClientVersion can't be grabbed for potions " + data.getClientVersion());
                    return;
                }

                data.queueToPrePing((uid) -> {
                    data.getEffectManager().removePotionEffect(potionId);
                });

                break;
            }

            case UPDATE_ATTRIBUTES: {
                final WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(e);
                if (packet.getEntityId() == data.getEntityId()) {
                    data.queueToPrePing(task -> {
                        for (WrapperPlayServerUpdateAttributes.Property property : packet.getProperties()) {
                            if (property.getAttribute() == Attributes.GENERIC_MOVEMENT_SPEED) {
                                double speed = PlayerUtil.getModifiedBaseValue(property.getModifiers(), property.getValue(), true, data);
                                data.setWalkSpeed((float) speed);
                                data.setWalkSpeedDouble(speed);
                            } else if (property.getAttribute() == Attributes.PLAYER_ENTITY_INTERACTION_RANGE) {
                                double mod = PlayerUtil.getModifiedBaseValue(property.getModifiers(), property.getValue(), false, data);
                                data.setInteractionRange(mod);
                            }
                        }
                    });
                } else {
                    data.queueToPrePing(task -> {
                        for (WrapperPlayServerUpdateAttributes.Property property : packet.getProperties()) {

                           if (property.getAttribute() == Attributes.GENERIC_SCALE) {
                               EntityData entityData = data.getEntityData().get(packet.getEntityId());

                               if (entityData != null) {
                                   entityData.setScale((float) property.getValue());
                               }
                           }
                        }
                    });
                }

                break;
            }

            case ENTITY_METADATA: {
                WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(e);

                if (packet.getEntityId() == data.getEntityId()) {

                    if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_14)) {
                        int id = 12;

                        if (Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
                            id = 13;
                        } else if(Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_17)){
                            id = 14;
                        }

                        com.github.retrooper.packetevents.protocol.entity.data.EntityData<?> bedBlock = getIndex(packet.getEntityMetadata(), id);
                        if (bedBlock != null) {

                            data.queueToPrePing( task -> {
                                Optional<Vector3i> bed = ((Optional<Vector3i>) bedBlock.getValue());
                                if (bed.isPresent()) {
                                    Vector3i bedPos = bed.get();
                                    data.setInBed(true);
                                    data.setBedPos(new Vec3(bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5));
                                } else {
                                    data.setInBed(false);
                                }
                            });
                        }
                    }

                    if (data.isNewerThan8()) {
                        com.github.retrooper.packetevents.protocol.entity.data.EntityData<?> actions = getIndex(packet.getEntityMetadata(), 0);

                        if (actions != null) {
                            Object zeroBitField = actions.getValue();

                            if (zeroBitField instanceof Byte) {
                                byte field = (byte) zeroBitField;
                                boolean glide = (field & 0x80) == 0x80;
                                boolean sprinting = (field & 0x8) == 0x8;

                                AtomicInteger receivedTick = new AtomicInteger();

                                data.queueToPrePing((uid) -> {
                                    if (glide) {
                                        if (Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_8_8)) {
                                            data.setLastGlide(data.getTotalTicks());
                                        }
                                    }
                                    data.setGliding(glide);


                                    data.setSettingMetadataSprint(true);
                                    receivedTick.set(data.getTotalTicks());
                                });

                                data.queueToPostPing((uid) -> {

                                    if (receivedTick.get() == data.getTotalTicks()) {
                                        data.setMetadataSprint(sprinting);
                                    } else {
                                        //GG transaction got split
                                    }

                                    data.queueToFlying(1, (tick) -> {
                                        data.setSettingMetadataSprint(false);
                                    });
                                });

                            }
                        }
                    }
                }

                if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9_4)) {
                    com.github.retrooper.packetevents.protocol.entity.data.EntityData<?> gravity = getIndex(packet.getEntityMetadata(), 5);

                    data.queueToPrePing((uid) -> {
                        if (gravity != null) {
                            Object gravityObject = gravity.getValue();

                            if (gravityObject instanceof Boolean) {
                                EntityData entityData = data.getEntityData().get(packet.getEntityId());
                                if (entityData != null) {
                                    entityData.gravity = !((Boolean) gravityObject);
                                }
                            }
                        }
                    });
                }

                if (Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_8_8)) {
                    data.queueToPrePing((uid) -> {
                        EntityData edata = data.entityData.get(packet.getEntityId());
                        if (edata == null) return;

                        if (edata.getType() == EntityTypes.FISHING_BOBBER) {
                            int index;
                            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_9_4)) {
                                index = 5;
                            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_14_4)) {
                                index = 6;
                            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
                                index = 7;
                            } else {
                                index = 8;
                            }

                            com.github.retrooper.packetevents.protocol.entity.data.EntityData<?> hookWatchableObject = getIndex(packet.getEntityMetadata(), index);
                            if (hookWatchableObject == null) return;

                            Integer attachedEntityID = (Integer) hookWatchableObject.getValue();

                            edata.hookAttachedToId = attachedEntityID - 1;
                        }
                    });
                }

                break;
            }

            case JOIN_GAME: {
                final WrapperPlayServerJoinGame packet = new WrapperPlayServerJoinGame(e);
                if (packet.getGameMode() != null) {
                    data.gameMode = packet.getGameMode();
                    data.setSpectating(data.getGameMode() == GameMode.SPECTATOR);
                }

                data.setEntityId(packet.getEntityId());
                break;
            }

            case CHANGE_GAME_STATE: {
                final WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(e);
                if (packet.getReason() == WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE) {
                    data.queueToPrePing((uid) -> {
                        data.gameMode = GameMode.getById((int) packet.getValue());
                        data.setSpectating(data.getGameMode() == GameMode.SPECTATOR);
                    });
                }
                break;
            }

            case PLAYER_ABILITIES: {
                final WrapperPlayServerPlayerAbilities packet = new WrapperPlayServerPlayerAbilities(e);

                data.getAbilityManager().onAbilityServer(packet);

                break;
            }

            case HELD_ITEM_CHANGE: {
                final WrapperPlayServerHeldItemChange packet = new WrapperPlayServerHeldItemChange(e);
                data.queueToPrePing((uid) -> {
                    data.lastServerSlot = packet.getSlot();
                    data.setCurrentSlot(packet.getSlot());
                    data.setUsingItem(false);
                    data.setEating(false);
                });
                break;
            }

            case ATTACH_ENTITY: {
                final WrapperPlayServerAttachEntity packet = new WrapperPlayServerAttachEntity(e);

                if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_9)) return;

                if(!packet.isLeash()) {
                    if (packet.getAttachedId() == data.getEntityId()) {
                        int vehicleId = packet.getHoldingId();

                        EntityData entityData = data.getEntityData().get(vehicleId);

                        EntityType entity;

                        if (entityData != null) entity = data.getEntityData().get(vehicleId).getType();
                        else entity = null;
                        

                        boolean riding = vehicleId != -1;

                        data.queueToPrePing((uid) -> {
                            data.setRiding(riding);
                            data.setLastUnmount(!riding ? data.getTotalTicks() : data.getLastUnmount());
                            data.setVehicleId(vehicleId);
                            data.setVehicle(entity);
                        });

                        data.setRidingUncertain(vehicleId != -1);
                    } else {
                        int vehicleId = packet.getHoldingId();

                        boolean riding = vehicleId != -1;

                        data.queueToPrePing((uid) -> {
                            EntityData edata = data.getEntityData().get(packet.getAttachedId());

                            if(edata != null) {
                                edata.setVehicleId(vehicleId);
                                edata.setRiding(riding);
                            }
                        });
                    }
                }

                break;
            }

            case SET_PASSENGERS: {
                final WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(e);

                data.queueToPrePing((uid) -> {
                    EntityData eData = data.getEntityData().get(data.getEntityId());

                    if (packet.getPassengers().length == 0) {
                        data.setRiding(false);
                        data.setLastUnmount(data.getTotalTicks());
                        data.setVehicleId(-1);
                        data.setVehicle(null);
                        if (eData != null) {
                            eData.setRiding(false);
                            eData.setVehicleId(-1);
                        }
                    }

                    for (int passengerId : packet.getPassengers()) {
                        EntityData passengerData = data.getEntityData().get(passengerId);
                        if (passengerId == data.getEntityId()) {
                            EntityData entityData = data.getEntityData().get(packet.getEntityId());
                            data.setRiding(true);
                            data.setVehicleId(packet.getEntityId());
                            data.setVehicle(entityData.getType());
                        }
                        if (passengerData != null) {
                            passengerData.setRiding(true);
                            passengerData.setVehicleId(packet.getEntityId());
                        }
                    }
                });

                break;
            }

            case ENTITY_STATUS: {
                WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus(e);

                int entityId = packet.getEntityId();
                int status = packet.getStatus();

                if (status == 31) {
                    EntityData edata = data.getEntityData().get(entityId);

                    if (edata.getType() == EntityTypes.FISHING_BOBBER) {
                        if (edata.hookAttachedToId == data.getEntityId()) {
                            data.queueToPrePing((uid) -> {
                                data.rodPulls.add(edata.owner);
                            });
                        }
                    }
                }
                break;
            }

            case RESPAWN: {
                data.queueToPrePing((uid) -> {
                    data.getCheckManager().runChecks(data.getCheckManager().getPacketChecks(), new RespawnEvent(), null);
                    data.setSprinting(false);
                    data.setSneaking(false);
                    data.setSprintAttribute(false);
                });
                break;
            }

            case KEEP_ALIVE: {
                data.setLastPingTime(nanoTime);
                break;
            }

            case CHUNK_DATA: {
                WrapperPlayServerChunkData packet = new WrapperPlayServerChunkData(e);
                data.getPacketWorldManager().handleChunkData(packet);
                break;
            }

            case MAP_CHUNK_BULK: {
                WrapperPlayServerChunkDataBulk packet = new WrapperPlayServerChunkDataBulk(e);
                data.getPacketWorldManager().handleChunkDataBulk(packet);
                break;
            }

            case BLOCK_CHANGE: {
                WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(e);
                data.getPacketWorldManager().handleBlockChange(packet);
                break;
            }

            case MULTI_BLOCK_CHANGE: {
                WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(e);
                data.getPacketWorldManager().handleMultiBlockChange(packet);
                break;
            }

            case UNLOAD_CHUNK: {
                WrapperPlayServerUnloadChunk packet = new WrapperPlayServerUnloadChunk(e);
                data.getPacketWorldManager().handleChunkUnload(packet);
                break;
            }

            default:
                break;

        }
    }


    public void handleTransaction(short number, long nanoTime, KarhuPlayer data) {
        if (number >= -20000 && number <= -3000) {

            if (!data.hasSentTickFirst) {
                data.hasSentTickFirst = true;
                data.transactionTime.put(number, nanoTime);

                data.useOldTransaction((uid) -> {
                    data.setServerTick(data.getServerTick() + 1);
                }, number);

            } else {
                data.hasSentTickFirst = false;
            }

            data.sendingPledgePackets = true;

        } else {
            if (!data.sendingPledgePackets && data.getTotalTicks() > 300) {
                Tasker.run(() -> {
                    data.getBukkitPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                            Karhu.getInstance().getConfigManager().getUninjectedKick()) + " (Time out)");
                });
            }
        }

        if (number < 0) {
            data.setCurrentServerTransaction(number);
        }

        data.getNetHandler().handleServerTransaction(number, nanoTime);
        data.setFirstTransactionSent(true);
    }

    public static com.github.retrooper.packetevents.protocol.entity.data.EntityData<?> getIndex(List<com.github.retrooper.packetevents.protocol.entity.data.EntityData<?>> objects, int index) {
        for (com.github.retrooper.packetevents.protocol.entity.data.EntityData<?> object : objects) {
            if (object.getIndex() == index) return object;
        }

        return null;
    }

}
