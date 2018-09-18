package com.androiddeft.mdp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.TextView;

import com.androiddeft.mdp.fragments.bluetooth.BluetoothChatFragment;

public class MainActivity extends AppCompatActivity{

    //Map
    private GridView gridView;
    int topLeftCorner;


    //Timer
    Button start, stop, auto, manual, configButton, bluetoothButton, up, down, left, right, waypointButton;
    EditText xValue, yValue;
    TextView time, boxID;
    private long startTime = 0L;

    private Handler customHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    boolean stopTimer = false;

    //for logging
    public static final String TAG = "MainActivity";

    //for defining a request code, and then start the activity using that request code
    private static final int REQUEST_RECONFIGURE_STRING = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Display the MAIN FRAGMENT
        Fragment fragment = new BluetoothChatFragment();
        displaySelectedFragment(fragment);


        //MAP
        //HARDCODE FOR NOW
        topLeftCorner = 255;

        GridLayout foreground = (GridLayout) findViewById(R.id.gridMapLayout);
        Drawable box = this.getResources().getDrawable(R.drawable.box);
        Drawable robot = this.getResources().getDrawable(R.drawable.robot);
        Drawable upDirection = this.getResources().getDrawable(R.drawable.up);


        for (int x = 0; x < foreground.getColumnCount() * foreground.getRowCount(); x++) {
            boxID = new TextView(this);
            boxID.setBackground(box);
            boxID.setId(x);
            //   boxID.setText(Integer.toString(boxID.getId()));
            boxID.setText("0");
            boxID.setGravity(Gravity.CENTER);


            foreground.addView(boxID);

            if (x == topLeftCorner || x == topLeftCorner + 1 || x == topLeftCorner + 2 || x == topLeftCorner + 15 || x == topLeftCorner + 16 || x == topLeftCorner + 17
                    || x == topLeftCorner + 30 || x == topLeftCorner + 31 || x == topLeftCorner + 32) {
                //xf (topLeftCorner + x < topLeftCorner + 3)
                if (x == topLeftCorner + 16) {
                    boxID.setText("");
                    boxID.setBackground(upDirection);
                } else {
                    boxID.setBackground(robot);
                    boxID.setText("1");
                    boxID.setTextColor(Color.parseColor("#FF0000"));
                }
            }

        }

        //Timer
        start = (Button) findViewById(R.id.startButton);
        stop = (Button) findViewById(R.id.stopButton);
        time = (TextView) findViewById(R.id.timerValue);
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                stopTimer = false;
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
                start.setVisibility(View.GONE);
                stop.setVisibility(View.VISIBLE);

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                stopTimer = true;
                customHandler.removeCallbacks(null);
                stop.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);
                start.setText("Restart");
                time.setText("00:00:000");

            }
        });

        //Auto or manual mode
        auto = (Button) findViewById(R.id.autoButton);
        manual = (Button) findViewById(R.id.manualButton);

        auto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                auto.setVisibility(View.GONE);
                manual.setVisibility(View.VISIBLE);

            }
        });

        manual.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                manual.setVisibility(View.GONE);
                auto.setVisibility(View.VISIBLE);
            }
        });

        //Directions
        up = (Button) findViewById(R.id.upButton);

        up.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                robotMovement("up");

            }
        });

        down = (Button) findViewById(R.id.downButton);

        down.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                robotMovement("down");

            }
        });

        left = (Button) findViewById(R.id.leftButton);

        left.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                robotMovement("left");

            }
        });

        right = (Button) findViewById(R.id.rightButton);

        right.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                robotMovement("right");

            }
        });


        //waypoint
        waypointButton = (Button) findViewById(R.id.waypointButton);
        xValue = (EditText) findViewById(R.id.xValue);
        yValue = (EditText) findViewById(R.id.yValue);
        final Drawable waypoint = this.getResources().getDrawable(R.drawable.waypoint);

        waypointButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int point = Integer.parseInt(String.valueOf(xValue.getText())) * Integer.parseInt(String.valueOf(xValue.getText()));
                TextView t = (TextView) findViewById(point);
                t.setText("P");
                t.setBackground(waypoint);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            //for reconfiguring string commands - command 1 and command 2 (Task C8)
            case R.id.reconString: {
                Intent reconStringIntent = new Intent(this,ConfigurableCommActivity.class);
                startActivityForResult(reconStringIntent, REQUEST_RECONFIGURE_STRING);
                return true;
            }
            //add other cases here - always start activity for result

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Loads the specified fragment to the frame
     *
     * @param fragment
     */
    private void displaySelectedFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            String localtime = "" + mins + ":" + String.format("%02d", secs)
                    + ":" + String.format("%03d", milliseconds);
            time.setText(localtime);
            if (mins == 1) {
                stopTimer = true;
            }
            if (!stopTimer)
                customHandler.postDelayed(this, 0);
        }

    };

    public void robotMovement(String direction) {

        switch (direction) {
            case "up":
                if (topLeftCorner < 15)
                    break;
                else
                    topLeftCorner -= 15;
                break;
            case "down":
                if (topLeftCorner >= 255)
                    break;
                else
                    topLeftCorner += 15;
                break;
            case "right":
                if (topLeftCorner % 15 == 12)
                    break;
                else
                    topLeftCorner += 1;
                break;
            case "left":
                if (topLeftCorner % 15 == 0)
                    break;
                else
                    topLeftCorner -= 1;
                break;
        }


        Drawable box = this.getResources().getDrawable(R.drawable.box);
        Drawable robot = this.getResources().getDrawable(R.drawable.robot);

        Drawable upImage = this.getResources().getDrawable(R.drawable.up);
        Drawable downImage = this.getResources().getDrawable(R.drawable.down);
        Drawable leftImage = this.getResources().getDrawable(R.drawable.left);
        Drawable rightImage = this.getResources().getDrawable(R.drawable.right);

        for (int y = 0; y < 300; y++) {
            TextView t = (TextView) findViewById(y);
            if (y == topLeftCorner || y == topLeftCorner + 1 || y == topLeftCorner + 2 || y == topLeftCorner + 15 || y == topLeftCorner + 16 || y == topLeftCorner + 17
                    || y == topLeftCorner + 30 || y == topLeftCorner + 31 || y == topLeftCorner + 32) {
                //if (topLeftCorner + y < topLeftCorner + 3)
                if (y == topLeftCorner + 16) {
                    t.setText("");
                    switch (direction) {
                        case "up":
                            t.setBackground(upImage);
                            break;
                        case "down":
                            t.setBackground(downImage);
                            break;
                        case "left":
                            t.setBackground(leftImage);
                            break;
                        case "right":
                            t.setBackground(rightImage);
                            break;

                    }
                } else {
                    t.setBackground(robot);
                    t.setText("1");
                    t.setTextColor(Color.parseColor("#FF0000"));
                }

            } else
                t.setBackground(box);


        }
    }


}