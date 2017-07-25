package me.dylanburton.blastarreborn;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by Dylan on 7/19/2017.
 */

public class LevelScreen extends Screen {
    private PlayScreen ps;
    private Paint p = new Paint();
    private MainActivity act;
    private int width = 0; //width and height used for manipulation
    private int height = 0;

    private int realWidth = 0;
    private int realHeight = 0;
    private Rect scaledDst = new Rect(); // generic rect for scaling
    private Rect levelScreenBounds = new Rect();

    private boolean startZoomAnimation;

    private static final String HIGHSCORE_FILE = "scoredata.dat";

    private boolean[] levelFinished = new boolean[6];
    private int[] starsEarned = new int[6];
    private int[] levelScreenCoords = new int[4];

    private Rect[] level = new Rect[6];

    private int levelSelectionWidth = 0;//the width of level selection
    private int levelSelectionHeight = 0;//the height of level selection
    private Bitmap levelSelection, starbackground, oneStar, twoStar, threeStar, lockedLevel, unlockedLevel;

    public LevelScreen(PlayScreen ps, MainActivity act){
        this.ps = ps;
        this.act = act;

        try {

            levelSelection = act.getScaledBitmap("levelselection/panel_portrait.png");


            starbackground = act.getScaledBitmap("maps/sidescrollingstars.jpg");

            //level stuff
            oneStar = act.getScaledBitmap("levelselection/onestar.png");
            twoStar = act.getScaledBitmap("levelselection/twostar.png");
            threeStar = act.getScaledBitmap("levelselection/threestar.png");
            lockedLevel = act.getScaledBitmap("levelselection/locked.png");
            unlockedLevel = act.getScaledBitmap("levelselection/unlocked.png");


        }catch(Exception e){

        }
    }

    public void resetVariables(){
        height = 0;
        width = 0;
    }

    public void update(View v){

        if(width == 0){
            width = v.getWidth();
            height = v.getHeight();

            realWidth = v.getWidth();
            realHeight = v.getHeight();

            startZoomAnimation = true;
            levelScreenCoords[0] = width/2;
            levelScreenCoords[1] = height/2;
            levelScreenCoords[2] = width/2;
            levelScreenCoords[3] = height/2;

            //checking the file for scores
            try{
                BufferedReader f = new BufferedReader(new FileReader(act.getFilesDir() + HIGHSCORE_FILE));
                String receiveString = "";
                int currentLevel = -1;
                for(int row = 0; row < 3; row++){
                    for(int column = 0; column < 2; column++){
                        currentLevel++;
                        if((receiveString = f.readLine())!= null) {
                            levelFinished[currentLevel] = Boolean.parseBoolean(receiveString);
                            starsEarned[currentLevel] = Integer.parseInt(f.readLine());
                        }else{
                            f.readLine();
                            levelFinished[currentLevel] = false;
                            starsEarned[currentLevel] = 0;
                        }
                    }
                }

                f.close();
            } catch (Exception e) {
                Log.d(MainActivity.LOG_ID, "ReadHighScore", e);
            }
        }

    }

    public void draw(Canvas c, View v){

        scaledDst.set(0,0,realWidth, realHeight);
        c.drawBitmap(starbackground,null,scaledDst,p);
        levelScreenBounds.set(levelScreenCoords[0], levelScreenCoords[1], levelScreenCoords[2], levelScreenCoords[3]);
        if(startZoomAnimation){
            levelScreenCoords[0] -= 10;
            levelScreenCoords[1] -= 16;
            levelScreenCoords[2] += 10;
            levelScreenCoords[3] += 16;

            if(levelScreenCoords[0] < width/20){
                startZoomAnimation = false;
            }

            levelSelectionWidth = levelScreenCoords[2] - levelScreenCoords[0];
            levelSelectionHeight = levelScreenCoords[3] - levelScreenCoords[1];

            //change bounds during zoom animation, afterwords it stays the same
            level[0] = new Rect(levelScreenCoords[0] + levelSelectionWidth*25/100,
                    levelScreenCoords[1] + levelSelectionHeight*17/100,
                    levelScreenCoords[0] + levelSelectionWidth*41/100,
                    levelScreenCoords[1] + levelSelectionHeight*33/100);
            level[1] = new Rect(levelScreenCoords[0] + levelSelectionWidth*58/100,
                    levelScreenCoords[1] + levelSelectionHeight*17/100,
                    levelScreenCoords[0] + levelSelectionWidth*74/100,
                    levelScreenCoords[1] + levelSelectionHeight*33/100);
            level[2] = new Rect(levelScreenCoords[0] + levelSelectionWidth*25/100,
                    levelScreenCoords[1] + levelSelectionHeight*42/100,
                    levelScreenCoords[0] + levelSelectionWidth*41/100,
                    levelScreenCoords[1] + levelSelectionHeight*58/100);
            level[3] = new Rect(levelScreenCoords[0] + levelSelectionWidth*58/100,
                    levelScreenCoords[1] + levelSelectionHeight*42/100,
                    levelScreenCoords[0] + levelSelectionWidth*74/100,
                    levelScreenCoords[1] + levelSelectionHeight*58/100);
            level[4] = new Rect(levelScreenCoords[0] + levelSelectionWidth*25/100,
                    levelScreenCoords[1] + levelSelectionHeight*67/100,
                    levelScreenCoords[0] + levelSelectionWidth*41/100,
                    levelScreenCoords[1] + levelSelectionHeight*83/100);
            level[5] = new Rect(levelScreenCoords[0] + levelSelectionWidth*58/100,
                    levelScreenCoords[1] + levelSelectionHeight*67/100,
                    levelScreenCoords[0] + levelSelectionWidth*74/100,
                    levelScreenCoords[1] + levelSelectionHeight*83/100);

        }
        c.drawBitmap(levelSelection, null, levelScreenBounds, p);



        p.setColor(Color.WHITE);
        p.setTypeface(act.getLevelFont());
        int currentLevel = -1;
        for(int row = 0; row < 3; row ++){
            for(int column = 0; column < 2; column++){
                currentLevel++;
                if(levelFinished[currentLevel]){
                    if(starsEarned[currentLevel] == 3){
                        //draws three stars on the current levels bounds
                        c.drawBitmap(threeStar, null, level[currentLevel], p);
                    }else if(starsEarned[currentLevel] == 2){

                        c.drawBitmap(twoStar, null, level[currentLevel], p);
                    }else if(starsEarned[currentLevel] == 1){

                        c.drawBitmap(oneStar, null, level[currentLevel], p);
                    }
                }else{
                    //draws locked level if its not completed
                    if(currentLevel == 0 || levelFinished[currentLevel-1]) {
                        c.drawBitmap(unlockedLevel, null, level[currentLevel], p);
                    }else{
                        c.drawBitmap(lockedLevel, null, level[currentLevel], p);
                    }
                }

                p.setTextSize((level[currentLevel].bottom - level[currentLevel].top)/2);

                int levelWidth = level[currentLevel].right - level[currentLevel].left;
                int levelHeight = level[currentLevel].bottom - level[currentLevel].top;
                c.drawText("" + (currentLevel+1), level[currentLevel].centerX()-levelWidth/5, level[currentLevel].centerY()+levelHeight/10, p);

            }
        }

    }


    @Override
    public boolean onTouch(MotionEvent e) {

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (level[0].contains((int) e.getX(), (int) e.getY())) {
                    resetVariables();
                    act.startGame(1);
                }else if (level[1].contains((int) e.getX(), (int) e.getY()) && levelFinished[0]) {
                    resetVariables();
                    act.startGame(2);
                }else if (level[2].contains((int) e.getX(), (int) e.getY()) && levelFinished[1]) {
                    resetVariables();
                    act.startGame(3);
                }else if (level[3].contains((int) e.getX(), (int) e.getY()) && levelFinished[2]) {
                    resetVariables();
                    act.startGame(4);
                }else if (level[4].contains((int) e.getX(), (int) e.getY()) && levelFinished[3]) {
                    resetVariables();
                    act.startGame(5);
                }else if (level[5].contains((int) e.getX(), (int) e.getY()) && levelFinished[4]) {
                    resetVariables();
                    act.startGame(6);
                }

                break;

            case MotionEvent.ACTION_MOVE:


                break;

            case MotionEvent.ACTION_UP:


                break;
        }


        return true;
    }
}
