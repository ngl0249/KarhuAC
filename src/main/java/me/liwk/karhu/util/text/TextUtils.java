package me.liwk.karhu.util.text;

import java.text.DecimalFormat;

public class TextUtils {

    private static final int[] decimalPlaces = {0, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

    public static double format(double d, int dec) {
        return (long) (d * decimalPlaces[dec] + 0.5) / (double) decimalPlaces[dec];
    }

    public static final DecimalFormat df = new DecimalFormat("#.#");

    public static String formatMillis(Long milis) {
        double seconds = (double) Math.max(0, milis) / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        double days = hours / 24;
        double weeks = days / 7;
        double months = days / 31;
        double years = months / 12;


        if (years >= 1) {
            return df.format(years) + " year" + (years != 1 ? "s" : "");
        } else if (months >= 1) {
            return df.format(months) + " month" + (months != 1 ? "s" : "");
        } else if (weeks >= 1) {
            return df.format(weeks) + " week" + (weeks != 1 ? "s" : "");
        } else if (days >= 1) {
            return df.format(days) + " day" + (days != 1 ? "s" : "");
        } else if (hours >= 1) {
            return df.format(hours) + " hour" + (hours != 1 ? "s" : "");
        } else if (minutes >= 1) {
            return df.format(minutes) + " minute" + (minutes != 1 ? "s" : "");
        } else {
            return df.format(seconds) + " second" + (seconds != 1 ? "s" : "");
        }
    }
}
