package me.dylanburton.blastarreborn.Enemies;

/**
 * Created by Dylan on 7/1/2017.
 */

public interface Enemy {

    //everytime movement behavior is called, will slightly change velocities of enemy. If contact with another enemy, reverses directions
    public void movementBehavior();

    public void spawn();
    public void shoot();

}
