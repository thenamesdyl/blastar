package me.dylanburton.blastarreborn;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Dylan on 6/30/2017.
 */

public class AboutScreen extends Activity{

    MainActivity main;
    public AboutScreen(MainActivity main){
        this.main = main;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_main);




    }

}
