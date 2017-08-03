package me.dylanburton.blastarreborn.powerups;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 8/3/2017.
 */

public class SlowTime extends Powerup {

    private PowerupType powerupType = PowerupType.SLOWTIME;
    private float x;
    private float y;
    private float dy = 3;
    private Bitmap slowTime;

    public SlowTime(Bitmap slowTime, float x, float y){
        this.x = x;
        this.y = y;
        this.slowTime = slowTime;
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
        return slowTime;
    }

    public void setBitmap(Bitmap slowTime) {
        this.slowTime = slowTime;
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
