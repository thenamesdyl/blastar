package me.dylanburton.blastarreborn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.InputStream;

public class EntryScreen extends Screen {
    MainActivity act;
    Paint p = new Paint();
    Bitmap screenbtm, playbtm, exitbtm;
    Rect scaledDst = new Rect(); // generic rect for scaling
    Rect playBtnBounds = null;
    Rect exitBtnBounds = null;
    private int width;
    private int height;


    public EntryScreen(MainActivity act) {
        this.act = act;
        try {
            // load screen bg
            AssetManager assetManager = act.getAssets();
            InputStream inputStream = assetManager.open("entryscreen.png");
            screenbtm = BitmapFactory.decodeStream(inputStream);
            inputStream.close();


            playbtm = act.getScaledBitmap("playbtn.png");

            exitbtm = act.getScaledBitmap("exitbtn.png");
        }
        catch (Exception e) {
            // what to do with an exception here on android?
            Log.d(MainActivity.LOG_ID, "onTouch", e);
        }
    }

    @Override
    public void update(View v) {
      // nothing to update
    }


    private void drawCenteredText(Canvas c, String msg, int height, Paint p, int shift) {
        c.drawText(msg, (width - p.measureText(msg)) / 2 + shift, height, p);
    }


    @Override
    public void draw(Canvas c, View v) {
        width = v.getWidth();
        height = v.getHeight();
        if (playBtnBounds == null) {
            // initialize button locations
            playBtnBounds = new Rect(width/2 - playbtm.getWidth()/2,
                    height/6 - playbtm.getHeight(),
                    width/2 + playbtm.getWidth()/2,
                    height/6 + playbtm.getHeight()/2);
            exitBtnBounds = new Rect(width*85/100 - exitbtm.getWidth()/2,
                    height*3/5  - exitbtm.getHeight()/2,
                    width*4/5 + exitbtm.getWidth()/2,
                    height*4/5 + exitbtm.getHeight()/2);
        }

        // draw the screen
        scaledDst.set(0, 0, width, height);
        c.drawBitmap(screenbtm, null, scaledDst, p);

        // version/copyright line
        p.setColor(Color.rgb(0,70,0));  // dark greenish
        p.setTextSize(act.TS_NORMAL);
        p.setTypeface(act.getGameFont());
        String msg = "1.0";
        int xTextEnd = (int)(width*.99f);
        c.drawText(msg, xTextEnd-p.measureText(msg), height - 80, p);
        int w1 = scaledDst.width();
        msg = "(c) 2017 Dylan Burton";
        c.drawText(msg, xTextEnd-p.measureText(msg), height - 40, p);
        p.setColor(Color.rgb(255,55,55));
        p.setTextSize(200);

        drawCenteredText(c, "Play", height*22/100-playbtm.getHeight()/2,p,0);
        p.setColor(Color.rgb(0,0,0));
        p.setTextSize(70);
        drawCenteredText(c, "About", height*78/100-playbtm.getHeight()/2,p,-width*31/100);
        p.setTextSize(70);
        drawCenteredText(c, "Exit", height*78/100-playbtm.getHeight()/2,p,+width*31/100);
        p.setTextSize(300);
        p.setColor(Color.rgb(255,255,255));
        drawCenteredText(c, "Blastar", height*14/15,p,0);

    }

    @Override
    public boolean onTouch(MotionEvent e) {
        if (playBtnBounds.contains((int)e.getX(), (int)e.getY()))
            act.startGame();
        if (exitBtnBounds.contains((int)e.getX(), (int)e.getY()))
            act.exit();

        // we don't care about followup events in this screen
        return false;
    }
}
