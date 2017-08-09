package me.dylanburton.blastarreborn.spaceships;

/**
 * Created by Dylan on 7/16/2017.
 */

public enum ShipType {
    //Name of ship followed by how strong the enemy is then points
    FIGHTER(7,20),
    IMPERIAL(10,50),
    BATTLECRUISER(13,100),
    MOTHERSHIP(100,200),
    BERSERKER(6,500),
    HEATSINKER(5,20),
    PLAYER(0,0);

    private final int lives;
    private final int points;

    ShipType(int lives, int points) {
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