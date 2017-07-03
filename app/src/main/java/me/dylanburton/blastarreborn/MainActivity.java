package me.dylanburton.blastarreborn;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    TextView titleTextView;
    ImageView centerImage, shipImage, centerImageAnimationHelper;
    ConstraintLayout playLayout;
    Button aboutButton, playButton;
    ValueAnimator animator;
    TranslateAnimation anim;

    AssetManager am;
    DisplayMetrics dm;


    public enum Screen{ ENTRY, PLAY, ABOUT};
    public Screen currentScreen = Screen.ENTRY;


    PlayScreen playScreen;
    AboutScreen aboutScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        dm = new DisplayMetrics();
        am = getAssets();
        playScreen = new PlayScreen(this);;
        aboutScreen = new AboutScreen(this);



    }

    public AssetManager getAssetManager(){
        return am;
    }

    @Override
    public void onBackPressed() {
        if(currentScreen != Screen.ENTRY) {
            setContentView(R.layout.content_main);
            if(currentScreen == Screen.PLAY){
                playScreen.pauseRunnerThread();
            }
            currentScreen = Screen.ENTRY;
            onWindowFocusChanged(true);

        }else{
            System.exit(0);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Screen getCurrentScreen(){
        return currentScreen;
    }

    public void setCurrentScreen(String screen){
        if(screen.toLowerCase().equals("entry")){
            this.currentScreen = Screen.ENTRY;
        }else if(screen.toLowerCase().equals("about")){
            this.currentScreen = Screen.ABOUT;

        }else if(screen.toLowerCase().equals("play")){
            this.currentScreen = Screen.PLAY;
        }
    }


    public MainActivity mainClass(){ return this; }


    public void startButtonListeners(){

        aboutButton = (Button) findViewById(R.id.aboutButton);
        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                playScreen.startGame();
          //      setContentView(R.layout.play_main);



            }
        });


        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                aboutScreen.draw();
          //      setContentView(R.layout.about_main);
            }
        });


    }


    BitmapFactory.Options sboptions = new BitmapFactory.Options();
    Bitmap getScaledBitmap(String fname) throws IOException
    {
        sboptions.inScreenDensity = dm.densityDpi;
        sboptions.inTargetDensity =  dm.densityDpi;

        InputStream inputStream = getAssets().open(fname);
        Bitmap btm = BitmapFactory.decodeStream(inputStream, null, sboptions);
        inputStream.close();
        return btm;

//        InputStream inputStream = getAssets().open(fname);
//        Bitmap btm = BitmapFactory.decodeStream(inputStream);
//        inputStream.close();
//        return Bitmap.createScaledBitmap(btm, (int)(btm.getWidth()*sizescalefactor), (int)(btm.getHeight()*sizescalefactor), false);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(currentScreen == Screen.ENTRY) {
            centerImage = (ImageView) findViewById(R.id.centerImage);
            centerImageAnimationHelper = (ImageView) findViewById(R.id.centerImageAnimationHelper);

            titleTextView = (TextView) findViewById(R.id.titleTextView);
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/elitedanger.ttf");
            titleTextView.setTypeface(typeface);


            //starts listeners on play and about button
            startButtonListeners();


            //starts image for background star movement
            animator = ValueAnimator.ofFloat(0.0f, 1.0f);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(30000L);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float progress = (float) animation.getAnimatedValue();
                    final float width = centerImage.getWidth();
                    final float translationX = -(width * progress);
                    centerImage.setTranslationX(translationX);
                    centerImageAnimationHelper.setTranslationX(translationX + width);
                }
            });
            animator.start();

            //the ship animation main screen
            shipImage = (ImageView) findViewById(R.id.shipImage);
            shipImage.setBackgroundResource(R.drawable.shipanimation);

            // Get the background, which has been compiled to an AnimationDrawable object.
            AnimationDrawable frameAnimation = (AnimationDrawable) shipImage.getBackground();

            // Start the animation (looped playback by default).
            frameAnimation.start();
        }else if(currentScreen == Screen.PLAY){


            //keeps ship moving backwards
            playScreen.pauseRunnerThread();
            playScreen.resumeRunnerThread();



        }else if(currentScreen == Screen.ABOUT){

        }
    }



}
