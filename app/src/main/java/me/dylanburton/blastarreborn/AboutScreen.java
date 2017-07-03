package me.dylanburton.blastarreborn;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Dylan on 6/30/2017.
 */

public class AboutScreen extends Activity implements Screen{

    MainActivity main;
    public AboutScreen(MainActivity main){
        this.main = main;
    }


    public void update(){

    }

    public void draw(){
        main.setContentView(R.layout.about_main);
        main.setCurrentScreen("about");

    }

}
