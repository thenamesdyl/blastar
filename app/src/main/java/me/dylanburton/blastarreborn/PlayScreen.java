package me.dylanburton.blastarreborn;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Dylan on 6/30/2017.
 */

public class PlayScreen extends Activity{

    MainActivity main;
    public PlayScreen(MainActivity main){
        this.main = main;
    }


    public void onCreate(){
        main.setContentView(R.layout.play_main);
    }

}
