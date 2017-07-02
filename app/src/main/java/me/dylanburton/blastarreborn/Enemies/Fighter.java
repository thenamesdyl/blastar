package me.dylanburton.blastarreborn.Enemies;

import me.dylanburton.blastarreborn.Tools.Coordinate;
import me.dylanburton.blastarreborn.Tools.Velocity;

/**
 * Created by Dylan on 7/1/2017.
 */

public class Fighter implements Enemy{



    //hopefully is being called multiple times in playScreen's runnerThread
    public Velocity movementBehavior(){

        return new Velocity(5,5);

    }

    public void shoot(){

    }

    public Coordinate spawn(){


        return new Coordinate(5,5);


    }

}
