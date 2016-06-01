package com.app.recognition;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable wrapper class for storing float values. While we use generic interface
 * normalizer, it's necessary to provide a class in implementing class
 * (primitives like float are not allowed)
 * @author kbaran
 */
public class FloatWrapper {

    private float value;
    /**
     * @param value float
     */
    public FloatWrapper(float value) {
        this.value = value;
    }
    /**
     * @return float value
     */
    public float getValue() {
        return value;
    }
    /**
     * @param value float
     */
    public void setValue(float value) {
        this.value = value;
    }

    /**
     * Creates List<FloatWrapper> with elements storing values from data
     * @param data float[]
     * @return list List<FloatWrapper>
     */
    public static List<FloatWrapper> floatToFloatWrapperArray(float[] data)
    {
        List<FloatWrapper> list = new ArrayList<>();
        for(float d : data)
        {
            list.add(new FloatWrapper(d));
        }
        return list;
    }
    /**
     * Creates float[] with elements storing values from data
     * @param data
     * @return
     */
    public static float[] floatWrapperToFloatArray(List<FloatWrapper> data)
    {
        float[] result = new float[data.size()];
        for(int i = 0; i < data.size(); i++)
            result[i] = data.get(i).getValue();
        return result;
    }
}