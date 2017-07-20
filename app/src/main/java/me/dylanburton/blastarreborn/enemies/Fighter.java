package me.dylanburton.blastarreborn.enemies;

import android.graphics.Bitmap;

import java.util.LinkedList;
import java.util.List;

import me.dylanburton.blastarreborn.lasers.DiagonalLaser;
import me.dylanburton.blastarreborn.lasers.ShipLaser;

/**
 * Created by Dylan on 7/16/2017.
 */

/*
 * Classes will be used to differentiate movement behavior/firing behavior by changing variables for each type of enemy. This will allow for special movements, and various other things
 */
public class Fighter extends Enemy {

    public Fighter(Bitmap shipBitmap){
        //calls main Enemy constructor
        super(shipBitmap, EnemyType.FIGHTER);

    }



}
