package me.dylanburton.blastarreborn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Represents the main screen of play for the game.
 *
 */
public class PlayScreen extends Screen {


    private MainActivity act;
    private Paint p;
    //how fast the spaceship moves backwards
    private final int DECAY_SPEED = 5;
    private final int LEVEL_FIGHTER = 1;  // level where different ships are added
    private final int LEVEL_IMPERIAL = 3;
    private final int LEVEL_BATTLECRUISER = 4;
    private final int LEVEL_BATTLESHIP = 5;
    private final int LEVEL_BERSERKER = 6;
    static final long ONESEC_NANOS = 1000000000L;

    private enum State {RUNNING, STARTROUND, ROUNDSUMMARY, STARTGAME, PLAYERDIED, GAMEOVER;}
    private volatile State gamestate = State.STARTGAME;

    private List<Enemy> enemiesFlying = Collections.synchronizedList(new LinkedList<Enemy>());  // enemies that are still alive
    private List<ShipExplosion> shipExplosions = new LinkedList<ShipExplosion>();  // ship explosions

    private List<Enemy> fightersFlying = Collections.synchronizedList(new LinkedList<Enemy>());  // enemies that are still alive
    //width and height of screen
    private int width = 0;
    private int height = 0;
    private int MIN_HEIGHT;

    //bitmap with a rect used for drawing
    private Bitmap starbackground, spaceship, spaceshipLaser, fighter, explosion[];
    private Rect scaledDst = new Rect();

    //main spaceships location and bound, using 1000 for spaceshipy and x because of a weird glitch where the spaceship is drawn at 0,0 for 100 ms
    private int spaceshipY=1000;
    private int spaceshipX=1000;
    private Rect spaceshipBounds;
    private boolean spaceshipIsMoving;
    private int spaceshipLaserX;
    private int spaceshipLaserY;

    //used to move the background image, need two pairs of these vars for animation
    private int mapAnimatorX;
    private int mapAnimatorY;
    private int secondaryMapAnimatorX;
    private int secondaryMapAnimatorY;

    //time stuff
    private long hitContactTime = 0;
    private boolean enemyStartDelayReached = false;
    private long frtime = 0;
    private long gameStartTime = 0;
    private int fps = 0;

    //various game things
    private int minRoundPass;
    private int currentLevel;
    private int score;
    private int lives;
    private int highscore = 0, highlev = 1;
    private static final String HIGHSCORE_FILE = "highscore.dat";
    private static final int START_NUMLIVES = 3;
    private Map<Integer, String> levelMap = new HashMap<Integer, String>();

    /*
     * Enemy AI Movement Variables.
     * Enemy slows down to 0,0 Vx Vy and then speeds up to new randomly generated velocity
     */

    private Random rand = new Random();
    private long finishedRandomGeneratorsTime; //after random velocities and random time assigned, this records time so we know how long we need to wait
    private long lastSlowedDownVelocityTime; //to make enemy slow down before changing direction, need the time to make delays and slow it down gradually
    private long lastSpeededUpVelocityTime; //like the last variable, need this to make enemy accelerate gradually as opposed to instantly
    private boolean enemyIsSlowingDown = false,enemyIsSpeedingUp = false, enemyIsFinishedVelocityChange = false, enemyIsAIStarted = false; //need to know when enemy is slowing down,speeding up, and when process of decelerating and accelerating is complete
    private float nextVelocityChangeInSeconds = 0; //randomly generated number between 1 and 3 seconds to start slowing down and changing velocity
    private float randomVelocityGeneratorX = 0, randomVelocityGeneratorY = 0; //randomly generated velocities between -5 and 5


    public PlayScreen(MainActivity act) {
        p = new Paint();
        this.act = act;
        AssetManager assetManager = act.getAssets();
        try {

            //background
            InputStream inputStream = assetManager.open("sidescrollingstars.jpg");
            starbackground = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            this.MIN_HEIGHT = (int) (starbackground.getHeight() * 0.7);

            //your spaceship and laser
            spaceship = act.getScaledBitmap("spaceshiptopview.png");
            spaceshipLaser = act.getScaledBitmap("spaceshiplaser.png");

            //fighter
            fighter = act.getScaledBitmap("fighter.png");

            //explosion
            explosion = new Bitmap[12];
            for(int i = 0; i < 12; i++) {
                explosion[i] = act.getScaledBitmap("explosion/explosion"+(i+1)+".png");
            }

            p.setTypeface(act.getGameFont());
            currentLevel = 1;

        } catch (IOException e) {
            Log.d(act.LOG_ID, "why tho?", e);
        }
    }

    /**
     * initialize and start a game
     */
    void initGame() {

        //used for slight delays on spawning things at the beginning
        gameStartTime = System.nanoTime();
        score = 0;
        currentLevel = 1;
        lives = START_NUMLIVES;
        highscore = 0;

        if(currentLevel == 1){
            fightersFlying.add(new Enemy(fighter, 20));
            fightersFlying.add(new Enemy(fighter, 20));
        }

        try {
            BufferedReader f = new BufferedReader(new FileReader(act.getFilesDir() + HIGHSCORE_FILE));
            highscore = Integer.parseInt(f.readLine());
            highlev = Integer.parseInt(f.readLine());
            f.close();
        } catch (Exception e) {
            Log.d(MainActivity.LOG_ID, "ReadHighScore", e);
        }
        gamestate = State.STARTGAME;
    }


    /**
     * init game for current round
     */
    private void initRound() {

        // how many enemies do we need to kill to progress?
        if (currentLevel == 1)
            minRoundPass = 10;
        else if (currentLevel < 4)
            minRoundPass = 30;
        else
            minRoundPass = 40;


        gamestate = State.RUNNING;
    }

    /**
     * player lost a life
     */
    private void loseLife() {
        lives--;

        if (lives == 0) {
            // game over!  wrap things up and write high score file
            gamestate = State.GAMEOVER;
            try {
                BufferedWriter f = new BufferedWriter(new FileWriter(act.getFilesDir() + HIGHSCORE_FILE));
                f.write(Integer.toString(highscore)+"\n");
                f.write(Integer.toString(highlev)+"\n");
                f.close();
            } catch (Exception e) { // if we can't write the high score file...oh well.
                Log.d(MainActivity.LOG_ID, "WriteHiScore", e);
            }
        } else
            gamestate = State.PLAYERDIED;
    }



    @Override
    public void update(View v) {
        long newtime = System.nanoTime();
        float elapsedsecs = (float) (newtime - frtime) / ONESEC_NANOS;
        frtime = newtime;
        fps = (int) (1 / elapsedsecs);


        if (gamestate == State.STARTROUND) {

            initRound();
            return;
        }

        if (width == 0) {
            // set variables that rely on screen size
            width = v.getWidth();
            height = v.getHeight();

            spaceshipX = width / 2;
            spaceshipY = height * 2 / 3;

            spaceshipLaserX = spaceshipX + spaceship.getWidth() / 8;
            spaceshipLaserY = spaceshipY + spaceship.getHeight() / 3;

            mapAnimatorX = width;
            mapAnimatorY = height;
            secondaryMapAnimatorX = width;
            secondaryMapAnimatorY = height;

        }

        if (gamestate == State.RUNNING) {

        }


        //need a place to update enemy positions, needs some sort of AI

        synchronized (fightersFlying) {
            Iterator<Enemy> enemiesIterator = fightersFlying.iterator();
            while (enemiesIterator.hasNext()) {
                Enemy e = enemiesIterator.next();
                if (e.x >= width * 4 / 5 || e.x <= 0) {
                    e.vx = -e.vx;


                    //this needs to be replaced with some sort of competent movement behavior.
                /*e.x += fighterSpeed;
                if(e.x >=width*4/5){
                    fighterSpeed=-fighterSpeed;
                }else if(e.x<=0){
                    fighterSpeed=-fighterSpeed;
                }*/

                    //

                    //Movement AI

                    if (enemyStartDelayReached) {
                        e.x = e.x + e.vx;
                        e.y = e.y + e.vy;
                        if (!enemyIsAIStarted) {
                            enemyIsFinishedVelocityChange = true;
                            enemyIsAIStarted = true;
                        }
                    }


                    if (enemyIsFinishedVelocityChange) {

                        nextVelocityChangeInSeconds = (rand.nextInt(1000) + 1000) / 1000;

                        randomVelocityGeneratorX = (rand.nextInt(10000) + 200) / 1000;
                        //makes it negative if it is bigger than 5
                        if (randomVelocityGeneratorX > 5) {
                            randomVelocityGeneratorX = randomVelocityGeneratorX - 10;
                        }

                        randomVelocityGeneratorY = (rand.nextInt(10000) + 200) / 1000;
                        if (randomVelocityGeneratorY > 5) {
                            randomVelocityGeneratorY = randomVelocityGeneratorY - 10;
                        }

                        if (!enemyIsSlowingDown) {
                            enemyIsSpeedingUp = true;
                        }

                        finishedRandomGeneratorsTime = System.nanoTime();

                        //just initiating these guys
                        lastSlowedDownVelocityTime = finishedRandomGeneratorsTime;
                        lastSpeededUpVelocityTime = finishedRandomGeneratorsTime;

                        enemyIsFinishedVelocityChange = false;

                    }

                    //wait a couple seconds for this to be true
                    if (frtime > (finishedRandomGeneratorsTime + (ONESEC_NANOS * nextVelocityChangeInSeconds))) {
                        if (enemyIsSlowingDown && (frtime > lastSlowedDownVelocityTime + (ONESEC_NANOS / 100))) {
                            //obv will never be 0. Half a second for slowing down, then speeding up
                            e.vx = e.vx - (e.vx / 50);
                            e.vy = e.vy - (e.vy / 50);


                            //borders
                            if(e.x < 0 || e.x > width*4/5){
                                e.vx = -e.vx;
                                randomVelocityGeneratorX = -randomVelocityGeneratorX;
                            }

                            if(e.y < 0 || e.y > height/4){
                                e.vy = -e.vy;
                                randomVelocityGeneratorY = -randomVelocityGeneratorY;

                            }

                        }
                            //delays this slowing down process a little
                            lastSlowedDownVelocityTime = System.nanoTime();

                        } else if (enemyIsSpeedingUp && (frtime > lastSpeededUpVelocityTime + (ONESEC_NANOS / 100))) {

                            //will not have asymptotes like the last one
                            e.vx = e.vx + (randomVelocityGeneratorX / 50);
                            e.vy = e.vy + (randomVelocityGeneratorY / 50);

                            //borders for x and y
                            if (e.x < 0 || e.x > width * 4 / 5) {
                                e.vx = -e.vx;
                                randomVelocityGeneratorX = -randomVelocityGeneratorX;

                            }
                            if (e.y < 0 || e.y > height / 3) {
                                e.vy = -e.vy;
                                randomVelocityGeneratorY = -randomVelocityGeneratorY;


                            }
                        if(e.y < 0 || e.y > height/4){
                            e.vy = -e.vy;
                            randomVelocityGeneratorY = -randomVelocityGeneratorY;

                            //just adding a margin of error regardless though, if the nanoseconds were slightly off it would not work
                            if ((e.vx > randomVelocityGeneratorX - .01 && e.vx < randomVelocityGeneratorX + .01) && (e.vy > randomVelocityGeneratorY - .01 || e.vy < randomVelocityGeneratorY + .01)) {
                                enemyIsSpeedingUp = false;
                                enemyIsSlowingDown = true;
                                enemyIsFinishedVelocityChange = true;
                            }

                            //just adding a margin of error regardless though, if the nanoseconds were slightly off it would not work
                            if( (e.vx > randomVelocityGeneratorX-.1 && e.vx < randomVelocityGeneratorX+.1) && (e.vy > randomVelocityGeneratorY-.1 || e.vy < randomVelocityGeneratorY+.1)){
                                enemyIsSpeedingUp = false;
                                enemyIsSlowingDown = true;
                                enemyIsFinishedVelocityChange = true;
                            }
                       }


                    }
                    if (e.y >= MIN_HEIGHT || e.y < 0) {
                        e.vy = -e.vy;
                    }
                    e.x += e.vx;
                    e.y += e.vy;
                }

            }

        }

        //spaceship decay
        if(spaceshipY<height*5/6 && !spaceshipIsMoving) {
            spaceshipY += DECAY_SPEED;
        }

        //resets spaceship laser
        spaceshipLaserY -= 20.0f;
        if(spaceshipLaserY < -height/6){
            spaceshipLaserY = spaceshipY+spaceship.getHeight()/3;
            spaceshipLaserX = spaceshipX+spaceship.getWidth()/8;
        }



        //resets spaceship laser
        spaceshipLaserY -= 20.0f;
        if (spaceshipLaserY < -150) {
            spaceshipLaserY = spaceshipY + spaceship.getHeight() / 3;
            spaceshipLaserX = spaceshipX + spaceship.getWidth() / 8;
        }

            //animator for map background
            mapAnimatorY += 2.0f;
            secondaryMapAnimatorY += 2.0f;
            //this means the stars are off the screen
            if (mapAnimatorY >= height * 2) {
                mapAnimatorY = height;
            } else if (secondaryMapAnimatorY >= height * 2) {
                secondaryMapAnimatorY = height;
            }
        }


    @Override
    public void draw(Canvas c, View v) {
        try {
            if(gameStartTime + (ONESEC_NANOS/10) < frtime){
                enemyStartDelayReached = true;
            }

            // actually draw the screen
            scaledDst.set(mapAnimatorX-width, mapAnimatorY-height, mapAnimatorX, mapAnimatorY);
            c.drawBitmap(starbackground,null,scaledDst,p);
            //secondary background for animation. Same as last draw, but instead, these are a height-length higher
            c.drawBitmap(starbackground,null,new Rect(secondaryMapAnimatorX-width, secondaryMapAnimatorY-(height*2),secondaryMapAnimatorX, secondaryMapAnimatorY-height),p);




            synchronized (shipExplosions) {
                for(ShipExplosion se: shipExplosions){
                    c.drawBitmap(explosion[se.currentFrame],se.x,se.y,p);


                    //semi-clever way of adding a very precise delay (yes, I am scratching my own ass)
                    if(hitContactTime + (ONESEC_NANOS/50) < frtime) {
                        hitContactTime = System.nanoTime();
                        se.nextFrame();

                    }

                    if(se.currentFrame == 11){
                        shipExplosions.remove(se.getExplosionNumber());
                    }
                }
            }

            synchronized (enemiesFlying) {
                for(Enemy e: enemiesFlying) {
                    if(enemyStartDelayReached) {
                        c.drawBitmap(e.getBitmap(), e.x, e.y, p);

                        if ((e.hasCollision(spaceshipLaserX, spaceshipLaserY) || e.hasCollision(spaceshipLaserX + spaceship.getWidth() * 64 / 100, spaceshipLaserY))) {
                            spaceshipLaserX = 4000;
                            enemiesFlying.remove(e);
                            shipExplosions.add(new ShipExplosion(e.x, e.y + e.getBitmap().getHeight() / 4, shipExplosions.size()));
                            hitContactTime = System.nanoTime();

                        }
                    }
                }
            }

            //main spaceship lasers, adds a delay because for some reason it occasionally destroys spawned ships on start
            c.drawBitmap(spaceshipLaser, spaceshipLaserX, spaceshipLaserY, p);
            c.drawBitmap(spaceshipLaser, spaceshipLaserX + spaceship.getWidth() * 64 / 100, spaceshipLaserY, p);

            //main spaceship
            c.drawBitmap(spaceship, spaceshipX, spaceshipY, p);


            p.setColor(Color.WHITE);
            p.setTextSize(act.TS_NORMAL);
            p.setTypeface(act.getGameFont());

            if (score >= highscore) {
                highscore = score;
                highlev = currentLevel;
            }


            if (gamestate == State.ROUNDSUMMARY
                    || gamestate == State.STARTGAME
                    || gamestate == State.PLAYERDIED
                    || gamestate == State.GAMEOVER) {
                if (gamestate != State.STARTGAME) {
                    // round ended, by completion or player death, display stats

                    if (gamestate == State.ROUNDSUMMARY) {

                    } else if (gamestate == State.PLAYERDIED
                            || gamestate == State.GAMEOVER){

                    }

                }

                if (gamestate != State.PLAYERDIED
                        && gamestate != State.GAMEOVER) {

                }

                if (gamestate != State.GAMEOVER) {

                }
            }
            if (gamestate == State.GAMEOVER) {
                /*p.setTextSize(act.TS_BIG);
                p.setColor(Color.RED);
                drawCenteredText(c, "GamE oVeR!", height /2, p, -2);
                drawCenteredText(c, "Touch to end game", height * 4 /5, p, -2);
                p.setColor(Color.WHITE);
                drawCenteredText(c, "GamE oVeR!", height /2, p, 0);
                drawCenteredText(c, "Touch to end game", height * 4 /5, p, 0);*/
            }


        } catch (Exception e) {
            Log.e(MainActivity.LOG_ID, "draw", e);
            e.printStackTrace();
        }

    }


    //center text
    private void drawCenteredText(Canvas c, String msg, int height, Paint p, int shift) {
        c.drawText(msg, (width - p.measureText(msg)) / 2 + shift, height, p);
    }

    DisplayMetrics dm = new DisplayMetrics();
    @Override
    public boolean onTouch(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;

            case MotionEvent.ACTION_MOVE:
                synchronized (spaceship){
                    spaceshipBounds = new Rect(spaceshipX,spaceshipY,spaceshipX+spaceship.getWidth(),spaceshipY+spaceship.getHeight());

                    if(spaceshipBounds.contains((int) e.getX(),(int) e.getY())){
                        spaceshipIsMoving = true;
                        spaceshipX = (int) e.getX()-spaceship.getWidth()/2;
                        spaceshipY = (int) e.getY()-spaceship.getHeight()/2;

                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                spaceshipIsMoving=false;

                break;
        }

        return true;
    }

    /**
     * An enemy is a template for all the enemies     */
    private class Enemy {
        Bitmap btm;
        float x=0;
        float y=0;
        float vx=0;
        float vy=0;
        int points;
        float width = 0; // width onscreen
        float height = 0;  // height onscreen
        float halfWidth = 0;  // convenience
        float halfHeight = 0;
        final float HALF_DIVISOR = 1.9f;  //changing the dimensions to be consistent
        Random random = new Random();

        Rect bounds = new Rect();

        public Enemy(Bitmap bitmap, int points) {
            this.btm = bitmap;
            this.width = bitmap.getWidth();
            this.height = bitmap.getHeight();
            this.halfWidth = width/HALF_DIVISOR;
            this.halfHeight = height/HALF_DIVISOR;
            this.points = points;
            this.vx = vx + (random.nextBoolean() ? 1 : -1);
            this.vy = vy + (random.nextBoolean() ? 1 : -1);
            this.vx *= random.nextBoolean() ? 1 : -1;
            this.vy *= random.nextBoolean() ? 1 : -1;
            System.out.println(this.vx + ", " + this.vy);
        }

        public Bitmap getBitmap(){
            return btm;
        }
        public boolean hasCollision(float collx, float colly) {
            return getBounds().contains((int) collx, (int) colly);
        }
        public Rect getBounds() {
            bounds.set((int)(this.x), (int)(this.y-getBitmap().getHeight()),
                    (int)(this.x+getBitmap().getWidth()), (int)(this.y+getBitmap().getHeight()));
            return bounds;
        }

    }

    // this is for creating multiple ship explosion animations
    private class ShipExplosion{
        float x=0;
        float y=0;
        int currentFrame = 0;
        int explosionNumber = 0;

        public ShipExplosion(float x, float y, int explosionNumber){
            this.x = x;
            this.y = y;
            this.explosionNumber = explosionNumber;
        }

        public int getCurrentFrame(){
            return currentFrame;
        }

        public void nextFrame(){
            currentFrame++;
        }

        public int getExplosionNumber(){
            return explosionNumber;
        }
    }


}
