package me.dylanburton.blastarreborn.lasers;

import android.graphics.Bitmap;

import me.dylanburton.blastarreborn.spaceships.Ship;
import me.dylanburton.blastarreborn.spaceships.ShipType;

/**
 * Created by Dylan on 7/16/2017.
 */

/*
 * Ship Laser will be used for all enemies
 */
public class ShipLaser {
    private boolean isEnemyLaser = true;
    private Ship ship;
    private float x = 0;
    private float y = 0;
    private float dx = 0;
    private float dy = 8;
    private int currentFrame = 0;
    private long lastImperialLaserFrameChange = 0;
    private long lastBattlecruiserLaserFrameChange = 0;

    private Bitmap bmp;

    public ShipLaser(){}

    public ShipLaser(Ship ship, Bitmap bmp, float x , float y){
        this.bmp = bmp;
        this.ship = ship;
        this.x = x;
        this.y = y;

    }
    public ShipLaser(Ship ship, Bitmap bmp, float x , float y, float speedAmplifier){
        this.bmp = bmp;
        this.ship = ship;
        this.x = x;
        this.y = y;
        this.dy = dy*speedAmplifier;

    }


    public boolean isEnemyLaser(){

        return isEnemyLaser;
    }

    public void setEnemyLaser(boolean isEnemyLaser){
        this.isEnemyLaser = isEnemyLaser;
    }

    public Bitmap getBmp() {
        return bmp;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public void setPosition(float x, float y){
        this.x = x;
        this.y = y;

    }

    public float getDx(){
        return dx;
    }

    public float getDy(){
        return dy;
    }

    public void setDx(float dx){
        this.dx = dx;
    }

    public void setDy(float dy){
        this.dy = dy;
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

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }


    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }
    public long getLastImperialLaserFrameChange() {
        return lastImperialLaserFrameChange;
    }

    public void setLastImperialLaserFrameChange(long lastImperialLaserFrameChange) {
        this.lastImperialLaserFrameChange = lastImperialLaserFrameChange;
    }

    public long getLastBattlecruiserLaserFrameChange() {
        return lastBattlecruiserLaserFrameChange;
    }

    public void setLastBattlecruiserLaserFrameChange(long lastBattlecruiserLaserFrameChange) {
        this.lastBattlecruiserLaserFrameChange = lastBattlecruiserLaserFrameChange;
    }





}
