package me.dylanburton.blastarreborn.levels;

import android.graphics.Bitmap;

import me.dylanburton.blastarreborn.PlayScreen;
import me.dylanburton.blastarreborn.enemies.EnemyType;

/**
 * Created by Dylan on 7/17/2017.
 */

public class Level1 extends Level{
    private Bitmap map;
    private PlayScreen ps;
    private int updateCheckerBoundary = 0; //defends against the checkers constantly drawing ships

    public Level1(PlayScreen ps){
        this.ps = ps;
    }

    public void checkLevelSequence(){

        //todo fix bug that if too many fighters are destroyed at the same time, the level doesnt progress
        if(ps.getEnemiesDestroyed() >=0 && ps.getEnemiesDestroyed() < 2 && updateCheckerBoundary == 0) {
            for (int i = 0; i < 3; i++) {
                ps.spawnEnemy(EnemyType.FIGHTER);
            }
            ps.spawnEnemy(EnemyType.BERSERKER);
            updateCheckerBoundary = 2;
        }else if(ps.getEnemiesDestroyed() >= 2 && ps.getEnemiesDestroyed() < 4 && updateCheckerBoundary == 2){
            for(int i = 0; i < 3; i++){
                ps.spawnEnemy(EnemyType.FIGHTER);
            }
            updateCheckerBoundary = 4;
        }else if(ps.getEnemiesDestroyed() >= 4 && updateCheckerBoundary == 4){
            for(int i = 0; i < 3; i++){
                ps.spawnEnemy(EnemyType.FIGHTER);
            }
            updateCheckerBoundary = 9;
        }

        if(ps.getEnemiesDestroyed() == 10){
            ps.playerWon();
        }

    }

    public int getUpdateCheckerBoundary() {
        return updateCheckerBoundary;
    }

    public void setUpdateCheckerBoundary(int updateCheckerBoundary) {
        this.updateCheckerBoundary = updateCheckerBoundary;
    }
}
