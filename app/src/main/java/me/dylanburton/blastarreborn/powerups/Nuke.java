package me.dylanburton.blastarreborn.powerups;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 8/3/2017.
 */

public class Nuke extends Powerup {

        public Nuke(Bitmap nuke, float x, float y){
            powerupType = PowerupType.NUKE;
            this.x = x;
            this.y = y;
            this.dy = 3;
            this.bmp = nuke;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public Bitmap getBitmap() {
            return bmp;
        }

        public void setBitmap(Bitmap nuke) {
            this.bmp = nuke;
        }

        public float getDy() {
            return dy;
        }

        public void setDy(float dy) {
            this.dy = dy;
        }

        public PowerupType getPowerupType() {
            return powerupType;
        }







}
