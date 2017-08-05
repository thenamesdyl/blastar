package me.dylanburton.blastarreborn.levels;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by Dylan on 7/17/2017.
 */

public abstract class Level {
    public abstract void checkLevelSequence();
    public abstract void setUpdateLevelStage(int updateBoundaryChecker);
    public abstract Bitmap getMap();
    public abstract Bitmap getMapEdge();

}
