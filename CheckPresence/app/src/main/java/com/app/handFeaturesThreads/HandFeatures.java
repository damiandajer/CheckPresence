package com.app.handFeaturesThreads;

import com.app.checkpresence.Configure;
import com.app.handfeatures.HandFeaturesData;
import com.app.handfeatures.HandFeaturesException;
import com.app.handfeatures.HandFeaturesRaport;
import com.app.measurement.AppExecutionTimes;
import com.app.measurement.ExecutionTimeName;
import com.app.memory.CopyManager;

/**
 * Created by Damian on 24.05.2016.
 */
public class HandFeatures implements Runnable {
    private com.app.handfeatures.HandFeatures segmentatedHand;
    private float[] handFeatures;
    private HandFeaturesData handFeaturesData = null;
    private HandFeaturesRaport.CalculationRaport report;
    //private native float[] findHandFeatures(int[] intARGBArray, int rows, int cols);

    public HandFeatures(com.app.handfeatures.HandFeatures segmentatedHand){
        this.segmentatedHand = segmentatedHand;
    }

    @Override
    public void run() {
        findHandFeatures();
    }

    private void findHandFeatures(){
        report = findHandFeaturesJava(this.segmentatedHand);
        if(handFeaturesData != null)
            this.handFeatures = handFeaturesData.features;
        if (handFeatures != null)
            System.out.println("Odnaleziono wszystkie cechy!");
    }

    private HandFeaturesRaport.CalculationRaport findHandFeaturesJava(com.app.handfeatures.HandFeatures handFeaturesObject){
        try {
            AppExecutionTimes.startTime(ExecutionTimeName.HAND_FEATURE_CALCULATE);
            handFeaturesObject.calculateFeatures();
            report = handFeaturesObject.getRaportCalculated();
            AppExecutionTimes.endTime(ExecutionTimeName.HAND_FEATURE_CALCULATE);
            if (report.isGood == true) {
                if (Configure.SAVE_HAND_RECOGNIZATION_STEPS) {
                    CopyManager.saveBitmapToDisk(handFeaturesObject.getProcessed(true), com.app.handfeatures.HandFeatures.foundedHandsFeatures, "calculated_");
                    CopyManager.saveBitmapToDisk(handFeaturesObject.getConturBitmap(false), handFeaturesObject.foundedHandsFeatures, "contour_");
                }
                handFeaturesData = new HandFeaturesData(handFeaturesObject);

                if (Configure.SHOW_FOUND_HAND_FEATURES == true) {
                    handFeaturesData.show(true); // cechy 1 lini
                    handFeaturesData.show(false); // wypisuje pogrupowane cechy
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return report;
    }

    public HandFeaturesData getHandFeaturesData(){
        return handFeaturesData;
    }

    public float[] getHandFeatures(){
        return this.handFeatures;
    }

    public HandFeaturesRaport.CalculationRaport getCalculatedReport(){
        return report;
    }
}
