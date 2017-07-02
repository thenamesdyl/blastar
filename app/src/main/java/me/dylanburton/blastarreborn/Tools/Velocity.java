package me.dylanburton.blastarreborn.Tools;

/**
 * Created by Dylan on 7/2/2017.
 */

public class Velocity {
    private int dX;
    private int dY;

    public Velocity(int dX, int dY){
        this.dX = dX;
        this.dY = dY;
    }

    public int getXVelocity(){
        return dX;
    }
    public int getYVelocity(){
        return dY;
    }
}
