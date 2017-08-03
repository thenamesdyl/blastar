package me.dylanburton.blastarreborn;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Dylan on 8/3/2017.
 */

//simple screen responsible for showing a singular image
public class AboutScreen extends Screen {
    private MainActivity act;
    private Bitmap aboutScreen;
    private Rect scaledDst;
    private int width;
    private int height;
    private Paint p = new Paint();

    public AboutScreen(MainActivity act){
        this.act = act;

        try {
            aboutScreen = act.getScaledBitmap("aboutscreen.png");


        }catch (Exception e){
            //lazy I know
        }
    }

    public void update(View v){

        if(width == 0){
            width = v.getWidth();
            height = v.getHeight();

            scaledDst = new Rect(0,0, width, height);

        }
    }

    public void draw(Canvas c, View v){

        c.drawBitmap(aboutScreen,null,scaledDst,p);

    }

    @Override
    public boolean onTouch(MotionEvent e) {

        return true;
    }
}
