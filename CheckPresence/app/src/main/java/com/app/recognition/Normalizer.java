package com.app.recognition;

import java.util.List;

/**
 *
 * @author kbaran
 */
public class Normalizer{

    /**
     * Max value for normalization
     */
    private final int upperBound = 1;
    /**
     * Normalize passed array using Min-Max normalization
     * @param data float[]
     * @return data float[] normalized array
     */
    public float[] process(float[] data) {

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for(int i = 0; i < data.length; i++)
        {
            if(data[i] < min) min = data[i];
            if(data[i] > max) max = data[i];
        }
        float a = upperBound / Math.abs(min - max);
        float b = upperBound - a * max;
        for(int i = 0; i < data.length; i++){
            data[i] = data[i] * a + b;
        }
        return data;
    }
    /**
     * Normalize passed list using Min-Max normalization
     * @param data float[]
     */
    public void process(List<FloatWrapper> data) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for(FloatWrapper el : data)
        {
            if(el.getValue() < min) min = el.getValue();
            if(el.getValue() > max) max = el.getValue();
        }
        float a = upperBound / Math.abs(min - max);
        float b = upperBound - a * max;
        for(FloatWrapper el : data)
            el.setValue((float) (el.getValue() * a + b));
    }
}