package com.app.recognition;

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



}