package com.app.handFeaturesThreads;

import com.app.threads.TaskManager;
import com.app.threads.ThreadHandler;

import java.util.IllegalFormatConversionException;
import java.util.List;

/**
 * Created by Damian on 24.05.2016.
 */
public class HandFeaturesThreads extends TaskManager {
    private static HandFeaturesThreads instance = null;

    private HandFeaturesThreads(){}

    public static HandFeaturesThreads getNewObject(){
        if(instance == null){
            instance = new HandFeaturesThreads();
            return instance;
        }
        return instance;
    }

    @Override
    public void executeThreads() {
        super.executeThreads();

        for (Runnable r: ThreadHandler.getRunnables()) {
            try {
                if(checkIfHandFeaturesNotNull(((HandFeatures) r).getHandFeatures()))
                    floatArrays.add(((HandFeatures) r).getHandFeatures());
            }catch (IllegalFormatConversionException e){
                e.printStackTrace();
            }
        }
        ThreadHandler.clearLists();
    }

    public void addNewThread(List<int[]> segmentatedHandsList, int height, int width){
        for (int[] segmentatedHand:segmentatedHandsList) {
            HandFeatures handFeatures = new HandFeatures(segmentatedHand, height, width);
            ThreadHandler.createThread(handFeatures);
        }
    }

    private boolean checkIfHandFeaturesNotNull(float[] handFeatures){
        if(handFeatures != null)
            return true;
        else
            return false;
    }

    public List<float[]> getListOfArraysWithHandFeatures(){
        return floatArrays;
    }
}
