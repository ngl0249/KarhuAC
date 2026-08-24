package me.liwk.karhu.handler.global;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.RequiredArgsConstructor;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.Velocity;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public final class VelocityManager {

    private final KarhuPlayer data;

    public final Map<Integer, Velocity> velocities = new HashMap<>();


    public void handlePreFlying(WrapperPlayClientPlayerFlying packet) {

    }


    public void handleTransaction(int uid) {

    }

    public void addVelocity(Vector vector, int uid) {
        velocities.put(uid, new Velocity(vector, uid, uid));
    }
}
