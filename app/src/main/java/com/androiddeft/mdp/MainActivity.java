package com.androiddeft.mdp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.TextView;


import com.androiddeft.mdp.fragments.bluetooth.BluetoothChatFragment;

public class MainActivity extends AppCompatActivity{

    //Map
    private GridView gridView;
    int topLeftCorner;


    //Timer
    Button start, stop, auto, manual, configButton, bluetoothButton, up, down, left, right;

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

}
