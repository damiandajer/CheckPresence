package com.app.recognition;

import java.util.List;

/**
 * Our normalizer. It uses normalization method provided by Mr.T.Gaciarz
 * @author kbaran
 */
public class StandardNormalizer implements Normalizer<FloatWrapper>
{
    /**
     * Implementation of process method from Normalizer interface
     * @param vector List<FloatWrapper>
     *//*
    @Override
    public void process(List<FloatWrapper> vector) {
        double ratio = 0;
        for(FloatWrapper el : vector)
            ratio += el.getValue() * el.getValue();
        ratio = Math.sqrt(ratio);
        for(FloatWrapper el : vector)
            el.setValue((float) (el.getValue()/ratio));
    }
    */
    public void process(List<FloatWrapper> vector) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for(FloatWrapper el : vector)
        {
            if(el.getValue() < min) min = el.getValue();
            if(el.getValue() > max) max = el.getValue();
        }
        float a = 10000 / Math.abs(min - max);
        float b = 10000 - a * max;
        for(FloatWrapper el : vector)
            el.setValue((float) (el.getValue() * a + b));
    }

}