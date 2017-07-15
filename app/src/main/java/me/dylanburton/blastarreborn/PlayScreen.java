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
    //width and height of screen
    private int width = 0;
    private int height = 0;
    private int MIN_HEIGHT;

    //bitmap with a rect used for drawing


    private Bitmap starbackground, spaceship, spaceshipLaser, fighter, explosion[];
    private Rect scaledDst = new Rect();

    //main spaceships location and bound
    private int spaceshipY;
    private int spaceshipX;
    private Rect spaceshipBounds;
    private boolean spaceshipIsMoving;
    private int spaceshipLaserX;
    private int spaceshipLaserY;
    //fighter speed, a temp var that will be erased when movement behavior is made competent
    private int fighterSpeed = 5;
    private int enemySpeed = 4;

    //used to move the background image, need two pairs of these vars for animation
    private int mapAnimatorX;
    private int mapAnimatorY;
    private int secondaryMapAnimatorX;
    private int secondaryMapAnimatorY;

    //time stuff
    Timer timer = new Timer();
    private long frtime = 0;
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

        score = 0;
        currentLevel = 1;
        lives = START_NUMLIVES;
        highscore = 0;

        if(currentLevel == 1){
            enemiesFlying.add(new Enemy(fighter,5));
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
            // game over!  wrap things up and write hi score file
            gamestate = State.GAMEOVER;
            try {
                BufferedWriter f = new BufferedWriter(new FileWriter(act.getFilesDir() + HIGHSCORE_FILE));
                f.write(Integer.toString(highscore)+"\n");
                f.write(Integer.toString(highlev)+"\n");
                f.close();
            } catch (Exception e) { // if we can't write the hi score file...oh well.
                Log.d(MainActivity.LOG_ID, "WriteHiScore", e);
            }
        } else
            gamestate = State.PLAYERDIED;
    }

    public void playExplosionAnimation(float x, float y, Canvas c, int recursionCounter){
        c.drawBitmap(explosion[recursionCounter],x,y,p);
        if(recursionCounter<11){
            playExplosionAnimation(x,y,c,recursionCounter+1);
        }

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

            spaceshipX = width/2;
            spaceshipY = height*2/3;

            spaceshipLaserX = spaceshipX+spaceship.getWidth()/8;
            spaceshipLaserY = spaceshipY+spaceship.getHeight()/3;

            mapAnimatorX = width;
            mapAnimatorY = height;
            secondaryMapAnimatorX=width;
            secondaryMapAnimatorY=height;

        }

        if (gamestate == State.RUNNING) {

        }


        //need a place to update enemy positions, needs some sort of AI

        synchronized (enemiesFlying){
            Iterator<Enemy> enemiesIterator = enemiesFlying.iterator();
            while (enemiesIterator.hasNext()) {
                Enemy e = enemiesIterator.next();


                //this needs to be replaced with some sort of competent movement behavior.
                e.x += enemySpeed;
                if(e.x >=width*4/5 || e.x <= 0){
                    enemySpeed = -enemySpeed;
                }
            }

        }

        //spaceship decay
        if(spaceshipY < MIN_HEIGHT && !spaceshipIsMoving) {
            spaceshipY += DECAY_SPEED;
        }

        //resets spaceship laser
        spaceshipLaserY -= 20.0f;
        if(spaceshipLaserY < -150){
            spaceshipLaserY = spaceshipY+spaceship.getHeight()/3;
            spaceshipLaserX = spaceshipX+spaceship.getWidth()/8;
        }

        //animator for map background
        mapAnimatorY+=2.0f;
        secondaryMapAnimatorY+=2.0f;
        //this means the stars are off the screen
        if(mapAnimatorY>=height*2){
            mapAnimatorY = height;
        }else if(secondaryMapAnimatorY>=height*2){
            secondaryMapAnimatorY = height;
        }
    }


    @Override
    public void draw(Canvas c, View v) {
        try {

            // actually draw the screen
            scaledDst.set(mapAnimatorX-width, mapAnimatorY-height, mapAnimatorX, mapAnimatorY);
            c.drawBitmap(starbackground,null,scaledDst,p);
            //secondary background for animation. Same as last draw, but instead, these are a height-length higher
            c.drawBitmap(starbackground,null,new Rect(secondaryMapAnimatorX-width, secondaryMapAnimatorY-(height*2),secondaryMapAnimatorX, secondaryMapAnimatorY-height),p);


            synchronized (enemiesFlying) {
                for(Enemy e: enemiesFlying) {
                    c.drawBitmap(e.getBitmap(), e.x, e.y, p);

                    if(e.hasCollision(spaceshipLaserX, spaceshipLaserY)|| e.hasCollision(spaceshipLaserX+spaceship.getWidth()*64/100, spaceshipLaserY)){
                        spaceshipLaserX = 4000;
                        enemiesFlying.remove(e);
                        playExplosionAnimation(e.x+e.getBitmap().getWidth()/2,e.y+e.getBitmap().getHeight()/2,c,0);


                    }
                }
            }

            synchronized (spaceship) {
                //main spaceship stuff
                c.drawBitmap(spaceshipLaser, spaceshipLaserX, spaceshipLaserY, p);
                c.drawBitmap(spaceshipLaser, spaceshipLaserX + spaceship.getWidth() * 64 / 100, spaceshipLaserY, p);
                c.drawBitmap(spaceship, spaceshipX, spaceshipY, p);
            }

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
        double vx = .1;
        double vy = .1;
        int points;
        float width=0; // width onscreen
        float height=0;  // height onscreen
        float halfWidth = 0;  // convenience
        float halfHeight = 0;
        final float HALF_DIVISOR = 1.9f;  //changing the dimensions to be consistent

        Rect bounds = new Rect();

        public Enemy(Bitmap bitmap, int points) {
            this.btm = bitmap;
            this.width = bitmap.getWidth();
            this.height = bitmap.getHeight();
            this.halfWidth = width/HALF_DIVISOR;
            this.halfHeight = height/HALF_DIVISOR;
            this.points = points;
        }

        public Bitmap getBitmap(){
            return btm;
        }
        public boolean hasCollision(float collx, float colly) {
            return getBounds().contains((int) collx, (int) colly);
        }
        public Rect getBounds() {
            bounds.set((int)(this.x - getBitmap().getWidth()/2), (int)(this.y-getBitmap().getHeight()/2),
                    (int)(this.x+getBitmap().getWidth()/2), (int)(this.y+getBitmap().getHeight()/2));
            return bounds;
        }

    }


}
