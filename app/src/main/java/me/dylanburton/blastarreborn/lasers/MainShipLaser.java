package me.dylanburton.blastarreborn.lasers;

/**
 * Created by Dylan on 7/16/2017.
 */

public class MainShipLaser extends ShipLaser {

    //simple yet effective
    public MainShipLaser(float x, float y){

        this.setX(x);
        this.setY(y);
        this.setDx(0);
        this.setDy(-20f);

    }

    public void updateMainShipLaserPositions(){
        setX(getX() + getDx());
        setY(getY() + getDy());
    }
}
