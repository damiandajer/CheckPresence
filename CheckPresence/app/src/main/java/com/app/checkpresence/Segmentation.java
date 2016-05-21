package com.app.checkpresence;

/**
 * Created by Damian on 16.05.2016.
 */
public class Segmentation implements Runnable {
    private volatile int[] segmentatedPicture;
    int[] argb;
    int warunek;
    int height;
    int width;

    /**
     *
     * @param argb int array of ARGB pixels
     * @param height height
     * @param width width
     * @param warunek number of segmentations condition
     */
    public Segmentation(int[] argb, int height, int width, int warunek) {
        this.argb = argb;
        this.height = height;
        this.width = width;
        this.warunek = warunek;
    }

    @Override
    public void run() {
        System.out.println("Thread is processing frame with condition: " + warunek);
        this.segmentatedPicture = myNativeCode(argb, height, width, warunek);
    }

    /**
     * Returns int array of pixels
     * @return
     */
    public int[] getSegmentatedPicture(){
        return this.segmentatedPicture;
    }

    private native int[] myNativeCode(int[] argb, int rows, int cols, int warunek);
    private native void deleteSmallAreas(int[] argb, int rows, int cols);
}
