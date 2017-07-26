package me.dylanburton.blastarreborn.enemies;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 7/19/2017.
 */

public class Battlecruiser extends Enemy {
    private int RANDOM_DIRECTION_SPEED = 3000;
    public Battlecruiser(Bitmap shipBitmap, Bitmap btmHit){
        //calls main Enemy constructor
        super(shipBitmap, btmHit, EnemyType.BATTLECRUISER);

    }

    @Override
    public int getRandomDirectionSpeed(){
        return RANDOM_DIRECTION_SPEED;

    }
}
