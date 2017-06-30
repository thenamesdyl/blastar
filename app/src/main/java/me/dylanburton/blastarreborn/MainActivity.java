package me.dylanburton.blastarreborn;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView titleTextView;
    ImageView centerImage, shipImage, centerImageAnimationHelper, centerImagePlay, centerImageAnimationHelperPlay,shipTopView;
    ConstraintLayout playLayout;
    Button aboutButton, playButton;
    ValueAnimator animator;
    String currentScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentScreen= "entry";
        setContentView(R.layout.activity_main);




    }

    @Override
    public void onBackPressed() {
        if(!currentScreen.equals("entry")) {
            setContentView(R.layout.activity_main);

            currentScreen = "entry";
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

    public String getCurrentScreen(){
        return currentScreen;
    }

    public void startVerticalAnimation(){
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(30000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                final float width = centerImagePlay.getHeight();
                final float translationY = (width * progress);
                centerImagePlay.setTranslationY(translationY);
                centerImageAnimationHelperPlay.setTranslationY(translationY - width);
            }
        });
        animator.start();
    }

    public void startButtonListeners(){

        aboutButton = (Button) findViewById(R.id.aboutButton);
        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currentScreen = "play";
                setContentView(R.layout.play_main);
                centerImagePlay = (ImageView) findViewById(R.id.centerImagePlay);
                centerImageAnimationHelperPlay = (ImageView) findViewById(R.id.centerImageAnimationHelperPlay);
                startVerticalAnimation();
                shipTopView = (ImageView) findViewById(R.id.shipTopView);
                shipTopView.setBackgroundResource(R.drawable.spaceshiptopview);

                playLayout = (ConstraintLayout) findViewById(R.id.playLayout);



                playLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        if (event.getAction() == MotionEvent.ACTION_DOWN){

                            shipTopView.setY(event.getY()-200);
                            shipTopView.setX(event.getX()-200);
                        }



                        return true;

                    }

                });


            }
        });


        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentScreen = "about";
                setContentView(R.layout.about_main);
            }
        });


    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(currentScreen.equals("entry")) {
            centerImage = (ImageView) findViewById(R.id.centerImage);
            centerImageAnimationHelper = (ImageView) findViewById(R.id.centerImageAnimationHelper);

            titleTextView = (TextView) findViewById(R.id.titleTextView);
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/elitedanger.ttf");
            titleTextView.setTypeface(typeface);


            startButtonListeners();
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


            shipImage = (ImageView) findViewById(R.id.shipImage);
            shipImage.setBackgroundResource(R.drawable.shipanimation);

            // Get the background, which has been compiled to an AnimationDrawable object.
            AnimationDrawable frameAnimation = (AnimationDrawable) shipImage.getBackground();

            // Start the animation (looped playback by default).
            frameAnimation.start();
        }else if(currentScreen.equals("play")){

            startVerticalAnimation();
        }
    }
}
