package me.dylanburton.blastarreborn.enemies;

import android.graphics.Bitmap;

import me.dylanburton.blastarreborn.spaceships.ShipType;

/**
 * Created by Dylan on 8/8/2017.
 */

public class Heatsinker extends Enemy{
    private int RANDOM_DIRECTION_SPEED = 20000;

    public Heatsinker(Bitmap shipBitmap, Bitmap btmHit, boolean isWorthEnemyDestroyedPoint) {
        //calls main Enemy constructor
        super(shipBitmap, btmHit, ShipType.HEATSINKER, isWorthEnemyDestroyedPoint);

    }

    @Override
    public int getRandomDirectionSpeed() {
        return RANDOM_DIRECTION_SPEED;

    }
}
