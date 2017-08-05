package me.dylanburton.blastarreborn.levels;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

import me.dylanburton.blastarreborn.MainActivity;
import me.dylanburton.blastarreborn.PlayScreen;
import me.dylanburton.blastarreborn.spaceships.ShipType;

/**
 * Created by Dylan on 7/25/2017.
 */

public class Level3 extends Level {
    private Bitmap map;
    private Bitmap mapEdge;
    private PlayScreen ps;
    private int updateLevelStage = 0; //defends against the checkers constantly drawing ships

    public Level3(PlayScreen ps, MainActivity act){

        this.ps = ps;

        try {
            AssetManager assetManager = act.getAssets();
            InputStream inputStream = assetManager.open("maps/map3.jpg");
            map = BitmapFactory.decodeStream(inputStream);
            mapEdge = BitmapFactory.decodeStream(assetManager.open("maps/map3edges.png"));
            inputStream.close();

        }catch(Exception e){
            //dont care sorry
        }
    }

    public void checkLevelSequence(){

        if(ps.getEnemiesDestroyed() >=0) {

            if(updateLevelStage == 0) {
                for (int i = 0; i < 3; i++) {
                    ps.spawnEnemy(ShipType.FIGHTER,true);
                }
                ps.spawnEnemy(ShipType.BERSERKER,true);
                updateLevelStage = 1;
            }

        }
        if(ps.getEnemiesDestroyed() >= 2){

            if(updateLevelStage == 1) {
                for (int i = 0; i < 3; i++) {
                    ps.spawnEnemy(ShipType.FIGHTER,true);
                }
                ps.spawnEnemy(ShipType.BATTLECRUISER,true);
                updateLevelStage = 2;
            }

        }
        if(ps.getEnemiesDestroyed() >= 4){

            if(updateLevelStage == 2) {
                for (int i = 0; i < 2; i++) {
                    ps.spawnEnemy(ShipType.BERSERKER, true);
                }
                updateLevelStage = 3;
            }

        }

        if(ps.getEnemiesDestroyed() == 10){
            ps.playerWon();
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
