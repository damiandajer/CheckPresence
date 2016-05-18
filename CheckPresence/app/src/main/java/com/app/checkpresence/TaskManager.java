package com.app.checkpresence;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damian on 17.05.2016.
 */
public abstract class TaskManager {
    List<Bitmap> bitmaps;
    List<int[]> intArrays;

    /**
     * Executing threads
     */
    public void executeThreads(){
        bitmaps = new ArrayList<>();
        intArrays = new ArrayList<>();

        ThreadHandler.startThreads();
        try {
            ThreadHandler.joinThreads();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //public List<Bitmap> getBitmaps(){return bitmaps;}

    //public List<int[]> getIntArrays(){return intArrays;}

}