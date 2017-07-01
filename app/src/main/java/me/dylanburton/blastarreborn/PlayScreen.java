package me.dylanburton.blastarreborn;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

/**
 * Created by Dylan on 6/30/2017.
 */

public class PlayScreen extends Activity{

    MainActivity main;
    playScreenRunnerThread runnerThread = new playScreenRunnerThread(this);

    Boolean dragActive = false;

    //after you let go of the ship, this is how fast it falls back to default y position
    public int shipScreenDownwardDecayRate = 1;

    ImageView centerImagePlay, centerImageAnimationHelperPlay, shipTopView;
    ConstraintLayout playLayout;
    ValueAnimator animator;
    public PlayScreen(MainActivity main){
        this.main = main;
    }


    public void draw(){
        main.setContentView(R.layout.play_main);
        main.setCurrentScreen("play");
        centerImagePlay = (ImageView) main.findViewById(R.id.centerImagePlay);
        centerImageAnimationHelperPlay = (ImageView) main.findViewById(R.id.centerImageAnimationHelperPlay);
        startVerticalAnimation();
        shipTopView = (ImageView) main.findViewById(R.id.shipTopView);
        shipTopView.setBackgroundResource(R.drawable.spaceshiptopview);

        playLayout = (ConstraintLayout) main.findViewById(R.id.playLayout);


        shipTopView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_MOVE){
                    dragActive = true;

                    shipTopView.setY(shipTopView.getY() + event.getY()-shipTopView.getHeight()/2);
                    shipTopView.setX(shipTopView.getX() + event.getX()-shipTopView.getWidth()/2);

                }

                if(event.getAction() == MotionEvent.ACTION_UP){
                    dragActive = false;
                }




                return true;

            }

        });

        runnerThread.resume();



    }


    public void startVerticalAnimation(){
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(10000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                final float height = centerImagePlay.getHeight();
                final float translationY = (height * progress);
                centerImagePlay.setTranslationY(translationY);
                centerImageAnimationHelperPlay.setTranslationY(translationY - height);
            }
        });
        animator.start();
    }

    public void update(){

        if(!(dragActive) && shipTopView.getY() < 1300){
            shipTopView.setY(shipTopView.getY()+shipScreenDownwardDecayRate);
        }




    }

    public void pauseRunnerThread(){
        runnerThread.pause();
    }


    //responsible for the running thread in playScreen. Will be responsible for various checking mechanisms
    private class playScreenRunnerThread implements Runnable{
        private volatile boolean isRendering = false;
        Thread renderThread = null;
        PlayScreen playScreen;

        public playScreenRunnerThread(Context context) {
            this.playScreen = (PlayScreen) context;
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



                    // update screen's context
                    playScreen.update();
                    Thread.sleep(10);

                }
            } catch (Exception e) {
                // arguably overzealous to grab all exceptions here...but i want to know.
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
    }

}
