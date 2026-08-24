package me.liwk.karhu.event;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@Getter
public class BlockPlaceEvent extends Event {

    private final Vector blockPos, origin;
    private final ItemStack itemStack;
    private final long nanoTime, timeMillis;
    private final BlockFace direction;
    private final int face;
    private final double blockX, blockY, blockZ;
    private final World world;

    public BlockPlaceEvent(Vector blockPos, Vector origin, ItemStack itemStack, double blockX, double blockY, double blockZ, BlockFace direction, int face, long nanoTime, long timeMillis, World world) {
        this.blockPos = blockPos;
        this.origin = origin;
        this.itemStack = itemStack;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.nanoTime = nanoTime;
        this.timeMillis = timeMillis;
        this.direction = direction;
        this.face = face;
        this.world = world;
    }

    public long getTimeStamp() {
        return timeMillis;
    }

    public boolean isUsableItem() {
        return (this.itemStack != null
                && this.origin.getX() == -1
                && (this.origin.getY() == -1 || this.origin.getY() == 255 || this.origin.getY() == 4095)
                && this.origin.getZ() == -1
                && this.face == 255);
    }

    public Location getTargetedBlockLocation() {
        switch (face) {
            case 0:
                return new Location(world, origin.getBlockX(), origin.getBlockY() - 1, origin.getBlockZ());
            case 1:
                return new Location(world, origin.getBlockX(), origin.getBlockY() + 1, origin.getBlockZ());
            case 2:
                return new Location(world, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ() - 1);
            case 3:
                return new Location(world, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ() + 1);
            case 4:
                return new Location(world, origin.getBlockX() - 1, origin.getBlockY(), origin.getBlockZ());
            case 5:
                return new Location(world, origin.getBlockX() + 1, origin.getBlockY(), origin.getBlockZ());
            default:
                return new Location(world, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
        }
    }

    public Vector getTargetedBlockVec() {
        switch (face) {
            case 0:
                return new Vector(origin.getX(), origin.getY() + 1, origin.getZ());
            case 1:
                return new Vector(origin.getX(), origin.getY() - 1, origin.getZ());
            case 2:
                return new Vector(origin.getX(), origin.getY(), origin.getZ() + 1);
            case 3:
                return new Vector(origin.getX(), origin.getY(), origin.getZ() - 1);
            case 4:
                return new Vector(origin.getX() + 1, origin.getY(), origin.getZ());
            case 5:
                return new Vector(origin.getX() - 1, origin.getY(), origin.getZ());
            default:
                return new Vector(origin.getX(), origin.getY(), origin.getZ());
        }
    }

    public Location get420Johannes() {
        switch (face) {
            case 0:
                return new Location(world, origin.getBlockX(), origin.getBlockY() + 1, origin.getBlockZ());
            case 1:
                return new Location(world, origin.getBlockX(), origin.getBlockY() - 1, origin.getBlockZ());
            case 2:
                return new Location(world, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ() + 1);
            case 3:
                return new Location(world, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ() - 1);
            case 4:
                return new Location(world, origin.getBlockX() + 1, origin.getBlockY(), origin.getBlockZ());
            case 5:
                return new Location(world, origin.getBlockX() - 1, origin.getBlockY(), origin.getBlockZ());
            default:
                return new Location(world, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
        }
    }

    public Vector3i getBlockFacePosition() {
        switch (face) {
            case 0:
                return new Vector3i(0, -1, 0);
            case 1:
                return new Vector3i(0, 1, 0);
            case 2:
                return new Vector3i(0, 0, -1);
            case 3:
                return new Vector3i(0, 0, 1);
            case 4:
                return new Vector3i(-1, 0, 0);
            case 5:
                return new Vector3i(1, 0, 0);
            default:
                return new Vector3i(0, 0, 0);
        }
    }

}
