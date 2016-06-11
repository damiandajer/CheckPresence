package com.app.threads;

import android.graphics.Bitmap;

import com.app.handfeatures.HandFeatures;
import com.app.handfeatures.HandFeaturesData;
import com.app.handfeatures.HandFeaturesRaport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damian on 17.05.2016.
 */
public abstract class TaskManager {
    protected List<Bitmap> bitmaps;
    protected List<int[]> intArrays;
    protected List<float[]> floatArrays;
    protected List<HandFeatures> handFeatures;
    protected HandFeaturesData handFeaturesData;
    protected HandFeaturesRaport report;
    protected  HandFeaturesRaport.CalculationRaport reportCalculated;

    /**
     * Executing threads
     */
    public void executeThreads(){
        bitmaps = new ArrayList<>();
        intArrays = new ArrayList<>();
        floatArrays = new ArrayList<>();
        handFeatures = new ArrayList<>();
        handFeaturesData = null;

        ThreadHandler.startThreads();
        try {
            ThreadHandler.joinThreads();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}