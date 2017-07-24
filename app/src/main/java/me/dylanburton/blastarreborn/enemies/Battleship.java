package me.dylanburton.blastarreborn.enemies;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 7/19/2017.
 */

public class Battleship extends Enemy {

    private static final int RANDOM_DIRECTION_SPEED = 5000;

    public Battleship(Bitmap shipBitmap){
        //calls main Enemy constructor
        super(shipBitmap, EnemyType.BATTLESHIP);

    }

    @Override
    public int getRandomDirectionSpeed(){
        return RANDOM_DIRECTION_SPEED;

    }



}
