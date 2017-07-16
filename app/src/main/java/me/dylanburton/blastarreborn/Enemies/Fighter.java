package me.dylanburton.blastarreborn.Enemies;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 7/16/2017.
 */

/*
 * Classes will be used to differentiate movement behavior by changing variables for each type of enemy. This will allow for special movements, and various other things
 */
public class Fighter extends Enemy {

    public Fighter(Bitmap bitmap){
        //calls main Enemy constructor
        super(bitmap, EnemyType.FIGHTER);
    }

}
