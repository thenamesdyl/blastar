package me.dylanburton.blastarreborn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.dylanburton.blastarreborn.enemies.Enemy;
import me.dylanburton.blastarreborn.enemies.Fighter;
import me.dylanburton.blastarreborn.lasers.ShipLaser;
import me.dylanburton.blastarreborn.levels.Level;
import me.dylanburton.blastarreborn.levels.Level1;
import me.dylanburton.blastarreborn.spaceships.ShipExplosion;
import me.dylanburton.blastarreborn.spaceships.ShipMain;

/**
 * Represents the main screen of play for the game.
 *
 */
public class PlayScreen extends Screen {


    private MainActivity act;
    private Paint p;
    //how fast the spaceship moves backwards
    private final int DECAY_SPEED=5;
    private final int LEVEL_FIGHTER = 1;  // level where different ships are added
    private final int LEVEL_IMPERIAL = 3;
    private final int LEVEL_BATTLECRUISER = 4;
    private final int LEVEL_BATTLESHIP = 5;
    private final int LEVEL_BERSERKER = 6;
    static final long ONESEC_NANOS = 1000000000L;

    private enum State {        RUNNING, STARTROUND, STARTGAME, PLAYERDIED, WIN    }
    private volatile State gamestate = State.STARTGAME;

    //lists
    private List<Enemy> enemiesFlying = Collections.synchronizedList(new LinkedList<Enemy>());  // enemies that are still alive
    private List<ShipExplosion> shipExplosions = new LinkedList<ShipExplosion>();  // ship explosions
    private List<ShipLaser> enemyShipLasers;  // Enemy ships lasers

    //width and height of screen
    private int width = 0;
    private int height = 0;
    //bitmap with a rect used for drawing
    private Bitmap starbackground, spaceship[], spaceshipHit[], spaceshipLaser, fighter, fighterOrb, hitFighter, explosion[];
    private Rect scaledDst = new Rect();

    //main spaceship
    ShipMain shipMain;

    //used to move the background image, need two pairs of these vars for animation
    private int mapAnimatorX;
    private int mapAnimatorY;
    private int secondaryMapAnimatorX;
    private int secondaryMapAnimatorY;

    //time stuff
    private long frtime = 0; //the global time
    private long gameStartTime = 0;
    private int fps = 0;


    //various game things
    private int enemiesDestroyed = 0;
    private int minRoundPass;
    private int currentLevel;
    private int score;
    private int lives;
    private int highscore=0, highlev=1;
    private static final String HIGHSCORE_FILE = "highscore.dat";
    private static final int START_NUMLIVES = 3;
    private Map<Integer, String> levelMap = new HashMap<Integer, String>();
    private Level level;



    //some AI Movement vars, to see how it works, look in Enemy class
    private boolean startDelayReached = false;
    private Random rand = new Random();


    public PlayScreen(MainActivity act) {
        p = new Paint();
        this.act = act;
        AssetManager assetManager = act.getAssets();
        try {

            //background
            InputStream inputStream = assetManager.open("sidescrollingstars.jpg");
            starbackground = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            //your spaceship and laser
            spaceship = new Bitmap[2];
            spaceship[0] = act.getScaledBitmap("spaceship/spaceshiptopview1.png");
            spaceship[1] = act.getScaledBitmap("spaceship/spaceshiptopview2.png");
            spaceshipLaser = act.getScaledBitmap("spaceshiplaser.png");

            spaceshipHit = new Bitmap[2];
            spaceshipHit[0] = act.getScaledBitmap("spaceship/hitspaceshiptopview1.png");
            spaceshipHit[1] = act.getScaledBitmap("spaceship/hitspaceshiptopview1.png");

            //todo implement hit main spaceship into update/draw

            //fighter
            fighter = act.getScaledBitmap("fighter.png");
            hitFighter = act.getScaledBitmap("hitfighter.png");
            fighterOrb = act.getScaledBitmap("enemyorbs.png");

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

            level = new Level1(this);
            level.checkLevelSequence();


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


    public void resetGame(){

        gamestate = State.STARTROUND;
        width = 0;
        height = 0;
        enemiesFlying.clear();
        startDelayReached = false;
        for(Enemy e: enemiesFlying){
            e.setFinishedVelocityChange(false);
            e.setAIStarted(false);
        }

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
            gamestate = State.PLAYERDIED;
            try {
                BufferedWriter f = new BufferedWriter(new FileWriter(act.getFilesDir() + HIGHSCORE_FILE));
                f.write(Integer.toString(highscore)+"\n");
                f.write(Integer.toString(highlev)+"\n");
                f.close();
            } catch (Exception e) { // if we can't write the high score file...oh well.
                Log.d(MainActivity.LOG_ID, "WriteHiScore", e);
            }
        }
    }


    @Override
    public void update(View v) {
        long newtime = System.nanoTime();
        float elapsedsecs = (float) (newtime - frtime) / ONESEC_NANOS;
        frtime = newtime;
        fps = (int) (1 / elapsedsecs);

        level.checkLevelSequence();//updates level spawning enemies

        if (gamestate == State.STARTROUND) {

            initRound();
            return;
        }

        if (width == 0) {
            // set variables that rely on screen size
            width = v.getWidth();
            height = v.getHeight();

            shipMain = new ShipMain(spaceship, spaceshipLaser, (width/2), (height*2/3));

            shipMain.spawnShipLaser(shipMain.getSpaceshipX()+spaceship[0].getWidth()/8, shipMain.getSpaceshipY()+spaceship[0].getHeight()/3);
            shipMain.spawnShipLaser(shipMain.getShipLaserArray().get(0).getX() + spaceship[0].getWidth() * 64 / 100, shipMain.getSpaceshipY()+spaceship[0].getHeight()/3);
            shipMain.setLastLaserSpawnTime(System.nanoTime());

            mapAnimatorX = width;
            mapAnimatorY = height;
            secondaryMapAnimatorX=width;
            secondaryMapAnimatorY=height;
        }

        if (gamestate == State.RUNNING) {

        }


        synchronized (enemiesFlying){
            Iterator<Enemy> enemiesIterator = enemiesFlying.iterator();
            while (enemiesIterator.hasNext()) {
                Enemy e = enemiesIterator.next();

                //delay of 100 ms before enemies spawn
                if(gameStartTime + (ONESEC_NANOS/10) < frtime){
                    startDelayReached = true;
                }


                /*
                 * Firing AI
                 */
                if(e.getEnemyFiringTime()+ (e.getRandomlyGeneratedEnemyFiringTimeInSeconds()*ONESEC_NANOS) < frtime && startDelayReached){
                    e.setEnemyFiringTime(System.nanoTime());
                    e.setRandomlyGeneratedEnemyFiringTimeInSeconds((rand.nextInt(3000))/1000);
                    e.spawnShipLasers();

                }

                //updates ships laser positions
                if(e.getShipLaserPositionsList().size() > 0){
                    e.updateShipLaserPositions();
                }


                /*
                 * Movement AI
                 */

                //handles collision for multiple enemies
                for(int i = 0; i<enemiesFlying.size(); i++){
                    if((e != enemiesFlying.get(i))){
                        if((e.getX()>= enemiesFlying.get(i).getX()-enemiesFlying.get(i).getBitmap().getWidth() && e.getX()<=enemiesFlying.get(i).getX()+enemiesFlying.get(i).getBitmap().getWidth()) &&
                                (e.getY()>=enemiesFlying.get(i).getY()-enemiesFlying.get(i).getBitmap().getHeight() && e.getY() <=enemiesFlying.get(i).getY()+enemiesFlying.get(i).getBitmap().getHeight()) ) {
                            e.setVx(-e.getVx());
                        }

                    }

                }

                if(!e.isAIStarted()){
                    e.setX(rand.nextInt(width*4/5));
                    e.setY(-height/10);
                    e.setFinishedVelocityChange(true);
                    e.setAIStarted(true);
                }

                if(startDelayReached) {
                    e.setX(e.getX() + e.getVx());
                    e.setY(e.getY() + e.getVy());

                }



                if(e.isFinishedVelocityChange()){


                    e.setRandomVelocityGeneratorX((rand.nextInt(10000)+1000)/1000);
                    e.setRandomVelocityGeneratorY((rand.nextInt(10000)+1000)/1000);


                    //makes it negative if it is bigger than 5
                    if(e.getRandomVelocityGeneratorX() > 5){
                        e.setRandomVelocityGeneratorX(e.getRandomVelocityGeneratorX() - 11);
                    }


                    if(e.getRandomVelocityGeneratorY() > 5){
                        e.setRandomVelocityGeneratorY(e.getRandomVelocityGeneratorY() - 11);

                    }

                    //makes the ship change direction soon if they are in a naughty area
                    if(e.getY() > height/6){
                        if(e.getRandomVelocityGeneratorY() > 0){
                            e.setRandomVelocityGeneratorY(-e.getRandomVelocityGeneratorY());
                        }
                    }else if(e.getY() < height/12){
                        if(e.getRandomVelocityGeneratorY() < 0){
                            e.setRandomVelocityGeneratorY(-e.getRandomVelocityGeneratorY());
                        }

                    }

                    if(!e.isSlowingDown()){
                        e.setSpeedingUp(true);
                    }

                    e.setFinishedRandomGeneratorsTime(System.nanoTime());

                    //just initiating these guys
                    e.setLastSlowedDownVelocityTime(e.getFinishedRandomGeneratorsTime());
                    e.setLastSpedUpVelocityTime(e.getFinishedRandomGeneratorsTime());

                    e.setFinishedVelocityChange(false);

                }

                if (e.isSlowingDown() && (frtime > e.getLastSlowedDownVelocityTime() + (ONESEC_NANOS/100))) {
                    //obv will never be 0. Half a second for slowing down, then speeding up
                    e.setVx(e.getVx() - (e.getVx()/50));
                    e.setVy(e.getVy() - (e.getVy()/50));

                    //borders
                    if(e.getX() < 0 || e.getX() > width*4/5){
                        //this check disables the ability for ship to get too far and then freeze in place
                        if(e.getX() < 0){
                            e.setX(0);
                        }else if (e.getX() > width*4/5){
                            e.setX(width*4/5);
                        }

                        e.setVx(-e.getVx());
                        e.setRandomVelocityGeneratorX(-e.getRandomVelocityGeneratorX());
                    }

                    //so we do this
                    if( (e.getVx() > -1 && e.getVx() < 1) &&  (e.getVy() > -1 && e.getVy() < 1) ){
                        e.setSlowingDown(false);
                        e.setSpeedingUp(true);

                    }
                    //delays this slowing down process a little
                    e.setLastSlowedDownVelocityTime(System.nanoTime());

                }else if(e.isSpeedingUp() && (frtime > e.getLastSpedUpVelocityTime() + (ONESEC_NANOS/100))){


                    //will not have asymptotes like the last one
                    e.setVx(e.getVx() + (e.getRandomVelocityGeneratorX()/50));
                    e.setVy(e.getVy() + (e.getRandomVelocityGeneratorY() /50));

                    //borders for x and y
                    if(e.getX() < 0 || e.getX() > width*4/5){
                        //this check disables the ability for ship to get too far and then freeze in place
                        if(e.getX() < 0){
                            e.setX(0);
                        }else if (e.getX() > width*4/5){
                            e.setX(width*4/5);
                        }

                        e.setVx(-e.getVx());
                        e.setRandomVelocityGeneratorX(-e.getRandomVelocityGeneratorX());
                    }

                    //just adding a margin of error regardless though, if the nanoseconds were slightly off it would not work
                    if( (e.getVx() > e.getRandomVelocityGeneratorX()-1 && e.getVx() < e.getRandomVelocityGeneratorX()+1) && (e.getVy() > e.getRandomVelocityGeneratorY() -1 || e.getVy() < e.getRandomVelocityGeneratorY() +1)){
                        e.setSlowingDown(true);
                        e.setSpeedingUp(false);
                        e.setFinishedVelocityChange(true);
                    }

                    e.setLastSpedUpVelocityTime(System.nanoTime());
                }


            }

        }




        //spaceship decay
        if(shipMain.getSpaceshipY()<height*9/10 && !shipMain.isSpaceshipMoving()){
            shipMain.setSpaceshipY(shipMain.getSpaceshipY()+ DECAY_SPEED);
        }

        //makes main spaceship lasers
        for(int i = 0; i < shipMain.getShipLaserArray().size(); i++) {
            shipMain.getShipLaserArray().get(i).updateMainShipLaserPositions();
            if (shipMain.getLastLaserSpawnTime() + (ONESEC_NANOS/2) < frtime) {

                shipMain.spawnShipLaser(shipMain.getSpaceshipX()+spaceship[0].getWidth()/8, shipMain.getSpaceshipY()+spaceship[0].getHeight()/3);
                shipMain.spawnShipLaser(shipMain.getShipLaserArray().get(shipMain.getShipLaserArray().size()-1).getX() + spaceship[0].getWidth() * 64 / 100, shipMain.getSpaceshipY()+spaceship[0].getHeight()/3);

                shipMain.setLastLaserSpawnTime(System.nanoTime());
            }
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

                    if(startDelayReached) {

                        //drawing enemy lasers
                        if(e.getShipLaserPositionsList().size() > 0){
                            this.enemyShipLasers = e.getShipLaserPositionsList();
                            for(ShipLaser sl: enemyShipLasers){
                                c.drawBitmap(sl.getBmp(), sl.getX(), sl.getY(), p);
                            }
                        }


                        //puts like a red tinge on the enemy for 100 ms if hes hit
                        if(e.isEnemyHitButNotDead()){
                            c.drawBitmap(hitFighter, e.getX(), e.getY(), p);

                            if(e.getHitContactTimeForTinge() + (ONESEC_NANOS/10) <frtime){
                                e.setEnemyIsHitButNotDead(false);
                            }

                        }else {
                            c.drawBitmap(e.getBitmap(), e.getX(), e.getY(), p);
                        }

                        for(int i = 0; i < shipMain.getShipLaserArray().size(); i++) {
                            if ((e.hasCollision(shipMain.getShipLaserArray().get(i).getX(), shipMain.getShipLaserArray().get(i).getY()))) {
                                shipMain.getShipLaserArray().get(i).setX(4000);
                                shipMain.getShipLaserArray().remove(i);
                                e.setHitContactTimeForTinge(System.nanoTime());
                                e.setHitContactTimeForExplosions(System.nanoTime());
                                //subtract a life
                                e.setLives(e.getLives() - 1);

                                //fun explosions
                                if (e.getLives() == 0) {
                                    shipExplosions.add(new ShipExplosion(e.getX() - e.getBitmap().getWidth() * 3 / 4, e.getY() - e.getBitmap().getHeight() / 2, e));
                                    //bye bye
                                    e.setX(10000);
                                    e.setY(10000);
                                    enemiesDestroyed++;

                                    e.setExplosionActivateTime(System.nanoTime()); //used to delay deletion so orbs dont disappear
                                } else {
                                    e.setEnemyIsHitButNotDead(true);
                                }


                            }
                        }

                        for(ShipExplosion se: shipExplosions) {
                            if (se.getEnemy() == e) {
                                c.drawBitmap(explosion[se.getCurrentFrame()], se.getX(), se.getY(), p);

                                //semi-clever way of adding a very precise delay (yes, I am scratching my own ass)
                                if (e.getHitContactTimeForExplosions() + (ONESEC_NANOS / 20) < frtime) {
                                    e.setHitContactTimeForExplosions(System.nanoTime());
                                    se.nextFrame();

                                }

                                if (se.getCurrentFrame() == 11) {
                                    shipExplosions.remove(se);
                                }
                            }

                            //deletes ship
                            if (e.getExplosionActivateTime() + (ONESEC_NANOS * 10) < frtime && e.getLives() == 0) {
                                enemiesFlying.remove(e);
                            }
                        }
                    }
                }
            }


            //main spaceship lasers
            for (int i = 0; i < shipMain.getShipLaserArray().size(); i++) {
                c.drawBitmap(spaceshipLaser, shipMain.getShipLaserArray().get(i).getX(), shipMain.getShipLaserArray().get(i).getY(), p);
            }
            //main spaceship
            for(int i = 0; i<spaceship.length; i++) {
                if(i == shipMain.getCurrentSpaceshipFrame() && frtime>shipMain.getSpaceshipFrameSwitchTime() + (ONESEC_NANOS/10)) {
                    if(shipMain.getCurrentSpaceshipFrame() == spaceship.length-1){
                        shipMain.setCurrentSpaceshipFrame(0);
                    }else{
                        shipMain.setCurrentSpaceshipFrame(shipMain.getCurrentSpaceshipFrame()+1);
                    }
                    c.drawBitmap(spaceship[i], shipMain.getSpaceshipX(), shipMain.getSpaceshipY(), p);

                    shipMain.setSpaceshipFrameSwitchTime(System.nanoTime());

                }else if(i==shipMain.getCurrentSpaceshipFrame()){
                    c.drawBitmap(spaceship[i], shipMain.getSpaceshipX(), shipMain.getSpaceshipY(), p);
                }
            }

            p.setColor(Color.WHITE);
            p.setTextSize(act.TS_NORMAL);
            p.setTypeface(act.getGameFont());

            if (score >= highscore) {
                highscore = score;
                highlev = currentLevel;
            }


            if (gamestate == State.WIN || gamestate == State.PLAYERDIED) {
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

    public void playerWon(){
        gamestate = State.WIN;
    }

    public void spawnFighter(){
        enemiesFlying.add(new Fighter(fighter, fighterOrb));
    }

    public int getEnemiesDestroyed() {
        return enemiesDestroyed;
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
                    shipMain.setSpaceshipBounds(new Rect((int) shipMain.getSpaceshipX(),(int) shipMain.getSpaceshipY(),(int) shipMain.getSpaceshipX()+spaceship[0].getWidth(),(int) shipMain.getSpaceshipY()+spaceship[0].getHeight()));

                    if(shipMain.getSpaceshipBounds().contains((int) e.getX(),(int) e.getY())){
                        shipMain.setSpaceshipIsMoving(true);
                        shipMain.setSpaceshipX(e.getX()-spaceship[0].getWidth()/2);
                        shipMain.setSpaceshipY(e.getY()-spaceship[0].getHeight()/2);

                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                shipMain.setSpaceshipIsMoving(false);

                break;
        }

        return true;
    }



}
