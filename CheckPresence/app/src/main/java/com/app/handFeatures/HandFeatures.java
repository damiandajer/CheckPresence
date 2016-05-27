package com.app.handFeatures;

/**
 * Created by Damian on 24.05.2016.
 */
public class HandFeatures implements Runnable {
    int height, width;
    private int[] segmentatedHand;
    private float[] handFeatures;
    private native float[] findHandFeatures(int[] intARGBArray, int rows, int cols);

    public HandFeatures(int[] segmentatedHand, int height, int width){
        this.height = height;
        this.width = width;
        this.segmentatedHand = segmentatedHand;
    }

    @Override
    public void run() {
        findHandFeatures();
    }

    private void findHandFeatures(){
        this.handFeatures = findHandFeatures(this.segmentatedHand.clone(), this.height, this.width);
        if (handFeatures != null)
            System.out.println("Odnaleziono wszystkie cechy!");
    }

    public float[] getHandFeatures(){
        return this.handFeatures;
    }
}
