package com.androiddeft.mdp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //Map
    private GridView gridView;
    int topLeftCorner = 255;

    //Timer
    Button start, stop, auto, manual, configButton, bluetoothButton, up, down, left, right, sendButton;
    EditText messageValue;
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

    //for waypoint
    int waypointList;
    TextView waypointXValue, waypointYValue;

    //for startpoint
    TextView startXValue, startYValue;

    //for rotation
    String currentDirection = "up";
    TextView movementTextView;

    BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 6;


    private Context mContext;
    private Activity mActivity;
    private PopupWindow mPopupWindow;
    private RelativeLayout mRelativeLayout;
    Runnable runnable = new Runnable() {
        public void run() {
            finish();
        }
    };
    private Button mButton;

    //send string
    BluetoothConnectionService mBluetoothConnection;

    //receive string
    String incomingMessage = null;
    boolean selectedWaypoint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridLayout foreground = findViewById(R.id.gridMapLayout);
        final Drawable box = this.getResources().getDrawable(R.drawable.box);
        Drawable robot = this.getResources().getDrawable(R.drawable.robot);
        Drawable upDirection = this.getResources().getDrawable(R.drawable.up);
        Drawable endpoint = this.getResources().getDrawable(R.drawable.endpoint);
        final Drawable waypoint = this.getResources().getDrawable(R.drawable.waypoint);

        for (int x = 0; x < foreground.getColumnCount() * foreground.getRowCount(); x++) {
            boxID = new TextView(this);
            boxID.setBackground(box);
            boxID.setId(x);
            //   boxID.setText(String.valueOf(x));
            boxID.setTextColor(Color.parseColor("#FF0000"));
            boxID.setGravity(Gravity.CENTER);

            boxID.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    topLeftCorner = view.getId();
                    if (topLeftCorner >= 150) {
                        robotStart();
                        Toast.makeText(getApplicationContext(), "Start coordinates selected. ",
                                Toast.LENGTH_SHORT).show();

                    } else
                        Toast.makeText(getApplicationContext(), "Starting coordinates must be before row 10.",
                                Toast.LENGTH_SHORT).show();


                    return true;    // <- set to true
                }
            });
            boxID.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectedWaypoint == false) {
                        //get the id of the selected box
                        int point = view.getId();
                        waypointXValue = findViewById(R.id.waypointXValue);
                        waypointYValue = findViewById(R.id.waypointYValue);

                        if (!(point == topLeftCorner || point == topLeftCorner + 1 || point == topLeftCorner + 2 || point == topLeftCorner + 15 || point == topLeftCorner + 16 || point == topLeftCorner + 17
                                || point == topLeftCorner + 30 || point == topLeftCorner + 31 || point == topLeftCorner + 32)) {
                            TextView tq = findViewById(point);
                            tq.setBackground(waypoint);
                            tq.setText("W");

                            int xCoord = point % 15;
                            int yCoord = 19 - ((point - xCoord) / 15);

                            waypointXValue.setText(String.valueOf(xCoord));
                            waypointYValue.setText(String.valueOf(yCoord));
                            Toast.makeText(getApplicationContext(), "Waypoint coordinates selected. ",
                                    Toast.LENGTH_SHORT).show();

                            Intent messaging_intent = new Intent("outMsg");
                            messaging_intent.putExtra("outgoingmsg", "W(" + waypointXValue.getText().toString() + "," + waypointYValue.getText().toString() + ")");
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

                        }
                        waypointList = point;
                        selectedWaypoint = true;
                    } else if (selectedWaypoint == true && waypointList == view.getId()) {
                        //remove waypoint
                        TextView oldPoint = findViewById(waypointList);
                        oldPoint.setBackground(box);
                        oldPoint.setText("");

                        waypointList = 0;
                        selectedWaypoint = false;
                        Toast.makeText(getApplicationContext(), "Waypoint coordinates removed.", Toast.LENGTH_LONG).show();
                    } else {
                        //change waypoint
                        TextView oldPoint = findViewById(waypointList);
                        oldPoint.setBackground(box);
                        oldPoint.setText("");

                        TextView newPoint = findViewById(view.getId());
                        newPoint.setBackground(waypoint);
                        newPoint.setText("W");

                        waypointList = view.getId();
                        selectedWaypoint = true;
                        Toast.makeText(getApplicationContext(), "Waypoint coordinates changed.", Toast.LENGTH_LONG).show();
                    }



                }
            });


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
            if (x == 12 || x == 13 || x == 14 || x == 27 || x == 28 || x == 29 || x == 42 || x == 43 || x == 44)
                boxID.setBackground(endpoint);


        }

        //Timer
        start = findViewById(R.id.startButton);
        stop = findViewById(R.id.stopButton);
        time = findViewById(R.id.timerValue);
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
        auto = findViewById(R.id.autoButton);
        manual = findViewById(R.id.manualButton);

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
        movementTextView = findViewById(R.id.movementTextView);
        up = findViewById(R.id.upButton);

        up.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                robotMovement();
                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "up");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
            }
        });

        down = findViewById(R.id.downButton);

        down.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                robotRotate("down");

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "down");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

            }
        });

        left = findViewById(R.id.leftButton);

        left.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                robotRotate("left");

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "left");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

            }
        });

        right = findViewById(R.id.rightButton);

        right.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                robotRotate("right");

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "right");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

            }
        });

        sendButton = findViewById(R.id.sendButton);
        messageValue = findViewById(R.id.messageValue);

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", messageValue.getText().toString());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

            }
        });


        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mBroadcastReceiver, filter);


        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver2, new IntentFilter("inMsg"));
    }

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            incomingMessage = intent.getStringExtra("incomingmsg");


            if (incomingMessage.equals("down") || incomingMessage.equals("right") || incomingMessage.equals("left"))
                robotRotate(incomingMessage);
            else if (incomingMessage.equals("up"))
                robotMovement();
            else if (incomingMessage.equals("stop"))
                movementTextView.setText("Stopped");
//            else {
//                String startCoord = "(.*)(^S\\(1?[0-9],1?[0-9]\\)$)(.*)";
//                Pattern startPattern = Pattern.compile(startCoord);
//                Matcher sp = startPattern.matcher(incomingMessage);
//
//                String waypointCoord = "(.*)(^W\\(1?[0-9],1?[0-9]\\)$)(.*)";
//                Pattern waypointPattern = Pattern.compile(waypointCoord);
//                Matcher wp = waypointPattern.matcher(incomingMessage);
//
//
//                if (sp.find()) //eg: S(5,2) no spacing
//                    Toast.makeText(getApplicationContext(), "Start coordinates Matched.", Toast.LENGTH_LONG).show();
//                else if(wp.find()) //eg: W(5,2) no spacing
//                    Toast.makeText(getApplicationContext(), "Waypoint coordinates Matched.", Toast.LENGTH_LONG).show();
//                else
//                    Toast.makeText(getApplicationContext(), "Fail to match.", Toast.LENGTH_LONG).show();
//            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.bluetooth:
                BluetoothOn();
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                break;
            case R.id.discoverable:
                mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
                BluetoothDiscoverable();
                break;

            case R.id.command:
                startActivity(new Intent(this, commandActivity.class));
                break;

        }
        return true;
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        BluetoothOn();
    }

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    protected final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Toast.makeText(getApplicationContext(), "Bluetooth is on", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(getApplicationContext(), "Bluetooth is turning on", Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "Bluetooth is turning on", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Toast.makeText(getApplicationContext(), "Bluetooth is visible to other devices  ", Toast.LENGTH_LONG).show();
                        break;
                }


            }
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

                //rl_msg.setVisibility(View.VISIBLE);
                //listmsg.setText("HI");

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    protected void BluetoothOn() {
        if (myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth does not support on this device", Toast.LENGTH_LONG).show();
        } else {
            if (!myBluetoothAdapter.isEnabled()) {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBluetoothIntent);

                IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mBroadcastReceiver, intentFilter);
                //startActivityForResult(btEnablingIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    protected void BluetoothDiscoverable() {
        if (!myBluetoothAdapter.isEnabled()) {
            BluetoothOn();
        }
        Toast.makeText(getApplicationContext(), "Making device discoverable for 300 seconds.", Toast.LENGTH_LONG).show();
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = myBluetoothAdapter.getRemoteDevice(address);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
        }
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

    public void robotRotate(String direction) {


        Drawable box = this.getResources().getDrawable(R.drawable.box);
        Drawable robot = this.getResources().getDrawable(R.drawable.robot);

        Drawable upImage = this.getResources().getDrawable(R.drawable.up);
        Drawable downImage = this.getResources().getDrawable(R.drawable.down);
        Drawable leftImage = this.getResources().getDrawable(R.drawable.left);
        Drawable rightImage = this.getResources().getDrawable(R.drawable.right);

        for (int y = 0; y < 300; y++) {
            TextView t = findViewById(y);
            if (waypointList != y) {
                if (y == topLeftCorner || y == topLeftCorner + 1 || y == topLeftCorner + 2 || y == topLeftCorner + 15 || y == topLeftCorner + 16 || y == topLeftCorner + 17
                        || y == topLeftCorner + 30 || y == topLeftCorner + 31 || y == topLeftCorner + 32) {
                    if (y == topLeftCorner + 16) {
                        t.setText("");
                        switch (direction) {
                            case "left":
                                if (currentDirection == "up") {
                                    t.setBackground(leftImage);
                                    currentDirection = "left";
                                } else if (currentDirection == "right") {
                                    t.setBackground(upImage);
                                    currentDirection = "up";
                                } else if (currentDirection == "down") {
                                    t.setBackground(rightImage);
                                    currentDirection = "right";
                                } else if (currentDirection == "left") {
                                    t.setBackground(downImage);
                                    currentDirection = "down";
                                }
                                break;
                            case "down":
                                if (currentDirection == "left") {
                                    t.setBackground(rightImage);
                                    currentDirection = "right";
                                } else if (currentDirection == "right") {
                                    t.setBackground(leftImage);
                                    currentDirection = "left";
                                } else if (currentDirection == "down") {
                                    t.setBackground(upImage);
                                    currentDirection = "up";
                                } else if (currentDirection == "up") {
                                    t.setBackground(downImage);
                                    currentDirection = "down";
                                }
                                break;
                            case "right":
                                if (currentDirection == "left") {
                                    t.setBackground(upImage);
                                    currentDirection = "up";
                                } else if (currentDirection == "right") {
                                    t.setBackground(downImage);
                                    currentDirection = "down";
                                } else if (currentDirection == "up") {
                                    t.setBackground(rightImage);
                                    currentDirection = "right";
                                } else if (currentDirection == "down") {
                                    t.setBackground(leftImage);
                                    currentDirection = "left";
                                }
                                break;

                        }
                    } else {
                        t.setBackground(robot);
                        t.setText("1");
                        t.setTextColor(Color.parseColor("#FF0000"));
                    }

                }
            }


        }
        movementTextView.setText("Turning " + currentDirection);
    }

    public void robotMovement() {
        Drawable box = this.getResources().getDrawable(R.drawable.box);
        Drawable robot = this.getResources().getDrawable(R.drawable.robot);
        Drawable endpoint = this.getResources().getDrawable(R.drawable.endpoint);

        Drawable upImage = this.getResources().getDrawable(R.drawable.up);
        Drawable downImage = this.getResources().getDrawable(R.drawable.down);
        Drawable leftImage = this.getResources().getDrawable(R.drawable.left);
        Drawable rightImage = this.getResources().getDrawable(R.drawable.right);

        switch (currentDirection) {
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

        for (int y = 0; y < 300; y++) {
            TextView t = findViewById(y);
            if (waypointList != y) {
                if (y == topLeftCorner || y == topLeftCorner + 1 || y == topLeftCorner + 2 || y == topLeftCorner + 15 || y == topLeftCorner + 16 || y == topLeftCorner + 17
                        || y == topLeftCorner + 30 || y == topLeftCorner + 31 || y == topLeftCorner + 32) {
                    //if (topLeftCorner + y < topLeftCorner + 3)
                    if (y == topLeftCorner + 16) {
                        t.setText("");
                        switch (currentDirection) {
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

                } else if (y == 12 || y == 13 || y == 14 || y == 27 || y == 28 || y == 29 || y == 42 || y == 43 || y == 44)
                    boxID.setBackground(endpoint);
                else
                    t.setBackground(box);

            }


        }
        movementTextView.setText("Moved " + currentDirection);
    }

    public void robotStart() {
        startXValue = findViewById(R.id.startXValue);
        startYValue = findViewById(R.id.startYValue);


        //bottom
        if ((19 - ((topLeftCorner - (topLeftCorner % 15)) / 15)) == 0)
            topLeftCorner -= 30;
        else if ((19 - ((topLeftCorner - (topLeftCorner % 15)) / 15)) == 1)
            topLeftCorner -= 15;

        //right
        if ((topLeftCorner % 15) == 13)
            topLeftCorner -= 1;
        else if ((topLeftCorner % 15) == 14)
            topLeftCorner -= 2;

        int xCoord = topLeftCorner % 15;
        int yCoord = 19 - ((topLeftCorner - xCoord) / 15);

        startXValue.setText(String.valueOf(xCoord));
        startYValue.setText(String.valueOf(yCoord));

        Intent messaging_intent = new Intent("outMsg");
        messaging_intent.putExtra("outgoingmsg", "S(" + startXValue.getText().toString() + "," + startYValue.getText().toString() + ")");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

        Drawable box = this.getResources().getDrawable(R.drawable.box);
        Drawable robot = this.getResources().getDrawable(R.drawable.robot);
        Drawable endpoint = this.getResources().getDrawable(R.drawable.endpoint);

        Drawable upImage = this.getResources().getDrawable(R.drawable.up);
        Drawable downImage = this.getResources().getDrawable(R.drawable.down);
        Drawable leftImage = this.getResources().getDrawable(R.drawable.left);
        Drawable rightImage = this.getResources().getDrawable(R.drawable.right);

        for (int y = 0; y < 300; y++) {
            TextView t = findViewById(y);
            //    t.setText(String.valueOf(y));
            if (waypointList != y) {
                if (y == topLeftCorner || y == topLeftCorner + 1 || y == topLeftCorner + 2 || y == topLeftCorner + 15 || y == topLeftCorner + 16 || y == topLeftCorner + 17
                        || y == topLeftCorner + 30 || y == topLeftCorner + 31 || y == topLeftCorner + 32) {

                    if (y == topLeftCorner + 16) {
                        t.setText("");
                        switch (currentDirection) {
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

                } else if (y == 12 || y == 13 || y == 14 || y == 27 || y == 28 || y == 29 || y == 42 || y == 43 || y == 44)
                    boxID.setBackground(endpoint);
                else {
                    t.setText("");
                    t.setBackground(box);

                }
            }


        }

    }


}