package twodicepigii.vidri.example.com.twodicepigii;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;



/**
 * MainActivity Fragment holds the logic for the dice rolls
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = "PigGame Activity";

    private int playerScore = 0;                        //Player's current score
    private int computerScore = 0;                      //Computer's current score
    private int trough = 0;                             //Trough (current run of roll totals)
    private int currentRoll1;                            //Amount of current roll, die #1
    private int currentRoll2;                           //Amount of current roll, die #2
    private int gamesWon = 0;                           //Number of games won by player
    private int numberOfRolls;                          //Number of rolls made
    private int targetValue;                            //Target value to win
    private Handler handler;                            //Used to delay computer rolls
    private Animation diceRoll1;                         //Animation for die1 roll
    private Animation diceRoll2;
    private String imgName;
    Drawable die1;
    Drawable die2;


    private TableLayout gameGridLayout;                  //GridLayout containing game
    private ImageView dieImageView1;                     //ImageView of die #1 file
    private ImageView dieImageView2;                    //ImageView of die #2 file
    private TextView playerScoreValue;                  //Displays player's current score
    private TextView computerScoreValue;                //Displays computer's current score
    private TextView troughTextValue;                   //Displays trough value
    private TextView resultTextView;                    //Displays result of rolls
    private Button rollAgain;                           //Button to roll again
    private Button hold;                                //Button to hold (and go to computer turn)


    //configures MainActivityFragment when view is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_main, container, false);


        handler = new Handler();

        //Load shake animation for die1 roll
        diceRoll1 = AnimationUtils.loadAnimation(getActivity(), R.anim.dice_roll);
        diceRoll1.setRepeatCount(3);

        diceRoll2 = AnimationUtils.loadAnimation(getActivity(), R.anim.dice_roll);
        diceRoll2.setRepeatCount(3);

        //Get references to GUI components
        gameGridLayout = (TableLayout) view.findViewById(R.id.gameGridLayout);
        playerScoreValue = (TextView) view.findViewById(R.id.playerScoreValue);
        computerScoreValue = (TextView) view.findViewById(R.id.computerScoreValue);
        dieImageView1 = (ImageView) view.findViewById(R.id.dieImageView1);
        dieImageView2 = (ImageView) view.findViewById(R.id.dieImageView2);
        troughTextValue = (TextView) view.findViewById(R.id.troughValue);
        rollAgain = (Button) view.findViewById(R.id.rollAgainButton);
        hold = (Button) view.findViewById(R.id.holdButton);
        resultTextView = (TextView) view.findViewById(R.id.resultTextView);


        resultTextView.setText(getString(R.string.welcome_message));

        //Make first roll
        //rollAgain();


        //Configure listeners for buttons
        rollAgain.setOnClickListener(rollAgainButtonListener);
        hold.setOnClickListener(holdButtonListener);


        return view;
    }

    //Updates the Target Score when user changes preferences
    public void updateTargetScore(SharedPreferences sharedPreferences) {
        String targetVal = sharedPreferences.getString(MainActivity.TARGET, "100");

        targetValue = Integer.parseInt(targetVal);
    }

    //Resets game and starts next game
    public void resetGame() {

        //Resets scores for a new game
        playerScore = 0;
        playerScoreValue.setText(Integer.toString(playerScore));

        computerScore = 0;
        computerScoreValue.setText(Integer.toString(computerScore));

        trough = 0;
        troughTextValue.setText(Integer.toString(trough));

        numberOfRolls = 0;

        resultTextView.setText(R.string.result_text_restart_game);


        //Start the next game by rolling the first die
        //rollAgain();
    }

    //Starts game with first roll and then whenever player chooses to roll the die1 again
    private void rollAgain() {


        //Randomly generates roll number, animates file of corresponding die face
        currentRoll1 = (int) (Math.random() * 6) + 1;
        currentRoll2 = (int) (Math.random() * 6) + 1;

        //updates number of rolls
        numberOfRolls++;

        //Get die1 pictures and display correct one according to roll, and shake
        AssetManager am1 = getActivity().getAssets();

        try (InputStream stream = am1.open(currentRoll1 + ".png")) {

            die1 = Drawable.createFromStream(stream, currentRoll1 + ".png");


            dieImageView1.setImageDrawable(die1);
            dieImageView1.startAnimation(diceRoll1);

        } catch (Exception exception) {
            imgName = Integer.toString(currentRoll1) + ".png";

            Log.e(TAG, "Error loading" + imgName, exception);
        }

        AssetManager am2 = getActivity().getAssets();

        try (InputStream stream2 = am2.open(currentRoll2 + ".png")) {
            die2 = Drawable.createFromStream(stream2, currentRoll2 + ".png");

            dieImageView2.setImageDrawable(die2);
            dieImageView2.startAnimation(diceRoll2);
        } catch (Exception exception) {
            imgName = Integer.toString(currentRoll2) + ".png";

            Log.e(TAG, "Error loading" + imgName, exception);
        }


        //If the die roll is 1, resets trough and starts the computer's turn
        if (currentRoll1 == 1 || currentRoll2 == 1) {
            trough = 0;
            troughTextValue.setText(Integer.toString(trough));
            resultTextView.setText(R.string.player_loses_trough);

            if (currentRoll1 == 1 && currentRoll2 == 1) {
                trough = 0;
                troughTextValue.setText(Integer.toString(trough));

                playerScore = 0;
                playerScoreValue.setText(Integer.toString(playerScore));

                resultTextView.setText(getString(R.string.snake_eyes));
            }

            //Delay start of computer turn
            handler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            computerTurn();
                        }
                    }, 2000);

        }

        //If the die roll is anything but 1, adds to trough and updates view
        else {
            trough = trough + currentRoll1 + currentRoll2;
            troughTextValue.setText(Integer.toString(trough));
        }
    }


    //Listener for when player clicks Roll Again button
    private OnClickListener rollAgainButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            rollAgain();
        }
    };

    //Listener for when player clicks Hold button and control passes to computer
    private OnClickListener holdButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //Player Score updated
            playerScore = playerScore + trough;
            playerScoreValue.setText(Integer.toString(playerScore));

            //Reset trough for computer
            trough = 0;
            troughTextValue.setText(Integer.toString(trough));

            //Check to see if player has hit target score
            if (playerScore >= targetValue) {
                gamesWon++;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


                builder.setMessage(getText(R.string.player_wins) + "  Score: " + playerScore + "   Number of Wins: " + gamesWon);

                builder.setPositiveButton(R.string.reset_game, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resetGame();
                    }
                });

                builder.create();

                builder.show();
            }

            //If player holds but score is not enough to win
            else {
                resultTextView.setText(R.string.player_holds);

                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                computerTurn();
                            }
                        }, 2000);

            }

        }
    };

    //Computer's turn
    private void computerTurn() {

        //Reset trough
        trough = 0;
        troughTextValue.setText(Integer.toString(trough));



        //Computer will only roll 3 times before "holding" or rolling a 1
        for (int i = 0; i < 3; i++) {


            currentRoll1 = (int) (Math.random() * 6) + 1;
            currentRoll2 = (int) (Math.random() * 6) + 1;

            AssetManager am = getActivity().getAssets();

            try (InputStream stream = am.open(currentRoll1 + ".png")) {

                die1 = Drawable.createFromStream(stream, currentRoll1 + ".png");


                dieImageView1.setImageDrawable(die1);
                dieImageView1.startAnimation(diceRoll1);


            } catch (Exception exception) {

                imgName = Integer.toString(currentRoll1) + ".png";
                Log.e(TAG, "Error loading" + imgName, exception);
            }

            AssetManager am2 = getActivity().getAssets();

            try (InputStream stream2 = am2.open(currentRoll2 + ".png")) {
                die2 = Drawable.createFromStream(stream2, currentRoll2 + ".png");


                dieImageView2.setImageDrawable(die2);
                dieImageView2.startAnimation(diceRoll2);

            } catch (Exception exception) {
                imgName = Integer.toString(currentRoll2) + ".png";

                Log.e(TAG, "Error loading" + imgName, exception);
            }

            //If computer rolls a 1, control returns to player
            if (currentRoll1 == 1 || currentRoll2 == 1) {
                trough = 0;
                troughTextValue.setText(Integer.toString(trough));
                resultTextView.setText(R.string.computer_loses_trough);

                if (currentRoll1 == 1 && currentRoll2 == 1) {
                    computerScore = 0;
                    computerScoreValue.setText(Integer.toString(computerScore));

                    resultTextView.setText(getString(R.string.snake_eyes_computer));
                }


                break;

            }

            //Computer will roll until 3 rolls are up or reaches target
            else {


                trough = trough + currentRoll1 + currentRoll2;
                troughTextValue.setText(Integer.toString(trough));

            }
        }


        //Update computer score
        computerScore = computerScore + trough;
        computerScoreValue.setText(Integer.toString(computerScore));


        //If computer reaches target, computer wins, game restarts
        if (computerScore >= targetValue) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(getText(R.string.player_loses) + "  Score: " + playerScore + "   Number of Wins: " + gamesWon);


            builder.setPositiveButton(R.string.reset_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    resetGame();
                }
            });

            builder.create();

            builder.show();
        }

        //Reset trough and player rolls again
        trough = 0;
        troughTextValue.setText(Integer.toString(trough));

        //Display computer holds if it completes all 3 rolls without rolling 1
        if (currentRoll1 != 1 && currentRoll2 != 1) {
            resultTextView.setText(R.string.computer_holds);
        }
    }
}
