package me.dylanburton.blastarreborn.lasers;

import android.graphics.Bitmap;

import me.dylanburton.blastarreborn.enemies.ShipType;

/**
 * Created by Dylan on 7/16/2017.
 */

public class MainShipLaser extends ShipLaser {

    private static final boolean isMainShipLaser = true;

    //simple yet effective
    public MainShipLaser(Bitmap laserBitmap, float x, float y){

        setShipType(ShipType.PLAYER);
        setEnemyLaser(false);
        setBmp(laserBitmap);

        this.setX(x);
        this.setY(y);
        this.setDx(0);
        this.setDy(-20f);

    }

    public static boolean isMainShipLaser() {
        return isMainShipLaser;
    }

}
