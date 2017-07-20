package me.dylanburton.blastarreborn.enemies;

import android.graphics.Bitmap;

import me.dylanburton.blastarreborn.utils.HeatSinker;

/**
 * Created by Dylan on 7/19/2017.
 */

//berserkers behavior will be charging playership, that's why heatsinker is used
public class Berserker extends Enemy {
    private HeatSinker hs;
    private float x;
    private float y;

    public Berserker(Bitmap shipBitmap){
        super(shipBitmap, EnemyType.BERSERKER);
        hs = new HeatSinker();


    }


    @Override
    public void updateShipVelocity(float cpx, float cpy){
        hs.updateHeatsink(x, y, cpx, cpy);
        setVx(hs.getDx());
        setVy(hs.getDy());
    }

}
