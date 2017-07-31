package me.dylanburton.blastarreborn.powerups;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 7/31/2017.
 */

public abstract class Powerup {
    public abstract Bitmap getBitmap();
    public abstract float getX();
    public abstract float getY();
    public abstract void setX(float x);
    public abstract void setY(float y);
    public abstract float getDy();
    public abstract PowerupType getPowerupType();
}
