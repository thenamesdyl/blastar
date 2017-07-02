package me.dylanburton.blastarreborn.Enemies;

import me.dylanburton.blastarreborn.Tools.Coordinate;
import me.dylanburton.blastarreborn.Tools.Velocity;

/**
 * Created by Dylan on 7/1/2017.
 */

public interface Enemy {

    //everytime movement behavior is called, will slightly change velocities of enemy. If contact with another enemy, reverses directions
    public Velocity movementBehavior();

    public Coordinate spawn();
    public void shoot();

}
