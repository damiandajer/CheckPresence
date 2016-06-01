package com.app.handfeatures;

import android.graphics.Rect;
import android.graphics.Point;

import java.util.ArrayList;

/**
 * Created by bijat on 26.05.2016.
 */
public class ImageArea {
    ImageArea() {
        points = new ArrayList<>();
        rect = new Rect(0, 0, 0, 0);

        numOfFoundAreas = 0;
        numOfFoundAllElementPixels = 0;
    }

    ArrayList<Point> points;
    Rect rect;

    // additional information about process finding area
    int numOfFoundAreas;
    int numOfFoundAllElementPixels;
}