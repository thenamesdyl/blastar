package me.dylanburton.blastarreborn.enemies;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 7/19/2017.
 */

public class Imperial extends Enemy {
    private static final int RANDOM_DIRECTION_SPEED = 8000;

    public Imperial(Bitmap bmp, Bitmap bmpHit, boolean isWorthEnemyDestroyedPoint){
        super(bmp,bmpHit,EnemyType.IMPERIAL, isWorthEnemyDestroyedPoint);
    }

    @Override
    public int getRandomDirectionSpeed(){
        return RANDOM_DIRECTION_SPEED;

    }

}
