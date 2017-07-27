package me.dylanburton.blastarreborn.enemies;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 7/19/2017.
 */

public class Mothership extends Enemy {

    private static final int RANDOM_DIRECTION_SPEED = 5000;

    public Mothership(Bitmap shipBitmap, Bitmap btmHit){
        //calls main Enemy constructor
        super(shipBitmap, btmHit, EnemyType.MOTHERSHIP);

    }

    @Override
    public int getRandomDirectionSpeed(){
        return RANDOM_DIRECTION_SPEED;

    }



}
