package me.liwk.karhu.util.benchmark;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class Benchmark {
    private final BenchmarkType profileType;
    private final double magnitudeMultiplier;
    private double runningAverage;
    private double runningMedian;
    private int results;

    public Benchmark(BenchmarkType profileType, int precision) {
        this.profileType = profileType;
        this.magnitudeMultiplier = 1.0 / precision;
        this.results = 0;
    }

    public void insertResult(long start, long end) {
        double nanosecondSpent = (double) (end - start) / 1E6;
        this.insertAverage(nanosecondSpent);
        this.insertMedian(nanosecondSpent);

        if(++results > 20000) {
            results /= 2;
        }
    }

    private void insertAverage(double sample) {
        runningAverage += (sample - runningAverage) * magnitudeMultiplier;
    }

    private void insertMedian(double sample) {
        runningMedian += Math.copySign(runningMedian * magnitudeMultiplier, sample - runningMedian);
    }
}