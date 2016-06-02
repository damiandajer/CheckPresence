package com.app.handFeaturesThreads;

import com.app.handfeatures.HandFeaturesData;
import com.app.handfeatures.HandFeaturesException;

/**
 * Created by Damian on 24.05.2016.
 */
public class HandFeatures implements Runnable {
    private com.app.handfeatures.HandFeatures segmentatedHand;
    private float[] handFeatures;
    private HandFeaturesData handFeaturesData = null;
    //private native float[] findHandFeatures(int[] intARGBArray, int rows, int cols);

    public HandFeatures(com.app.handfeatures.HandFeatures segmentatedHand){
        this.segmentatedHand = segmentatedHand;
    }

    @Override
    public void run() {
        findHandFeatures();
    }

    private void findHandFeatures(){
        findHandFeaturesJava(this.segmentatedHand);
        if(handFeaturesData != null)
            this.handFeatures = handFeaturesData.features;
        if (handFeatures != null)
            System.out.println("Odnaleziono wszystkie cechy!");
    }

    private void findHandFeaturesJava(com.app.handfeatures.HandFeatures handFeaturesObject){
        try {
            if (handFeaturesObject.calculateFeatures() == true) {
                //CopyManager.saveBitmapToDisk(handFeatures.getConturBitmap(true), CameraView.foundedHandsFeatures++, "Contour_");
                handFeaturesData = new HandFeaturesData(handFeaturesObject);
                handFeaturesData.show(true); // cechy 1 lini
                handFeaturesData.show(false); // wypisuje pogrupowane cechy
                ++com.app.handfeatures.HandFeatures.foundedHandsFeatures;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    public HandFeaturesData getHandFeaturesData(){
        return handFeaturesData;
    }

    public float[] getHandFeatures(){
        return this.handFeatures;
    }
}
