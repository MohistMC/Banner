// Paper start - Further improve server tick loop
package com.mohistmc.banner.paper;

public class RollingAverage {
    private final int size;
    private long time;
    private java.math.BigDecimal total;
    private int index = 0;
    private final java.math.BigDecimal[] samples;
    private final long[] times;

    public RollingAverage(int size, int tps, long secInHand) {
        this.size = size;
        this.time = size * secInHand;
        this.total = dec(tps).multiply(dec(secInHand)).multiply(dec(size));
        this.samples = new java.math.BigDecimal[size];
        this.times = new long[size];
        for (int i = 0; i < size; ++i) {
            this.samples[i] = dec(tps);
            this.times[i] = secInHand;
        }
    }

    private static java.math.BigDecimal dec(long t) {
        return new java.math.BigDecimal(t);
    }
    public void add(java.math.BigDecimal x, long t) {
        time -= times[index];
        total = total.subtract(samples[index].multiply(dec(times[index])));
        samples[index] = x;
        times[index] = t;
        time = t;
        total = total.add(x.multiply(dec(t)));
        if (index == size) {
            index = 0;
        }
    }

    public double getAverage() {
        return total.divide(dec(time), 30, java.math.RoundingMode.HALF_UP).doubleValue();
    }
}