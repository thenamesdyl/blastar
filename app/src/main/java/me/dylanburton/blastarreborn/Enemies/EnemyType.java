package me.dylanburton.blastarreborn.Enemies;

/**
 * Created by Dylan on 7/16/2017.
 */

public enum EnemyType {
    //Name of ship followed by how many hits they can take
    FIGHTER(2),
    IMPERIAL(3),
    BATTLECRUISER(3),
    BATTLESHIP(5),
    BERSERKER(9);

    private final int lives;

    EnemyType(int lives) {
        this.lives = lives;
    }
    int lives() {
        return this.lives;
    }

}