package me.liwk.karhu.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class AttackIndicator {


    public static int getPassedTicks(Player player) {
        try {
            LivingEntity livingEntity = (LivingEntity) player;
            Object handle = getOBCClass("entity.CraftLivingEntity").getDeclaredClasses()[0].getMethod("getHandle").invoke(livingEntity);
            Object object = getNMSClass("EntityLiving").getDeclaredClasses()[0].getField("aD").get(handle);
            try {
                return (int) object;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void resetAttackCooldown(Player player) {
        try {
            HumanEntity humanEntity = (HumanEntity) player;
            Object handle = getOBCClass("entity.CraftHumanEntity").getDeclaredClasses()[0].getMethod("getHandle").invoke(humanEntity);
            getNMSClass("EntityHuman").getDeclaredClasses()[0].getMethod("cZ").invoke(handle);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static float getMaximumRechargeTime(Player player) {
        try {
            HumanEntity humanEntity = (HumanEntity) player;
            Object handle = getOBCClass("entity.CraftHumanEntity").getDeclaredClasses()[0].getMethod("getHandle").invoke(humanEntity);
            Object object = getNMSClass("EntityHuman").getDeclaredClasses()[0].getMethod("cZ").invoke(handle);
            try {
                return (float) object;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return 0;
        }
    }

   /* public static float getCurrentCharge(Player player, float offset) {
        try {
            HumanEntity humanEntity = (HumanEntity) player;
            Object progress = 1;
            Object handle = NMSUtils.getCraftEntityHandle.invoke(humanEntity);
            if(Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_8_8) && Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_10)) {
                progress = getNMSClass("EntityHuman").getMethod("o").invoke(offset);
            } else if(Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_11_2) && Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_13)) {
                progress = getNMSClass("EntityHuman").getMethod("n", float.class).invoke(offset);
            } else if(Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_12_2) && Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_14)) {
                progress = getNMSClass("EntityHuman").getMethod("r").invoke(offset);
            }
            try {
                return (float) progress;
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return 1;
        }
    }*/

    private static Class<?> getOBCClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + getVersion() + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }
}
