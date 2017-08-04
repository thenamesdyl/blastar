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
        import java.util.Iterator;
        import java.util.LinkedList;
        import java.util.List;
        import java.util.Random;

        import me.dylanburton.blastarreborn.enemies.Battlecruiser;
        import me.dylanburton.blastarreborn.enemies.Mothership;
        import me.dylanburton.blastarreborn.enemies.Berserker;
        import me.dylanburton.blastarreborn.enemies.Enemy;
        import me.dylanburton.blastarreborn.enemies.ShipType;
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
        import me.dylanburton.blastarreborn.powerups.DoubleFire;
        import me.dylanburton.blastarreborn.powerups.Forcefield;
        import me.dylanburton.blastarreborn.powerups.HealthPack;
        import me.dylanburton.blastarreborn.powerups.Nuke;
        import me.dylanburton.blastarreborn.powerups.Powerup;
        import me.dylanburton.blastarreborn.powerups.PowerupType;
        import me.dylanburton.blastarreborn.powerups.SlowTime;
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

    private List<Enemy> enemiesFlying = Collections.synchronizedList(new LinkedList<Enemy>());  // enemies that are still alive
    private List<ShipExplosion> shipExplosions = new LinkedList<ShipExplosion>();
    private List<ShipLaser> shipLasers = new LinkedList<ShipLaser>();
    private List<Powerup> powerups = new LinkedList<Powerup>();


    //width and height of screen
    private int width = 0;
    private int height = 0;


    private Bitmap starbackground, spaceship, spaceshipHit, spaceshipLaser, fighter, fighterOrb, fighterHit, explosion[], gameOverOverlay, playerDiedText, playerWonText;
    private Bitmap imperial, imperialHit, imperialOrb[], berserker, berserkerHit, berserkerReverse, battlecruiser, battlecruiserHit, battlecruiserFire[], mothership, mothershipHit, healthPack;
    private Bitmap doubleFire, doubleFireShot, oneStar, twoStar, threeStar, noStar, nuke, slowTime, forceField, shield;
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
    private long gameEndTimeCheck = 0;
    private long powerupSpawnTime = 0;
    private long slowDownTime = 0; //how long the slow down is
    private long forceFieldTime = 0;
    private float elapsedSecs;
    private int fps = 0;

    //various game things
    private int enemiesDestroyed = 0;
    private int currentLevel;
    private int score;
    private int lives;
    private static final String HIGHSCORE_FILE = "scoredata.dat";
    private static final int START_NUMLIVES = 8;
    private Level level;
    private float leftBorder;
    private float rightBorder;

    private int livesPercentage; //for lives rectangle

    private int starsEarned = 0;
    private int starsEarnedFile = 0;
    private boolean levelCompleted = false;

    private String[] receivingInfo = new String[13]; //for reading file

    //some AI Movement vars, to see how it works, look in Enemy class
    private float newBerserkerVelocityX = 0;
    private float newBerserkerVelocityY = 0;
    private float differenceBerserkerVelocityX;
    private float differenceBerserkerVelocityY;
    private Random rand = new Random();

    private boolean isSpawnEnemyImperial = false;//used for mothership imperial spawning

    //powerup vars
    private boolean isDoubleFireSpeed = false;
    private boolean isSlowDown = false;
    private boolean isForcefield = false;
    private long powerupActivateTime = 0;


    public PlayScreen(MainActivity act) {
        p = new Paint();
        this.act = act;
        AssetManager assetManager = act.getAssets();
        try {

            //background
            InputStream inputStream = assetManager.open("maps/map1.jpg");
            starbackground = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            //your spaceship and laser
            spaceship = act.getScaledBitmap("spaceship/playerspaceship.png");
            spaceshipLaser = act.getScaledBitmap("spaceshiplaser.png");
            doubleFireShot = act.getScaledBitmap("doublefireshot.png");

            spaceshipHit = act.getScaledBitmap("spaceship/playerspaceshiphit.png");

            //enemies
            fighter = act.getScaledBitmap("enemies/fighter.png");
            fighterHit= act.getScaledBitmap("enemies/fighterhit.png");
            fighterOrb = act.getScaledBitmap("enemies/fighterorbs.png");

            imperial = act.getScaledBitmap("enemies/imperial.png");
            imperialHit = act.getScaledBitmap("enemies/imperialhit.png");

            imperialOrb = new Bitmap[8];
            for(int i = 0; i < 8; i++) {
                imperialOrb[i] = act.getScaledBitmap("enemies/imperialorbs/imperialorb" + (i+1) + ".png");
            }

            berserker = act.getScaledBitmap("enemies/berserker.png");
            berserkerHit = act.getScaledBitmap("enemies/berserkerhit.png");
            berserkerReverse = act.getScaledBitmap("enemies/berserkerreverse.png");

            battlecruiser = act.getScaledBitmap("enemies/battlecruiser.png");
            battlecruiserHit = act.getScaledBitmap("enemies/battlecruiserhit.png");
            battlecruiserFire = new Bitmap[6];
            for(int i = 0; i < 6; i++){
                battlecruiserFire[i] = act.getScaledBitmap("enemies/battlecruiserfire/battlecruiserfire" + (i+1) + ".png");
            }

            mothership = act.getScaledBitmap("enemies/mothership.png");
            mothershipHit = act.getScaledBitmap("enemies/mothershiphit.png");

            healthPack = act.getScaledBitmap("powerups/healthpack.png");
            doubleFire = act.getScaledBitmap("powerups/doublefire.png");
            nuke = act.getScaledBitmap("powerups/nuke.png");
            slowTime = act.getScaledBitmap("powerups/timeslow.png");
            forceField = act.getScaledBitmap("powerups/forcefield.png");
            shield = act.getScaledBitmap("powerups/shield.png");

            oneStar = act.getScaledBitmap("endgame/onestar.png");
            twoStar = act.getScaledBitmap("endgame/twostars.png");
            threeStar = act.getScaledBitmap("endgame/threestars.png");
            noStar = act.getScaledBitmap("endgame/nostars.png");

            //explosion
            explosion = new Bitmap[12];
            for(int i = 0; i < 12; i++) {
                explosion[i] = act.getScaledBitmap("explosion/explosion"+(i+1)+".png");
            }

            //game over stuff
            gameOverOverlay = act.getScaledBitmap("slightlytransparentoverlay.png");
            playerDiedText = act.getScaledBitmap("playerdiedtext.png");
            playerWonText = act.getScaledBitmap("playerwontext.png");

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


        // add score data for level screen
        try {
            BufferedReader f = new BufferedReader(new FileReader(act.getFilesDir() + HIGHSCORE_FILE));
            String receiveString = "";


            //gets us to the right place
            for (int i = 0; i < (currentLevel-1)*2; i++){
                receivingInfo[i] = f.readLine();
            }

            receiveString = f.readLine();

            //this looks weird but works i swear on me mum
            if(receiveString != "false" && (receiveString = f.readLine())!= null) {
                starsEarnedFile = Integer.parseInt(receiveString);

            }

            //now read the rest if there is any
            for(int i = ((currentLevel-1)*2)+2; i < 12; i++){
                receivingInfo[i] = f.readLine();
            }

        }catch (Exception e){

            e.printStackTrace();
        }


    }


    public void resetGame(){

        gamestate = State.STARTGAME;
        width = 0;
        height = 0;
        enemiesFlying.clear();
        shipLasers.clear();
        shipExplosions.clear();
        for(Enemy e: enemiesFlying){
            e.setFinishedVelocityChange(false);
            e.setAIStarted(false);
        }
        enemiesDestroyed = 0;
        level.setUpdateLevelStage(0);

    }

    /**
     * player lost a life
     */
    private void loseLife() {
        if(!isForcefield) {
            lives--;
        }

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
        if(e.getShipType() == ShipType.FIGHTER) {
            shipExplosions.add(new ShipExplosion(e.getX() - e.getBitmap().getWidth() * 3 / 4, e.getY() - e.getBitmap().getHeight() / 2, e));
        }else if(e.getShipType() == ShipType.BERSERKER){
            shipExplosions.add(new ShipExplosion(e.getX() + e.getBitmap().getWidth()/3, e.getY() + e.getBitmap().getHeight()/3,e));
        }else if(e.getShipType() == ShipType.IMPERIAL){
            shipExplosions.add(new ShipExplosion(e.getX() , e.getY() + e.getBitmap().getHeight()/4,e));
        }else if(e.getShipType() == ShipType.BATTLECRUISER){
            shipExplosions.add(new ShipExplosion(e.getX() , e.getY() + e.getBitmap().getHeight()/4,e));
        }else if(e.getShipType() == ShipType.MOTHERSHIP){
            shipExplosions.add(new ShipExplosion(e.getX() , e.getY() + e.getBitmap().getHeight()/4,e));
        }

        e.setX(10000);
        e.setAIDisabled(true);
        enemyDestroyed(e);

        e.setExplosionActivateTime(System.nanoTime());
    }

    public void enemyDestroyed(Enemy e){
        if(e.isWorthEnemyDestroyedPoint()){
            enemiesDestroyed++;
        }
        //else we dont care
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

            int randomTime = rand.nextInt(10)+3;
            powerupSpawnTime = System.nanoTime() + (ONESEC_NANOS*randomTime);

            gameEndTimeCheck = 0;

            leftBorder = width/10;
            rightBorder = width*9/10;

        }

        if (gamestate == State.RUNNING ) {

            //live percentages for lives rectangle
            if(lives >= 0) {
                livesPercentage = width / 9 - (((width / 9 - width / 3) / START_NUMLIVES) * lives);
            }else{
                livesPercentage = width / 9;
            }


            //powerup spawning
            if(powerupSpawnTime < frtime){
                int randomChoice = rand.nextInt(5);
                int randomX = rand.nextInt(width);
                if(randomChoice == 0){
                    powerups.add(new HealthPack(healthPack,(float) randomX, -height/10, 1 ));
                }else if(randomChoice == 1){
                    powerups.add(new DoubleFire(doubleFire,(float) randomX, -height/10));
                }else if(randomChoice == 2){
                    powerups.add(new Nuke(nuke,(float) randomX, -height/10));
                }else if(randomChoice == 3){
                    powerups.add(new SlowTime(slowTime,(float) randomX, -height/10));
                }else if(randomChoice == 4){
                    powerups.add(new Forcefield(shield, (float) randomX, -height/10));
                }

                int randomSpawnTime = rand.nextInt(10) + 3;
                powerupSpawnTime = System.nanoTime() + (ONESEC_NANOS*randomSpawnTime);
            }

            for(Powerup p: powerups){
                p.setY(p.getY() + p.getDy());

                if(playerShip.hasCollision(p.getX(), p.getY()) ||
                        playerShip.hasCollision(p.getX() + p.getBitmap().getWidth(),p.getY()) ||
                        playerShip.hasCollision(p.getX() + p.getBitmap().getWidth(),p.getY() + p.getBitmap().getHeight()) ||
                        playerShip.hasCollision(p.getX(),p.getY() + p.getBitmap().getHeight())){

                    p.setX(10000);

                    if(p.getPowerupType() == PowerupType.HEALTHPACK) {
                        lives++;
                    }else if(p.getPowerupType() == PowerupType.DOUBLEFIRE){
                        isDoubleFireSpeed = true;
                        powerupActivateTime = System.nanoTime() + (ONESEC_NANOS*4);
                    }else if(p.getPowerupType() == PowerupType.NUKE){
                        for(Enemy e: enemiesFlying){
                            //checks if actually on the screen
                            if(e.getX() < width && e.getShipType() != ShipType.MOTHERSHIP){
                                addEnemyExplosion(e);
                            }
                        }
                    }else if(p.getPowerupType() == PowerupType.SLOWTIME){
                        isSlowDown = true;
                    }else if(p.getPowerupType() == PowerupType.FORCEFIELD){
                        isForcefield = true;
                    }

                }
            }

            synchronized (enemiesFlying){
            Iterator<Enemy> enemiesIterator = enemiesFlying.iterator();
            while (enemiesIterator.hasNext()) {
                Enemy e = enemiesIterator.next();

                //Mothership spawning

                //makes sure mothership is on the screen before spawning more imperials
                if (e.getShipType() == ShipType.MOTHERSHIP && e.getX() < width) {
                    Mothership ms = (Mothership) e;
                    if (ms.getMotherShipSpawner() + (ONESEC_NANOS * 2) < frtime) {

                        //have to use boolean for spawning imperial because of concurrent exception
                        isSpawnEnemyImperial = true;
                        ms.setMotherShipSpawner(System.nanoTime());
                    }
                }


                /*
                    * Charging behavior
                    * What happens when PlayerShip has collision with EnemyShip
                    */

                //for some reason it wont work with rectangles, just doing it like this for an accurate hitbox
                if ((e.hasCollision(playerShip.getX() + spaceship.getWidth(), playerShip.getY() + spaceship.getHeight()) ||
                        e.hasCollision(playerShip.getX() + spaceship.getWidth(), playerShip.getY() + spaceship.getHeight() / 3) ||
                        e.hasCollision(playerShip.getX() + spaceship.getWidth() / 2, playerShip.getY() + spaceship.getHeight() * 3 / 4) ||
                        e.hasCollision(playerShip.getX(), playerShip.getY() + spaceship.getHeight() / 3) ||
                        e.hasCollision(playerShip.getX() + spaceship.getWidth() / 2, playerShip.getY()))
                        && lives > 0) {

                    if(!isForcefield) {
                        int playerShipLivesLost = e.getShipType().getLives() / 5;
                        lives = lives - playerShipLivesLost;
                    }
                    if (lives > 0) {
                        addEnemyExplosion(e);

                    } else {

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
                if (e.getY() > height * 12 / 13) {
                    addEnemyExplosion(e);
                    e.setY(0);
                }

                for (int i = 0; i < shipLasers.size(); i++) {
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


                //enemy laser firing, if firing time is 0, set the time variables
                if (e.getEnemyFiringTime() == 0) {
                    e.setRandomlyGeneratedEnemyFiringTimeInSeconds((rand.nextInt(5000) + 1000) / 1000);
                    e.setEnemyFiringTime(System.nanoTime() + (long) (ONESEC_NANOS * e.getRandomlyGeneratedEnemyFiringTimeInSeconds()));
                }
                if (e.getEnemyFiringTime() < frtime) {
                    e.setRandomlyGeneratedEnemyFiringTimeInSeconds((rand.nextInt(5000) + 1000) / 1000);
                    e.setEnemyFiringTime(System.nanoTime() + (long) (ONESEC_NANOS * e.getRandomlyGeneratedEnemyFiringTimeInSeconds()));
                    if (e.getShipType() == ShipType.FIGHTER) {
                        shipLasers.add(new DiagonalLaser(ShipType.FIGHTER, fighterOrb, e.getX() + e.getBitmap().getWidth() * 3 / 5, e.getY() + e.getBitmap().getHeight() / 2, 1));
                        shipLasers.add(new ShipLaser(ShipType.FIGHTER, fighterOrb, e.getX() + e.getBitmap().getWidth() / 3, e.getY() + e.getBitmap().getHeight() * 3 / 4));
                        shipLasers.add(new DiagonalLaser(ShipType.FIGHTER, fighterOrb, e.getX() + e.getBitmap().getWidth() / 6, e.getY() + e.getBitmap().getHeight() / 2, -1));
                    } else if (e.getShipType() == ShipType.IMPERIAL) {
                        shipLasers.add(new ShipLaser(ShipType.IMPERIAL, imperialOrb[0], e.getX() + e.getBitmap().getWidth() / 6, e.getY() + e.getBitmap().getHeight() * 4 / 5));
                    } else if (e.getShipType() == ShipType.BATTLECRUISER) {
                        int randomSide = rand.nextInt(2);
                        if (randomSide == 0) {
                            shipLasers.add(new ShipLaser(ShipType.BATTLECRUISER, battlecruiserFire[0], e.getX() + e.getBitmap().getWidth() / 10, e.getY() + e.getBitmap().getHeight() * 3 / 4, 2.0f));
                        } else {
                            shipLasers.add(new ShipLaser(ShipType.BATTLECRUISER, battlecruiserFire[0], e.getX() + e.getBitmap().getWidth() * 65 / 100, e.getY() + e.getBitmap().getHeight() * 3 / 4, 2.0f));
                        }
                    }

                }


                /*
                 * Movement AI
                 */

                //handles collision for multiple enemies
                if (!e.isAIDisabled()) {


                    //for slow down powerup
                    if(isSlowDown) {
                        e.setX(e.getX() + e.getVx()/5);
                        e.setY(e.getY() + e.getVy()/5);
                    }else{
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

                    if (e.getShipType() != ShipType.BERSERKER) {


                        /*for (int i = 0; i < enemiesFlying.size(); i++) {
                            if ((e != enemiesFlying.get(i))) {
                                if ((e.getX() >= enemiesFlying.get(i).getX() - enemiesFlying.get(i).getBitmap().getWidth() && e.getX() <= enemiesFlying.get(i).getX() + enemiesFlying.get(i).getBitmap().getWidth()) &&
                                        (e.getY() >= enemiesFlying.get(i).getY() - enemiesFlying.get(i).getBitmap().getHeight() && e.getY() <= enemiesFlying.get(i).getY() + enemiesFlying.get(i).getBitmap().getHeight())) {
                                    e.setVx(-e.getVx());
                                }

                            }

                        }*/


                        //I present to you, next stage of enemy movement and all its glory
                        if (e.isFinishedVelocityChange()) {

                            e.setRandomVelocityGeneratorX((rand.nextInt(e.getRandomDirectionSpeed())) / 1000);
                            e.setRandomVelocityGeneratorY((rand.nextInt(e.getRandomDirectionSpeed())) / 1000);


                            //makes it negative if it is bigger than half
                            if (e.getRandomVelocityGeneratorX() > (e.getRandomDirectionSpeed()/1000) / 2) {
                                e.setRandomVelocityGeneratorX(e.getRandomVelocityGeneratorX() - (e.getRandomDirectionSpeed()/1000));
                            }


                            if (e.getRandomVelocityGeneratorY() > (e.getRandomDirectionSpeed()/1000) / 2) {
                                e.setRandomVelocityGeneratorY(e.getRandomVelocityGeneratorY() - (e.getRandomDirectionSpeed()/1000));

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

                        if (e.isSlowingDown() && (frtime > e.getLastSlowedDownVelocityTime())) {
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
                            e.setLastSlowedDownVelocityTime(System.nanoTime() + (ONESEC_NANOS / 100));

                        } else if (e.isSpeedingUp() && (frtime > e.getLastSpedUpVelocityTime() )) {


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
                            e.setLastSpedUpVelocityTime(System.nanoTime() + (ONESEC_NANOS/100));
                        }
                    } else if (e.getShipType() == ShipType.BERSERKER) {

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
                    if(isSlowDown && sl.isEnemyLaser()) {
                        sl.setX(sl.getX() + sl.getDx()/5);
                        sl.setY(sl.getY() + sl.getDy()/5);
                    }else{
                        sl.setX(sl.getX() + sl.getDx());
                        sl.setY(sl.getY() + sl.getDy());
                    }
                }
                //checks if the orb has hit player
                for( int i = 0; i < shipLasers.size(); i++){

                    //PLAYER HIT ***********
                    if(shipLasers.get(i).isEnemyLaser()) {
                        //accuracy since cant use rect for some reason
                        if (playerShip.hasCollision(shipLasers.get(i).getX(), shipLasers.get(i).getY())
                                || playerShip.hasCollision(shipLasers.get(i).getX()+shipLasers.get(i).getBmp().getWidth(), shipLasers.get(i).getY())
                                || playerShip.hasCollision(shipLasers.get(i).getX()+shipLasers.get(i).getBmp().getWidth(), shipLasers.get(i).getY() + shipLasers.get(i).getBmp().getHeight())
                                || playerShip.hasCollision(shipLasers.get(i).getX(), shipLasers.get(i).getY() + shipLasers.get(i).getBmp().getHeight())) {

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

            if(!isDoubleFireSpeed) {
                if (playerShip.getLastLaserSpawnTime() + (ONESEC_NANOS / 2) < frtime) {

                    if (lives > 0) {
                        shipLasers.add(new MainShipLaser(spaceshipLaser, playerShip.getX() + spaceship.getWidth() / 20, playerShip.getY() + spaceship.getHeight() / 3));
                        shipLasers.add(new MainShipLaser(spaceshipLaser, shipLasers.get(shipLasers.size() - 1).getX() + spaceship.getWidth() * 80 / 100, playerShip.getY() + spaceship.getHeight() / 3));
                    }
                    playerShip.setLastLaserSpawnTime(System.nanoTime());
                }
            }else{
                if (playerShip.getLastLaserSpawnTime() + (ONESEC_NANOS / 4) < frtime) {
                    if (lives > 0) {
                        shipLasers.add(new MainShipLaser(spaceshipLaser, playerShip.getX() + spaceship.getWidth() / 8, playerShip.getY() + spaceship.getHeight() / 3));
                        shipLasers.add(new MainShipLaser(spaceshipLaser, shipLasers.get(shipLasers.size() - 1).getX() + spaceship.getWidth() * 64 / 100, playerShip.getY() + spaceship.getHeight() / 3));
                    }
                    playerShip.setLastLaserSpawnTime(System.nanoTime());

                    if(powerupActivateTime < frtime){
                        isDoubleFireSpeed = false;
                    }
                }
            }

            if(isSpawnEnemyImperial){
                spawnEnemy(ShipType.IMPERIAL,false);
                //subtracts an enemy destroyed because this imperial spawn is from mothership
                isSpawnEnemyImperial = false;
            }




            //slow down powerup controller
            if(slowDownTime == 0 && isSlowDown == true){
                slowDownTime = System.nanoTime() + (ONESEC_NANOS*2);

            }else if(slowDownTime < frtime && isSlowDown == true){
                slowDownTime = 0;
                isSlowDown = false;
            }

            //forcefield powerup controller
            if(forceFieldTime == 0 && isForcefield == true){
                forceFieldTime = System.nanoTime() + (ONESEC_NANOS*4);
            }else if(forceFieldTime < frtime && isForcefield == true){
                forceFieldTime = 0;
                isForcefield = false;
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



            for( Powerup pw: powerups){
                c.drawBitmap(pw.getBitmap(), pw.getX(), pw.getY(), p);

            }

            synchronized (enemiesFlying) {
                for (Enemy e : enemiesFlying) {


                    //puts like a red tinge on the enemy for 100 ms if hes hit
                    if (e.isEnemyHitButNotDead()) {

                        c.drawBitmap(e.getHitBitmap(), e.getX(), e.getY(), p);

                        if (e.getHitContactTimeForTinge() + (ONESEC_NANOS / 10) < frtime) {
                            e.setEnemyIsHitButNotDead(false);
                        }

                    } else {
                        if (e.getShipType() != ShipType.BERSERKER) {
                            c.drawBitmap(e.getBitmap(), e.getX(), e.getY(), p);
                        } else if (e.getShipType() == ShipType.BERSERKER) {
                            if (e.getVy() > 0) {
                                c.drawBitmap(e.getBitmap(), e.getX(), e.getY(), p);
                            } else {
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
                                if (se.getCurrentFrame() < 11) {
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



            //drawing lasers
            if (shipLasers.size() > 0) {
                for (int i = 0; i < shipLasers.size(); i++) {

                    if(shipLasers.get(i).getShipType() != ShipType.IMPERIAL && shipLasers.get(i).getShipType() != ShipType.BATTLECRUISER && shipLasers.get(i).getShipType() != ShipType.PLAYER) {
                        c.drawBitmap(shipLasers.get(i).getBmp(), shipLasers.get(i).getX(), shipLasers.get(i).getY(), p);
                    }else if(shipLasers.get(i).getShipType() == ShipType.IMPERIAL){
                        c.drawBitmap(imperialOrb[shipLasers.get(i).getCurrentFrame()], shipLasers.get(i).getX(), shipLasers.get(i).getY(), p);
                        if(shipLasers.get(i).getLastImperialLaserFrameChange() < frtime){
                            shipLasers.get(i).setCurrentFrame(shipLasers.get(i).getCurrentFrame() + 1);
                            if(shipLasers.get(i).getCurrentFrame() == 8){
                                shipLasers.get(i).setCurrentFrame(0);
                            }
                            shipLasers.get(i).setLastImperialLaserFrameChange(System.nanoTime() + (ONESEC_NANOS/20));
                        }
                    }else if(shipLasers.get(i).getShipType() == ShipType.BATTLECRUISER){
                        c.drawBitmap(battlecruiserFire[shipLasers.get(i).getCurrentFrame()], shipLasers.get(i).getX(), shipLasers.get(i).getY(), p);
                        if(shipLasers.get(i).getLastBattlecruiserLaserFrameChange() < frtime){
                            shipLasers.get(i).setCurrentFrame(shipLasers.get(i).getCurrentFrame() + 1);
                            if(shipLasers.get(i).getCurrentFrame() == 6){
                                shipLasers.get(i).setCurrentFrame(0);
                            }
                            shipLasers.get(i).setLastBattlecruiserLaserFrameChange(System.nanoTime() + (ONESEC_NANOS/15));
                        }
                    }else if(shipLasers.get(i).getShipType() == ShipType.PLAYER){
                        if(!isDoubleFireSpeed){
                            c.drawBitmap(shipLasers.get(i).getBmp(), shipLasers.get(i).getX(), shipLasers.get(i).getY(), p);
                        }else{
                            c.drawBitmap(doubleFireShot, shipLasers.get(i).getX(), shipLasers.get(i).getY(), p);
                        }
                    }
                }
            }


            //main spaceship if lives does not equal 0 it shows spaceship, if it does, it shows boom boom
            if(lives > 0) {


                //drawing either the tinge or the normal spaceship, based off of delays
                if (playerShip.isPlayerHitButNotDead() && !isForcefield) {
                    c.drawBitmap(spaceshipHit, playerShip.getX(), playerShip.getY(), p);
                } else {
                    if(isForcefield){
                        c.drawBitmap(forceField, playerShip.getX()-spaceship.getWidth()/2, playerShip.getY()-spaceship.getHeight()/3, p);
                    }
                    c.drawBitmap(spaceship, playerShip.getX(), playerShip.getY(), p);
                }

                if (playerShip.getShipHitForTingeTime() + (ONESEC_NANOS / 5) < frtime) {
                    playerShip.setPlayerHitButNotDead(false);
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



            //life counter
            p.setColor(Color.rgb(20,20,20));
            c.drawRect(0, 0 , width, height/17, p);
            p.setColor(Color.rgb(255,0,0));
            c.drawRect(width/9, height/35, livesPercentage, height/23, p);
            p.setColor(Color.rgb(255,255,255));
            drawCenteredText(c, "Life", height/23, p, -width*45/100);

            p.setColor(Color.WHITE);
            p.setTextSize(act.TS_NORMAL);
            p.setTypeface(act.getGameFont());



            if (gamestate == State.WIN || gamestate == State.PLAYERDIED) {

                //end game time check for delaying menu hit
                if(gameEndTimeCheck == 0){
                    gameEndTimeCheck = System.nanoTime();
                }

                if(gamestate == State.PLAYERDIED) {
                    c.drawBitmap(gameOverOverlay,null,new Rect(0,0,width,height),p);

                    c.drawBitmap(noStar, width*22/100, height/5, p);
                    c.drawBitmap(playerDiedText, width*27/100, height *38 /100, p);

                }else{

                    c.drawBitmap(gameOverOverlay,null,new Rect(0,0,width,height),p);
                    //playerwon
                    c.drawBitmap(playerWonText, width*19/100, height *38 /100, p);

                    if(starsEarned == 3){
                        c.drawBitmap(threeStar, width*23/100, height/5, p);

                    }else if (starsEarned == 2){
                        c.drawBitmap(twoStar, width*23/100, height/5, p);
                    }else{
                        c.drawBitmap(oneStar, width*23/100, height/5, p);
                    }


                }

                drawCenteredText(c, "Press to continue", height*4/5,p,0);


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

        //write to data file
        try {

            BufferedWriter f = new BufferedWriter(new FileWriter(act.getFilesDir() + HIGHSCORE_FILE));



            for(int i = 0; i < (currentLevel-1)*2; i++){
                f.write(receivingInfo[i] + "\n");
            }


            f.write(Boolean.toString(levelCompleted)+"\n");

            if (starsEarnedFile <= starsEarned) {
                f.write(Integer.toString(starsEarned) + "\n");
            }else{
                f.write(Integer.toString(starsEarnedFile) + "\n");
            }

            for(int i = ((currentLevel-1)*2)+2; i < 13; i++){
                f.write(receivingInfo[i] + "\n");
            }

            f.close();
        } catch (Exception e) {
            Log.d(MainActivity.LOG_ID, "WriteHiScore", e);
        }




    }

    public void spawnEnemy(ShipType shipType, boolean isWorthEnemyDestroyedPoint){
        if(shipType == ShipType.FIGHTER) {
            enemiesFlying.add(new Fighter(fighter, fighterHit, isWorthEnemyDestroyedPoint));
        }else if(shipType == ShipType.IMPERIAL){
            enemiesFlying.add(new Imperial(imperial, imperialHit, isWorthEnemyDestroyedPoint));

        }else if(shipType == ShipType.BERSERKER){
            enemiesFlying.add(new Berserker(berserker, berserkerHit, isWorthEnemyDestroyedPoint));

        }else if(shipType == ShipType.MOTHERSHIP){
            enemiesFlying.add(new Mothership(mothership, mothershipHit, isWorthEnemyDestroyedPoint));
        }else if(shipType == ShipType.BATTLECRUISER){
            enemiesFlying.add(new Battlecruiser(battlecruiser, battlecruiserHit, isWorthEnemyDestroyedPoint));
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
                    playerShip.setX(e.getX() - spaceship.getWidth() / 2);
                    playerShip.setY(e.getY() - spaceship.getHeight() / 2);

                }

                break;
            case MotionEvent.ACTION_UP:
                //just using these time checks so I dont have to make a new one. There is no flush mouse hits method for some reason so I have to add a delay

                if (gameEndTimeCheck + (ONESEC_NANOS*2) < frtime) {
                    if ((gamestate == State.PLAYERDIED || gamestate == State.WIN)) {
                        act.onBackPressed(); //just simulates them pressing the back button, resets the game stats and whatnot

                    }
                }


                playerShip.setSpaceshipIsMoving(false);

                break;
        }

        return true;
    }



}
