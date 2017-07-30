package me.dylanburton.blastarreborn.levels;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

import me.dylanburton.blastarreborn.MainActivity;
import me.dylanburton.blastarreborn.PlayScreen;
import me.dylanburton.blastarreborn.enemies.EnemyType;

/**
 * Created by Dylan on 7/25/2017.
 */

public class Level4 extends Level {
    private Bitmap map;
    private PlayScreen ps;
    private int updateCheckerBoundary = 0; //defends against the checkers constantly drawing ships

    public Level4(PlayScreen ps, MainActivity act){

        this.ps = ps;

        try {
            AssetManager assetManager = act.getAssets();
            InputStream inputStream = assetManager.open("maps/lavamap.jpg");
            map = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        }catch(Exception e){
            //dont care sorry
        }
    }

    public void checkLevelSequence(){

        if(ps.getEnemiesDestroyed() >=0 && ps.getEnemiesDestroyed() < 2) {

            if(updateCheckerBoundary == 0) {
                for (int i = 0; i < 3; i++) {
                    ps.spawnEnemy(EnemyType.IMPERIAL);
                }
                ps.spawnEnemy(EnemyType.FIGHTER);
            }

            updateCheckerBoundary = 2;
        }else if(ps.getEnemiesDestroyed() >= 2 && ps.getEnemiesDestroyed() < 4){

            if(updateCheckerBoundary == 2) {
                for (int i = 0; i < 3; i++) {
                    ps.spawnEnemy(EnemyType.BATTLECRUISER);
                }
            }

            updateCheckerBoundary = 4;
        }else if(ps.getEnemiesDestroyed() >= 4 && ps.getEnemiesDestroyed() < 6){

            if(updateCheckerBoundary == 4) {
                for (int i = 0; i < 3; i++) {
                    ps.spawnEnemy(EnemyType.FIGHTER);
                }
            }else if(updateCheckerBoundary == 2){
                updateCheckerBoundary = 4;
            }

            updateCheckerBoundary = 9;
        }else if(ps.getEnemiesDestroyed() >=6){
            if(updateCheckerBoundary == 9){
                for (int i = 0; i < 3; i++) {
                    ps.spawnEnemy(EnemyType.IMPERIAL);
                }
                for (int i = 0; i < 3; i++) {
                    ps.spawnEnemy(EnemyType.FIGHTER);
                }
            }

            updateCheckerBoundary = 11;
        }

        if(ps.getEnemiesDestroyed() == 16){
            ps.playerWon();
        }

    }

    public int getUpdateCheckerBoundary() {
        return updateCheckerBoundary;
    }

    public void setUpdateCheckerBoundary(int updateCheckerBoundary) {
        this.updateCheckerBoundary = updateCheckerBoundary;
    }

    public Bitmap getMap() {
        return map;
    }
}
