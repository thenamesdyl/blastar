package me.dylanburton.blastarreborn.powerups;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 7/31/2017.
 */

public class HealthPack extends Powerup {
    private PowerupType powerupType = PowerupType.HEALTHPACK;
    private float x;
    private float y;
    private float dy = 3;
    private int amountOfLives;
    private Bitmap healthPack;

    public HealthPack(Bitmap healthPack, float x, float y, int amountOfLives){
        this.x = x;
        this.y = y;
        this.amountOfLives = amountOfLives;
        this.healthPack = healthPack;
    }

    public int getAmountOfLives() {
        return amountOfLives;
    }

    public void setAmountOfLives(int amountOfLives) {
        this.amountOfLives = amountOfLives;
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
        return healthPack;
    }

    public void setBitmap(Bitmap healthPack) {
        this.healthPack = healthPack;
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
