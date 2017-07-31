package me.dylanburton.blastarreborn.powerups;

/**
 * Created by Dylan on 7/31/2017.
 */

public class HealthPack {
    float x;
    float y;
    float dy = 3;
    int amountOfLives;

    public HealthPack(float x, float y, int amountOfLives){
        this.x = x;
        this.y = y;
        this.amountOfLives = amountOfLives;
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


}
