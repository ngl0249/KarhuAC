package me.liwk.karhu.check.setback;

import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.boundingbox.BoundingBox;
import me.liwk.karhu.util.player.BlockUtil;
import me.liwk.karhu.util.task.Tasker;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

//todo - utility mostly
public final class Setbacks {

    public static Location forgeToRotatedLocation(Location loc, KarhuPlayer data) {
        final Location locMod = loc.clone();
        locMod.setYaw(data.getLocation().yaw);
        locMod.setPitch(data.getLocation().pitch);
        return locMod;
    }

    public static CustomLocation forgeToRotatedLocation(CustomLocation loc, KarhuPlayer data) {
        final CustomLocation locMod = loc.clone();
        locMod.setYaw(data.getLocation().yaw);
        locMod.setPitch(data.getLocation().pitch);
        return locMod;
    }

    public static Location moveOutOfBlockSafely(double x, double z, KarhuPlayer data) {
        int blockX = MathHelper.floor(x);
        int blockZ = MathHelper.floor(z);

        if (!suffocatesAt(blockX, blockZ, data)) {
            return null;
        }

        double relativeXMovement = x - blockX;
        double relativeZMovement = z - blockZ;
        BlockFace direction = null;
        double lowestValue = Double.MAX_VALUE;
        for (BlockFace direction2 : new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH}) {
            double d6;
            double d7 = direction2 == BlockFace.WEST || direction2 == BlockFace.EAST ? relativeXMovement : relativeZMovement;
            d6 = direction2 == BlockFace.EAST || direction2 == BlockFace.SOUTH ? 1.0 - d7 : d7;
            // d7 and d6 flip the movement direction based on desired movement direction
            boolean doesSuffocate;
            switch (direction2) {
                case EAST:
                    doesSuffocate = suffocatesAt(blockX + 1, blockZ, data);
                    break;
                case WEST:
                    doesSuffocate = suffocatesAt(blockX - 1, blockZ, data);
                    break;
                case NORTH:
                    doesSuffocate = suffocatesAt(blockX, blockZ - 1, data);
                    break;
                default:
                case SOUTH:
                    doesSuffocate = suffocatesAt(blockX, blockZ + 1, data);
                    break;
            }

            if (d6 >= lowestValue || doesSuffocate) continue;
            lowestValue = d6;
            direction = direction2;
        }

        if (direction != null) {
            Location loc = data.getLocation().toLocation(data.getWorld());
            Location toSetback = null;
            if (direction == BlockFace.WEST || direction == BlockFace.EAST) {
                Location locSubtract = loc.clone();
                Location locAddition = loc.clone();

                locSubtract.setX(loc.getX() - 0.1 * (double) direction.getModX());
                locAddition.setX(loc.getX() + 0.1 * (double) direction.getModX());

                if(BlockUtil.chunkLoaded(locSubtract) && !locSubtract.getBlock().getType().isSolid()) {
                    Tasker.run(() -> {
                        data.getBukkitPlayer().teleport(locSubtract);
                    });
                    return locSubtract;
                } else if(BlockUtil.chunkLoaded(locAddition) && !locAddition.getBlock().getType().isSolid()) {
                    Tasker.run(() -> {
                        data.getBukkitPlayer().teleport(locAddition);
                    });
                    return locAddition;
                }
            } else {
                Location locSubtract = loc.clone();
                Location locAddition = loc.clone();

                locSubtract.setZ(loc.getZ() - 0.1 * (double) direction.getModZ());
                locAddition.setZ(loc.getZ() + 0.1 * (double) direction.getModZ());

                if(BlockUtil.chunkLoaded(locSubtract) && !locSubtract.getBlock().getType().isSolid()) {
                    Tasker.run(() -> {
                        data.getBukkitPlayer().teleport(locSubtract);
                    });
                    return locSubtract;
                } else if(BlockUtil.chunkLoaded(locAddition) && !locAddition.getBlock().getType().isSolid()) {
                    Tasker.run(() -> {
                        data.getBukkitPlayer().teleport(locAddition);
                    });
                    return locAddition;
                }
            }
        }
        return null;
    }

    public static boolean suffocatesAt(int x, int z, KarhuPlayer data) {
        BoundingBox boundingBox = new BoundingBox(data,
                x,
                data.getBoundingBox().minY,
                z,
                x + 1.0,
                data.getBoundingBox().maxY,
                z + 1.0)
                .expand(-1.0E-7);

        return !boundingBox.getCollidingBlocks().isEmpty();
    }

}
