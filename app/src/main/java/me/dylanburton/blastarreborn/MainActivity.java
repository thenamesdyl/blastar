package me.dylanburton.blastarreborn;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity {
    static final String LOG_ID = "Dylan";
    static final float EXPECTED_DENSITY = 315.0f;  // original target density of runtime device
    static final float EXPECTED_WIDTH = 720.0f;  // original target width of runtime device
    int TS_NORMAL; // normal text size
    int TS_BIG; // large text size
    float densityscalefactor;
    float sizescalefactor;
    DisplayMetrics dm;
    Screen entryScreen;
    PlayScreen playScreen;
    LevelScreen levelScreen;
    Screen currentScreen;
    FullScreenView mainView;
    Typeface gamefont;
    Typeface levelfont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            super.onCreate(savedInstanceState);
            dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            gamefont = Typeface.createFromAsset(getAssets(), "fonts/elitedanger.ttf");
            levelfont = Typeface.createFromAsset(getAssets(), "fonts/sugarpunch.ttf");

            //consistent dpi/screen stuff
            densityscalefactor = (float)dm.densityDpi / EXPECTED_DENSITY;
            if (densityscalefactor > 1.5f)
                densityscalefactor = 1.5f;
            else if (densityscalefactor < 0.5f)
                densityscalefactor = 0.5f;
            sizescalefactor = (float)dm.widthPixels / EXPECTED_WIDTH;
            if (sizescalefactor > 2f)
                sizescalefactor = 2f;
            else if (sizescalefactor < 0.4f)
                sizescalefactor = 0.4f;
            TS_NORMAL = (int)(38 * sizescalefactor);
            TS_BIG = (int)(70 * sizescalefactor);

            // create screens
            entryScreen = new EntryScreen(this);
            playScreen = new PlayScreen(this);
            levelScreen = new LevelScreen(playScreen, this);

            mainView = new FullScreenView(this);
            setContentView(mainView);

        } catch (Exception e) {
            // tell me specifically whats happening and where
            Log.d(LOG_ID, "onCreate", e);
        }
    }

    BitmapFactory.Options sboptions = new BitmapFactory.Options();
    /**
     * load and scale bitmap according to the apps scale factors.
     *
     */
    public Bitmap getScaledBitmap(String fname) throws IOException
    {
        sboptions.inScreenDensity = dm.densityDpi;
        sboptions.inTargetDensity =  dm.densityDpi;
        sboptions.inDensity = (int)(dm.densityDpi / sizescalefactor); // hack: want to load bitmap scaled for width, abusing density scaling options to do it
        InputStream inputStream = getAssets().open(fname);
        Bitmap btm = BitmapFactory.decodeStream(inputStream, null, sboptions);
        inputStream.close();
        return btm;
    }


    DisplayMetrics getDisplayMetrics() { return dm; }

    /**
     * Handle resuming of the game,
     */
    @Override
    protected void onResume() {
        super.onResume();
        mainView.resume();
    }

    /*
     * Back pressed
     */
    @Override
    public void onBackPressed() {
        if(currentScreen != entryScreen) {
            if(currentScreen == playScreen){


                levelScreen.resetVariables();
                currentScreen = levelScreen;
                playScreen.resetGame();


            }else if(currentScreen == levelScreen){
                currentScreen = entryScreen;
                levelScreen.resetVariables();
            }

        }else{
            System.exit(0);
        }


    }

    /**
     * Handle pausing of the game.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mainView.pause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public Typeface getGameFont() {
        return gamefont;
    }
    public Typeface getLevelFont() {
        return levelfont;
    }


    /**
     * Start a new game.
     */
    public void startGame(int level) {
        this.playScreen.initGame(level);
        currentScreen = this.playScreen;
    }

    public void startLevelScreen(){
        currentScreen = this.levelScreen;
    }

    /**
     * Leave game and return to title screen.
     */
    public void leaveGame() {
        currentScreen = this.entryScreen;
    }

    /**
     * completely exit the game.
     */
    public void exit() {
        finish();
        System.exit(0);
    }

    /**
     * This inner class handles the main render loop, and delegates drawing and event handling to
     * the individual screens.
     */
    private class FullScreenView extends SurfaceView implements Runnable, View.OnTouchListener {
        private volatile boolean isRendering = false;
        Thread renderThread = null;
        SurfaceHolder holder;

        public FullScreenView(Context context) {
            super(context);
            holder = getHolder();
            currentScreen = entryScreen;
            setOnTouchListener(this);
        }

        public void resume() {
            isRendering = true;
            renderThread = new Thread(this);
            renderThread.start();
        }

        @Override
        public void run() {
            try {
                while(isRendering){
                    while(!holder.getSurface().isValid()) {
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) { /* we don't care */  }
                    }

                    // update screen's context
                    currentScreen.update(this);

                    // draw screen
                    Canvas c = holder.lockCanvas();
                    currentScreen.draw(c, this);
                    holder.unlockCanvasAndPost(c);
                }
            } catch (Exception e) {

                Log.d(LOG_ID, "View", e);
                e.printStackTrace();
            }
        }

        public void pause() {
            isRendering = false;
            while(true) {
                try {
                    renderThread.join();
                    return;
                } catch (InterruptedException e) {
                    // retry
                }
            }
        }

        public boolean onTouch(View v, MotionEvent event) {
            try {

                return currentScreen.onTouch(event);
            }
            catch (Exception e) {
                Log.d(LOG_ID, "onTouch", e);
            }
            return false;
        }
    }
}
