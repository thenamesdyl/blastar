package me.dylanburton.blastarreborn.powerups;

import android.graphics.Bitmap;

/**
 * Created by Dylan on 8/3/2017.
 */

public class Nuke extends Powerup {

        private PowerupType powerupType = PowerupType.NUKE;
        private float x;
        private float y;
        private float dy = 3;
        private Bitmap nuke;

        public Nuke(Bitmap nuke, float x, float y){
            this.x = x;
            this.y = y;
            this.nuke = nuke;
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
            return nuke;
        }

        public void setBitmap(Bitmap doubleFire) {
            this.nuke = nuke;
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
