package me.dylanburton.blastarreborn.Tools;

/**
 * Created by Dylan on 7/2/2017.
 */

public class Coordinate {

    private int dX;
    private int dY;

    public Coordinate(int dX, int dY){
        this.dX = dX;
        this.dY = dY;
    }

    public int getXCoordinate(){
        return dX;
    }
    public int getYCoordinate(){
        return dY;
    }
}

