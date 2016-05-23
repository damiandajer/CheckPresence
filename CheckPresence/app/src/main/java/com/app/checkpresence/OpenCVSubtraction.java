package com.app.checkpresence;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.IntBuffer;
import java.util.List;

import static org.opencv.core.Core.subtract;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * Created by Damian on 17.05.2016.
 */
public class OpenCVSubtraction implements Runnable {
    private volatile Bitmap bmp;
    Bitmap inputBitmap, backgroundBitmap;
    int height, width, threshold;
    Mat imgToProcess1, imgToProcess2, imgToProcess, mask;
    int[] intARGBArray;
    List<Integer> thresholds;
    private native int[] deleteSmallAreas(int[] intARGBArray, int height, int width);
    private native float[] findHandFeatures(int[] intARGBArray, int rows, int cols);


    /**
     *
     * @param inputBitmap Bitmap to process
     * @param backgroundBitmap Bitmap with background
     */
    public OpenCVSubtraction(Bitmap inputBitmap, Bitmap backgroundBitmap, int threshold) {
        this.inputBitmap = inputBitmap;
        this.backgroundBitmap = backgroundBitmap;

        this.height = inputBitmap.getHeight();
        this.width = inputBitmap.getWidth();
        this.threshold = threshold;

        this.imgToProcess = new Mat(height, width, CvType.CV_8UC4);
        this.imgToProcess1 = new Mat(height, width, CvType.CV_8UC4);
        this.imgToProcess2 = new Mat(height, width, CvType.CV_8UC4);
        this.mask = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void run() {
        //System.out.println("Thread is processing frame with OpenCV");
        setConfToBitmap();
        createMatsFromBitmap();
        processMats();
        createBitmapFromMat();
        convertBitmapToIntArray();
        /*clearIntArrayFromSmallAreas();
        convertIntArrayToBitmap();*/
        this.intARGBArray = deleteSmallAreas(this.intARGBArray, this.height, this.width);
        bmp.copyPixelsFromBuffer(IntBuffer.wrap(intARGBArray));
        float[] handFeatures = findHandFeatures(this.intARGBArray.clone(), this.height, this.width);
        if (handFeatures == null) {
            //System.out.println("Problem z etapem 2. Nie odnaleziono wszystkich cech!");
        }
        else {
            System.out.println("Odnaleziono wszystkie cechy!. Czekamy na etap 3!!!!!");
        }
    }

    private void setConfToBitmap(){
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        bmp = Bitmap.createBitmap(width, height, conf);
    }

    private void createMatsFromBitmap(){
        try {
            Utils.bitmapToMat(inputBitmap, imgToProcess1);
            Utils.bitmapToMat(backgroundBitmap, imgToProcess2);
        } catch(Exception e){
            Log.d("Warning", "Bitmap to Mat err: " + e.getMessage());
        }
    }

    private void processMats(){
        //absdiff(imgToProcess1, imgToProcess2, imgToProcess);
        subtract(imgToProcess2, imgToProcess1, imgToProcess);

        cvtColor(imgToProcess, mask, Imgproc.COLOR_RGBA2GRAY, 1); //your conversion specifier may vary
        Imgproc.threshold(mask, mask, threshold, 255, Imgproc.THRESH_BINARY);
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

    private void clearIntArrayFromSmallAreas(){
        this.intARGBArray = deleteSmallAreas(this.intARGBArray, this.height, this.width);
    }

    private void convertIntArrayToBitmap(){
        bmp.copyPixelsFromBuffer(IntBuffer.wrap(intARGBArray));
    }
}
