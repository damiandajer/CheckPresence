package com.app.handfeatures;

/**
 * Created by bijat on 11.06.2016.
 */
public class HandFeaturesRaport {
    public static class BinaryzationRaport {
        public int width;
        public int height;
        public int elPixels;
        public boolean usedOpenCV;

        /*public int width() {return width;}
        public int height() { return height;}
        public boolean isUsedOpenCV() {return usedOpenCV;}
        public int elPixels() {return elPixels;}*/
    }

    public static class SegmentationRaport {
        public int width;
        public int height;
        public int numAreas;
        public int theBiggestAreaPixels;
        public int allAreasPixels;

        /*public int width() {return width;}
        public int height() { return height;}
        public int numAreas() { return numAreas;}
        public int theBiggestAreaPixels() { return theBiggestAreaPixels;}
        public int allAreasPixels() { return allAreasPixels;}*/
    }

    public static class CalculationRaport {
        public boolean isGood;
    }

    public BinaryzationRaport bin;
    public SegmentationRaport seg;
    public CalculationRaport  cal;
}