package me.liwk.karhu.util.location;

import com.github.retrooper.packetevents.util.Vector3d;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import static java.lang.Math.abs;

public class CustomLocation implements Cloneable {
    public double x, y, z;
    public float yaw, pitch;
    public boolean ground, moved, rotated, cheats, teleport;
    public long timeStamp;

    public CustomLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        timeStamp = System.currentTimeMillis();
    }

    public CustomLocation(double x, double y, double z, boolean ground) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.ground = ground;

        timeStamp = System.currentTimeMillis();
    }

    public CustomLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        timeStamp = System.currentTimeMillis();
    }

    public CustomLocation(double x, double y, double z, float yaw, float pitch, long timeStamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.timeStamp = timeStamp;
    }

    public CustomLocation(Location loc) {
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();

        this.timeStamp = System.currentTimeMillis();
    }

    public CustomLocation(Vector vector) {
        this.x = vector.getX();
        this.y = vector.getY();
        this.z = vector.getZ();

        this.timeStamp = System.currentTimeMillis();
    }

    public double distance(double x, double y, double z) {
        return abs(this.x - x) + abs(this.y - y) + abs(this.z - z);
    }

    public double distance(com.github.retrooper.packetevents.protocol.world.Location loc) {
        return abs(this.x - loc.getX()) + abs(this.y - loc.getY()) + abs(this.z - loc.getZ());
    }

    public double distance(CustomLocation o) {
        return Math.sqrt(NumberConversions.square(this.x - o.x) + NumberConversions.square(this.y - o.y) + NumberConversions.square(this.z - o.z));
    }

    public double distance(Location o) {
        return Math.sqrt(NumberConversions.square(this.x - o.getX()) + NumberConversions.square(this.y - o.getY()) + NumberConversions.square(this.z - o.getZ()));
    }

    public double distanceSquared(Vector3d other) {
        double distX = (this.x - other.x) * (this.x - other.x);
        double distY = (this.y - other.y) * (this.y - other.y);
        double distZ = (this.z - other.z) * (this.z - other.z);
        return distX + distY + distZ;
    }

    public double horizontal(CustomLocation o) {
        return Math.sqrt(NumberConversions.square(this.x - o.x) + NumberConversions.square(this.z - o.z));
    }

    public double horizontal(Location o) {
        return Math.sqrt(NumberConversions.square(this.x - o.getX()) + NumberConversions.square(this.z - o.getZ()));
    }

    public double vertical(Location o) {
        return abs(this.y - o.getY());
    }

    public double vertical(CustomLocation o) {
        return abs(this.y - o.getY());
    }

    @SneakyThrows
    public CustomLocation clone() {
        return (CustomLocation) super.clone();
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public Vector3d toVector3d() {
        return new Vector3d(x, y, z);
    }


    public Vector toBlockVector() {
        return new Vector(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    public int getBlockZ() {
        return NumberConversions.floor(this.z);
    }

    public int getBlockX() {
        return NumberConversions.floor(this.x);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String toString() {
        return String.format("%.2f", x) + ", " + String.format("%.2f", y) + ", " + String.format("%.2f", z);
    }

    public CustomLocation subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.z = z;
        this.y = y;
    }

    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public boolean isNotSet() {
        return x == 0 && y == 0 && z == 0 && yaw == 0 && pitch == 0;
    }

    public void setGround(boolean ground) {
        this.ground = ground;
    }

    public void setCheats(boolean cheats) {
        this.cheats = cheats;
    }
    public void setTeleport(boolean teleport) {
        this.teleport = teleport;
    }

}

