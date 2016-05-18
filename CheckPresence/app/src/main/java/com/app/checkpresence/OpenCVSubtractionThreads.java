package com.app.checkpresence;

import android.graphics.Bitmap;

import java.util.IllegalFormatConversionException;
import java.util.List;

/**
 * Created by Damian on 18.05.2016.
 */
public class OpenCVSubtractionThreads extends TaskManager {

    private static OpenCVSubtractionThreads instance = null;

    private OpenCVSubtractionThreads(){}

    /**
     * Returns OpenCVSubtractionThreads instance
     * @return OpenCVSubtractionThreads instance
     */
    public static OpenCVSubtractionThreads getNewObject(){
        if(instance == null){
            instance = new OpenCVSubtractionThreads();
            return instance;
        }
        return instance;
    }


    @Override
    public void executeThreads() {
        super.executeThreads();

        for (Runnable r:ThreadHandler.getRunnables()) {
            try {
                bitmaps.add(((OpenCVSubtraction) r).getBitmap());
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
     * Add new thread to process OpenCV Subtraction with submitted parameters
     * @param frameBitmap Bitmap with frame to process
     * @param backgroundBitmap Bitmap with background to substract
     */
    public void addNewThread(Bitmap frameBitmap, Bitmap backgroundBitmap){
        OpenCVSubtraction openCVSubtraction = new OpenCVSubtraction(frameBitmap, backgroundBitmap);
        ThreadHandler.createThread(openCVSubtraction);
    }

    /**
     * Add new threads to process OpenCV Subtraction with submitted parameters
     * @param frameBitmaps List of Bitmaps with frame to process
     * @param backgroundBitmap Bitmap with background to substract
     */
    public void addNewThread(List<Bitmap> frameBitmaps, Bitmap backgroundBitmap){
        for (Bitmap frameBitmap:frameBitmaps) {
            OpenCVSubtraction openCVSubtraction = new OpenCVSubtraction(frameBitmap, backgroundBitmap);
            ThreadHandler.createThread(openCVSubtraction);
        }
    }
}
