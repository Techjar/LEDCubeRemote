package com.techjar.ledcr.util;

/**
 *
 */
public class Color {
    private byte red, green, blue, alpha;

    public Color() {
        this(0, 0, 0, 255);
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a) {
        set(r, g, b, a);
    }

    public Color(Color c) {
        set(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    public void set(int r, int g, int b, int a) {
        red = (byte)r;
        green = (byte)g;
        blue = (byte)b;
        alpha = (byte)a;
    }

    public void set(byte r, byte g, byte b, byte a) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
    }

    public void set(int r, int g, int b) {
        set(r, g, b, 255);
    }

    public void set(byte r, byte g, byte b) {
        set(r, g, b, (byte) 255);
    }

    public int getRed() {
        return red & 0xFF;
    }

    public int getGreen() {
        return green & 0xFF;
    }

    public int getBlue() {
        return blue & 0xFF;
    }

    public int getAlpha() {
        return alpha & 0xFF;
    }

    public byte getRedByte() {
        return red;
    }

    public byte getGreenByte() {
        return green;
    }

    public byte getBlueByte() {
        return blue;
    }

    public byte getAlphaByte() {
        return alpha;
    }

    public void setRed(int red) {
        this.red = (byte)red;
    }

    public void setGreen(int green) {
        this.green = (byte)green;
    }

    public void setBlue(int blue) {
        this.blue = (byte)blue;
    }

    public void setAlpha(int alpha) {
        this.alpha = (byte)alpha;
    }

    public void setRed(byte red) {
        this.red = red;
    }

    public void setGreen(byte green) {
        this.green = green;
    }

    public void setBlue(byte blue) {
        this.blue = blue;
    }

    public void setAlpha(byte alpha) {
        this.alpha = alpha;
    }

    public String toString() {
        return "Color [" + getRed() + ", " + getGreen() + ", " + getBlue() + ", " + getAlpha() + "]";
    }

    public boolean equals(Object o) {
        return o != null && o instanceof Color && ((Color)o).getRed() == this.getRed() && ((Color)o).getGreen() == this.getGreen() && ((Color)o).getBlue() == this.getBlue() && ((Color)o).getAlpha() == this.getAlpha();
    }

    public int hashCode() {
        return (red << 24) | (green << 16) | (blue << 8) | alpha;
    }
}
