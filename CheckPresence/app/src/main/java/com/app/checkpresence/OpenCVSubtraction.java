package com.app.checkpresence;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.IntBuffer;

import static org.opencv.core.Core.subtract;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * Created by Damian on 17.05.2016.
 */
public class OpenCVSubtraction implements Runnable {
    private volatile Bitmap bmp;
    Bitmap inputBitmap, backgroundBitmap;
    int height, width;
    Mat imgToProcess1, imgToProcess2, imgToProcess, mask;
    int[] intARGBArray;
    int[] intARGBArrayAfter;

    private native int[] deleteSmallAreas(int[] argb, int rows, int cols);

    /**
     *
     * @param inputBitmap Bitmap to process
     * @param backgroundBitmap Bitmap with background
     */
    public OpenCVSubtraction(Bitmap inputBitmap, Bitmap backgroundBitmap) {
        this.inputBitmap = inputBitmap;
        this.backgroundBitmap = backgroundBitmap;

        this.height = inputBitmap.getHeight();
        this.width = inputBitmap.getWidth();

        this.imgToProcess = new Mat(height, width, CvType.CV_8UC4);
        this.imgToProcess1 = new Mat(height, width, CvType.CV_8UC4);
        this.imgToProcess2 = new Mat(height, width, CvType.CV_8UC4);
        this.mask = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void run() {
        System.out.println("Thread is processing frame with OpenCV");
        setConfToBitmap();
        createMatsFromBitmap();
        processMats();
        createBitmapFromMat();
        convertBitmapToIntArray();
        //double test = 0;
        /*intARGBArrayAfter = intARGBArray.clone();
        for (int i = 0; i < height/2; ++i)
            for (int j = 0; j < width/2; ++j) {
                test += intARGBArray[i * width/2 + j];
                intARGBArrayAfter[i * width / 2 + j] = 0;
            }
        System.out.println("wartosc przed: " + test);*/
        intARGBArray = deleteSmallAreas(this.intARGBArray, this.height, this.width);
        //intARGBArray = intARGBArrayAfter.clone();
        /*test = 0;
        for (int i = 0; i < height/2; ++i)
            for (int j = 0; j < width/2; ++j)
                test += intARGBArray[i * width/2 + j];
        System.out.println("wartosc po: " + test);*/
        /*for (int i = 0; i < height; ++i)
            for (int j = 0; j < width; ++j) {
                intARGBArray[i * width + j] = 0xffffaaaa;
            }*/
        bmp.copyPixelsFromBuffer(IntBuffer.wrap(intARGBArray));
    }

    private void setConfToBitmap(){
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        bmp = Bitmap.createBitmap(width, height, conf);
    }

    private void createMatsFromBitmap(){
        Utils.bitmapToMat(inputBitmap, imgToProcess1);
        Utils.bitmapToMat(backgroundBitmap, imgToProcess2);
    }

    private void processMats(){
        //absdiff(imgToProcess1, imgToProcess2, imgToProcess);
        subtract(imgToProcess2, imgToProcess1, imgToProcess);

        cvtColor(imgToProcess, mask, Imgproc.COLOR_RGBA2GRAY, 1); //your conversion specifier may vary
        Imgproc.threshold(mask, mask, 50, 255, Imgproc.THRESH_BINARY);
    }

    private void createBitmapFromMat(){
        Utils.matToBitmap(mask, bmp);
    }

    /**
     * Returns Bitmap
     * @return Bitmap
     */
    public Bitmap getBitmap(){
        return this.bmp;
    }

    private void convertBitmapToIntArray(){
        intARGBArray = new int[width * height];
        bmp.getPixels(intARGBArray, 0, width, 0, 0, width, height);
    }

    /**
     * Returns int ARGB Array with pixels
     * @return
     */
    public int[] getARGBIntArray(){
        return intARGBArray;
    }
}
