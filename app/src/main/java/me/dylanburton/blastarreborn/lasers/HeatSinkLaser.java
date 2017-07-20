package me.dylanburton.blastarreborn.lasers;

import android.graphics.Bitmap;

import me.dylanburton.blastarreborn.utils.HeatSinker;

/**
 * Created by Dylan on 7/19/2017.
 */

public class HeatSinkLaser extends ShipLaser {
    HeatSinker hs;
    Bitmap bmp;
    private float x;
    private float y;
    private float dx;
    private float dy;

    public HeatSinkLaser(Bitmap bmp, int x , int y){
        setEnemyLaser(true);
        this.bmp = bmp;
        this.x = x;
        this.y = y;
        hs = new HeatSinker();

    }

    public void updateHeatsink(float x, float y, float cpx, float cpy){
        hs.updateHeatsink(x,y,cpx,cpy);
        this.dy = hs.getDy();
        this.dx = hs.getDx();
    }

}
