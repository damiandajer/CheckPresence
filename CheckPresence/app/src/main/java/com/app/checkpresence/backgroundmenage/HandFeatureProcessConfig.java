package com.app.checkpresence.backgroundmenage;

/**
 * Created by bijat on 12.06.2016.
 */

/**
 * Describes requirements to continue hand features process.
 * Make helper for HandFeatureRaportManager.
 */
final public class HandFeatureProcessConfig {
    final public static float MIN_BINARYZATION = 0.03f;
    final public static float MAX_BINARYZATION = 0.70f;

    final public static float MAX_AREA_CEVERAGE_FOR_NEW_BACKGROUND_SEGMENTATION = 0.20f;

    final public static float MIN_AREA_TO_CEVERAGE_SEGMENTATION = 0.30f;
    final public static float MAX_AREA_TO_CEVERAGE_SEGMENTATION = 0.70f;

    final public static float MAX_ALLOWED_AREAS_SEGMENTATION = 50;
}
