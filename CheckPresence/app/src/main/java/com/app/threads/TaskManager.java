package com.app.threads;

import android.graphics.Bitmap;

import com.app.handfeatures.HandFeaturesData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damian on 17.05.2016.
 */
public abstract class TaskManager {
    protected List<Bitmap> bitmaps;
    protected List<int[]> intArrays;
    protected List<float[]> floatArrays;
    protected HandFeaturesData handFeaturesData;

    /**
     * Executing threads
     */
    public void executeThreads(){
        bitmaps = new ArrayList<>();
        intArrays = new ArrayList<>();
        floatArrays = new ArrayList<>();
        handFeaturesData = null;

        ThreadHandler.startThreads();
        try {
            ThreadHandler.joinThreads();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}