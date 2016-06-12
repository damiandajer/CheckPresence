package com.app.checkpresence.backgroundmenage;

/**
 * Created by bijat on 11.06.2016.
 */
public class HandFeaturesRaport {
    public static class BinaryzationRaport {
        public int width;
        public int height;
        public int elPixels;
        public boolean usedOpenCV;
    }

    public static class SegmentationRaport {
        public int width;
        public int height;
        public int numAreas;
        public int theBiggestAreaPixels;
        public int allAreasPixels;
        public float theBiggestAreaCoverage;
    }

    public static class CalculationRaport {
        public boolean isGood;
    }

    public BinaryzationRaport bin;
    public SegmentationRaport seg;
    public CalculationRaport  cal;
}