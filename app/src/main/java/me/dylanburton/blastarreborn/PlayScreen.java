package me.dylanburton.blastarreborn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
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

/**
 * Represents the main screen of play for the game.
 *
 */
public class PlayScreen extends Screen {


    private MainActivity act;
    private Paint p;
    //how fast the spaceship moves backwards
    private int decay_speed=1;

    private enum State {        RUNNING, STARTROUND, ROUNDSUMMARY, STARTGAME, PLAYERDIED, GAMEOVER    }
    private volatile State gamestate = State.STARTGAME;

    private int width = 0;
    private int height = 0;


    private Bitmap starbackground, spaceship, fighter[];
    private Rect scaledDst = new Rect();

    private int minRoundPass;
    private int round;
    private int score;
    private int lives;
    private int highscore=0, highlev=1;
    private static final String HIGHSCORE_FILE = "highscore.dat";
    private static final int START_NUMLIVES = 3;
    private Map<Integer, String> levelMap = new HashMap<Integer, String>();
    private final int LEVEL_FIGHTER = 1;  // level where different ships are added
    private final int LEVEL_IMPERIAL = 3;
    private final int LEVEL_BATTLECRUISER = 4;
    private final int LEVEL_BATTLESHIP = 5;
    private final int LEVEL_BERSERKER = 6;


    public PlayScreen(MainActivity act) {
        p = new Paint();
        this.act = act;
        AssetManager assetManager = act.getAssets();
        try {
            // wall
            InputStream inputStream = assetManager.open("sidescrollingstars.jpg");
            starbackground = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            //your spaceship
            spaceship = act.getScaledBitmap("spaceshiptopview");

            //fighter, making it an array in case I want to add multiple states of the ship
            fighter = new Bitmap[0];
          //  fighter[0]=act.getScaledBitmap("");

            p.setTypeface(act.getGameFont());
            round = 1;

        } catch (IOException e) {
            Log.d(act.LOG_ID, "why tho?", e);
        }
    }

    /**
     * initialize and start a game
     */
    void initGame() {
        score = 0;
        round = 1;
        lives = START_NUMLIVES;
        highscore = 0;

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
        if (round == 1)
            minRoundPass = 10;
        else if (round < 4)
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



    @Override
    public void update(View v) {


        if (gamestate == State.STARTROUND) {

            initRound();
            return;
        }

        if (width == 0) {
            // set variables that rely on screen size
            width = v.getWidth();
            height = v.getHeight();

        }

        if (gamestate == State.RUNNING) {

        }

        //need a place to update enemy positions, needs some sort of AI
        //need to update background stars which will be moving
        //need to make sure ship is decaying


    }


    @Override
    public void draw(Canvas c, View v) {
        try {
            // actually draw the screen
            scaledDst.set(0, 0, width, height);
            c.drawBitmap(starbackground, null, scaledDst, p);

            c.drawBitmap(spaceship, null, new Rect(width/2-spaceship.getWidth()/2, height/2-spaceship.getHeight()/2, width/2+spaceship.getWidth(), height/2+spaceship.getHeight()/2),p);
            //c.drawBitmap(fighter, null, scaledDst, p);

            p.setColor(Color.WHITE);
            p.setTextSize(act.TS_NORMAL);
            p.setTypeface(act.getGameFont());

            if (score >= highscore) {
                highscore = score;
                highlev = round;
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
                p.setTextSize(act.TS_BIG);
                p.setColor(Color.RED);
                drawCenteredText(c, "GamE oVeR!", height /2, p, -2);
                drawCenteredText(c, "Touch to end game", height * 4 /5, p, -2);
                p.setColor(Color.WHITE);
                drawCenteredText(c, "GamE oVeR!", height /2, p, 0);
                drawCenteredText(c, "Touch to end game", height * 4 /5, p, 0);
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
                if (gamestate == State.ROUNDSUMMARY
                        || gamestate == State.STARTGAME
                        || gamestate == State.PLAYERDIED) {
                    gamestate = State.STARTROUND; // prep and start round
                    return false; // no followup msgs
                }
                else if (gamestate == State.GAMEOVER) {
                    act.leaveGame(); // user touched after gameover -> back to entry screen
                    return false;  // no followup msgs
                }

                break;

            case MotionEvent.ACTION_MOVE:

                break;

            case MotionEvent.ACTION_UP:

                break;
        }

        return true;
    }

    /**
     * An enemy is a template for all the enemies     */
    private class Enemy {
        int points; // points this type of enemy is worth when destroyed
        Bitmap btm[];
        float width=0; // width onscreen
        float height=0;  // height onscreen
        float halfWidth = 0;  // convenience
        float halfHeight = 0;
        final float HALF_DIVISOR = 1.9f;  //changing the dimensions to be consistent

        public Enemy(Bitmap bitmaps[], int points) {
            this.btm = bitmaps;
            this.width = bitmaps[0].getWidth();
            this.height = bitmaps[0].getHeight();
            this.halfWidth = width/HALF_DIVISOR;
            this.halfHeight = height/HALF_DIVISOR;
            this.points = points;
        }
    }


}
