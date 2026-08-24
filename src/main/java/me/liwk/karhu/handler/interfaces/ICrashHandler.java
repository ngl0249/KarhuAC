package me.liwk.karhu.handler.interfaces;

import me.liwk.karhu.util.location.CustomLocation;

public interface ICrashHandler {

    void handleClientKeepAlive();

    void handleFlying(boolean moved, boolean looked, CustomLocation location, CustomLocation lastLocation);

    void handleArm();

    void handleWindowClick(int slot, int mode, int id, int button);

    void handleSlot();

    void handleCustomPayload();
    
    void handlePlace();

}
