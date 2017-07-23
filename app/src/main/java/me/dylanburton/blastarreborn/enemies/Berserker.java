package me.dylanburton.blastarreborn.enemies;

import android.graphics.Bitmap;

import me.dylanburton.blastarreborn.utils.HeatSinker;

/**
 * Created by Dylan on 7/19/2017.
 */

//berserkers behavior will be charging playership, that's why heatsinker is used
public class Berserker extends Enemy {
    private HeatSinker hs;
    private long updateVelocityTime;

    public Berserker(Bitmap shipBitmap){
        super(shipBitmap, EnemyType.BERSERKER);
        hs = new HeatSinker();


    }


    @Override
    public void updateShipVelocity(float cpx, float cpy){
        hs.updateHeatsink(getX(), getY(), cpx, cpy);
        setVx(hs.getDx());
        setVy(hs.getDy());
    }


    public long getUpdateVelocityTime() {
        return updateVelocityTime;
    }

    public void setUpdateVelocityTime(long updateVelocityTime) {
        this.updateVelocityTime = updateVelocityTime;
    }

}
