package com.app.checkpresence;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.IllegalFormatConversionException;
import java.util.List;

/**
 * Created by Damian on 18.05.2016.
 */
public class SegmentationThreads extends TaskManager {

    private static SegmentationThreads instance = null;
    private static List<Integer> listOfConditions;

    private SegmentationThreads(){}

    /**
     * Returns SegmentationThreads instance
     * @return SegmentationThreads instance
     */
    public static SegmentationThreads getNewObject(){
        if(instance == null){
            instance = new SegmentationThreads();
            return instance;
        }
        return instance;
    }

    @Override
    public void executeThreads() {
        super.executeThreads();

        for (Runnable r:ThreadHandler.getRunnables()) {
            try {
                intArrays.add(((Segmentation) r).getSegmentatedPicture());
            }catch (IllegalFormatConversionException e){
                e.printStackTrace();
            }
        }
        ThreadHandler.clearLists();
    }

    /**
     * Returns list of int array with pixels
     * @return List of int array with pixels
     */
    public List<int[]> getIntArrays(){return intArrays;}

    /**
     * Add new thread to process segmentation with submitted parameters
     * @param argb int array with ARGB pixels
     * @param height height of picture
     * @param width width of picture
     */
    public void addNewThread(int[] argb, int height, int width){
        for (int cond:listOfConditions) {
            Segmentation newSegmentation = new Segmentation(argb, height, width, cond);
            ThreadHandler.createThread(newSegmentation);
        }
    }

    /**
     * Add new threads to process segmentation with submitted parameters
     * @param argb List of int arrays with ARGB pixels
     * @param height height of picture
     * @param width width of picture
     */
    public void addNewThread(List<int[]> argb, int height, int width){
        for (int[] array:argb) {
            for (int cond:listOfConditions) {
                Segmentation newSegmentation = new Segmentation(array, height, width, cond);
                ThreadHandler.createThread(newSegmentation);
            }
        }
    }

    /**
     * Create list of conditions of segmentation (min value is 1)
     * @param n number of conditions to process
     */
    public void createListOfConditions(int n){
        listOfConditions = new ArrayList<>();
        for(int i = 1; i <= n; i++)
            listOfConditions.add(i);
    }
}
