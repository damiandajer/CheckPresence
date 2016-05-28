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
     */
    @Override
    public void process(List<FloatWrapper> vector) {
        double ratio = 0;
        for(FloatWrapper el : vector)
            ratio += el.getValue() * el.getValue();
        ratio = Math.sqrt(ratio);
        for(FloatWrapper el : vector)
            el.setValue((float) (el.getValue()/ratio));
    }

}