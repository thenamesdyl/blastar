package me.dylanburton.blastarreborn;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Dylan on 6/30/2017.
 */

public class AboutScreen extends Activity implements Screen{

    MainActivity main;
    public AboutScreen(MainActivity main){
        this.main = main;
    }

    public void draw(){
        main.setContentView(R.layout.about_main);
        main.setCurrentScreen("about");
    }

    public void update(){

    }

}
