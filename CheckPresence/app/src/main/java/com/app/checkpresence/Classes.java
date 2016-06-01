package com.app.checkpresence;

/**
 * Created by Szymon on 2016-05-31.
 */
public class Classes {

    private String groupName;

    private int year;
    private int month;
    private int day;

    //id w bazie danych
    private int id;

    public Classes(String groupName, int id, int year, int month, int day) {
        this.groupName = groupName;
        this.id = id;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public String getGroupName() {
        return groupName;
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

    public int getId(){ return id; }

    @Override
    public String toString() {
        return Integer.toString(id) + ". " + groupName + " - "
                + Integer.toString(day) + " " + Integer.toString(month) + " "+ Integer.toString(year);
    }
}
