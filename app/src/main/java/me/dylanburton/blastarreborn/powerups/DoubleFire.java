package me.dylanburton.blastarreborn.powerups;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 8/3/2017.
 */

public class DoubleFire extends Powerup {
    private PowerupType powerupType = PowerupType.DOUBLEFIRE;
    private float x;
    private float y;
    private float dy = 3;
    private Bitmap doubleFire;

    public DoubleFire(Bitmap doubleFire, float x, float y){
        this.x = x;
        this.y = y;
        this.doubleFire = doubleFire;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Bitmap getBitmap() {
        return doubleFire;
    }

    public void setBitmap(Bitmap doubleFire) {
        this.doubleFire = doubleFire;
    }

    public float getDy() {
        return dy;
    }

    public void setDy(float dy) {
        this.dy = dy;
    }

    public PowerupType getPowerupType() {
        return powerupType;
    }





}
