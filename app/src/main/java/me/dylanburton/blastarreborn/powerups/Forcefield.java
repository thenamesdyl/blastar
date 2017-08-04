package me.dylanburton.blastarreborn.powerups;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 8/3/2017.
 */

public class Forcefield extends Powerup {
    private PowerupType powerupType = PowerupType.FORCEFIELD;
    private float x;
    private float y;
    private float dy = 3;
    private Bitmap forceField;

    public Forcefield(Bitmap forceField, float x, float y){
        this.x = x;
        this.y = y;
        this.forceField = forceField;
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
        return forceField;
    }

    public void setBitmap(Bitmap doubleFire) {
        this.forceField = doubleFire;
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
