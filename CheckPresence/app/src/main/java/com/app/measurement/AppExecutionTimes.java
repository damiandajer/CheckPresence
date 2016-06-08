package com.app.measurement;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by bijat on 08.06.2016.
 */
public class AppExecutionTimes {
    /*public AppExecutionTimes() {
        executionTimeList = new HashMap<>();
    }*/

    public static void startTime(ExecutionTimeName name) {
        AppExecutionTimes.executionTimeList.put(name, new FunctionExecutionTime(System.nanoTime()));
    }

    public static void endTime(ExecutionTimeName name) {
        long time = System.nanoTime();
        FunctionExecutionTime fet = AppExecutionTimes.executionTimeList.get(name);
        if (fet != null) {
            fet.setEndTime(time);
        }
    }

    public static void show(boolean ordered) {
        System.out.println("Collected execution times:");
        if (ordered == false) {
            for (Map.Entry<ExecutionTimeName, FunctionExecutionTime> fet : AppExecutionTimes.executionTimeList.entrySet()) {
                System.out.println("" + fet.getKey().name() + ": " + String.format(Locale.getDefault(), "%.4f", fet.getValue().getTime()) + "ms.");
            }
        }
        else {
            for (ExecutionTimeName name : ExecutionTimeName.values()) {
                FunctionExecutionTime fet = AppExecutionTimes.executionTimeList.get(name);
                if (fet != null) {
                    System.out.println("" + name.name() + ": " + String.format(Locale.getDefault(), "%.4f", fet.getTime()) + "ms.");
                }
            }
        }
    }

    public static void clear() {
        executionTimeList.clear();
    }

    private static Map<ExecutionTimeName, FunctionExecutionTime> executionTimeList = new HashMap<>();;
}


final class FunctionExecutionTime {
    FunctionExecutionTime(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    FunctionExecutionTime(long startTime) {
        this.startTime = startTime;
        this.endTime = this.startTime;
    }

    /**
     * Calculate and return time between StartTime and EndTime.
     * @return return difference time between StartTime and EndTime as miliseconds.
     */
    public float getTime() {
        return Math.abs(endTime - startTime) / 1000000f;
    }

    public long getStartTime()                  { return startTime; }
    public void setStartTime(long startTime)    { this.startTime = startTime; }
    public long getEndTime()                    { return endTime;}
    public void setEndTime(long endTime)        { this.endTime = endTime; }

    private long startTime;
    private long endTime;
}
