package me.liwk.karhu.util.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PlayerUtil {

    private static final UUID SPRINTING_SPEED_BOOST = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    public static final String legacyMovementSpeed = "generic.movementSpeed";
    public static final String movementSpeed = "minecraft:generic.movement";

    public static final String movementSpeedNew = "minecraft:movement_speed";

    public static float getScaledFriction(KarhuPlayer data){
        final float f4 = data.getCurrentFriction();
        final float f = 0.16277136F / (f4 * f4 * f4);
        float f5;
        if (data.isLastOnGroundPacket()) {
            f5 = data.getAttributeSpeed() * f;
        } else {
            f5 = data.isWasWasSprinting() ? (float) ((double) 0.02f + (double) 0.02f * 0.3D) : 0.02F;
        }
        return f5;
    }

    public static float getScaledFriction(KarhuPlayer data, boolean sprinting){
        final float f4 = data.getCurrentFriction();
        final float f = 0.16277136F / (f4 * f4 * f4);
        float f5;
        if (data.isLastOnGroundPacket()) {
            f5 = data.getAttributeSpeed() * f;
        } else {
            f5 = sprinting ? (float) ((double) 0.02f + (double) 0.02f * 0.3D) : 0.02F;
        }
        return f5;
    }

    public static double getModifiedBaseValue(List<WrapperPlayServerUpdateAttributes.PropertyModifier> collection,
                                              double base,
                                              boolean speed,
                                              KarhuPlayer data) {

        for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : getModifiers(Operation.ADDITION, collection, speed, data)) {
            base += modifier.getAmount();
        }
        double moveSpeed = base;
        for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : getModifiers(Operation.MULTIPLY_BASE, collection, speed, data)) {
            moveSpeed += base * modifier.getAmount();
        }
        for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : getModifiers(Operation.MULTIPLY_TOTAL, collection, speed, data)) {
            moveSpeed *= 1.0 + modifier.getAmount();
        }
        return moveSpeed;
    }

    /**
     * Filters and processes attribute modifiers based on operation type and speed conditions.
     *
     * @param operation the target operation to filter by
     * @param modifiers the list of modifiers to process
     * @param isSpeed whether this is processing speed-related attributes
     * @param playerData the player data object to update
     * @return filtered list of modifiers matching the specified operation
     */
    private static List<WrapperPlayServerUpdateAttributes.PropertyModifier> getModifiers(
            Operation operation,
            List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers,
            boolean isSpeed,
            KarhuPlayer playerData) {

        List<WrapperPlayServerUpdateAttributes.PropertyModifier> filteredModifiers = new ArrayList<>();

        // Handle speed-specific logic
        if (isSpeed) {
            processSpeedModifiers(modifiers, playerData);
        }

        // Check for sprint reset conditions
        if (playerData.isResettingSprint() && playerData.isSprintAttribute()) {
            playerData.setInvalidSprint(true);
        }

        // Filter modifiers based on operation and speed conditions
        for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : modifiers) {
            if (shouldIncludeModifier(modifier, operation, isSpeed)) {
                filteredModifiers.add(modifier);
            }
        }

        return filteredModifiers;
    }

    /**
     * Processes speed-related modifiers and updates player sprint attributes.
     */
    private static void processSpeedModifiers(
            List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers,
            KarhuPlayer playerData) {

        // Check if there is a sprinting speed boost modifier
        for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : modifiers) {
            if (isSprintingModifier(modifier)) {
                playerData.setSprintAttribute(true);
                playerData.setSprintAttributeTick(playerData.getTotalTicks());
                break;
            }
        }
    }

    /**
     * Determines whether a modifier should be included in the filtered results.
     */
    private static boolean shouldIncludeModifier(
            WrapperPlayServerUpdateAttributes.PropertyModifier modifier,
            Operation targetOperation,
            boolean isSpeed) {

        // Skip sprinting-related modifiers for speed processing
        if (isSpeed && isSprintingModifier(modifier)) {
            return false;
        }

        // Only include modifiers with matching operations
        return getOperation(modifier.getOperation()) == targetOperation;
    }

    /**
     * Checks if a modifier is related to sprinting.
     */
    private static boolean isSprintingModifier(WrapperPlayServerUpdateAttributes.PropertyModifier modifier) {
        return modifier.getUUID().equals(SPRINTING_SPEED_BOOST) ||
                "sprinting".equals(modifier.getName().getKey());
    }

    /**
     * Defines the types of operations that can be applied to attribute modifiers.
     */
    public enum Operation {
        /** Add the modifier value to the base attribute */
        ADDITION,

        /** Multiply the base attribute value */
        MULTIPLY_BASE,

        /** Multiply the total attribute value (after all other modifications) */
        MULTIPLY_TOTAL
    }

    private static Operation getOperation(WrapperPlayServerUpdateAttributes.PropertyModifier.Operation operation) {
        switch (operation) {
            case ADDITION:
                return Operation.ADDITION;
            case MULTIPLY_BASE:
                return Operation.MULTIPLY_BASE;
            case MULTIPLY_TOTAL:
                return Operation.MULTIPLY_TOTAL;
            default:
                return null;
        }
    }


    public static float getJumpHeight(KarhuPlayer data, float base) {
        return base + getJumpBooster(data);
    }

    public static float getJumpHeight(KarhuPlayer data) {
        return Math.max(0, (0.42F * data.getJumpFactor()) + getJumpBooster(data, false));
    }

    public static float getJumpBooster(KarhuPlayer data) {
        return getJumpBoostLevel(data) * 0.1F;
    }

    public static float getJumpBooster(KarhuPlayer data, boolean maxed) {
        return getJumpBoostLevel(data, maxed) * 0.1F;
    }

    public static int getJumpBoostLevel(KarhuPlayer data) {
        return Math.max(0, Math.max(data.getJumpBoost(), data.getCacheBoost()));
    }

    public static int getJumpBoostLevel(KarhuPlayer data, boolean maxed) {
        return maxed ? Math.max(0, Math.max(data.getJumpBoost(), data.getCacheBoost())) : Math.max(data.getJumpBoost(), data.getCacheBoost());
    }

    public static float getBaseSpeedAttribute(KarhuPlayer data, float mult) {
        return data.getWalkSpeed() * mult;
    }

    public static float getBaseSpeedAirAttribute(KarhuPlayer data, float multi) {
        return 0.35f + (data.getAttributeSpeed() * multi);
    }

    public static void sendPacket(Player player, PacketWrapper<?> wrapper) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper);
    }

    public static void sendPacket(Player player, short id) {

        if (Karhu.PING_PONG_MODE) {
            final WrapperPlayServerPing ping = new WrapperPlayServerPing(id);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, ping);
        } else {
            final WrapperPlayServerWindowConfirmation transaction = new WrapperPlayServerWindowConfirmation(0, id, false);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, transaction);
        }

    }

    public static void sendPacket(User user, short id) {

        if (Karhu.PING_PONG_MODE) {
            final WrapperPlayServerPing ping = new WrapperPlayServerPing(id);
            user.sendPacket(ping);
        } else {
            final WrapperPlayServerWindowConfirmation transaction = new WrapperPlayServerWindowConfirmation(0, id, false);
            user.sendPacket(transaction);
        }

    }

    public static void sendPacket(Object channel, PacketWrapper<?> wrapper) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(channel, wrapper);
    }

    public static boolean isGeyserPlayer(Player player) {
        if(!Karhu.getInstance().isFloodgate()) {
            return false;
        }
        return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }
    public static boolean isGeyserPlayer(UUID uuid) {
        if(!Karhu.getInstance().isFloodgate()) {
            return false;
        }
        return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
    }

}
