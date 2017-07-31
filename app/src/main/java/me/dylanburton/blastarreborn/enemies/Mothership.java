package me.dylanburton.blastarreborn.enemies;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 7/19/2017.
 */

public class Mothership extends Enemy {

    private static final int RANDOM_DIRECTION_SPEED = 5000;
    private long motherShipSpawner = 0;

    public Mothership(Bitmap shipBitmap, Bitmap btmHit, boolean isWorthEnemyDestroyedPoint){
        //calls main Enemy constructor
        super(shipBitmap, btmHit, EnemyType.MOTHERSHIP, isWorthEnemyDestroyedPoint);

    }

    @Override
    public int getRandomDirectionSpeed(){
        return RANDOM_DIRECTION_SPEED;

    }


    public long getMotherShipSpawner() {
        return motherShipSpawner;
    }

    public void setMotherShipSpawner(long motherShipSpawner) {
        this.motherShipSpawner = motherShipSpawner;
    }




}
