package com.app.handfeatures;

import android.graphics.Point;

/**
 * Created by bijat on 28.05.2016.
 */

class Vector2D {
    public Vector2D(FPoint start, FPoint end) {
        x = end.x - start.x;
        y = end.y - start.y;
    }

    public Vector2D(FPoint start, Point end) {
        x = end.x - start.x;
        y = end.y - start.y;
    }

    public Vector2D(Point start, Point end) {
        x = end.x - start.x;
        y = end.y - start.y;
    }

    public Vector2D(FPoint end) {
        x = end.x ;
        y = end.y;
    }

    public Vector2D(float x, float y) {
        this.x = x ;
        this.y = y;
    }

    public float length() {
        return (float)(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
    }

    public void normalize() {
        float length = length();
        x = x / length;
        y = y / length;
    }

    public Vector2D add(float x, float y) {
        this.x += x;
        this.y += y;

        return new Vector2D(this.x + x, this.y + y);
    }

    public Vector2D multiply(float scalar) {
        x *= scalar;
        y *= scalar;

        return new Vector2D(x * scalar, y * scalar);
    }

    public float x;
    public float y;

    public static float angleBetweenUnitVectors(Vector2D v1, Vector2D v2) {
        return (float)Math.acos(v1.x * v2.x + v1.y * v2.y);
    }
}

class FPoint {
    FPoint() {
        x = 0.0f;
        y = 0.0f;
    }

    FPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    FPoint(FPoint p) {
        x = p.x;
        y = p.y;
    }

    FPoint(Point p) {
        x = p.x;
        y = p.y;
    }

    Point asIntPoint() {
        return new Point((int)(x + 0.5f), (int)(y + 0.5f));
    }

    public void normalize() {
        float d = (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        x = x / d;
        y = y / d;
    }

    public FPoint add(Vector2D vec) {
        this.x += vec.x;
        this.y += vec.y;

        return new FPoint(this.x + vec.x, this.y + vec.y);
    }

    float x;
    float y;
}

final class LineEquationFactors {
    LineEquationFactors() {
        a = b = c = 0.0f;
    }

    LineEquationFactors(float a, float b, float c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    LineEquationFactors(LineEquationFactors other) {
        a = other.a;
        b = other.b;
        c = other.c;
    }

    LineEquationFactors(Point p1, Point p2) {
        calculateFactors(new FPoint(p1), new FPoint(p2));
    }

    LineEquationFactors(FPoint p1, FPoint p2) {
        calculateFactors(p1, p2);
    }

    void calculateFactors(FPoint p1, FPoint p2)
    {
        // (x- p1X) / (p2X - p1X) = (y - p1Y) / (p2Y - p1Y)
        a = p2.y - p1.y;
        b = p2.x - p1.x;
        c = p1.x * p2.y - p2.x * p1.y;
    }

    public String toString() {
        return "" + a + ", " + b + ", " + c;
    }

    public float a;
    public float b;
    public float c;
}

public class Utils {
    final public static float PI_2 = (float)Math.PI / 2.0f;
    final public static float PI = (float)Math.PI;

    public static float distance(Point p1, Point p2) {
        return (float)Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }

    public static float lineEquationFacotrA(Point start, Point end) {
        float dy = end.y - start.y;
        float dx = end.x - start.x;
        float a = dy / dx;

        return a;
    }

    public static float angle(FPoint p1, FPoint p2) {
        p1.normalize();
        p2.normalize();
        return (float)Math.acos(p1.x * p2.x + p1.y * p2.y);
    }

    public static float parrarelLineEquationFacotrA(Point start, Point end) {
        float dy = end.y - start.y;
        float dx = end.x - start.x;
        float a = (-dx) / dy;

        return a;
    }

    public static FPoint findLineEquation(float factor_a, Point X) {
        return new FPoint(factor_a, X.y - (factor_a * X.x));
    }

   /* public static float DistancePointToLine(Point p, float a, float b) {
        return (float)(Math.abs(a * p.x + b * p.y + 0) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2)));
    }*/

    public static float distance(FPoint p, float a, float b, float c)
    {
        return (float)(Math.abs(a * p.x + b * p.y + c) / Math.sqrt(a * a + b * b));
    }
    public static float distance(FPoint p, LineEquationFactors lef)
    {
        return (float)(Math.abs(lef.a * p.x + lef.b * p.y + lef.c) / Math.sqrt(lef.a * lef.a + lef.b * lef.b));
    }
    public static float distance(Point p, LineEquationFactors lef)
    {
        return (float)(Math.abs(lef.a * p.x + lef.b * p.y + lef.c) / Math.sqrt(lef.a * lef.a + lef.b * lef.b));
    }

    /**
     * Real in name means than the distance is not abs value
     * @param p - point
     * @param lef - line equalition factors
     * @return not abs() distance
     */
    public static float distanceReal(FPoint p, LineEquationFactors lef)
    {
        return (float)(Math.abs(lef.a * p.x + lef.b * p.y + lef.c) / Math.sqrt(lef.a * lef.a + lef.b * lef.b));
    }
    public static float distanceReal(Point p, LineEquationFactors lef)
    {
        return (float)(Math.abs(lef.a * p.x + lef.b * p.y + lef.c) / Math.sqrt(lef.a * lef.a + lef.b * lef.b));
    }
}
