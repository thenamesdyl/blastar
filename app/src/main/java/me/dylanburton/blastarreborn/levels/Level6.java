package me.dylanburton.blastarreborn.levels;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.util.Random;

import me.dylanburton.blastarreborn.MainActivity;
import me.dylanburton.blastarreborn.PlayScreen;
import me.dylanburton.blastarreborn.spaceships.ShipType;

/**
 * Created by Dylan on 7/25/2017.
 */

public class Level6 extends Level {
    private Bitmap map;
    private Bitmap mapEdge;
    private PlayScreen ps;
    private int updateLevelStage = 0; //defends against the checkers constantly drawing ships
    private static final int END_LEVEL = 5;
    private int totalEnemies;
    private int randomAmountShips = 0;
    private int randomShip = 0;
    private Random rand = new Random();

    public Level6(PlayScreen ps, MainActivity act){

        this.ps = ps;

        try {
            AssetManager assetManager = act.getAssets();
            InputStream inputStream = assetManager.open("maps/map6.jpg");
            map = BitmapFactory.decodeStream(inputStream);
            mapEdge = BitmapFactory.decodeStream(assetManager.open("maps/map6edges.png"));
            inputStream.close();

        }catch(Exception e){
            //dont care sorry
        }
    }

    public void checkLevelSequence(){

        /*
         * Everytime player has destroyed all but 2 enemies, the next wave comes in. Once player reaches designated END level, game ends.
         */
        if(ps.getEnemiesDestroyed() >= totalEnemies-2){

            if(updateLevelStage != END_LEVEL) {

                updateLevelStage++;

                if(updateLevelStage != 2) {
                    randomAmountShips = rand.nextInt(9) + 3;
                }else{
                    randomAmountShips = 1;
                }

                for (int i = 0; i < randomAmountShips; i++) {


                    //boss battle yo
                    if(updateLevelStage == 2) {
                        ps.spawnEnemy(ShipType.MOTHERSHIP, true);
                        ps.spawnEnemy(ShipType.FIGHTER, true);
                        ps.spawnEnemy(ShipType.FIGHTER, true);

                        totalEnemies=totalEnemies+3;
                    }
                    randomShip = rand.nextInt(99) + 1;

                    if (randomShip <=30) {
                        ps.spawnEnemy(ShipType.FIGHTER, true);
                    } else if (randomShip > 30 && randomShip <= 60) {
                        ps.spawnEnemy(ShipType.BATTLECRUISER, true);
                    } else if (randomShip > 60 && randomShip <=90) {
                        ps.spawnEnemy(ShipType.IMPERIAL, true);
                    } else if (randomShip > 90) {
                        ps.spawnEnemy(ShipType.BERSERKER, true);
                    }
                    totalEnemies++;
                }
            }else{

                //waits for player to destroy the last two enemies
                if(ps.getEnemiesDestroyed() == totalEnemies){
                    ps.playerWon();
                }
            }

        }

    }

    public int getUpdateLevelStage() {
        return updateLevelStage;
    }

    public void setUpdateLevelStage(int updateLevelStage) {
        this.updateLevelStage = updateLevelStage;
    }

    public Bitmap getMap() {
        return map;
    }

    public Bitmap getMapEdge(){ return mapEdge; }
}
