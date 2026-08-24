package me.liwk.karhu.database.mongo;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.util.NetUtil;

import java.io.File;

public class MongoLoader {
    public static void init() {
        try {
            File mongo_lib = new File(Karhu.getInstance().getDataFolder().getAbsolutePath() + File.separator + "libs" + File.separator, "mongo.jar");
            //File mongo_lib = new File("plugins/Karhu/libs/", "mongo.jar");
            if (!mongo_lib.exists()) {
                NetUtil.download(mongo_lib, "http://maven.org/maven2/org/mongodb/mongo-java-driver/3.9.1/mongo-java-driver-3.9.1.jar");
            }
            //NetUtil.injectURL(mongo_lib, mongo_lib.toURI().toURL());
        } catch (Exception e) {
            System.out.println("Failed to download mongo: " + e.getMessage());
        }
    }
}
