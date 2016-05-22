package com.app.checkpresence;

import android.graphics.Bitmap;

import java.util.IllegalFormatConversionException;
import java.util.List;

/**
 * Created by Damian on 17.05.2016.
 */
public class BitmapFromPixelsThreads extends TaskManager {

    private static BitmapFromPixelsThreads instance = null;

    private BitmapFromPixelsThreads(){}

    /**
     * Returns BitmapFromPixelsThreads instance
     * @return BitmapFromPixelsThreads instance
     */
    public static BitmapFromPixelsThreads getNewObject(){
        if(instance == null){
            instance = new BitmapFromPixelsThreads();
            return instance;
        }
        return instance;
    }

    @Override
    public void executeThreads() {
        super.executeThreads();

        for (Runnable r:ThreadHandler.getRunnables()) {
            try {
                bitmaps.add(((CreateBitmapFromPixels) r).getBitmap());
            }catch (IllegalFormatConversionException e){
                e.printStackTrace();
            }
        }
        ThreadHandler.clearLists();
    }

    /**
     * Returns List of processed Bitmaps
     * @return List of processed Bitmaps
     */
    public List<Bitmap> getBitmaps() {
        return bitmaps;
    }

    /**
     * Add new thread to create Bitmap with submitted parameters
     * @param argb int array with ARGB pixels
     * @param height height of picture
     * @param width width of picture
     */
    public void addNewThread(int[] argb, int height, int width){
        CreateBitmapFromPixels colouredBitmapFromPixels = new CreateBitmapFromPixels(argb, height, width);
        ThreadHandler.createThread(colouredBitmapFromPixels);
    }

    /**
     * Add new threads to create Bitmaps with submitted parameters
     * @param argb List of int arrays with ARGB pixels
     * @param height height of picture
     * @param width width of picture
     */
    public void addNewThread(List<int[]> argb, int height, int width){
        for (int[] array:argb) {
            CreateBitmapFromPixels colouredBitmapFromPixels = new CreateBitmapFromPixels(array, height, width);
            ThreadHandler.createThread(colouredBitmapFromPixels);
        }
    }
}