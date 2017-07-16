package me.dylanburton.blastarreborn.Enemies;

/**
 * Created by Dylan on 7/16/2017.
 */

public enum EnemyType {
    //Name of ship followed by how many hits they can take
    FIGHTER(2,20),
    IMPERIAL(3,50),
    BATTLECRUISER(3,100),
    BATTLESHIP(5,200),
    BERSERKER(9,500);

    private final int lives;
    private final int points;

    EnemyType(int lives, int points) {
        this.lives = lives;
        this.points = points;
    }
    int getLives() {
        return this.lives;
    }
    int getPoints(){
        return this.points;
    }

}