package me.dylanburton.blastarreborn.lasers;

import android.graphics.Bitmap;

import me.dylanburton.blastarreborn.utils.HeatSinker;

/**
 * Created by Dylan on 7/19/2017.
 */

public class HeatSinkLaser extends ShipLaser {
    HeatSinker hs;
    Bitmap bmp;
    private int x;
    private int y;
    private int dx;
    private int dy;

    public HeatSinkLaser(Bitmap bmp, int x , int y){
        setEnemyLaser(true);
        this.bmp = bmp;
        this.x = x;
        this.y = y;
        hs = new HeatSinker();

    }

    public void updateHeatsink(int x, int y, int cpx, int cpy){
        hs.updateHeatsink(x,y,cpx,cpy);
        this.dy = hs.getDy();
        this.dx = hs.getDx();
    }

}
