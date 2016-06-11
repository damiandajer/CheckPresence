package com.app.segmentation;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.app.handfeatures.HandFeatures;
import com.app.handfeatures.HandFeaturesData;
import com.app.handfeatures.HandFeaturesRaport;
import com.app.threads.TaskManager;
import com.app.threads.ThreadHandler;

import java.util.ArrayList;
import java.util.IllegalFormatConversionException;
import java.util.List;

/**
 * Created by Damian on 18.05.2016.
 */
public class OpenCVSubtractionThreads extends TaskManager {

    private static OpenCVSubtractionThreads instance = null;
    private static List<Integer> thresholds;

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

        for (Runnable r: ThreadHandler.getRunnables()) {
            try {
                bitmaps.add(((OpenCVSubtraction) r).getBitmap());
                handFeatures.add(((OpenCVSubtraction) r).getHandFeaturesObject());
                report = ((OpenCVSubtraction) r).getReport();
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

    public HandFeaturesRaport getReport() {
        return report;
    }

    public HandFeaturesRaport.CalculationRaport getReportCalculated() {
        return reportCalculated;
    }


    public List<int[]> getListOfIntArrays() {
        return intArrays;
    }

    public List<HandFeatures> getHandFeaturesObjects(){
        return handFeatures;
    }

    /**
     * Add new threads to process OpenCV Subtraction with submitted parameters
     * @param frameBitmap Bitmap with frame to process
     * @param backgroundBitmap Bitmap with background to substract
     */
    public void addNewThread(Bitmap frameBitmap, Bitmap backgroundBitmap, Rect sizeOfBitmapToSegmentation){
        for (int threshold:thresholds) {
            OpenCVSubtraction openCVSubtraction = new OpenCVSubtraction(frameBitmap, backgroundBitmap, threshold, sizeOfBitmapToSegmentation);
            ThreadHandler.createThread(openCVSubtraction);
        }
    }

    /**
     * Create List of thresholds to subtraction with OpenCV
     * For example with values (50, 100, 3) thresholds will be: 50, 75, 100)
     * @param min minimum value of threshold (0-255)
     * @param max maximum value of threshold (0-255)
     * @param n number of thresholds to process with
     */
    public void createListOfThresholds(int min, int max, int n){
        thresholds = new ArrayList<>();
        int iterator = 1;
        if(n > 1)
            iterator = Math.abs(max - min) / (n - 1);
        for(int minimum = min; minimum <= max; minimum+=iterator)
            thresholds.add(minimum);
    }
}
