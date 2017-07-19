package me.dylanburton.blastarreborn.utils;

/**
 * Created by Dylan on 7/19/2017.
 */

public class HeatSinker {
    private int currentPlayershipLocationX;
    private int currentPlayershipLocationY;
    private int x;
    private int y;
    private int dx;
    private int dy;


    public void HeatSink() {
    }

    //surprisingly simple. Thanks basic geometry
    public void updateHeatsink(int x, int y, int cpx, int cpy) {
        cpy = cpy - y;
        cpx = cpx - x;

        dy = cpy;
        dx = cpx;

    }


    public int getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public int getDy() {
        return dy;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }


}
