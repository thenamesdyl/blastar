package me.dylanburton.blastarreborn.enemies;

/**
 * Created by Dylan on 7/16/2017.
 */

public enum EnemyType {
    //Name of ship followed by how strong the enemy is then points
    FIGHTER(5,20),
    IMPERIAL(8,50),
    BATTLECRUISER(13,100),
    MOTHERSHIP(15,200),
    BERSERKER(20,500);

    private final int lives;
    private final int points;

    EnemyType(int lives, int points) {
        this.lives = lives;
        this.points = points;
    }
    public int getLives() {
        return this.lives;
    }
    public int getPoints(){
        return this.points;
    }

}