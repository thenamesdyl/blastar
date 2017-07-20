package me.dylanburton.blastarreborn.lasers;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 7/19/2017.
 */

public class FastLaser extends ShipLaser {


    public FastLaser(Bitmap laserBitmap, float x, float y){
        setEnemyLaser(true);
        this.setBmp(laserBitmap);

        this.setX(x);
        this.setY(y);

        this.setDx(0);
        this.setDy(10);
    }
}
