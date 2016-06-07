package com.app.handfeatures;

/**
 * Created by bijat on 26.05.2016.
 */
public class Color {
    final static public byte BG_COLOR = 0;
    final static public byte EL_COLOR = -1;
    final static public byte BG_COLOR_AREA = 1;
    final static public byte EL_COLOR_AREA = 2;
    final static public byte GREY = 127;
    final static public byte GREEN = 126;
    final static public byte RED = 125;
    final static public byte BLUE = 124;
    final static public byte GREY_LIGHT = 123;
    final static public byte GREY_DARK = 122;
    final static public byte BLUE_LIGHT = 121;
    final static public byte BLUE_DARK = 120;
    final static public byte ORANGE = 119;
    final static public byte GREEN_LIGHT = 118;
    final static public byte MAGENTA = 117;

    final static public int toABGR(int r, int g, int b) {
        return 0xFF000000 | (b << 16) | (g << 8) | (r << 0);
    }

    final static public int colorToABGR(byte color) {
        switch (color) {
            case EL_COLOR:      return 0xFFFFFFFF;
            case BG_COLOR:      return 0x00000000;
            case RED:           return Color.toABGR(237, 28, 36);
            case GREEN:         return Color.toABGR(34, 177, 76);
            case GREEN_LIGHT:   return Color.toABGR(181, 230, 29);
            case BLUE:          return Color.toABGR(0, 162, 232);
            case BLUE_LIGHT:    return Color.toABGR(153, 217, 234);
            case BLUE_DARK:     return Color.toABGR(0, 111, 157);
            case GREY:          return Color.toABGR(170, 170, 170);
            case GREY_LIGHT:    return Color.toABGR(195, 195, 195);
            case GREY_DARK:     return Color.toABGR(100, 100, 100);
            case ORANGE:        return Color.toABGR(255, 127, 39);
            case MAGENTA:        return Color.toABGR(255, 0, 255);
            default:            //return 0xFF000000 | (color << 16) | (color << 8) | color;
                System.out.println("Nie ma takiego koloru!");
                return 0x00000000;
        }
    }

    Color(byte color) {
        this.color = color;
    }

    public byte getValue() { return this.color; }

    private byte color;
}
