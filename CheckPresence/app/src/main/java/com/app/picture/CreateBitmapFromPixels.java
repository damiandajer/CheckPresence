package com.app.picture;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Damian on 16.05.2016.
 */
public class CreateBitmapFromPixels implements Runnable {
    private volatile Bitmap bmp;
    int[] inputColorSegmentationDataPicture;
    int height;
    int width;

    /**
     *
     * @param inputColorSegmentationDataPicture int array of ARGB pixels
     * @param height height
     * @param width width
     */
    public CreateBitmapFromPixels(int[] inputColorSegmentationDataPicture, int height, int width) {
        this.inputColorSegmentationDataPicture = inputColorSegmentationDataPicture;
        this.height = height;
        this.width = width;
    }

    @Override
    public void run() {
        //System.out.println("Thread is creating Bitmap from array");
        this.bmp = createProcessedBitmap(inputColorSegmentationDataPicture, height, width);
    }

    /**
     * Returns Bitmap
     * @return Bitmap
     */
    public Bitmap getBitmap(){
        return this.bmp;
    }

    /**
     * Method create a Bitmap from int array of ARGB pixels,
     * set Bitmap configuration to ARGB_8888, rotate Bitmap to portrait mode and
     * flip it by its y axis (when we convert right hand, thumb is in right side
     * of picture)
     * @param inputColorSegmentationDataPicture int array of ARGB pixels before segmentation
     * @param height Size parameters of camera in device
     * @param width Size parameters of camera in device
     * @return Bitmap
     */
    public static Bitmap createProcessedBitmap(int[] inputColorSegmentationDataPicture, int height, int width){
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(width, height, conf);
        bmp.setPixels(inputColorSegmentationDataPicture, 0, width, 0, 0, width, height);
        bmp = RotateBitmap(bmp, -90);
        bmp = flipBitmap(bmp);

        Bitmap bmpCropped = Bitmap.createBitmap(bmp, 0, 0, height, (int)(width*0.6));
        return bmpCropped;
    }

    /**
     * Method is rotating bitmap
     * @param source Bitmap to rotation
     * @param angle rotation angle
     * @return Bitmap
     */
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //System.out.println("Bitmap rotated");
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Method is flipping bitmap by its y axis
     * @param source Bitmap to flip
     * @return Bitmap
     */
    public static Bitmap flipBitmap(Bitmap source)
    {
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        //System.out.println("Bitmap flipped");
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
