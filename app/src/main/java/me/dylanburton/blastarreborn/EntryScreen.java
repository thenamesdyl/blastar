package me.dylanburton.blastarreborn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import me.dylanburton.blastarreborn.spaceships.ShipExplosion;

public class EntryScreen extends Screen {

    private static final long ONESEC_NANOS = 1000000000L;


    private MainActivity act;
    private Paint p = new Paint();
    private Bitmap screenbtm, asteroidBmp, starbackground, grayedShip;
    private Rect scaledDst = new Rect(); // generic rect for scaling
    private Rect playBtnBounds = null;
    private Rect exitBtnBounds = null;
    private Rect scaledAsteroidDst = new Rect();
    private Rect grayedShipBounds = new Rect();
    private long lastSpawnedAsteroid = 0;

    private long lastSpedUpTime = 0;
    private float grayedShipVelocityXChange = -.1f;
    private float grayedShipDx = 0;
    private float grayedShipX = 0;
    private float grayedShipY = 0;

    private int xStretch = 0;
    private int yStretch = 0;
    private int playSubtract;
    private long startAnimationTime = 0;

    private boolean startAnimation = false;

    private List<Asteroid> asteroidList = new LinkedList<Asteroid>();

    private Random rand = new Random();
    private int randomAsteroidSpawnTime = 0;


    private long frtime = 0;
    private int width;
    private int height;
    private int realWidth;
    private int realHeight;


    public EntryScreen(MainActivity act) {
        this.act = act;
        try {
            // load screen bg
            AssetManager assetManager = act.getAssets();
            InputStream inputStream = assetManager.open("entryscreen.png");
            screenbtm = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            asteroidBmp = act.getScaledBitmap("asteroid.png");

            starbackground = act.getScaledBitmap("maps/sidescrollingstars.jpg");

            grayedShip = act.getScaledBitmap("grayedship.png");
        }
        catch (Exception e) {
            // what to do with an exception here on android?
            Log.d(MainActivity.LOG_ID, "onTouch", e);
        }
    }

    @Override
    public void update(View v) {
        //nothing to update
        if(width == 0){
            width = v.getWidth();
            height = v.getHeight();

            //this is the things that shouldnt stretch on play
            realHeight = v.getHeight();
            realWidth = v.getWidth();

            grayedShipX = width*49/100;
            grayedShipY = height*72/100;

            // initialize button locations
            playBtnBounds = new Rect(width/4,
                    height/10,
                    width*3/4,
                    height/4);
            exitBtnBounds = new Rect(width*7/10,
                    height*2/3,
                    width*8/9,
                    height*4/5);
        }

    }


    private void drawCenteredText(Canvas c, String msg, int height, Paint p, int shift) {
        c.drawText(msg, (width - p.measureText(msg)) / 2 + shift, height, p);
    }


    @Override
    public void draw(Canvas c, View v) {
        frtime = System.nanoTime();


        if(startAnimation){

            //keeps updating it by the scale factor
            playBtnBounds.set(width/4,
                    height/10,
                    width*3/4,
                    height/4);
            exitBtnBounds.set(width*7/10,
                    height*2/3,
                    width*8/9,
                    height*4/5);
        }


        if(startAnimation){
            width = width + 60;
            height = height +120;
            xStretch = xStretch- 60;
            yStretch = yStretch-65;

        }

        if(lastSpawnedAsteroid + (ONESEC_NANOS*randomAsteroidSpawnTime) < frtime){
            asteroidList.add(new Asteroid(width, height));
            lastSpawnedAsteroid = System.nanoTime();
            randomAsteroidSpawnTime = rand.nextInt(5)+1;
        }

        // draw the screen
        scaledDst.set(0, 0, width, height);
        if(startAnimation){
            scaledDst.set(xStretch, yStretch, width, height);
        }
        c.drawBitmap(starbackground,null,new Rect(0,0,realWidth,realHeight),p);

        //0 is left, 1 is right for asteroid
        for(Asteroid a: asteroidList) {

            scaledAsteroidDst.set(a.currentAsteroidX[0],a.currentAsteroidY[0],a.currentAsteroidX[1],a.currentAsteroidY[1]);
            c.drawBitmap(asteroidBmp,null,scaledAsteroidDst,p);

            if(a.lastScaleUpTime + (ONESEC_NANOS/50) < frtime) {
                if (a.randomDirection == 0) {
                    //don't think a for loop makes this any faster
                    a.currentAsteroidX[1] = a.currentAsteroidX[1] - a.smallerXScale;
                    a.currentAsteroidX[0] = a.currentAsteroidX[0] - a.largerXScale;

                    a.currentAsteroidY[1] = a.currentAsteroidY[1] + a.YScale;
                    a.currentAsteroidY[0] = a.currentAsteroidY[0] - a.YScale;

                    //lets get a move on...
                    if(startAnimation){
                        a.currentAsteroidX[1] = a.currentAsteroidX[1] - 50;
                        a.currentAsteroidX[0] = a.currentAsteroidX[0] - 70;

                        a.currentAsteroidY[1] = a.currentAsteroidY[1] + 3;
                        a.currentAsteroidY[0] = a.currentAsteroidY[0] - 3;
                    }

                } else {
                    a.currentAsteroidX[0] = a.currentAsteroidX[0] + a.smallerXScale;
                    a.currentAsteroidX[1] = a.currentAsteroidX[1] + a.largerXScale;

                    a.currentAsteroidY[1] = a.currentAsteroidY[1] + a.YScale;
                    a.currentAsteroidY[0] = a.currentAsteroidY[0] - a.YScale;

                    if(startAnimation){
                        a.currentAsteroidX[0] = a.currentAsteroidX[0] + 50;
                        a.currentAsteroidX[1] = a.currentAsteroidX[1] + 70;

                        a.currentAsteroidY[1] = a.currentAsteroidY[1] + 3;
                        a.currentAsteroidY[0] = a.currentAsteroidY[0] - 3;
                    }

                }
                a.lastScaleUpTime = System.nanoTime();
            }

          /*  if(a.currentAsteroidX[0] < 0 || a.currentAsteroidX[0] > width){
                asteroidList.remove(a);
            }*/
        }

        c.drawBitmap(screenbtm, null, scaledDst, p);


        //some might wonder how I come up with these numbers, lets just say I have the world's best eye for estimation
        grayedShipBounds.set((int) grayedShipX-(width*16/100)/2,(int) grayedShipY-(height*77/1000)/2,(int) grayedShipX + (width*16/100)/2,(int) grayedShipY + (height*77/1000)/2);
        if(startAnimation){
            grayedShipY = grayedShipY+ realHeight/28;
        }
        c.drawBitmap(grayedShip, null, grayedShipBounds, p);

        if(lastSpedUpTime + (ONESEC_NANOS/30) < frtime) {

            if(grayedShipDx > .95 || grayedShipDx < -1){
                grayedShipVelocityXChange = - grayedShipVelocityXChange;
            }

            grayedShipDx = grayedShipDx + grayedShipVelocityXChange;
            grayedShipX = grayedShipX + grayedShipDx;

            lastSpedUpTime = System.nanoTime();

        }






        // version/copyright line
        p.setColor(Color.rgb(0,70,0));  // dark greenish
        p.setTextSize(act.TS_NORMAL);
        p.setTypeface(act.getGameFont());
        String msg = "1.0";
        int xTextEnd = (int)(width*.99f);
        c.drawText(msg, xTextEnd-p.measureText(msg), height - 80, p);
        int w1 = scaledDst.width();
        msg = "(c) 2017 Dylan Burton";
        c.drawText(msg, xTextEnd-p.measureText(msg), height - 40, p);
        p.setColor(Color.rgb(255,55,55));
        p.setTextSize(200);

        p.setTextSize((playBtnBounds.right - playBtnBounds.left)*2/5);
        if(!startAnimation) {
            c.drawText("Play", realWidth*33/100, realHeight/6,p);
        }else{
            playSubtract = playSubtract + 30;
            c.drawText("Play", realWidth*33/100, realHeight/6-playSubtract,p);
        }

        p.setTextSize(act.TS_NORMAL);
        p.setColor(Color.rgb(0,0,0));
        p.setTextSize(70);
        drawCenteredText(c, "About", height*73/100,p,-width*31/100);
        p.setTextSize(70);
        drawCenteredText(c, "Exit", height*73/100,p,+width*31/100);
        p.setTextSize(300);
        p.setColor(Color.rgb(255,255,255));
        drawCenteredText(c, "Blastar", height*14/15,p,0);

        if(startAnimation && startAnimationTime + (ONESEC_NANOS/2) < frtime){
            width = 0;
            height = 0;
            startAnimation = false;
            yStretch = 0;
            xStretch = 0;
            playSubtract = 0;
            act.startLevelScreen();

        }

    }

    @Override
    public boolean onTouch(MotionEvent e) {
        if (playBtnBounds.contains((int)e.getX(), (int)e.getY())) {
            // act.startGame()
            startAnimation = true;
            startAnimationTime = System.nanoTime();
        }
        if (exitBtnBounds.contains((int)e.getX(), (int)e.getY()))
            act.exit();

        // we don't care about followup events in this screen
        return false;
    }

    private class Asteroid{
        //asteroid stuff
        private Random rand = new Random();
        private int randomDirection = 0;
        private int smallerXScale = 8;
        private int largerXScale = 15;
        private int YScale = 5;
        private long lastScaleUpTime = 0;
        private Integer currentAsteroidX[] = new Integer[2]; //0 is left x, 1 is right x
        private Integer currentAsteroidY[] = new Integer[2]; //0 is top y, 1 is bottom

        public Asteroid(int x, int y){

            randomDirection = rand.nextInt(height/2);
            for(int i = 0; i < 2; i++){
                currentAsteroidX[i] = x/2;
                currentAsteroidY[i] = randomDirection;
            }
            randomDirection = rand.nextInt(2);

        }

        public Integer[] getCurrentAsteroidX() {
            return currentAsteroidX;
        }

        public void setCurrentAsteroidX(int index, int value) {
            currentAsteroidX[index] = value;
        }

        public Integer[] getCurrentAsteroidY() {
            return currentAsteroidY;
        }

        public void setCurrentAsteroidY(int index, int value) {
            currentAsteroidY[index] = value;
        }

    }
}