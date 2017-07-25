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

        import me.dylanburton.blastarreborn.enemies.Battlecruiser;
        import me.dylanburton.blastarreborn.enemies.Battleship;
        import me.dylanburton.blastarreborn.enemies.Berserker;
        import me.dylanburton.blastarreborn.enemies.Enemy;
        import me.dylanburton.blastarreborn.enemies.EnemyType;
        import me.dylanburton.blastarreborn.enemies.Fighter;
        import me.dylanburton.blastarreborn.enemies.Imperial;
        import me.dylanburton.blastarreborn.lasers.DiagonalLaser;
        import me.dylanburton.blastarreborn.lasers.MainShipLaser;
        import me.dylanburton.blastarreborn.lasers.ShipLaser;
        import me.dylanburton.blastarreborn.levels.Level;
        import me.dylanburton.blastarreborn.levels.Level1;
        import me.dylanburton.blastarreborn.levels.Level2;
        import me.dylanburton.blastarreborn.levels.Level3;
        import me.dylanburton.blastarreborn.levels.Level4;
        import me.dylanburton.blastarreborn.levels.Level5;
        import me.dylanburton.blastarreborn.levels.Level6;
        import me.dylanburton.blastarreborn.spaceships.PlayerShip;
        import me.dylanburton.blastarreborn.spaceships.ShipExplosion;

/**
 * Represents the main screen of play for the game.
 *
 */
public class PlayScreen extends Screen {


    private MainActivity act;
    private Paint p;
    //how fast the spaceship moves backwards
    private static final int DECAY_SPEED=5;
    private static final long ONESEC_NANOS = 1000000000L;

    private enum State {        RUNNING, STARTGAME, PLAYERDIED, WIN    }
    private volatile State gamestate = State.STARTGAME;

    //lists
    private List<Enemy> enemiesFlying = Collections.synchronizedList(new LinkedList<Enemy>());  // enemies that are still alive
    private List<ShipExplosion> shipExplosions = new LinkedList<ShipExplosion>();  // ship explosions

    //width and height of screen
    private int width = 0;
    private int height = 0;
    //bitmap with a rect used for drawing
    private Bitmap starbackground, spaceship[], spaceshipHit[], spaceshipLaser, fighter, fighterOrb, fighterHit, explosion[], gameOverOverlay, playerDiedText, playerWonText,filledstar,emptystar;
    private Bitmap imperial, imperialHit, berserker, berserkerHit, berserkerReverse, battlecruiser, battlecruiserHit, battleship, battleshipHit;
    private Rect scaledDst = new Rect();

    //main spaceship
    PlayerShip playerShip;

    //used to move the background image, need two pairs of these vars for animation
    private int mapAnimatorX;
    private int mapAnimatorY;
    private int secondaryMapAnimatorX;
    private int secondaryMapAnimatorY;

    //time stuff
    private long frtime = 0; //the global time
    private long firstStarTimeCheck = 0;
    private long secondStarTimeCheck = 0;
    private long gameStartTime = 0;
    private float elapsedSecs;
    private int fps = 0;

    //various game things
    private int enemiesDestroyed = 0;
    private int minRoundPass;
    private int currentLevel;
    private int score;
    private int lives;
    private static final String HIGHSCORE_FILE = "scoredata.dat";
    private static final int START_NUMLIVES = 5;
    private Map<Integer, String> levelMap = new HashMap<Integer, String>();
    private Level level;
    private List<ShipLaser> shipLasers = new LinkedList<ShipLaser>();

    private int livesPercentage; //for lives counter

    private int starsEarned = 0;
    private int starsEarnedFile = 0;
    private boolean levelCompleted = false;

    private String[] receivingInfo = new String[12];



    //some AI Movement vars, to see how it works, look in Enemy class
    private boolean startDelayReached = false;
    private float newBerserkerVelocityX = 0;
    private float newBerserkerVelocityY = 0;
    private float differenceBerserkerVelocityX;
    private float differenceBerserkerVelocityY;
    private Random rand = new Random();


    public PlayScreen(MainActivity act) {
        p = new Paint();
        this.act = act;
        AssetManager assetManager = act.getAssets();
        try {

            //background
            InputStream inputStream = assetManager.open("maps/sidescrollingstars.jpg");
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

            //enemies
            fighter = act.getScaledBitmap("enemies/fighter.png");
            fighterHit= act.getScaledBitmap("enemies/fighterhit.png");
            fighterOrb = act.getScaledBitmap("enemies/fighterorbs.png");

            imperial = act.getScaledBitmap("enemies/imperial.png");
            imperialHit = act.getScaledBitmap("enemies/imperialhit.png");

            berserker = act.getScaledBitmap("enemies/berserker.png");
            berserkerHit = act.getScaledBitmap("enemies/berserkerhit.png");
            berserkerReverse = act.getScaledBitmap("enemies/berserkerreverse.png");

            battlecruiser = act.getScaledBitmap("enemies/battlecruiser.png");
            battlecruiserHit = act.getScaledBitmap("enemies/battlecruiserhit.png");

            battleship = act.getScaledBitmap("enemies/battleship.png");
            battleshipHit = act.getScaledBitmap("enemies/battleshiphit.png");




            //explosion
            explosion = new Bitmap[12];
            for(int i = 0; i < 12; i++) {
                explosion[i] = act.getScaledBitmap("explosion/explosion"+(i+1)+".png");
            }

            //game over stuff
            gameOverOverlay = act.getScaledBitmap("slightlytransparentoverlay.png");
            playerDiedText = act.getScaledBitmap("playerdiedtext.png");
            playerWonText = act.getScaledBitmap("playerwontext.png");
            filledstar = act.getScaledBitmap("filledstar.png");
            emptystar = act.getScaledBitmap("emptystar.png");

            p.setTypeface(act.getGameFont());
            currentLevel = 1;

        } catch (IOException e) {
            Log.d(act.LOG_ID, "why tho?", e);
        }
    }

    /**
     * initialize and start a game
     */
    void initGame(int currentLevel) {

        //used for slight delays on spawning things at the beginning
        gameStartTime = System.nanoTime();
        score = 0;
        this.currentLevel = currentLevel;
        lives = START_NUMLIVES;

        if(currentLevel == 1){

            level = new Level1(this, act);
            level.checkLevelSequence();


        }else if(currentLevel == 2){

            level = new Level2(this, act);
            level.checkLevelSequence();

        }else if(currentLevel == 3){

            level = new Level3(this, act);
            level.checkLevelSequence();

        }else if(currentLevel == 4){

            level = new Level4(this, act);
            level.checkLevelSequence();

        }else if(currentLevel == 5){

            level = new Level5(this, act);
            level.checkLevelSequence();

        }else if(currentLevel == 6){

            level = new Level6(this, act);
            level.checkLevelSequence();

        }

        gamestate = State.RUNNING;

        try {
            BufferedReader f = new BufferedReader(new FileReader(act.getFilesDir() + HIGHSCORE_FILE));
            String receiveString = "";


            //gets us to the right place
            for (int i = 0; i < (currentLevel-1)*2; i++){
                receivingInfo[i] = f.readLine();
            }

            f.readLine();//getting past level completed boolean
            if((receiveString = f.readLine())!= null) {
                starsEarnedFile = Integer.parseInt(receiveString);
            }

        }catch (Exception e){

        }


    }


    public void resetGame(){

        gamestate = State.STARTGAME;
        width = 0;
        height = 0;
        enemiesFlying.clear();
        shipLasers.clear();
        shipExplosions.clear();
        startDelayReached = false;
        for(Enemy e: enemiesFlying){
            e.setFinishedVelocityChange(false);
            e.setAIStarted(false);
        }
        enemiesDestroyed = 0;
        level.setUpdateCheckerBoundary(0);
        firstStarTimeCheck = 0;
        secondStarTimeCheck = 0;

    }

    /**
     * player lost a life
     */
    private void loseLife() {
        lives--;

        if (lives <= 0) {
            if(!playerShip.isEndOfTheRoad()) {
                playerShip.setShipExplosionActivateTime(System.nanoTime());
                shipExplosions.add(new ShipExplosion(playerShip.getX(), playerShip.getY(), playerShip));
                playerShip.setEndOfTheRoad(true);
            }

            //the rest of this will happen when explosion is completed in draw
        }
    }

    public void addEnemyExplosion(Enemy e){
        if(e.getEnemyType() == EnemyType.FIGHTER) {
            shipExplosions.add(new ShipExplosion(e.getX() - e.getBitmap().getWidth() * 3 / 4, e.getY() - e.getBitmap().getHeight() / 2, e));
        }else if(e.getEnemyType() == EnemyType.BERSERKER){
            shipExplosions.add(new ShipExplosion(e.getX() + e.getBitmap().getWidth()/3, e.getY() + e.getBitmap().getHeight()/3,e));
        }
        e.setX(10000);
        e.setAIDisabled(true);
        enemiesDestroyed++;

        e.setExplosionActivateTime(System.nanoTime());
    }


    @Override
    public void update(View v) {
        long newtime = System.nanoTime();
        elapsedSecs = (float) (newtime - frtime) / ONESEC_NANOS;
        frtime = newtime;
        fps = (int) (1 / elapsedSecs);

        level.checkLevelSequence();//updates level spawning enemies

        if (width == 0) {
            // set variables that rely on screen size
            width = v.getWidth();
            height = v.getHeight();

            playerShip = new PlayerShip(spaceship, spaceshipLaser, (width/2), (height*2/3));

            mapAnimatorX = width;
            mapAnimatorY = height;
            secondaryMapAnimatorX=width;
            secondaryMapAnimatorY=height;

        }

        if (gamestate == State.RUNNING ) {

            //live percentages for lives rectangle
            if(lives >= 0) {
                livesPercentage = width / 5 - (((width / 5 - width / 2) / 5) * lives);
            }else{
                livesPercentage = width / 5;
            }

            synchronized (enemiesFlying) {
                Iterator<Enemy> enemiesIterator = enemiesFlying.iterator();
                while (enemiesIterator.hasNext()) {
                    Enemy e = enemiesIterator.next();

                    //delay of 100 ms before enemies spawn
                    if (gameStartTime + (ONESEC_NANOS / 10) < frtime) {
                        startDelayReached = true;
                    }


                    /*
                     * Charging behavior
                     * What happens when PlayerShip has collision with EnemyShip
                     */

                    //for some reason it wont work with rectangles, just doing it like this for an accurate hitbox
                    if((e.hasCollision(playerShip.getX() + spaceship[0].getWidth()/2, playerShip.getY()+ spaceship[0].getHeight()/3) ||
                            e.hasCollision(playerShip.getX() + spaceship[0].getWidth()/2, playerShip.getY()+ spaceship[0].getHeight()) ||
                            e.hasCollision(playerShip.getX() + spaceship[0].getWidth()/2, playerShip.getY()+ spaceship[0].getHeight()/2))
                            && lives > 0) {

                        int playerShipLivesLost = e.getEnemyType().getLives() / 5;
                        lives = lives - playerShipLivesLost;
                        if(lives > 0){
                            addEnemyExplosion(e);

                        }else{

                            //for red tinge on enemy, not like it matters though, players dead
                            e.setHitContactTimeForTinge(System.nanoTime());

                            playerShip.setShipExplosionActivateTime(System.nanoTime());
                            shipExplosions.add(new ShipExplosion(playerShip.getX(), playerShip.getY(), playerShip));
                            playerShip.setEndOfTheRoad(true);

                        }

                    }





                     /*
                     * Explosions update
                     */


                    //ship explodes when charges into opposite side of screen
                    if(e.getY() > height*12/13){
                        addEnemyExplosion(e);
                        e.setY(0);
                    }

                    for(int i = 0; i < shipLasers.size(); i++) {
                        if (!shipLasers.get(i).isEnemyLaser()) {
                            if ((e.hasCollision(shipLasers.get(i).getX(), shipLasers.get(i).getY()))) {
                                //setting it to 4000 immediately just so it doesnt register collision more than once
                                shipLasers.get(i).setX(4000);
                                shipLasers.remove(i);
                                e.setHitContactTimeForTinge(System.nanoTime());
                                //subtract a life
                                e.setLives(e.getLives() - 1);

                                //fun explosions
                                if (e.getLives() == 0) {

                                    addEnemyExplosion(e);


                                } else {
                                    e.setEnemyIsHitButNotDead(true);
                                }


                            }
                        }
                    }


                    /*
                     * Firing AI
                     */


                    //if enemy is not at starting position, spawn lasers. The problem was lasers was spawning before the enemy ship was
                    if(e.getX() != 0 && e.getY() != 500) {
                        if (e.getEnemyFiringTime() + (e.getRandomlyGeneratedEnemyFiringTimeInSeconds() * ONESEC_NANOS) < frtime && startDelayReached) {
                            e.setEnemyFiringTime(System.nanoTime());
                            e.setRandomlyGeneratedEnemyFiringTimeInSeconds((rand.nextInt(5000)+1000) / 1000);
                            if(e.getEnemyType() == EnemyType.FIGHTER) {
                                shipLasers.add(new DiagonalLaser(fighterOrb, e.getX()+e.getBitmap().getWidth()*3/5, e.getY()+e.getBitmap().getHeight()/2,1));
                                shipLasers.add(new ShipLaser(fighterOrb, e.getX()+e.getBitmap().getWidth()/3, e.getY()+e.getBitmap().getHeight()*3/4));
                                shipLasers.add(new DiagonalLaser(fighterOrb, e.getX()+e.getBitmap().getWidth()/6, e.getY()+e.getBitmap().getHeight()/2,-1));
                            }else if(e.getEnemyType() == EnemyType.IMPERIAL){
                                //todo add implementation of imperial
                            }

                        }
                    }

                /*
                 * Movement AI
                 */

                    //handles collision for multiple enemies
                    if(!e.isAIDisabled()) {


                        if (startDelayReached) {
                            e.setX(e.getX() + e.getVx());
                            e.setY(e.getY() + e.getVy());

                        }


                        //this starts the next stage of enemy movement
                        if (!e.isAIStarted()) {
                            e.setX(rand.nextInt(width * 4 / 5));
                            e.setY(-height / 10);
                            e.setFinishedVelocityChange(true);
                            e.setAIStarted(true);
                        }

                        if (e.getEnemyType() == EnemyType.FIGHTER) {
                            for (int i = 0; i < enemiesFlying.size(); i++) {
                                if ((e != enemiesFlying.get(i))) {
                                    if ((e.getX() >= enemiesFlying.get(i).getX() - enemiesFlying.get(i).getBitmap().getWidth() && e.getX() <= enemiesFlying.get(i).getX() + enemiesFlying.get(i).getBitmap().getWidth()) &&
                                            (e.getY() >= enemiesFlying.get(i).getY() - enemiesFlying.get(i).getBitmap().getHeight() && e.getY() <= enemiesFlying.get(i).getY() + enemiesFlying.get(i).getBitmap().getHeight())) {
                                        e.setVx(-e.getVx());
                                    }

                                }

                            }


                            //I present to you, next stage of enemy movement and all its glory
                            if (e.isFinishedVelocityChange()) {

                                e.setRandomVelocityGeneratorX((rand.nextInt(e.getRandomDirectionSpeed()) + 1000) / 1000);
                                e.setRandomVelocityGeneratorY((rand.nextInt(e.getRandomDirectionSpeed()) + 1000) / 1000);


                                //makes it negative if it is bigger than half
                                if (e.getRandomVelocityGeneratorX() > e.getRandomDirectionSpeed()/2) {
                                    e.setRandomVelocityGeneratorX(e.getRandomVelocityGeneratorX() - e.getRandomDirectionSpeed());
                                }


                                if (e.getRandomVelocityGeneratorY() > e.getRandomDirectionSpeed()/2) {
                                    e.setRandomVelocityGeneratorY(e.getRandomVelocityGeneratorY() - e.getRandomDirectionSpeed());

                                }

                                //makes the ship change direction soon if they are in a naughty area
                                if (e.getY() > height / 6) {
                                    if (e.getRandomVelocityGeneratorY() > 0) {
                                        e.setRandomVelocityGeneratorY(-e.getRandomVelocityGeneratorY());
                                    }
                                } else if (e.getY() < height / 12) {
                                    if (e.getRandomVelocityGeneratorY() < 0) {
                                        e.setRandomVelocityGeneratorY(-e.getRandomVelocityGeneratorY());
                                    }

                                }

                                if (!e.isSlowingDown()) {
                                    e.setSpeedingUp(true);
                                }

                                e.setFinishedRandomGeneratorsTime(System.nanoTime());

                                //just initiating these guys
                                e.setLastSlowedDownVelocityTime(e.getFinishedRandomGeneratorsTime());
                                e.setLastSpedUpVelocityTime(e.getFinishedRandomGeneratorsTime());

                                e.setFinishedVelocityChange(false);

                            }

                            if (e.isSlowingDown() && (frtime > e.getLastSlowedDownVelocityTime() + (ONESEC_NANOS / 100))) {
                                //obv will never be 0. Half a second for slowing down, then speeding up later on
                                e.setVx(e.getVx() - (e.getVx() / 50));
                                e.setVy(e.getVy() - (e.getVy() / 50));

                                //borders
                                if (e.getX() < 0 || e.getX() > width * 4 / 5) {
                                    //this check disables the ability for ship to get too far and then freeze in place
                                    if (e.getX() < 0) {
                                        e.setX(0);
                                    } else if (e.getX() > width * 4 / 5) {
                                        e.setX(width * 4 / 5);
                                    }

                                    e.setVx(-e.getVx());
                                    e.setRandomVelocityGeneratorX(-e.getRandomVelocityGeneratorX());
                                }

                                //so we do this
                                if ((e.getVx() > -1 && e.getVx() < 1) && (e.getVy() > -1 && e.getVy() < 1)) {
                                    e.setSlowingDown(false);
                                    e.setSpeedingUp(true);

                                }
                                //delays this slowing down process a little
                                e.setLastSlowedDownVelocityTime(System.nanoTime());

                            } else if (e.isSpeedingUp() && (frtime > e.getLastSpedUpVelocityTime() + (ONESEC_NANOS / 100))) {


                                //will not have asymptotes like the last one
                                e.setVx(e.getVx() + (e.getRandomVelocityGeneratorX() / 50));
                                e.setVy(e.getVy() + (e.getRandomVelocityGeneratorY() / 50));

                                //borders for x and y
                                if (e.getX() < 0 || e.getX() > width * 4 / 5) {
                                    //this check disables the ability for ship to get too far and then freeze in place
                                    if (e.getX() < 0) {
                                        e.setX(0);
                                    } else if (e.getX() > width * 4 / 5) {
                                        e.setX(width * 4 / 5);
                                    }

                                    e.setVx(-e.getVx());
                                    e.setRandomVelocityGeneratorX(-e.getRandomVelocityGeneratorX());
                                }

                                //just adding a margin of error regardless though, if the nanoseconds were slightly off it would not work
                                if ((e.getVx() > e.getRandomVelocityGeneratorX() - 1 && e.getVx() < e.getRandomVelocityGeneratorX() + 1) && (e.getVy() > e.getRandomVelocityGeneratorY() - 1 || e.getVy() < e.getRandomVelocityGeneratorY() + 1)) {
                                    e.setSlowingDown(true);
                                    e.setSpeedingUp(false);
                                    e.setFinishedVelocityChange(true);
                                }

                                //delays speeding up process
                                e.setLastSpedUpVelocityTime(System.nanoTime());
                            }
                        } else if (e.getEnemyType() == EnemyType.BERSERKER) {

                            Berserker b = (Berserker) e;
                            if (b.getUpdateVelocityTime() + (ONESEC_NANOS) < frtime) {
                                newBerserkerVelocityX = b.updateShipVelocityX(playerShip.getX(), playerShip.getY());
                                newBerserkerVelocityY = b.updateShipVelocityY(playerShip.getX(), playerShip.getY());

                                differenceBerserkerVelocityX = newBerserkerVelocityX - e.getVx();
                                differenceBerserkerVelocityY = newBerserkerVelocityY - e.getVy();

                                b.setUpdateVelocityTime(System.nanoTime());
                            }

                            //acceleration
                            if (b.getLastAccelerationTime() + (ONESEC_NANOS / 30) < frtime) {
                                e.setVx(e.getVx() + (differenceBerserkerVelocityX / 30));
                                e.setVy(e.getVy() + (differenceBerserkerVelocityY / 30));
                                b.setLastAccelerationTime(System.nanoTime());
                            }




                        }
                    }


                }


            }



            //ship laser positions
            if (shipLasers.size() > 0) {
                for(ShipLaser sl: shipLasers){
                    sl.setX(sl.getX() + sl.getDx());
                    sl.setY(sl.getY() + sl.getDy());
                }
                //checks if the orb has hit player
                for( int i = 0; i < shipLasers.size(); i++){

                    //PLAYER HIT ***********
                    if(shipLasers.get(i).isEnemyLaser()) {
                        if (playerShip.hasCollision(shipLasers.get(i).getX(), shipLasers.get(i).getY())) {

                            //bye bye
                            shipLasers.get(i).setX(4000);
                            shipLasers.remove(shipLasers.get(i));

                            //need that red tinge which will be in draw method
                            if (lives != 0) {
                                playerShip.setShipHitForTingeTime(System.nanoTime());
                                playerShip.setPlayerHitButNotDead(true);
                                loseLife();
                            }


                        }
                    }


                }
            }

            //deletes enemy orbs
            if(shipLasers.size()> 0) {
                //had to make another if because of problems with the last one when they were both deleting the orbs
                for( int i = 0; i < shipLasers.size(); i++) {
                    if (shipLasers.size() != 0 && shipLasers.get(i).getY() > height || shipLasers.get(i).getX() < -100 || shipLasers.get(i).getX() > width * 4 / 3|| shipLasers.get(i).getY() < -height ) {
                        shipLasers.remove(i);
                    }
                }
            }


            //spaceship decay
            if (playerShip.getY() < height * 9 / 10 && !playerShip.isSpaceshipMoving()) {
                playerShip.setY(playerShip.getY() + DECAY_SPEED);
            }

            //makes main spaceship lasers

            if (playerShip.getLastLaserSpawnTime() + (ONESEC_NANOS / 2) < frtime) {

                if(lives > 0) {
                    shipLasers.add(new MainShipLaser(spaceshipLaser, playerShip.getX() + spaceship[0].getWidth() / 8, playerShip.getY() + spaceship[0].getHeight() / 3));
                    shipLasers.add(new MainShipLaser(spaceshipLaser, shipLasers.get(shipLasers.size() - 1).getX() + spaceship[0].getWidth() * 64 / 100, playerShip.getY() + spaceship[0].getHeight() / 3));
                }
                playerShip.setLastLaserSpawnTime(System.nanoTime());
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

    }



    @Override
    public void draw(Canvas c, View v) {

        try {

            // actually draw the screen
            scaledDst.set(mapAnimatorX - width, mapAnimatorY - height, mapAnimatorX, mapAnimatorY);
            c.drawBitmap(level.getMap(), null, scaledDst, p);
            //secondary background for animation. Same as last draw, but instead, these are a height-length higher
            c.drawBitmap(level.getMap(), null, new Rect(secondaryMapAnimatorX - width, secondaryMapAnimatorY - (height * 2), secondaryMapAnimatorX, secondaryMapAnimatorY - height), p);

            synchronized (enemiesFlying) {
                for (Enemy e : enemiesFlying) {

                    if (startDelayReached) {


                        //puts like a red tinge on the enemy for 100 ms if hes hit
                        if (e.isEnemyHitButNotDead()) {
                            if(e.getEnemyType() == EnemyType.BERSERKER){
                                c.drawBitmap(berserkerHit, e.getX(), e.getY(), p);
                            }else if(e.getEnemyType() == EnemyType.FIGHTER) {
                                c.drawBitmap(fighterHit, e.getX(), e.getY(), p);
                            }

                            if (e.getHitContactTimeForTinge() + (ONESEC_NANOS / 10) < frtime) {
                                e.setEnemyIsHitButNotDead(false);
                            }

                        } else {
                            if(e.getEnemyType() == EnemyType.FIGHTER) {
                                c.drawBitmap(e.getBitmap(), e.getX(), e.getY(), p);
                            }else if(e.getEnemyType() == EnemyType.BERSERKER){
                                if(e.getVy()>0){
                                    c.drawBitmap(e.getBitmap(), e.getX(), e.getY(), p);
                                }else{
                                    c.drawBitmap(berserkerReverse, e.getX(), e.getY(), p);
                                }
                            }
                        }

                        //explosion time checker
                        for (ShipExplosion se : shipExplosions) {
                            if (se.getShip() == e) {

                                //semi-clever way of adding a very precise delay (yes, I am scratching my own ass)
                                if (e.getExplosionActivateTime() + (ONESEC_NANOS / 20) < frtime) {
                                    e.setExplosionActivateTime(System.nanoTime());
                                    if(se.getCurrentFrame() < 11) {
                                        se.nextFrame();
                                    }

                                }

                            }

                        }

                        //deletes ship
                        if (e.getExplosionActivateTime() + (ONESEC_NANOS * 5) < frtime && e.getLives() == 0) {
                            enemiesFlying.remove(e);
                        }
                    }
                }
            }



            //explosion drawer
            for (ShipExplosion se : shipExplosions) {
                c.drawBitmap(explosion[se.getCurrentFrame()], se.getX(), se.getY(), p);

                //for some reason, removing the explosion caused major lag. So we're doing it this way
                if (se.getCurrentFrame() == 11) {
                    se.setX(4000);
                    se.setY(4000);
                    // shipExplosions.remove(se);
                }

            }



            //drawing enemy lasers
            if (shipLasers.size() > 0) {
                for (int i = 0; i < shipLasers.size(); i++) {
                    c.drawBitmap(shipLasers.get(i).getBmp(), shipLasers.get(i).getX(), shipLasers.get(i).getY(), p);
                }
            }


            //main spaceship if lives does not equal 0 it shows spaceship, if it does, it shows boom boom
            if(lives > 0) {
                for (int i = 0; i < spaceship.length; i++) {
                    if (i == playerShip.getCurrentSpaceshipFrame() && frtime > playerShip.getSpaceshipFrameSwitchTime() + (ONESEC_NANOS / 10)) {


                        //if frame = 1, make it 0
                        if (playerShip.getCurrentSpaceshipFrame() == spaceship.length - 1) {
                            playerShip.setCurrentSpaceshipFrame(0);
                        } else {
                            //else make it 1
                            playerShip.setCurrentSpaceshipFrame(playerShip.getCurrentSpaceshipFrame() + 1);
                        }


                        //drawing either the tinge or the normal spaceship, based off of delays
                        if (playerShip.isPlayerHitButNotDead()) {
                            c.drawBitmap(spaceshipHit[i], playerShip.getX(), playerShip.getY(), p);
                            if (playerShip.getShipHitForTingeTime() + (ONESEC_NANOS / 10) < frtime) {
                                playerShip.setPlayerHitButNotDead(false);
                            }
                        } else {
                            c.drawBitmap(spaceship[i], playerShip.getX(), playerShip.getY(), p);
                        }

                        //delay for frames
                        playerShip.setSpaceshipFrameSwitchTime(System.nanoTime());

                    } else if (i == playerShip.getCurrentSpaceshipFrame()) {

                        if (playerShip.isPlayerHitButNotDead()) {
                            c.drawBitmap(spaceshipHit[i], playerShip.getX(), playerShip.getY(), p);
                            if (playerShip.getShipHitForTingeTime() + (ONESEC_NANOS / 5) < frtime) {
                                playerShip.setPlayerHitButNotDead(false);
                            }
                        } else {
                            c.drawBitmap(spaceship[i], playerShip.getX(), playerShip.getY(), p);
                        }
                    }

                }
            }else{
                /*
                 * Explosion for main ship
                 */

                for (ShipExplosion se : shipExplosions) {
                    if (se.getShip() == playerShip) {
                        c.drawBitmap(explosion[se.getCurrentFrame()], se.getX() , se.getY(), p);

                        //semi-clever way of adding a very precise delay (yes, I am scratching my own ass)
                        if (playerShip.getShipExplosionActivateTime() + (ONESEC_NANOS / 20) < frtime) {
                            playerShip.setShipExplosionActivateTime(System.nanoTime());
                            se.nextFrame();

                        }

                        if (se.getCurrentFrame() == 11) {
                            shipExplosions.remove(se);

                            //end of game folks, thanks for playing
                            gamestate = State.PLAYERDIED;

                        }
                    }

                }
            }



            //live counter
            p.setColor(Color.rgb(20,20,20));
            c.drawRect(0, 0 , width, height/17, p);
            p.setColor(Color.rgb(255,0,0));
            c.drawRect(width/5, height/30, livesPercentage, height/20, p);
            p.setColor(Color.rgb(255,255,255));
            drawCenteredText(c, "Life", height/20, p, -width*35/100);

            p.setColor(Color.WHITE);
            p.setTextSize(act.TS_NORMAL);
            p.setTypeface(act.getGameFont());



            if (gamestate == State.WIN || gamestate == State.PLAYERDIED) {

                if(gamestate == State.PLAYERDIED) {
                    c.drawBitmap(playerDiedText, width / 6, height / 3, p);

                }else{

                    c.drawBitmap(gameOverOverlay,null,new Rect(0,0,width,height),p);
                    //playerwon
                    c.drawBitmap(playerWonText, width / 5, height / 3, p);

                    if(starsEarned == 3){
                        c.drawBitmap(filledstar, width/10, height/2,p);
                        if(firstStarTimeCheck == 0) {
                            firstStarTimeCheck = System.nanoTime();
                        }
                        if(firstStarTimeCheck + (ONESEC_NANOS/2) < frtime) {
                            c.drawBitmap(filledstar, width * 4 / 10, height / 2, p);
                            if(secondStarTimeCheck == 0) {
                                secondStarTimeCheck = System.nanoTime();
                            }

                            if(secondStarTimeCheck + (ONESEC_NANOS/2) < frtime) {
                                c.drawBitmap(filledstar, width * 7 / 10, height / 2, p);
                            }
                        }
                    }else if (starsEarned == 2){
                        c.drawBitmap(filledstar, width/10, height/2,p);
                        if(firstStarTimeCheck == 0) {
                            firstStarTimeCheck = System.nanoTime();
                        }
                        if(firstStarTimeCheck + (ONESEC_NANOS/2) < frtime) {
                            c.drawBitmap(filledstar, width * 4 / 10, height / 2, p);
                            if(secondStarTimeCheck == 0) {
                                secondStarTimeCheck = System.nanoTime();
                            }

                            if(secondStarTimeCheck + (ONESEC_NANOS/2) < frtime) {
                                c.drawBitmap(emptystar, width * 7 / 10, height / 2, p);
                            }
                        }
                    }else{
                        c.drawBitmap(filledstar, width/10, height/2,p);
                        if(firstStarTimeCheck == 0) {
                            firstStarTimeCheck = System.nanoTime();
                        }
                        if(firstStarTimeCheck + (ONESEC_NANOS/2) < frtime) {
                            c.drawBitmap(emptystar, width * 4 / 10, height / 2, p);
                            if(secondStarTimeCheck == 0) {
                                secondStarTimeCheck = System.nanoTime();
                            }

                            if(secondStarTimeCheck + (ONESEC_NANOS/2) < frtime) {
                                c.drawBitmap(emptystar, width * 7 / 10, height / 2, p);
                            }
                        }
                    }


                }

                drawCenteredText(c, "Press to continue", height*4/5,p,0);

                BufferedReader r = new BufferedReader(new FileReader(act.getFilesDir() + HIGHSCORE_FILE));
                String receiveString = "";
                int counter = 2;

                while((receiveString = r.readLine()) != null){
                    counter++;
                    drawCenteredText(c, receiveString, height/counter, p, 0);
                }

                r.close();


                //write to data file
                try {

                    BufferedWriter f = new BufferedWriter(new FileWriter(act.getFilesDir() + HIGHSCORE_FILE));



                    int counter2 = 0;
                    while(receivingInfo[counter2] != null){
                        f.write(receivingInfo[counter2] + "\n");
                        counter2++;
                    }
                    f.write(Boolean.toString(levelCompleted)+"\n");



                    if (starsEarnedFile <= starsEarned) {
                        f.write(Integer.toString(starsEarned) + "\n");
                    }else{
                        f.write(Integer.toString(starsEarnedFile) + "\n");
                    }

                    f.close();
                } catch (Exception e) {
                    Log.d(MainActivity.LOG_ID, "WriteHiScore", e);
                }

            }


        } catch (Exception e) {
            Log.e(MainActivity.LOG_ID, "draw", e);
            e.printStackTrace();
        }

    }

    public void playerWon(){
        if(elapsedSecs < 20 && lives >= 3){
            starsEarned = 3;
        }else if(elapsedSecs < 30 && lives >= 2){
            starsEarned = 2;
        }else{
            starsEarned = 1;
        }
        gamestate = State.WIN;
        levelCompleted = true;





    }

    public void spawnEnemy(EnemyType enemyType){
        if(enemyType == EnemyType.FIGHTER) {
            enemiesFlying.add(new Fighter(fighter));
        }else if(enemyType == EnemyType.IMPERIAL){
            enemiesFlying.add(new Imperial(imperial));

        }else if(enemyType == EnemyType.BERSERKER){
            enemiesFlying.add(new Berserker(berserker));

        }else if(enemyType == EnemyType.BATTLESHIP){
            enemiesFlying.add(new Battleship(battleship));
        }else if(enemyType == EnemyType.BATTLECRUISER){
            enemiesFlying.add(new Battlecruiser(battlecruiser));
        }
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


                if(playerShip.hasCollision(e.getX(),e.getY()) && gamestate == State.RUNNING){
                    playerShip.setSpaceshipIsMoving(true);
                    playerShip.setX(e.getX()-spaceship[0].getWidth()/2);
                    playerShip.setY(e.getY()-spaceship[0].getHeight()/2);

                }

                break;
            case MotionEvent.ACTION_UP:
                //just using these time checks so I dont have to make a new one. There is no flush mouse hits method for some reason so I have to add a delay
                if(firstStarTimeCheck != 0) {
                    if (firstStarTimeCheck + (ONESEC_NANOS) < frtime) {
                        if ((gamestate == State.PLAYERDIED || gamestate == State.WIN)) {
                            act.onBackPressed(); //just simulates them pressing the back button, resets the game stats and whatnot

                        }
                    }
                }else{
                    if(playerShip.getShipExplosionActivateTime() + (ONESEC_NANOS)<frtime){
                        if ((gamestate == State.PLAYERDIED || gamestate == State.WIN)) {
                            act.onBackPressed(); //just simulates them pressing the back button, resets the game stats and whatnot

                        }
                    }
                }
                playerShip.setSpaceshipIsMoving(false);

                break;
        }

        return true;
    }



}
