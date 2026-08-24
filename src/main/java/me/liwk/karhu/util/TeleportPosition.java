package me.liwk.karhu.util;

import com.github.retrooper.packetevents.util.Vector3d;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.liwk.karhu.util.location.CustomLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class TeleportPosition {

    protected final double x;
    protected final double y;
    protected final double z;

    public double horizontal(Vector vector) {
        return Math.sqrt(NumberConversions.square(this.x - vector.getX()) +
                NumberConversions.square(this.z - vector.getZ()));
    }

    public double distance(Vector vector) {
        return Math.sqrt(NumberConversions.square(this.x - vector.getX()) +
                NumberConversions.square(this.y - vector.getY()) +
                NumberConversions.square(this.z - vector.getZ()));
    }

    public double distance(Vector3d vector) {
        return Math.sqrt(NumberConversions.square(this.x - vector.getX()) +
                NumberConversions.square(this.y - vector.getY()) +
                NumberConversions.square(this.z - vector.getZ()));
    }

    public double vertical(Vector vector) {
        return Math.sqrt(NumberConversions.square(this.y - vector.getY()));
    }

    @Override
    public String toString() {
        return "X " + x + ", " + "Y " + y + ", " + "Z " + z;
    }

    @NotNull
    public Location toLocation(@NotNull World world) {
        return new Location(world, this.x, this.y, this.z);
    }
    @NotNull
    public CustomLocation toCLocation() {
        return new CustomLocation(this.x, this.y, this.z);
    }
}
