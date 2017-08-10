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
import me.dylanburton.blastarreborn.spaceships.ShipType;
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
import me.dylanburton.blastarreborn.utils.Sound;

/**
 * Represents the main screen of play for the game.
 */
public class PlayScreen extends Screen {


    private MainActivity act;
    private Paint p;
    //how fast the spaceship moves backwards
    private static final int DECAY_SPEED = 5;
    private static final long ONESEC_NANOS = 1000000000L;

    private enum State {RUNNING, STARTGAME, PLAYERDIED, WIN}

    private volatile State gamestate = State.STARTGAME;

    private List<Enemy> enemiesFlying = Collections.synchronizedList(new LinkedList<Enemy>());  // enemies that are still alive
    private List<ShipExplosion> shipExplosions = new LinkedList<ShipExplosion>();
    private List<ShipLaser> shipLasers = new LinkedList<ShipLaser>();
    private List<Powerup> powerups = new LinkedList<Powerup>();


    //width and height of screen
    private int width = 0;
    private int height = 0;


    private Bitmap spaceship, spaceshipHit, spaceshipLaser, fighter, fighterOrb, fighterHit, explosion[], gameOverOverlay, playerDiedText, playerWonText;
    private Bitmap imperial, imperialHit, imperialOrb[], berserker, berserkerHit, berserkerReverse, battlecruiser, battlecruiserHit, battlecruiserFire[], mothership, mothershipHit, healthPack;
    private Bitmap doubleFire, doubleFireShot, oneStar, twoStar, threeStar, noStar, nuke, slowTime, forceField, shield;
    private Bitmap lifeBarEmpty, lifeBarRect, powerupTimeRect, slantedContainer;
    private Rect scaledDst = new Rect();

    //main spaceship
    PlayerShip playerShip;

    //used to move the background image, need two pairs of these vars for animation
    private int mapAnimatorX;
    private int mapAnimatorY;

    //time stuff
    private long frtime = 0; //the global time
    private long gameEndTimeCheck = 0;
    private long powerupSpawnTime = 0;
    private float elapsedSecs;

    //various game things
    private int enemiesDestroyed = 0;
    private int currentLevel;
    private int score;
    private int lives;
    private static final String HIGHSCORE_FILE = "scoredata.dat";
    private static final int START_NUMLIVES = 8;
    private Level level;

    private int livesPercentage; //for lives rectangle
    private float powerupCounterPercentage; //for powerup rect at top UI
    private long powerupTimeDuration = ONESEC_NANOS * 3;

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
    private long powerupEndTime = 0;


    public PlayScreen(MainActivity act) {
        p = new Paint();
        this.act = act;
        AssetManager assetManager = act.getAssets();
        try {

            //your spaceship and laser
            spaceship = act.getScaledBitmap("spaceship/playerspaceship.png");
            spaceshipLaser = act.getScaledBitmap("spaceshiplaser.png");
            doubleFireShot = act.getScaledBitmap("doublefireshot.png");

            spaceshipHit = act.getScaledBitmap("spaceship/playerspaceshiphit.png");

            //enemies
            fighter = act.getScaledBitmap("enemies/fighter.png");
            fighterHit = act.getScaledBitmap("enemies/fighterhit.png");
            fighterOrb = act.getScaledBitmap("enemies/fighterorbs.png");

            imperial = act.getScaledBitmap("enemies/imperial.png");
            imperialHit = act.getScaledBitmap("enemies/imperialhit.png");

            imperialOrb = new Bitmap[8];
            for (int i = 0; i < 8; i++) {
                imperialOrb[i] = act.getScaledBitmap("enemies/imperialorbs/imperialorb" + (i + 1) + ".png");
            }

            berserker = act.getScaledBitmap("enemies/berserker.png");
            berserkerHit = act.getScaledBitmap("enemies/berserkerhit.png");
            berserkerReverse = act.getScaledBitmap("enemies/berserkerreverse.png");

            battlecruiser = act.getScaledBitmap("enemies/battlecruiser.png");
            battlecruiserHit = act.getScaledBitmap("enemies/battlecruiserhit.png");
            battlecruiserFire = new Bitmap[6];
            for (int i = 0; i < 6; i++) {
                battlecruiserFire[i] = act.getScaledBitmap("enemies/battlecruiserfire/battlecruiserfire" + (i + 1) + ".png");
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

            lifeBarRect = act.getScaledBitmap("topui/lifebarrect.png");
            lifeBarEmpty = act.getScaledBitmap("topui/lifebarfoundation.png");
            powerupTimeRect = act.getScaledBitmap("topui/poweruptimerect.png");
            slantedContainer = act.getScaledBitmap("topui/slantedcontainer.png");

            //explosion for all ships
            explosion = new Bitmap[12];
            for (int i = 0; i < 12; i++) {
                explosion[i] = act.getScaledBitmap("explosion/explosion" + (i + 1) + ".png");
            }

            //game over stuff
            gameOverOverlay = act.getScaledBitmap("slightlytransparentoverlay.png");
            playerDiedText = act.getScaledBitmap("endgame/playerdiedtext.png");
            playerWonText = act.getScaledBitmap("endgame/playerwontext.png");

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

        if (currentLevel == 1) {

            level = new Level1(this, act);
            level.checkLevelSequence();


        } else if (currentLevel == 2) {

            level = new Level2(this, act);
            level.checkLevelSequence();

        } else if (currentLevel == 3) {

            level = new Level3(this, act);
            level.checkLevelSequence();

        } else if (currentLevel == 4) {

            level = new Level4(this, act);
            level.checkLevelSequence();

        } else if (currentLevel == 5) {

            level = new Level5(this, act);
            level.checkLevelSequence();

        } else if (currentLevel == 6) {

            level = new Level6(this, act);
            level.checkLevelSequence();

        }

        gamestate = State.RUNNING;


        // add score data for level screen
        try {
            BufferedReader f = new BufferedReader(new FileReader(act.getFilesDir() + HIGHSCORE_FILE));
            String receiveString = "";


            //gets us to the right place
            for (int i = 0; i < (currentLevel - 1) * 2; i++) {
                receivingInfo[i] = f.readLine();
            }

            receiveString = f.readLine();

            //this looks weird but works i swear on me mum
            if (receiveString != "false" && (receiveString = f.readLine()) != null) {
                starsEarnedFile = Integer.parseInt(receiveString);

            }

            //now read the rest if there is any
            for (int i = ((currentLevel - 1) * 2) + 2; i < 12; i++) {
                receivingInfo[i] = f.readLine();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }


    }


    public void resetGame() {

        if (width != 0) {
            gamestate = State.STARTGAME;
            width = 0;
            height = 0;
            enemiesFlying.clear();
            shipLasers.clear();
            shipExplosions.clear();
            for (Enemy e : enemiesFlying) {
                e.setFinishedVelocityChange(false);
                e.setAIStarted(false);
            }
            enemiesDestroyed = 0;
            level.setUpdateLevelStage(0);

        }
    }

    /**
     * player lost a life
     */
    private void loseLife() {
        if (!isForcefield) {
            lives--;
        }

        if (lives <= 0) {


            shipExplosions.add(new ShipExplosion(playerShip.getX(), playerShip.getY(), ShipType.PLAYER));
            shipExplosions.get(shipExplosions.size() - 1).setExplosionActivateTime(System.nanoTime());

            //the rest of this will happen when explosion is completed in draw
        }
    }

    public void addEnemyExplosion(Enemy e) {
        if (e.getShipType() == ShipType.FIGHTER) {
            shipExplosions.add(new ShipExplosion(e.getX() - e.getBitmap().getWidth() / 5, e.getY() - e.getBitmap().getHeight() / 4, e.getShipType()));
        } else if (e.getShipType() == ShipType.BERSERKER) {
            shipExplosions.add(new ShipExplosion(e.getX() + e.getBitmap().getWidth() / 3, e.getY() + e.getBitmap().getHeight() / 5, e.getShipType()));
        } else if (e.getShipType() == ShipType.IMPERIAL) {
            shipExplosions.add(new ShipExplosion(e.getX() - e.getBitmap().getWidth() / 5, e.getY() + e.getBitmap().getHeight() / 5, e.getShipType()));
        } else if (e.getShipType() == ShipType.BATTLECRUISER) {
            shipExplosions.add(new ShipExplosion(e.getX(), e.getY() + e.getBitmap().getHeight() / 5, e.getShipType()));
        } else if (e.getShipType() == ShipType.MOTHERSHIP) {
            shipExplosions.add(new ShipExplosion(e.getX(), e.getY() + e.getBitmap().getHeight() / 4, e.getShipType()));
        }

        enemyDestroyed(e);

        //set the shipExplosion we just added explosion activate time
        shipExplosions.get(shipExplosions.size() - 1).setExplosionActivateTime(System.nanoTime());
    }

    public void enemyDestroyed(Enemy e) {
        if (e.isWorthEnemyDestroyedPoint()) {
            enemiesDestroyed++;
        }
        //else we dont care
    }

    @Override
    public void update(View v) {
        // long newtime = System.nanoTime();
        // elapsedSecs = (float) (newtime - frtime) / ONESEC_NANOS;
        frtime = System.nanoTime();

        level.checkLevelSequence();//updates level spawning enemies

        if (width == 0) {
            // set variables that rely on screen size
            width = v.getWidth();
            height = v.getHeight();

            playerShip = new PlayerShip(spaceship, spaceshipLaser, (width / 2), (height * 2 / 3));

            mapAnimatorX = width;
            mapAnimatorY = height;

            int randomTime = rand.nextInt(10) + 3;
            powerupSpawnTime = System.nanoTime() + (ONESEC_NANOS * randomTime);

            gameEndTimeCheck = 0;

            act.playSound(Sound.BATTLE);


        }

        if (gamestate == State.RUNNING) {


            //live percentages for lives rectangle
            if (lives >= 0) {
                livesPercentage = (width / 9) / 4 - ((((width / 9) / 4 - width / 3) / START_NUMLIVES) * lives);
            } else {
                livesPercentage = (width / 9) / 4;
            }

            if (frtime < powerupEndTime) {
                float timeLeftPercentage = (float) (powerupEndTime - frtime) / powerupTimeDuration;

                powerupCounterPercentage = (width / 5) + (width * 32 / 100 - width / 5) * timeLeftPercentage;
            } else {
                powerupCounterPercentage = width * 328 / 1000;
            }


            //powerup spawning
            if (powerupSpawnTime < frtime) {
                int randomChoice = rand.nextInt(5);
                int randomX = rand.nextInt(width * 9 / 10) + width / 20;
                if (randomChoice == 0) {
                    powerups.add(new HealthPack(healthPack, (float) randomX, -height / 10, 1));
                } else if (randomChoice == 1) {
                    powerups.add(new DoubleFire(doubleFire, (float) randomX, -height / 10));
                } else if (randomChoice == 2) {
                    powerups.add(new Nuke(nuke, (float) randomX, -height / 10));
                } else if (randomChoice == 3) {
                    powerups.add(new SlowTime(slowTime, (float) randomX, -height / 10));
                } else if (randomChoice == 4) {
                    powerups.add(new Forcefield(shield, (float) randomX, -height / 10));
                }

                int randomSpawnTime = rand.nextInt(10) + 3;
                powerupSpawnTime = System.nanoTime() + (ONESEC_NANOS * randomSpawnTime);
            }

            for (Powerup p : powerups) {
                p.setY(p.getY() + p.getDy());

                if (playerShip.hasCollision(p.getX(), p.getY()) ||
                        playerShip.hasCollision(p.getX() + p.getBitmap().getWidth(), p.getY()) ||
                        playerShip.hasCollision(p.getX() + p.getBitmap().getWidth(), p.getY() + p.getBitmap().getHeight()) ||
                        playerShip.hasCollision(p.getX(), p.getY() + p.getBitmap().getHeight())) {

                    p.setX(10000);

                    if (p.getPowerupType() == PowerupType.HEALTHPACK) {
                        if (lives != START_NUMLIVES && !(lives <= 0)) {
                            lives++;
                        }
                    } else if (p.getPowerupType() == PowerupType.DOUBLEFIRE) {
                        isDoubleFireSpeed = true;
                        powerupEndTime = System.nanoTime() + powerupTimeDuration;
                    } else if (p.getPowerupType() == PowerupType.NUKE) {
                        Iterator<Enemy> enemiesIterator = enemiesFlying.iterator();
                        while (enemiesIterator.hasNext()) {
                            Enemy e = enemiesIterator.next();
                            //checks if actually on the screen
                            if (e.getX() < width && e.getShipType() != ShipType.MOTHERSHIP) {
                                addEnemyExplosion(e);
                                enemiesIterator.remove();

                            }
                        }
                    } else if (p.getPowerupType() == PowerupType.SLOWTIME) {
                        isSlowDown = true;
                        powerupEndTime = System.nanoTime() + powerupTimeDuration;
                    } else if (p.getPowerupType() == PowerupType.FORCEFIELD) {
                        isForcefield = true;
                        powerupEndTime = System.nanoTime() + powerupTimeDuration;
                    }

                }
            }


            //this is an iterator so we can remove enemies safely from the list without concurrent modification errors
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

                    if (!isForcefield) {

                        int playerShipLivesLost = e.getShipType().getLives() / 5;
                        lives = lives - playerShipLivesLost;

                    }

                    if (lives > 0 && !isForcefield) {
                        addEnemyExplosion(e);
                        enemiesIterator.remove();

                    } else if (!isForcefield) {

                        //for red tinge on enemy, not like it matters though, players dead
                        e.setHitContactTimeForTinge(System.nanoTime());

                        shipExplosions.add(new ShipExplosion(playerShip.getX(), playerShip.getY(), ShipType.PLAYER));
                        shipExplosions.get(shipExplosions.size() - 1).setExplosionActivateTime(System.nanoTime());
                    }

                }


                //ship explodes when charges into opposite side of screen
                if (e.getY() > height * 12 / 13) {
                    addEnemyExplosion(e);
                    enemiesIterator.remove();
                }


                    /*
                     * Firing AI
                     */


                Iterator<ShipLaser> shipLaser = shipLasers.iterator();
                while (shipLaser.hasNext()) {
                    ShipLaser sl = shipLaser.next();
                    if (!sl.isEnemyLaser()) {
                        if ((e.hasCollision(sl.getX(), sl.getY()))) {
                            shipLaser.remove();
                            e.setHitContactTimeForTinge(System.nanoTime());
                            //subtract a life
                            e.setLives(e.getLives() - 1);

                            //fun explosions
                            if (e.getLives() == 0) {

                                addEnemyExplosion(e);
                                //this deletes it on next iterator runthrough
                                e.setAIDisabled(true);


                            } else {
                                e.setEnemyIsHitButNotDead(true);
                            }


                        }
                    }
                }

                //enemy laser firing, if firing time is 0, set the time variables
                if (e.getEnemyFiringTime() == 0) {
                    e.setRandomlyGeneratedEnemyFiringTimeInSeconds((rand.nextInt(5000) + 1000) / 1000);
                    e.setEnemyFiringTime(System.nanoTime() + (long) (ONESEC_NANOS * e.getRandomlyGeneratedEnemyFiringTimeInSeconds()));
                }
                if (e.getEnemyFiringTime() < frtime) {
                    e.setRandomlyGeneratedEnemyFiringTimeInSeconds((rand.nextInt(5000) + 1000) / 1000);
                    e.setEnemyFiringTime(System.nanoTime() + (long) (ONESEC_NANOS * e.getRandomlyGeneratedEnemyFiringTimeInSeconds()));
                    if (e.getShipType() == ShipType.FIGHTER) {
                        shipLasers.add(new DiagonalLaser(e, fighterOrb, e.getX() + e.getBitmap().getWidth() * 3 / 5, e.getY() + e.getBitmap().getHeight() / 2, 1));
                        shipLasers.add(new ShipLaser(e, fighterOrb, e.getX() + e.getBitmap().getWidth() / 3, e.getY() + e.getBitmap().getHeight() * 3 / 4));
                        shipLasers.add(new DiagonalLaser(e, fighterOrb, e.getX() + e.getBitmap().getWidth() / 6, e.getY() + e.getBitmap().getHeight() / 2, -1));
                    } else if (e.getShipType() == ShipType.IMPERIAL) {
                        shipLasers.add(new ShipLaser(e, imperialOrb[0], e.getX() + e.getBitmap().getWidth() / 6, e.getY() + e.getBitmap().getHeight() * 4 / 5));
                    } else if (e.getShipType() == ShipType.BATTLECRUISER) {
                        int randomSide = rand.nextInt(2);
                        if (randomSide == 0) {
                            shipLasers.add(new ShipLaser(e, battlecruiserFire[0], e.getX() + e.getBitmap().getWidth() / 10, e.getY() + e.getBitmap().getHeight() * 3 / 4, 2.0f));
                        } else {
                            shipLasers.add(new ShipLaser(e, battlecruiserFire[0], e.getX() + e.getBitmap().getWidth() * 65 / 100, e.getY() + e.getBitmap().getHeight() * 3 / 4, 2.0f));
                        }
                    }

                }


                /*
                 * Movement AI
                 */

                //handles collision for multiple enemies
                if (!e.isAIDisabled()) {


                    //for slow down powerup
                    if (isSlowDown) {
                        e.setX(e.getX() + e.getVx() / 5);
                        e.setY(e.getY() + e.getVy() / 5);
                    } else {
                        e.setX(e.getX() + e.getVx());
                        e.setY(e.getY() + e.getVy());
                    }

                    //this starts the next stage of enemy movement
                    if (!e.isAIStarted()) {
                        e.setX(rand.nextInt(width * 4 / 5));
                        if (e.getShipType() == ShipType.MOTHERSHIP) {
                            e.setY(-height / 3);
                        } else {
                            e.setY(-height / 7);
                        }
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
                            if (e.getRandomVelocityGeneratorX() > (e.getRandomDirectionSpeed() / 1000) / 2) {
                                e.setRandomVelocityGeneratorX(e.getRandomVelocityGeneratorX() - (e.getRandomDirectionSpeed() / 1000));
                            }


                            if (e.getRandomVelocityGeneratorY() > (e.getRandomDirectionSpeed() / 1000) / 2) {
                                e.setRandomVelocityGeneratorY(e.getRandomVelocityGeneratorY() - (e.getRandomDirectionSpeed() / 1000));

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
                            if (e.getX() < 0 || e.getX() > width * 9 / 10) {
                                //this check disables the ability for ship to get too far and then freeze in place
                                if (e.getX() < 0) {
                                    e.setX(0);
                                } else if (e.getX() > width * 9 / 10) {
                                    e.setX(width * 9 / 10);
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

                        } else if (e.isSpeedingUp() && (frtime > e.getLastSpedUpVelocityTime())) {


                            //will not have asymptotes like the last one
                            e.setVx(e.getVx() + (e.getRandomVelocityGeneratorX() / 50));
                            e.setVy(e.getVy() + (e.getRandomVelocityGeneratorY() / 50));

                            //borders for x and y
                            if (e.getX() < 0 || e.getX() > width * 9 / 10) {
                                //this check disables the ability for ship to get too far and then freeze in place
                                if (e.getX() < 0) {
                                    e.setX(0);
                                } else if (e.getX() > width * 9 / 10) {
                                    e.setX(width * 9 / 10);
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
                            e.setLastSpedUpVelocityTime(System.nanoTime() + (ONESEC_NANOS / 100));
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


                } else {
                    //if ai is disabled, delete the bastard cause hes useless
                    enemiesIterator.remove();
                }


            }
        }


         /*
             * Ship lasers
             */


        Iterator<ShipLaser> shipLaser = shipLasers.iterator();
        while (shipLaser.hasNext()) {
            ShipLaser sl = shipLaser.next();

            if (sl.isEnemyLaser()) {
                //PLAYER HIT ***********

                //accuracy since cant use rect for some reason
                if (playerShip.hasCollision(sl.getX(), sl.getY())
                        || playerShip.hasCollision(sl.getX() + sl.getBmp().getWidth(), sl.getY())
                        || playerShip.hasCollision(sl.getX() + sl.getBmp().getWidth(), sl.getY() + sl.getBmp().getHeight())
                        || playerShip.hasCollision(sl.getX(), sl.getY() + sl.getBmp().getHeight())) {

                    //bye bye
                    shipLaser.remove();

                    //need that red tinge which will be in draw method
                    if (lives != 0) {
                        playerShip.setShipHitForTingeTime(System.nanoTime());
                        playerShip.setPlayerHitButNotDead(true);
                        loseLife();
                    }


                }
            }


            //if powerup slow down is activated, slow it down by five times
            if (isSlowDown && sl.isEnemyLaser()) {
                sl.setX(sl.getX() + sl.getDx() / 5);
                sl.setY(sl.getY() + sl.getDy() / 5);
            } else {
                sl.setX(sl.getX() + sl.getDx());
                sl.setY(sl.getY() + sl.getDy());
            }

            //deletes enemy orbs if off the screen
            if (shipLasers.size() != 0 && sl.getY() > height || sl.getX() < -100 || sl.getX() > width * 4 / 3 || sl.getY() < -height) {
                shipLaser.remove();
            }

        }

        //spaceship decay
        if (playerShip != null && playerShip.getY() < height * 15 / 16 && !playerShip.isSpaceshipMoving()) {
            playerShip.setY(playerShip.getY() + DECAY_SPEED);
        }

        //makes main spaceship lasers

        if (playerShip != null && playerShip.getLastLaserSpawnTime() < frtime) {

            if (lives > 0) {
                shipLasers.add(new MainShipLaser(playerShip, spaceshipLaser, playerShip.getX() + spaceship.getWidth() / 20, playerShip.getY() + spaceship.getHeight() / 3));
                shipLasers.add(new MainShipLaser(playerShip, spaceshipLaser, shipLasers.get(shipLasers.size() - 1).getX() + spaceship.getWidth() * 80 / 100, playerShip.getY() + spaceship.getHeight() / 3));
            }
            if (!isDoubleFireSpeed) {
                playerShip.setLastLaserSpawnTime(System.nanoTime() + ONESEC_NANOS / 2);
            } else {
                playerShip.setLastLaserSpawnTime(System.nanoTime() + (ONESEC_NANOS / 4));

                if (powerupEndTime < frtime) {
                    isDoubleFireSpeed = false;
                }
            }
        }

        if (isSpawnEnemyImperial) {
            spawnEnemy(ShipType.IMPERIAL, false);
            //subtracts an enemy destroyed because this imperial spawn is from mothership
            isSpawnEnemyImperial = false;
        }

        //ending powerups
        if (powerupEndTime < frtime && isDoubleFireSpeed) {
            isDoubleFireSpeed = false;
        }
        if (powerupEndTime < frtime && isSlowDown) {
            isSlowDown = false;
        }
        if (powerupEndTime < frtime && isForcefield) {
            isForcefield = false;
        }


        //animator for map background
        mapAnimatorY += 2.0f;
        //this means the stars are off the screen
        if (mapAnimatorY >= height * 2) {
            mapAnimatorY = height;
        }


        //this is outside the last if statement for CURRENTGAME because I want the explosion to finish its animation even if you win/lose during the period of the explosion
        Iterator<ShipExplosion> explosionIterator = shipExplosions.iterator();
        while (explosionIterator.hasNext()) {
            ShipExplosion se = explosionIterator.next();

            //for some reason, removing the explosion caused major lag. So we're doing it this way for now
            if (se.getCurrentFrame() == 11) {
                explosionIterator.remove();
                if (se.getShip() == ShipType.PLAYER) {
                    gamestate = State.PLAYERDIED;
                }
            }

            //using nanosecond time to create delays for animation completion
            if (se.getExplosionActivateTime() + (ONESEC_NANOS / 20) < frtime) {
                se.setExplosionActivateTime(System.nanoTime());
                if (se.getCurrentFrame() < 11) {
                    se.nextFrame();
                }

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
            c.drawBitmap(level.getMap(), null, new Rect(mapAnimatorX - width, mapAnimatorY - (height * 2), mapAnimatorX, mapAnimatorY - height), p);

            for (Powerup pw : powerups) {
                c.drawBitmap(pw.getBitmap(), pw.getX(), pw.getY(), p);

            }


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


            }


            //explosion drawer
            for (ShipExplosion se : shipExplosions) {
                c.drawBitmap(explosion[se.getCurrentFrame()], se.getX(), se.getY(), p);

            }


            //drawing lasers
            if (shipLasers.size() > 0) {
                for (ShipLaser sl : shipLasers) {

                    if (sl.getShip().getShipType() != ShipType.IMPERIAL && sl.getShip().getShipType() != ShipType.BATTLECRUISER && sl.getShip().getShipType() != ShipType.PLAYER) {
                        c.drawBitmap(sl.getBmp(), sl.getX(), sl.getY(), p);
                    } else if (sl.getShip().getShipType() == ShipType.IMPERIAL) {
                        c.drawBitmap(imperialOrb[sl.getCurrentFrame()], sl.getX(), sl.getY(), p);
                        if (sl.getLastImperialLaserFrameChange() < frtime) {
                            sl.setCurrentFrame(sl.getCurrentFrame() + 1);
                            if (sl.getCurrentFrame() == 8) {
                                sl.setCurrentFrame(0);
                            }
                            sl.setLastImperialLaserFrameChange(System.nanoTime() + (ONESEC_NANOS / 20));
                        }
                    } else if (sl.getShip().getShipType() == ShipType.BATTLECRUISER) {
                        c.drawBitmap(battlecruiserFire[sl.getCurrentFrame()], sl.getX(), sl.getY(), p);
                        if (sl.getLastBattlecruiserLaserFrameChange() < frtime) {
                            sl.setCurrentFrame(sl.getCurrentFrame() + 1);
                            if (sl.getCurrentFrame() == 6) {
                                sl.setCurrentFrame(0);
                            }
                            sl.setLastBattlecruiserLaserFrameChange(System.nanoTime() + (ONESEC_NANOS / 15));
                        }
                    } else if (sl.getShip().getShipType() == ShipType.PLAYER) {
                        if (!isDoubleFireSpeed) {
                            c.drawBitmap(sl.getBmp(), sl.getX(), sl.getY(), p);
                        } else {
                            c.drawBitmap(doubleFireShot, sl.getX(), sl.getY(), p);
                        }
                    }
                }
            }


            //main spaceship if lives does not equal 0 it shows spaceship, if it does, it shows boom boom
            if (lives > 0 && playerShip != null) {


                //drawing either the tinge or the normal spaceship, based off of delays
                if (playerShip.isPlayerHitButNotDead() && !isForcefield) {
                    c.drawBitmap(spaceshipHit, playerShip.getX(), playerShip.getY(), p);
                } else {
                    if (isForcefield) {
                        c.drawBitmap(forceField, null, new Rect(playerShip.getBounds().left - width / 20, playerShip.getBounds().top - height / 20, playerShip.getBounds().right + width / 20, playerShip.getBounds().bottom + height / 20), p);
                    }
                    c.drawBitmap(spaceship, playerShip.getX(), playerShip.getY(), p);
                }

                if (playerShip.getShipHitForTingeTime() + (ONESEC_NANOS / 5) < frtime) {
                    playerShip.setPlayerHitButNotDead(false);
                }


            }

            if (level.getMapEdge() != null) {
                c.drawBitmap(level.getMapEdge(), null, scaledDst, p);
                c.drawBitmap(level.getMapEdge(), null, new Rect(mapAnimatorX - width, mapAnimatorY - (height * 2), mapAnimatorX, mapAnimatorY - height), p);
            }

            //topui
            c.drawBitmap(slantedContainer, null, new Rect(0, 0, width * 38 / 100, height / 12), p);
            // c.drawBitmap(normalContainer, null, new Rect(width*38/100, 0, width*3/5,height/12), p);
            //  c.drawBitmap(normalContainer, null, new Rect(width*3/5, 0, width,height/12), p);
            c.drawBitmap(lifeBarEmpty, null, new Rect((width * 140 / 1000) / 5, height / 48, width * 34 / 100, (height * 679 / 10000)), p);
            c.drawBitmap(lifeBarRect, null, new Rect((width * 144 / 1000) / 4, height / 35, livesPercentage, (height / 23)), p);
            c.drawBitmap(powerupTimeRect, null, new Rect((width * 202 / 1000), height * 52 / 1000, (int) powerupCounterPercentage, (height * 61 / 1000)), p);


            p.setColor(Color.WHITE);
            p.setTextSize(act.TS_NORMAL);
            p.setTypeface(act.getGameFont());


            if (gamestate == State.WIN || gamestate == State.PLAYERDIED) {

                //end game time check for delaying menu hit
                if (gameEndTimeCheck == 0) {
                    gameEndTimeCheck = System.nanoTime();
                }

                if (gamestate == State.PLAYERDIED) {
                    c.drawBitmap(gameOverOverlay, null, new Rect(0, 0, width, height), p);

                    c.drawBitmap(noStar, width * 32 / 100, height / 5, p);
                    c.drawBitmap(playerDiedText, width * 35 / 100, height * 38 / 100, p);

                } else {

                    c.drawBitmap(gameOverOverlay, null, new Rect(0, 0, width, height), p);
                    //playerwon
                    c.drawBitmap(playerWonText, width * 32 / 100, height * 38 / 100, p);

                    if (starsEarned == 3) {
                        c.drawBitmap(threeStar, width * 35 / 100, height / 5, p);

                    } else if (starsEarned == 2) {
                        c.drawBitmap(twoStar, width * 35 / 100, height / 5, p);
                    } else {
                        c.drawBitmap(oneStar, width * 35 / 100, height / 5, p);
                    }


                }

                drawCenteredText(c, "Press to continue", height * 3 / 5, p, 0);


            }


        } catch (Exception e) {
            Log.e(MainActivity.LOG_ID, "draw", e);
            e.printStackTrace();
        }

    }

    public void playerWon() {
        if (lives >= 6) {
            starsEarned = 3;
        } else if (lives >= 3) {
            starsEarned = 2;
        } else {
            starsEarned = 1;
        }
        gamestate = State.WIN;
        levelCompleted = true;

        //write to data file
        try {

            BufferedWriter f = new BufferedWriter(new FileWriter(act.getFilesDir() + HIGHSCORE_FILE));


            for (int i = 0; i < (currentLevel - 1) * 2; i++) {
                f.write(receivingInfo[i] + "\n");
            }


            f.write(Boolean.toString(levelCompleted) + "\n");

            if (starsEarnedFile <= starsEarned) {
                f.write(Integer.toString(starsEarned) + "\n");
            } else {
                f.write(Integer.toString(starsEarnedFile) + "\n");
            }

            for (int i = ((currentLevel - 1) * 2) + 2; i < 13; i++) {
                f.write(receivingInfo[i] + "\n");
            }

            f.close();
        } catch (Exception e) {
            Log.d(MainActivity.LOG_ID, "WriteHiScore", e);
        }


    }

    public void spawnEnemy(ShipType shipType, boolean isWorthEnemyDestroyedPoint) {
        if (shipType == ShipType.FIGHTER) {
            enemiesFlying.add(new Fighter(fighter, fighterHit, isWorthEnemyDestroyedPoint));
        } else if (shipType == ShipType.IMPERIAL) {
            enemiesFlying.add(new Imperial(imperial, imperialHit, isWorthEnemyDestroyedPoint));

        } else if (shipType == ShipType.BERSERKER) {
            enemiesFlying.add(new Berserker(berserker, berserkerHit, isWorthEnemyDestroyedPoint));

        } else if (shipType == ShipType.MOTHERSHIP) {
            enemiesFlying.add(new Mothership(mothership, mothershipHit, isWorthEnemyDestroyedPoint));
        } else if (shipType == ShipType.BATTLECRUISER) {
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

    @Override
    public boolean onTouch(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;

            case MotionEvent.ACTION_MOVE:


                if (playerShip != null && playerShip.hasCollision(e.getX(), e.getY()) && gamestate == State.RUNNING) {
                    playerShip.setSpaceshipIsMoving(true);
                    playerShip.setX(e.getX() - spaceship.getWidth() / 2);
                    playerShip.setY(e.getY() - spaceship.getHeight() / 2);

                }

                break;
            case MotionEvent.ACTION_UP:

                if (gameEndTimeCheck + (ONESEC_NANOS) < frtime) {
                    if ((gamestate == State.PLAYERDIED || gamestate == State.WIN)) {
                        act.onBackPressed(); //just simulates them pressing the back button, resets the game stats and whatnot

                    }
                }


                if (playerShip != null) {
                    playerShip.setSpaceshipIsMoving(false);
                }

                break;
        }

        return true;
    }
}