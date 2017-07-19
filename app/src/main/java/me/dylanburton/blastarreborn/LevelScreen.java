package me.dylanburton.blastarreborn;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Dylan on 7/19/2017.
 */

public class LevelScreen extends Screen {
    PlayScreen ps;
    MainActivity act;

    public LevelScreen(PlayScreen ps, MainActivity act){
        this.ps = ps;
        this.act = act;
    }

    public void update(View v){}

    public void draw(Canvas c, View v){}


    @Override
    public boolean onTouch(MotionEvent e) {
        //todo implement this screen with level selection choices

        /*
        Boundaries for level selector.
        ps.setCurrentLevel(selected);
        act.startGame();
         */
        return true;
    }
}
