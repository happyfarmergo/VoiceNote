package com.android.voicenote.util;

import java.util.Calendar;

/**
 * Created by lvjinhua on 6/1/2016.
 */
public class TimeHolder {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    static Calendar calendar = Calendar.getInstance();

    public int getSum() {
        return year + month + day + hour + minute + second;
    }

    public TimeHolder() {

    }

    public static String getCurrentTime() {
        TimeHolder holder = new TimeHolder();
        calendar.setTimeInMillis(System.currentTimeMillis());
        holder.setTime(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
        return holder.getDate() + " " + holder.getTime();
    }

    public static TimeHolder parseTime(String s) {
        TimeHolder holder = new TimeHolder();
        String[] times = s.split(" ");
        String[] date = times[0].split("/");
        String[] time = times[1].split(":");
        if (date.length < 3 || time.length < 3) return null;
        holder.setTime(
                Integer.parseInt(date[2]),
                Integer.parseInt(date[1]),
                Integer.parseInt(date[0]),
                Integer.parseInt(time[0]),
                Integer.parseInt(time[1]),
                Integer.parseInt(time[2])
        );
        return holder;
    }

    public static boolean isTimeInvalid(TimeHolder create_time, TimeHolder alarm_time) {
        if (create_time.getYear() > alarm_time.getYear())
            return true;
        if (create_time.getMonth() > alarm_time.getMonth())
            return true;
        if (create_time.getDay() > alarm_time.getDay())
            return true;
        if (create_time.getHour() > alarm_time.getHour())
            return true;
        if (create_time.getMinute() > alarm_time.getMinute())
            return true;
        return false;
    }

    public void setTime(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public void setTime(int year, int month, int day, int hour, int minute, int second) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public String getDate() {
        return day + "/" + month + "/" + year;
    }

    public String getTime() {
        return hour + ":" + minute + ":" + second;
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

}
