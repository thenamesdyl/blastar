package me.dylanburton.blastarreborn.enemies;

/**
 * Created by Dylan on 7/16/2017.
 */

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.LinkedList;
import java.util.List;

import me.dylanburton.blastarreborn.lasers.ShipLaser;

/**
 * An enemy is a template for all the enemies     */
public class Enemy {
    private final float HALF_DIVISOR = 1.9f;  //changing the dimensions to be consistent
    private Bitmap btm;
    private float x=0;
    private float y=500;
    private float vx=0;
    private float vy=0;
    private int points;
    private int lives;
    private float width=0; // width onscreen
    private float height=0;  // height onscreen
    private float halfWidth = 0;  // convenience
    private float halfHeight = 0;
    private boolean enemyIsHitButNotDead = false; //specific yet helpful boolean for my hit animation
    private long explosionActivateTime; //adding so I dont have to delete the enemy after explosion for a couple seconds, this way their orbs dont dissapear
    private long hitContactTimeForTinge = 0; //for if the laser hits the enemy
    private long hitContactTimeForExplosions = 0; //for if the laser hits the enemy

    // firing stuff
    private List<ShipLaser> shipLasers = new LinkedList<ShipLaser>(); //the lasers for the ship
    private float randomlyGeneratedEnemyFiringTimeInSeconds; //variable for enemy firing stuff
    private long enemyFiringTime = 0; //for controlling enemy firing

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
    private boolean enemyHasAIStarted = false; //has the AI for this enemy object started yet


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

    public void spawnShipLasers(){} //to be overwritten by specific enemy classes

    //update method in playscreen
    public void updateShipLaserPositions(){
        for(ShipLaser sl: shipLasers){
            sl.setX(sl.getX() + sl.getDx());
            sl.setY(sl.getY() + sl.getDy());
        }

    }

    public List getShipLaserPositionsList(){
        return shipLasers;
    }

    public void addToShipLaserPositionsList(ShipLaser sl){
        shipLasers.add(sl);

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

    public float getVx() {
        return vx;
    }

    public void setVx(float vx) {
        this.vx = vx;
    }

    public float getVy() {
        return vy;
    }

    public void setVy(float vy) {
        this.vy = vy;
    }

    public long getFinishedRandomGeneratorsTime() {
        return finishedRandomGeneratorsTime;
    }

    public void setFinishedRandomGeneratorsTime(long finishedRandomGeneratorsTime) {
        this.finishedRandomGeneratorsTime = finishedRandomGeneratorsTime;
    }

    public long getLastSlowedDownVelocityTime() {
        return lastSlowedDownVelocityTime;
    }

    public void setLastSlowedDownVelocityTime(long lastSlowedDownVelocityTime) {
        this.lastSlowedDownVelocityTime = lastSlowedDownVelocityTime;
    }

    public long getLastSpedUpVelocityTime() {
        return lastSpedUpVelocityTime;
    }

    public void setLastSpedUpVelocityTime(long lastSpeededUpVelocityTime) {
        this.lastSpedUpVelocityTime = lastSpeededUpVelocityTime;
    }

    public boolean isSlowingDown() {
        return isSlowingDown;
    }

    public void setSlowingDown(boolean slowingDown) {
        isSlowingDown = slowingDown;
    }

    public boolean isSpeedingUp() {
        return isSpeedingUp;
    }

    public void setSpeedingUp(boolean speedingUp) {
        isSpeedingUp = speedingUp;
    }

    public boolean isFinishedVelocityChange() {
        return isFinishedVelocityChange;
    }

    public void setFinishedVelocityChange(boolean finishedVelocityChange) {
        isFinishedVelocityChange = finishedVelocityChange;
    }

    public boolean isAIStarted() {
        return enemyHasAIStarted;
    }

    public void setAIStarted(boolean enemyHasAIStarted) {
        this.enemyHasAIStarted = enemyHasAIStarted;
    }
    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }
    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
    public boolean isEnemyHitButNotDead() {
        return enemyIsHitButNotDead;
    }

    public void setEnemyIsHitButNotDead(boolean enemyIsHitButNotDead) {
        this.enemyIsHitButNotDead = enemyIsHitButNotDead;
    }
    public float getRandomlyGeneratedEnemyFiringTimeInSeconds() {
        return randomlyGeneratedEnemyFiringTimeInSeconds;
    }

    public void setRandomlyGeneratedEnemyFiringTimeInSeconds(float randomlyGeneratedEnemyFiringTimeInSeconds) {
        this.randomlyGeneratedEnemyFiringTimeInSeconds = randomlyGeneratedEnemyFiringTimeInSeconds;
    }

    public long getEnemyFiringTime() {
        return enemyFiringTime;
    }

    public void setEnemyFiringTime(long enemyFiringTime) {
        this.enemyFiringTime = enemyFiringTime;
    }
    public long getExplosionActivateTime() {
        return explosionActivateTime;
    }

    public void setExplosionActivateTime(long explosionActivateTime) {
        this.explosionActivateTime = explosionActivateTime;
    }
    public long getHitContactTimeForTinge() {
        return hitContactTimeForTinge;
    }

    public void setHitContactTimeForTinge(long hitContactTime) {
        this.hitContactTimeForTinge = hitContactTime;
    }
    public long getHitContactTimeForExplosions() {
        return hitContactTimeForTinge;
    }

    public void setHitContactTimeForExplosions(long hitContactTime) {
        this.hitContactTimeForExplosions = hitContactTime;
    }

}