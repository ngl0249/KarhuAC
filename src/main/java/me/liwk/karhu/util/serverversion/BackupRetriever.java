package me.liwk.karhu.util.serverversion;

import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class BackupRetriever {

    public static ServerVersion checkDefault(String ver) {
        if(ver.contains("1_7")) {
            return ServerVersion.V_1_7_10;
        }
        return ServerVersion.V_1_8_8;
    }

    public static String truncateLicense(String lic) {
        return lic.substring(0, 9);
    }

}
