package me.liwk.karhu;

public final class KarhuLogger {

    public static void info(String info) {
        System.out.println("[Karhu] " + info);
    }

    public static void critical(String info){
        System.out.println("----------------------------------------------------------------");
        System.out.println("[Karhu] ERROR: " + info);
        System.out.println("----------------------------------------------------------------");
    }

}
