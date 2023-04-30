package me.illusion.cosmos.utilities.time;

import java.util.concurrent.TimeUnit;

public class Time {

    private final int time;
    private final TimeUnit unit;

    public Time(int time, TimeUnit unit) {
        this.time = time;
        this.unit = unit;
    }

    public long as(TimeUnit unit) {
        return unit.convert(time, this.unit);
    }

    public long asTicks() {
        return as(TimeUnit.MILLISECONDS) / 50;
    }

}
