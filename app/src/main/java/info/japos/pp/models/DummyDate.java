package info.japos.pp.models;

/**
 * Created by HWAHYUDI on 28-Feb-18.
 */

public class DummyDate {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    public DummyDate setYear(int year) {
        this.year = year;
        return this;
    }

    public DummyDate setMonth(int month) {
        this.month = month;
        return this;
    }

    public DummyDate setDay(int day) {
        this.day = day;
        return this;
    }

    public DummyDate setHour(int hour) {
        this.hour = hour;
        return this;
    }

    public DummyDate setMinute(int minute) {
        this.minute = minute;
        return this;
    }

    public DummyDate setSecond(int second) {
        this.second = second;
        return this;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }
}
