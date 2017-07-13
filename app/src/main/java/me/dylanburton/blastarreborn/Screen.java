package me.dylanburton.blastarreborn;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public abstract class Screen {


    /**
     * Update the screen's state.
     */
    public abstract void update(View v);

    /**
     * draw the screen to the canvas.
     */
    public abstract void draw(Canvas c, View v);

    /**
     * handle touch event.
     */
    public abstract boolean onTouch(MotionEvent e);

    /**
     * returns tru if the inpassed event location is within the inpassed rectangle bounds.

     */
    boolean eventInBounds(MotionEvent event, int x, int y, int width, int height) {
        float ex = event.getX();
        float ey = event.getY();
        return (ex > x && ex < x + width - 1 &&
                ey > y && ey < y + height - 1);
    }
}
