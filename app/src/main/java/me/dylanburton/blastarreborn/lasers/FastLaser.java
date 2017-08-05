package me.dylanburton.blastarreborn.lasers;

import android.graphics.Bitmap;

import me.dylanburton.blastarreborn.spaceships.ShipType;

/**
 * Created by Dylan on 7/19/2017.
 */

public class FastLaser extends ShipLaser {


    public FastLaser(ShipType shipType, Bitmap laserBitmap, float x, float y){
        setEnemyLaser(true);
        setShipType(shipType);
        this.setBmp(laserBitmap);

        this.setX(x);
        this.setY(y);

        this.setDx(0);
        this.setDy(10);
    }
}
