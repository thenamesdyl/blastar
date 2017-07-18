package me.dylanburton.blastarreborn.spaceships;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.LinkedList;
import java.util.List;

import me.dylanburton.blastarreborn.lasers.MainShipLaser;
import me.dylanburton.blastarreborn.lasers.ShipLaser;

/**
 * Created by Dylan on 7/16/2017.
 */

public class PlayerShip {

    //main spaceships location and bound
    private Bitmap mainSpaceShip[];
    private float spaceshipY=0;
    private float spaceshipX=0;
    private float width;
    private float height;
    private int currentSpaceshipFrame=0; //frame of spaceship for animation
    private Rect spaceshipBounds;
    private boolean spaceshipIsMoving;
    private List<MainShipLaser> shipLasers = new LinkedList<MainShipLaser>();
    //timer for spawning new laser
    private long lastLaserSpawnTime = 0;
    private long spaceshipFrameSwitchTime = 0; //for spaceships fire animation
    private long shipHitForTingeTime = 0; //for red tinge on your spaceship
    private boolean playerHitButNotDead = false; //also for red tinge

    Rect bounds = new Rect();//bounds for the PlayerShip


    public PlayerShip(Bitmap mainSpaceShip[], Bitmap mainSpaceShipLaser, float x, float y){
        this.mainSpaceShip = mainSpaceShip;

        if(mainSpaceShip.length > 0) {
            this.width = mainSpaceShip[0].getWidth();
            this.height = mainSpaceShip[0].getHeight();
        }else{
            //screw you
        }

        this.spaceshipX = x;
        this.spaceshipY = y;
        for(int i = 0; i< shipLasers.size(); i++){
            shipLasers.get(i).setBmp(mainSpaceShipLaser);
        }
    }

    public void spawnShipLaser(float x, float y){

        shipLasers.add(new MainShipLaser(x,y));

    }

    public boolean hasCollision(float collx, float colly) {
        return getBounds().contains((int) collx, (int) colly);
    }

    public boolean hasCollision(Rect rect) {
        return getBounds().contains(rect);
    }

    public Rect getBounds() {
        bounds.set((int)(this.spaceshipX), (int)(this.spaceshipY),
                (int)(this.spaceshipX+width), (int)(this.spaceshipY+height));
        return bounds;
    }



    //getters and setters


    public List<MainShipLaser> getShipLaserArray() {
        return shipLasers;
    }

    public void setShipLaserArray(List<MainShipLaser> shipLaser) {
        this.shipLasers = shipLaser;
    }

    public float getSpaceshipY() {
        return spaceshipY;
    }

    public void setSpaceshipY(float spaceshipY) {
        this.spaceshipY = spaceshipY;
    }

    public float getSpaceshipX() {
        return spaceshipX;
    }

    public void setSpaceshipX(float spaceshipX) {
        this.spaceshipX = spaceshipX;
    }

    public int getCurrentSpaceshipFrame() {
        return currentSpaceshipFrame;
    }

    public void setCurrentSpaceshipFrame(int currentSpaceshipFrame) {
        this.currentSpaceshipFrame = currentSpaceshipFrame;
    }

    public Rect getSpaceshipBounds() {
        return spaceshipBounds;
    }

    public void setSpaceshipBounds(Rect spaceshipBounds) {
        this.spaceshipBounds = spaceshipBounds;
    }

    public boolean isSpaceshipMoving() {
        return spaceshipIsMoving;
    }

    public void setSpaceshipIsMoving(boolean spaceshipIsMoving) {
        this.spaceshipIsMoving = spaceshipIsMoving;
    }
    public long getLastLaserSpawnTime() {
        return lastLaserSpawnTime;
    }

    public void setLastLaserSpawnTime(long lastLaserSpawnTime) {
        this.lastLaserSpawnTime = lastLaserSpawnTime;
    }
    public long getSpaceshipFrameSwitchTime() {
        return spaceshipFrameSwitchTime;
    }

    public void setSpaceshipFrameSwitchTime(long spaceshipFrameSwitchTime) {
        this.spaceshipFrameSwitchTime = spaceshipFrameSwitchTime;
    }

    public long getShipHitForTingeTime() {
        return shipHitForTingeTime;
    }

    public void setShipHitForTingeTime(long shipHitForTinge) {
        this.shipHitForTingeTime = shipHitForTinge;
    }

    public boolean isPlayerHitButNotDead() {
        return playerHitButNotDead;
    }

    public void setPlayerHitButNotDead(boolean playerHitButNotDead) {
        this.playerHitButNotDead = playerHitButNotDead;
    }


}
