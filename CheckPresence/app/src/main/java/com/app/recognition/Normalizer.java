package com.app.recognition;

import java.util.List;

/**
 * Generic functional interface Normalizer. Implementation is used in HandRecogniser class.
 * To implement your own normalization method you can implement this interface,
 * or use lambda expression
 *
 * @author kbaran
 */

public interface Normalizer<T> {
    /**
     * @param vector List<T> list of values to normalize
     */
    public void process(List<T> vector);

}