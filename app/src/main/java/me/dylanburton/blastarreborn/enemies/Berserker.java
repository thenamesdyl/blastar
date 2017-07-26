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
    private long lastAccelerationTime;

    public Berserker(Bitmap shipBitmap, Bitmap btmHit){
        super(shipBitmap, btmHit, EnemyType.BERSERKER);
        hs = new HeatSinker();


    }


    public float updateShipVelocityX(float cpx, float cpy){
        hs.updateHeatsink(getX(), getY(), cpx, cpy);
        return hs.getDx();
    }

    public float updateShipVelocityY(float cpx, float cpy){
        hs.updateHeatsink(getX(), getY(), cpx, cpy);
        return hs.getDy();
    }


    public long getUpdateVelocityTime() {
        return updateVelocityTime;
    }

    public void setUpdateVelocityTime(long updateVelocityTime) {
        this.updateVelocityTime = updateVelocityTime;
    }

    public long getLastAccelerationTime() {
        return lastAccelerationTime;
    }

    public void setLastAccelerationTime(long lastAccelerationTime) {
        this.lastAccelerationTime = lastAccelerationTime;
    }


}
