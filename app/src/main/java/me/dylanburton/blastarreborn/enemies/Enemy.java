package me.dylanburton.blastarreborn.enemies;

/**
 * Created by Dylan on 7/16/2017.
 */

import android.graphics.Bitmap;
import android.graphics.Rect;

import me.dylanburton.blastarreborn.spaceships.Ship;
import me.dylanburton.blastarreborn.spaceships.ShipType;

/**
 * An enemy is a template for all the enemies     */
public class Enemy implements Ship {
    private ShipType shipType;
    private Bitmap btm;
    private Bitmap btmHit;
    private float x;
    private float y;
    private float vx=0;
    private float vy=0;
    private int points;
    private int lives;
    private boolean enemyIsHitButNotDead = false; //specific yet helpful boolean for my hit animation
    private long hitContactTimeForTinge = 0; //for if the laser hits the enemy

    // firing stuff
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
    private boolean isAIDisabled = false;


    Rect bounds = new Rect();

    private boolean isWorthEnemyDestroyedPoint = true; //this disables points if the enemy was created by a mothership

    //default constructor for inheritance
    public Enemy(){

    }

    public Enemy(Bitmap bitmap, Bitmap btmHit, ShipType shipType, boolean isWorthEnemyDestroyedPoint) {
        setX(0);
        setY(-50);
        this.btmHit = btmHit;
        this.btm = bitmap;
        this.lives = shipType.getLives();
        this.points = shipType.getPoints();
        this.shipType = shipType;
        setWorthEnemyDestroyedPoint(isWorthEnemyDestroyedPoint);
    }

    public Bitmap getBitmap(){
        return btm;
    }
    public Bitmap getHitBitmap(){ return btmHit;}
    public void setHitBitmap(Bitmap btmHit){ this.btmHit = btmHit;}
    public boolean hasCollision(float collx, float colly) {
        return getBounds().contains((int) collx, (int) colly);
    }

    public boolean hasCollision(Rect rect) {
        return getBounds().contains(rect);
    }

    public Rect getBounds() {
        bounds.set((int)(this.getX()), (int)(this.getY()),
                (int)(this.getX()+getBitmap().getWidth()), (int)(this.getY()+getBitmap().getHeight()));
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
    public long getHitContactTimeForTinge() {
        return hitContactTimeForTinge;
    }

    public void setHitContactTimeForTinge(long hitContactTime) {
        this.hitContactTimeForTinge = hitContactTime;
    }
    public boolean isAIDisabled() {
        return isAIDisabled;
    }

    public void setAIDisabled(boolean AIDisabled) {
        isAIDisabled = AIDisabled;
    }

    public ShipType getShipType() {
        return shipType;
    }

    public void setShipType(ShipType shipType) {
        this.shipType = shipType;
    }


    public int getRandomDirectionSpeed() {return 0;} //override

    public boolean isWorthEnemyDestroyedPoint() {
        return isWorthEnemyDestroyedPoint;
    }

    public void setWorthEnemyDestroyedPoint(boolean worthEnemyDestroyedPoint) {
        isWorthEnemyDestroyedPoint = worthEnemyDestroyedPoint;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }


}