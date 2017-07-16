package me.dylanburton.blastarreborn.Enemies;

/**
 * Created by Dylan on 7/16/2017.
 */

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * An enemy is a template for all the enemies     */
public class Enemy {
    private final float HALF_DIVISOR = 1.9f;  //changing the dimensions to be consistent
    private Bitmap btm;
    private float x=0;
    private float y=0;
    private float vx=0;
    private float vy=0;
    private int points;
    private int lives;
    private float width=0; // width onscreen
    private float height=0;  // height onscreen
    private float halfWidth = 0;  // convenience
    private float halfHeight = 0;

    /*
     * Enemy AI Movement Variables.
     * Enemy slows down to 0,0 Vx Vy and then speeds up to new randomly generated velocity
     */

    private long finishedRandomGeneratorsTime; //after random velocities and random time assigned, this records time so we know how long we need to wait
    private long lastSlowedDownVelocityTime; //to make enemy slow down before changing direction, need the time to make delays and slow it down gradually
    private long lastSpedUpVelocityTime; //like the last variable, need this to make enemy accelerate gradually as opposed to instantly
    private boolean isSlowingDown = false, isSpeedingUp = false, isFinishedVelocityChange = false;

    private float randomVelocityGeneratorX = 0;
    private float randomVelocityGeneratorY = 0; //randomly generated velocities between -5 and 5
    private boolean AIStarted = false;
    private boolean enemyIsHitButNotDead = false; //helpful for the enemy animation


    Rect bounds = new Rect();

    //default constructor for inheritance
    public Enemy(){

    }

    public Enemy(Bitmap bitmap, EnemyType enemyType) {
        this.btm = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.halfWidth = width/HALF_DIVISOR;
        this.halfHeight = height/HALF_DIVISOR;
        this.lives = enemyType.getLives();
        this.points = enemyType.getPoints();
    }


    public Bitmap getBitmap(){
        return btm;
    }
    public boolean hasCollision(float collx, float colly) {
        return getBounds().contains((int) collx, (int) colly);
    }

    public boolean hasCollision(Rect rect) {
        return getBounds().contains(rect);
    }

    public Rect getBounds() {
        bounds.set((int)(this.x), (int)(this.y-getBitmap().getHeight()),
                (int)(this.x+getBitmap().getWidth()), (int)(this.y+getBitmap().getHeight()));
        return bounds;
    }

    public void setRandomVelocityGeneratorX(float num) {
        randomVelocityGeneratorX = num;
    }

    public void setRandomVelocityGeneratorY(float num) {
        randomVelocityGeneratorY = num;
    }

    public float getRandomVelocityGeneratorX() {
        return randomVelocityGeneratorX;
    }

    public float getRandomVelocityGeneratorY() {
        return randomVelocityGeneratorY;
    }
    public Bitmap getBtm() {
        return btm;
    }

    public void setBtm(Bitmap btm) {
        this.btm = btm;
    }

    public boolean isSpeedingUp() {
        return isSpeedingUp;
    }

    public boolean isSlowingDown() {
        return isSlowingDown;
    }

    public void setSlowingDown(boolean slowingDown) {
        isSlowingDown = slowingDown;
    }

    public void setSpeedingUp(boolean speedingUp) {
        isSpeedingUp = speedingUp;
    }

    public void setX(float num) { x = num; }

    public void setY(float num) { y = num; }

    public float getVy() { return vy; }

    public float getVx() { return vx; }

    public void setVy(float num) { vy = num; }

    public void setVx(float num) { vx = num; }

    public float getX() { return x; }

    public float getY() { return y; }

    public void setFinishedVelocityChange(boolean finishedVelocityChange) { isFinishedVelocityChange = finishedVelocityChange; }

    public boolean getFinishedVelocityChange() { return isFinishedVelocityChange; }

    public boolean isEnemyHitButNotDead() { return enemyIsHitButNotDead; }

    public void setEnemyIsHitButNotDead(boolean enemyIsHitButNotDead) { this.enemyIsHitButNotDead = enemyIsHitButNotDead; }

    public void setLives(int lives) { this.lives = lives; }

    public int getLives() { return lives; }

    public void setLastSlowedDownVelocityTime(long lastSlowedDownVelocityTime) { this.lastSlowedDownVelocityTime = lastSlowedDownVelocityTime; }

    public long getLastSlowedDownVelocityTime() { return lastSlowedDownVelocityTime; }

    public long getLastSpedUpVelocityTime() { return lastSpedUpVelocityTime; }

    public void setLastSpedUpVelocityTime(long lastSpedUpVelocityTime) { this.lastSpedUpVelocityTime = lastSpedUpVelocityTime; }

    public void setFinishedRandomGeneratorsTime(long finishedRandomGeneratorsTime) { this.finishedRandomGeneratorsTime = finishedRandomGeneratorsTime;  }

    public long getFinishedRandomGeneratorsTime() { return finishedRandomGeneratorsTime; }

    public void setAIStarted(boolean AIStarted) { this.AIStarted = AIStarted; }

    public boolean getAIStarted() { return AIStarted; }

}