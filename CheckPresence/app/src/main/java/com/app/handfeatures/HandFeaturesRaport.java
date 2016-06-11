package com.app.handfeatures;

/**
 * Created by bijat on 11.06.2016.
 */
public class HandFeaturesRaport {
    static class BinaryzationRaport {
        int width;
        int height;
        int elPixels;
        boolean usedOpenCV;
    }

    static class SegmentationRaport {
        int width;
        int height;
        int numAreas;
        int theBiggestAreaPixels;
        int allAreasPixels;
    }
    public BinaryzationRaport bin;
    public SegmentationRaport seg;
}