package me.dylanburton.blastarreborn.enemies;

import android.graphics.Bitmap;

import me.dylanburton.blastarreborn.utils.HeatSinker;

/**
 * Created by Dylan on 7/19/2017.
 */

//berserkers behavior will be charging playership, that's why heatsinker is used
public class Berserker extends Enemy {
    private HeatSinker hs;
    private Bitmap laserBitmap;
    private int x;
    private int y;
    private int dx;
    private int dy;

    public Berserker(Bitmap shipBitmap, Bitmap laserBitmap){
        super(shipBitmap, EnemyType.FIGHTER);
        this.laserBitmap = laserBitmap;
        hs = new HeatSinker();

    }

    public void updateShipVelocity(int cpx, int cpy){
        hs.updateHeatsink(x, y, cpx, cpy);
        this.dx = hs.getDx();
        this.dy = hs.getDy();
    }

}
