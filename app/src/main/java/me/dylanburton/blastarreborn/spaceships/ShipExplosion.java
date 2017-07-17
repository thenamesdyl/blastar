package me.dylanburton.blastarreborn.spaceships;

/**
 * Created by Dylan on 7/16/2017.
 */

//this is for creating multiple ship explosion animations
public class ShipExplosion{
    float x=0;
    float y=0;
    int currentFrame = 0;
    int explosionNumber = 0;

    public ShipExplosion(float x, float y, int explosionNumber){
        this.x = x;
        this.y = y;
        this.explosionNumber = explosionNumber;
    }

    public int getCurrentFrame(){
        return currentFrame;
    }

    public void nextFrame(){
        currentFrame++;
    }

    public int getExplosionNumber(){
        return explosionNumber;
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
