package me.dylanburton.blastarreborn;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
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

import me.dylanburton.blastarreborn.utils.Sound;

public class MainActivity extends Activity {
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
    AboutScreen aboutScreen;
    Screen currentScreen;
    FullScreenView mainView;
    Typeface gamefont;
    Typeface levelfont;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {

            super.onCreate(savedInstanceState);
            dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            gamefont = Typeface.createFromAsset(getAssets(), "fonts/elitedanger.ttf");
            levelfont = Typeface.createFromAsset(getAssets(), "fonts/sugarpunch.ttf");


            // create screens
            entryScreen = new EntryScreen(this);
            playScreen = new PlayScreen(this);
            aboutScreen = new AboutScreen(this);
            levelScreen = new LevelScreen(playScreen, this);

            mainView = new FullScreenView(this);
            setContentView(mainView);

            playSound(Sound.ENTRY);
        } catch (Exception e) {
            // tell me specifically whats happening and where
            Log.d(LOG_ID, "onCreate", e);
        }
    }

    public void playSound(Sound s){
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        if(s == Sound.BATTLE){
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.battlemusic);
        }else if(s == Sound.LEVEL_SELECTION){
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.level);
        }else if(s == Sound.ENTRY){
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.entry);
        }

        mediaPlayer.start();
    }
    BitmapFactory.Options sboptions = new BitmapFactory.Options();
    /**
     * load and scale bitmap according to the apps scale factors.
     *
     */
    public Bitmap getScaledBitmap(String fname) throws IOException
    {
        InputStream inputStream = getAssets().open(fname);
        Bitmap btm = BitmapFactory.decodeStream(inputStream);
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
        mediaPlayer.start();
    }

    /*
     * Back pressed
     */
    @Override
    public void onBackPressed() {
        if(currentScreen != entryScreen) {
            if(currentScreen == playScreen){

                levelScreen.resetVariables();
                playSound(Sound.LEVEL_SELECTION);
                currentScreen = levelScreen;

            }else if(currentScreen == levelScreen){
                playSound(Sound.ENTRY);
                currentScreen = entryScreen;
                levelScreen.resetVariables();
            }else if(currentScreen == aboutScreen){
                currentScreen = entryScreen;
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
        mediaPlayer.pause();
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
                        //this is the delay before the screen starts
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
