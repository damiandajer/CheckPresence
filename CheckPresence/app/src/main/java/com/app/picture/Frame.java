package com.app.picture;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;

import com.app.checkpresence.CameraView;
import com.app.handFeaturesThreads.HandFeaturesThreads;
import com.app.handfeatures.HandFeatures;
import com.app.handfeatures.HandFeaturesData;
import com.app.segmentation.OpenCVSubtractionThreads;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damian on 20.05.2016.
 */
public class Frame {
    int[] argb;
    Camera.Size size = CameraView.size;
    Bitmap bmpBackground, actualFrame;
    private List<Bitmap> openCVBitmaps;
    private List<Bitmap> cppBitmaps;
    private List<int[]> openCVIntArrays;
    private List<float[]> handFeatures;
    private int numberOfConditions;
    private int min, max, numberOfThresholds;
    private int segmentatedHeight, segmentatedWidth;
    private List<HandFeatures> handFeaturesObjects;

    public Frame(){}

    /**
     * Method is converting byte array to int array with argb pixels
     * @param argb int array with argb pixels
     * @param yuv byte array
     * @param width width of picture
     * @param height height of picture
     */
    private void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width, int height) {
        final int frameSize = width * height;

        final int ii = 0;
        final int ij = 0;
        final int di = +1;
        final int dj = +1;

        int a = 0;
        for (int i = 0, ci = ii; i < height; ++i, ci += di) {
            for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                int y = (0xff & ((int) yuv[ci * width + cj]));
                int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
                int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    /**
     * Set array of pixels in ARGB configuration
     * @param data byte array from PreviewFrame
     */
    private void createIntArrayFromPreviewFrame(byte[] data){
        this.argb = new int[size.height * size.width];
        YUV_NV21_TO_RGB(argb, data, size.width, size.height);
    }

    /**
     * Set Bitmap created from int Array of pixels
     */
    private void getBitmapFromIntArray(){
        BitmapFromPixelsThreads bitmapFromPixelsThreads = BitmapFromPixelsThreads.getNewObject();
        bitmapFromPixelsThreads.addNewThread(this.argb, size.height, size.width);
        bitmapFromPixelsThreads.executeThreads();

        this.actualFrame = bitmapFromPixelsThreads.getBitmaps().get(0);
    }

    /**
     * Returns List of Bitmap created from List of int Arrays of pixels
     * @param argb int Array of pixels
     * @return List of Bitmaps
     */
    private List<Bitmap> getBitmapsFromIntArray(List<int[]> argb){
        BitmapFromPixelsThreads bitmapFromPixelsThreads = BitmapFromPixelsThreads.getNewObject();
        bitmapFromPixelsThreads.addNewThread(argb, size.height, size.width);
        bitmapFromPixelsThreads.executeThreads();

        return bitmapFromPixelsThreads.getBitmaps();
    }

    private void setSizeOfSegmentatedBitmaps(int height, int width){
        this.segmentatedHeight = height;
        this.segmentatedWidth = width;
    }

    private Rect createRectWithSizeOfSegmentatedBitmap(){
        //(int)(height*0.15), 0, (int)(height*0.7), (int)(width*0.6)
        int left = (int)(actualFrame.getWidth()*0.1);
        int top = 0;
        int right = left + (int)(actualFrame.getWidth()*0.8);
        int bottom = top + actualFrame.getHeight();
        Rect rect = new Rect(left, top, right, bottom);
        return rect;
    }

    public void setBackground(Bitmap bmpBackground){
        this.bmpBackground = bmpBackground;
    }

    public void setActualFrame(byte[] data){
        createIntArrayFromPreviewFrame(data);
        getBitmapFromIntArray();
    }

    public Bitmap getActualBitmap(){
        return this.actualFrame;
    }

    public int[] getActualIntArray(){
        return this.argb;
    }

    public void findHandFeatures(){
        handFeatures = new ArrayList<>();

        HandFeaturesThreads handFeaturesThreads = HandFeaturesThreads.getNewObject();
        handFeaturesThreads.addNewThread(handFeaturesObjects);
        handFeaturesThreads.executeThreads();

        handFeatures = handFeaturesThreads.getListOfArraysWithHandFeatures();
    }

    public void segmentateFrameWithOpenCV(){
        openCVBitmaps = new ArrayList<>();
        openCVIntArrays = new ArrayList<>();
        Rect sizeOfBitmapToSegmentation = createRectWithSizeOfSegmentatedBitmap();

        OpenCVSubtractionThreads openCVSubtractionThreads = OpenCVSubtractionThreads.getNewObject();
        openCVSubtractionThreads.createListOfThresholds(this.min, this.max, this.numberOfThresholds);
        openCVSubtractionThreads.addNewThread(actualFrame, bmpBackground, sizeOfBitmapToSegmentation);
        openCVSubtractionThreads.executeThreads();

        openCVIntArrays = openCVSubtractionThreads.getListOfIntArrays();
        openCVBitmaps = openCVSubtractionThreads.getBitmaps();
        handFeaturesObjects = openCVSubtractionThreads.getHandFeaturesObjects(); // may be null
        setSizeOfSegmentatedBitmaps(openCVBitmaps.get(0).getHeight(), openCVBitmaps.get(0).getWidth());
    }

    public List<Bitmap> getOpenCVBitmaps() {
        return openCVBitmaps;
    }

    public void setThresholds(int min, int max, int numberOfThresholds){
        this.min = min;
        this.max = max;
        this.numberOfThresholds = numberOfThresholds;
    }

    public List<float[]> getHandFeatures() {
        return handFeatures;
    }

}
