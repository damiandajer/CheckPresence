package com.app.handfeatures;

import java.util.Locale;

/**
 * Created by bijat on 29.05.2016.
 */

enum FeatureNames {
    ThumbBaseLength,
    IndexBaseLength,
    MiddleBaseLength,
    RingBaseLength,
    PinkyBaseLength,

    ThumbLength,
    IndexLength,
    MiddleLength,
    RingLength,
    PinkyLength,

    IndexDistToThumb,
    MiddleDistToThumb,
    RingDistToThumb,
    PinkyDistToThumb,
    LowerHandWidth,
    UpperHandWidth,

    ThumbUpperCircleRadious,
    IndexUpperCircleRadious,
    MiddleUpperCircleRadious,
    RingUpperCircleRadious,
    PinkyUpperCircleRadious,

    IndexLowerCircleRadious,
    MiddleLowerCircleRadious,
    RingLowerCircleRadious,
    PinkyLowerCircleRadious,

    ThumbArea,
    IndexArea,
    MiddleArea,
    RingArea,
    PinkyArea;
}


public class HandFeaturesData {
    public HandFeaturesData() {
        size = FeatureNames.values().length;
        features = new float[size];
    }

    public HandFeaturesData(HandFeatures handFeatures){
        size = FeatureNames.values().length;
        features = new float[size];

        Finger[] tempFingers = handFeatures.getFingers();

        features[FeatureNames.ThumbBaseLength.ordinal()]  = tempFingers[Finger.THUMB].base_length;
        features[FeatureNames.IndexBaseLength.ordinal()]  = tempFingers[Finger.INDEX_FINGER].base_length;
        features[FeatureNames.MiddleBaseLength.ordinal()] = tempFingers[Finger.MIDDLE_FINGER].base_length;
        features[FeatureNames.RingBaseLength.ordinal()]   = tempFingers[Finger.RING_FINGER].base_length;
        features[FeatureNames.PinkyBaseLength.ordinal()]  = tempFingers[Finger.PINKY].base_length;

        features[FeatureNames.ThumbLength.ordinal()]  = tempFingers[Finger.THUMB].length;
        features[FeatureNames.IndexLength.ordinal()]  = tempFingers[Finger.INDEX_FINGER].length;
        features[FeatureNames.MiddleLength.ordinal()] = tempFingers[Finger.MIDDLE_FINGER].length;
        features[FeatureNames.RingLength.ordinal()]   = tempFingers[Finger.RING_FINGER].length;
        features[FeatureNames.PinkyLength.ordinal()]  = tempFingers[Finger.PINKY].length;

        features[FeatureNames.IndexDistToThumb.ordinal()]  = tempFingers[Finger.INDEX_FINGER].distance_from_thumb;
        features[FeatureNames.MiddleDistToThumb.ordinal()] = tempFingers[Finger.MIDDLE_FINGER].distance_from_thumb;
        features[FeatureNames.RingDistToThumb.ordinal()]   = tempFingers[Finger.RING_FINGER].distance_from_thumb;
        features[FeatureNames.PinkyDistToThumb.ordinal()]  = tempFingers[Finger.PINKY].distance_from_thumb;
        features[FeatureNames.LowerHandWidth.ordinal()]    = 0.0f; //handFeatures.getLowerHandWidth();
        features[FeatureNames.UpperHandWidth.ordinal()]    = handFeatures.getUpperHandWidth();

        features[FeatureNames.ThumbUpperCircleRadious.ordinal()]  = tempFingers[Finger.THUMB].circle_top_radius;
        features[FeatureNames.IndexUpperCircleRadious.ordinal()]  = tempFingers[Finger.INDEX_FINGER].circle_top_radius;
        features[FeatureNames.MiddleUpperCircleRadious.ordinal()] = tempFingers[Finger.MIDDLE_FINGER].circle_top_radius;
        features[FeatureNames.RingUpperCircleRadious.ordinal()]   = tempFingers[Finger.RING_FINGER].circle_top_radius;
        features[FeatureNames.PinkyUpperCircleRadious.ordinal()]  = tempFingers[Finger.PINKY].circle_top_radius;

        features[FeatureNames.IndexLowerCircleRadious.ordinal()]  = tempFingers[Finger.INDEX_FINGER].circle_bottom_radius;
        features[FeatureNames.MiddleLowerCircleRadious.ordinal()] = tempFingers[Finger.MIDDLE_FINGER].circle_bottom_radius;
        features[FeatureNames.RingLowerCircleRadious.ordinal()]   = tempFingers[Finger.RING_FINGER].circle_bottom_radius;
        features[FeatureNames.PinkyLowerCircleRadious.ordinal()]  = tempFingers[Finger.PINKY].circle_bottom_radius;

        features[FeatureNames.ThumbArea.ordinal()]  = tempFingers[Finger.THUMB].area;
        features[FeatureNames.IndexArea.ordinal()]  = tempFingers[Finger.INDEX_FINGER].area;
        features[FeatureNames.MiddleArea.ordinal()] = tempFingers[Finger.MIDDLE_FINGER].area;
        features[FeatureNames.RingArea.ordinal()]   = tempFingers[Finger.RING_FINGER].area;
        features[FeatureNames.PinkyArea.ordinal()]  = tempFingers[Finger.PINKY].area;
    }

    public void show(boolean oneLine) {
        if (oneLine) {
            for (int i = 0; i < size - 1; ++i)
                System.out.print(String.format(Locale.getDefault(), "%.2f, ", features[i]));
            System.out.print(String.format(Locale.getDefault(), "%.2f, ", features[size - 1]));
            System.out.println("");
        }
        else {
            System.out.println(String.format(Locale.getDefault(), "Base: %.2f, %.2f, %.2f, %.2f, %.2f ",
                    features[FeatureNames.ThumbBaseLength.ordinal()], features[FeatureNames.IndexBaseLength.ordinal()],
                    features[FeatureNames.MiddleBaseLength.ordinal()], features[FeatureNames.RingBaseLength.ordinal()],
                    features[FeatureNames.PinkyBaseLength.ordinal()]));

            System.out.println(String.format(Locale.getDefault(), "Length: %.2f, %.2f, %.2f, %.2f, %.2f ",
                    features[FeatureNames.ThumbLength.ordinal()], features[FeatureNames.IndexLength.ordinal()],
                    features[FeatureNames.MiddleLength.ordinal()], features[FeatureNames.RingLength.ordinal()],
                    features[FeatureNames.PinkyLength.ordinal()]));

            System.out.println(String.format(Locale.getDefault(), "Length to Thumb: %.2f, %.2f, %.2f, %.2f ",
                    features[FeatureNames.IndexDistToThumb.ordinal()], features[FeatureNames.MiddleDistToThumb.ordinal()],
                    features[FeatureNames.RingDistToThumb.ordinal()], features[FeatureNames.PinkyDistToThumb.ordinal()]));

            System.out.println(String.format(Locale.getDefault(), "Hand width lower, upper: %.2f, %.2f ",
                    features[FeatureNames.LowerHandWidth.ordinal()], features[FeatureNames.UpperHandWidth.ordinal()]));

            System.out.println(String.format(Locale.getDefault(), "Upper circle radius: %.2f, %.2f, %.2f, %.2f, %.2f ",
                    features[FeatureNames.ThumbUpperCircleRadious.ordinal()], features[FeatureNames.IndexUpperCircleRadious.ordinal()],
                    features[FeatureNames.MiddleUpperCircleRadious.ordinal()], features[FeatureNames.RingUpperCircleRadious.ordinal()],
                    features[FeatureNames.PinkyUpperCircleRadious.ordinal()]));

            System.out.println(String.format(Locale.getDefault(), "Lower circle radius: %.2f, %.2f, %.2f, %.2f",
                    features[FeatureNames.IndexLowerCircleRadious.ordinal()], features[FeatureNames.MiddleLowerCircleRadious.ordinal()],
                    features[FeatureNames.RingLowerCircleRadious.ordinal()], features[FeatureNames.PinkyLowerCircleRadious.ordinal()]));

            System.out.println(String.format(Locale.getDefault(), "Area: %.2f, %.2f, %.2f, %.2f, %.2f ",
                    features[FeatureNames.ThumbArea.ordinal()], features[FeatureNames.IndexArea.ordinal()],
                    features[FeatureNames.MiddleArea.ordinal()], features[FeatureNames.RingArea.ordinal()],
                    features[FeatureNames.PinkyArea.ordinal()]));
            System.out.println("Wypisano wszystkie cechy.");
        }
    }

    public float features[];
    public int size;
}
