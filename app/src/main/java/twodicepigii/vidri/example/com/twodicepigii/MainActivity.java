package twodicepigii.vidri.example.com.twodicepigii;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

import twodicepigii.vidri.example.com.twodicepigii.R;


public class MainActivity extends AppCompatActivity {

    public static final String TARGET = "pref_target_score";            //The target scores

    private boolean phoneDevice = true;                                 //If the device is a phone
    private boolean preferencesChanged = true;                          //To determine if pref changed

    //Get default values for preference manager and determine if device is a phone
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferencesChangeListener);

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        //If screen size is large it's a tablet
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
        {
            phoneDevice = false;

        }

        if (phoneDevice)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    //Start game fragment and reset the game with correct preferences
    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChanged)
        {
            MainActivityFragment gameFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.gameFragment);

            gameFragment.updateTargetScore(PreferenceManager.getDefaultSharedPreferences(this));

            gameFragment.resetGame();
            preferencesChanged = false;
        }
    }

    //Inflate options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        else
            return false;
    }

    //Start a preference intent
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);

        return super.onOptionsItemSelected(item);
    }

    // listener for changes to the app's SharedPreferences
    private OnSharedPreferenceChangeListener preferencesChangeListener =
            new OnSharedPreferenceChangeListener() {
                // called when the user changes the app's preferences
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String key) {
                    preferencesChanged = true; // user changed app setting

                    MainActivityFragment gameFragment = (MainActivityFragment)
                            getSupportFragmentManager().findFragmentById(
                                    R.id.gameFragment);


                    //if (key.equals(TARGET)) { // target number changed
                    Set<String> target =
                            sharedPreferences.getStringSet(TARGET, null);

                    // if (target != null && target.size() > 0) {
                    gameFragment.updateTargetScore(sharedPreferences);

                    gameFragment.resetGame();
                    //  }


                    //Let user know preferences were changed and resetting the game
                    Toast.makeText(MainActivity.this,
                            R.string.restarting_game,
                            Toast.LENGTH_SHORT).show();
                }
            };

}
