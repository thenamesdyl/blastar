package me.dylanburton.blastarreborn.enemies;

import android.graphics.Bitmap;

import me.dylanburton.blastarreborn.spaceships.ShipType;

/**
 * Created by Dylan on 7/16/2017.
 */

/*
 * Classes will be used to differentiate movement behavior/firing behavior by changing variables for each type of enemy. This will allow for special movements, and various other things
 */
public class Fighter extends Enemy {
    private static final int RANDOM_DIRECTION_SPEED = 15000;
    public Fighter(Bitmap shipBitmap, Bitmap shipHit, boolean isWorthEnemyDestroyedPoint) {
        //calls main Enemy constructor
        super(shipBitmap, shipHit, ShipType.FIGHTER, isWorthEnemyDestroyedPoint);

    }

    @Override
    public int getRandomDirectionSpeed(){
        return RANDOM_DIRECTION_SPEED;

    }


}
