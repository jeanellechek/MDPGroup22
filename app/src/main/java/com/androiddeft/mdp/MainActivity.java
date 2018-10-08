//MDF

package com.androiddeft.mdp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    //Map
    private GridView gridView;
    int topLeftCorner = 255;

    //Timer
    Button start, stop, start1, stop1, refresh, configButton, bluetoothButton, up, down, left, right, sendButton;
    EditText messageValue;
    TextView time, time1, boxID;
    private long startTime = 0L;
    private long startTime1 = 0L;

    private Handler customHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    long timeInMilliseconds1 = 0L;
    long timeSwapBuff1 = 0L;
    long updatedTime1 = 0L;
    boolean stopTimer = false;
    boolean stopTimer1 = false;

    //for logging
    public static final String TAG = "MainActivity";

    //for defining a request code, and then start the activity using that request code
    private static final int REQUEST_RECONFIGURE_STRING = 1;

    //for waypoint
    ArrayList<Integer> waypointList = new ArrayList<>();
    TextView waypointXValue, waypointYValue;

    //for startpoint
    TextView startXValue, startYValue;


    //for rotation
    String currentDirection = "w";
    TextView movementTextView;

    //for obstacles
    ArrayList<String> arrowObstacles = new ArrayList<String>(); //with arrows with direction
    ArrayList<Integer> arrowObstaclelist = new ArrayList<Integer>(); //with arrows
    ArrayList<Integer> obstaclemap = new ArrayList<>(); //from p2
    ArrayList<Integer> obstaclelist = new ArrayList<Integer>();//without arrows
    ArrayList<String> obstacleInstruction = new ArrayList<String>(); //for manual


    int receivedCoordinates = 0;
    String arrowCoordinates;
    String direction = null;
    int obstacleCount = 0;

    //MDF
    String MDF1Value = null;
    String MDF2Value = null;
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


    //mode
    Switch modeSwitch;
    boolean autoMode = true;
    ArrayList<String> manualList = new ArrayList<String>();
    ArrayList<String> InstructionList = new ArrayList<String>();

    //explored
    String exploredX = null;
    String exploredY = null;
    TextView exploredTV;
    ArrayList<Integer> exploredmap = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setSubtitle("Not Connected");

        //Display starting coordinates
        startXValue = findViewById(R.id.startXValue);
        startYValue = findViewById(R.id.startYValue);


        int xCoord = topLeftCorner % 15;
        int yCoord = 19 - ((topLeftCorner - xCoord) / 15);

        startXValue.setText(String.valueOf(xCoord));
        startYValue.setText(String.valueOf(yCoord));


        GridLayout foreground = findViewById(R.id.gridMapLayout);
        final Drawable box = this.getResources().getDrawable(R.drawable.box);
        Drawable robot = this.getResources().getDrawable(R.drawable.robot);
        Drawable upDirection = this.getResources().getDrawable(R.drawable.up);
        Drawable endpoint = this.getResources().getDrawable(R.drawable.endpoint);
        final Drawable waypoint = this.getResources().getDrawable(R.drawable.waypoint);

        for (int x = 0; x < foreground.getColumnCount() * foreground.getRowCount(); x++) {
            boxID = new TextView(this);
            boxID.setBackground(box);
            boxID.setTag("Unknown");
            boxID.setId(x);
            //boxID.setText(String.valueOf(x));
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
                            messaging_intent.putExtra("outgoingmsg", "pW_" + waypointXValue.getText().toString() + "," + waypointYValue.getText().toString());
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

                        }
                        waypointList.add(point);
                        selectedWaypoint = true;
                    } else if (selectedWaypoint == true && waypointList.contains(view.getId())) {
                        //remove waypoint
                        TextView oldPoint = findViewById(view.getId());
                        oldPoint.setBackground(box);
                        oldPoint.setText("");
                        waypointXValue = findViewById(R.id.waypointXValue);
                        waypointYValue = findViewById(R.id.waypointYValue);
                        waypointXValue.setText("");
                        waypointYValue.setText("");

                        waypointList.clear();
                        selectedWaypoint = false;
                        Toast.makeText(getApplicationContext(), "Waypoint coordinates removed.", Toast.LENGTH_SHORT).show();
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


        //Auto or manual mode
        modeSwitch = findViewById(R.id.modeSwitch);
        refresh = findViewById(R.id.refreshButton);


        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    autoMode = true;
                    for (int i = 0; i < manualList.size(); i++) {
                        if (manualList.get(i).equals("w"))
                            robotMovement();
                        else
                            robotRotate(manualList.get(i));
                    }
                    //clear
                    manualList.clear();

                    if (InstructionList != null)
                        for (int i = 0; i < InstructionList.size(); i++)
                            mapDecoder(InstructionList.get(i));
                    InstructionList.clear();

                    if (obstacleInstruction != null)
                        for (int i = 0; i < obstacleInstruction.size(); i++)
                            getArrowObstacleCoord(obstacleInstruction.get(i));
                    obstacleInstruction.clear();

                    refresh.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "In auto mode", Toast.LENGTH_SHORT).show();

                } else {
                    // The toggle is disabled
                    autoMode = false;
                    refresh.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "In manual mode", Toast.LENGTH_SHORT).show();

                }
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                for (int i = 0; i < manualList.size(); i++) {
                    if (manualList.get(i).equals("w"))
                        robotMovement();
                    else
                        robotRotate(manualList.get(i));
                }
                //clear
                manualList.clear();

                if (InstructionList != null)
                    for (int i = 0; i < InstructionList.size(); i++)
                        mapDecoder(InstructionList.get(i));
                InstructionList.clear();

                if (obstacleInstruction != null)
                    for (int i = 0; i < obstacleInstruction.size(); i++)
                        getArrowObstacleCoord(obstacleInstruction.get(i));
                obstacleInstruction.clear();

                Toast.makeText(getApplicationContext(), "Refreshed.", Toast.LENGTH_SHORT).show();

            }
        });


        //Directions
        movementTextView = findViewById(R.id.movementTextView);
        up = findViewById(R.id.upButton);

        up.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bw");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

                if (autoMode == true)
                    robotMovement();
                else
                    manualList.add("w");

            }
        });
        up.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bw");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
                if (autoMode == true)
                    robotMovement();
                else
                    manualList.add("w");


            }
        }));
        down = findViewById(R.id.downButton);

        down.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bs");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
                if (autoMode == true)
                    robotRotate("s");
                else
                    manualList.add("s");


            }
        });

        down.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bs");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
                if (autoMode == true)
                    robotRotate("s");
                else
                    manualList.add("s");

            }
        }));

        left = findViewById(R.id.leftButton);

        left.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "ba");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
                if (autoMode == true)
                    robotRotate("a");
                else
                    manualList.add("a");


            }
        });

        left.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "ba");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
                if (autoMode == true)
                    robotRotate("a");
                else
                    manualList.add("a");

            }
        }));
        right = findViewById(R.id.rightButton);

        right.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bd");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
                if (autoMode == true)
                    robotRotate("d");
                else
                    manualList.add("d");


            }
        });
        right.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bd");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
                if (autoMode == true)
                    robotRotate("d");
                else
                    manualList.add("d");

            }
        }));
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
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver6, new IntentFilter("bluetoothStatus"));
    }


    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("incomingmsg");
            boolean isReceived = true;
            String t1 = text;
            String tempMsg = text.toLowerCase();

            switch (tempMsg) {
                case "a":
                    tempMsg = "Left";
                    robotRotate("a");
                    break;
                case "s":
                    tempMsg = "Down";
                    robotRotate(tempMsg);
                    break;
                case "d":
                    tempMsg = "Right";
                    robotRotate(tempMsg);
                    break;
                case "w":
                    robotMovement();
                    break;
                case "clear":
                    robotStart();
                    break;
                case "explore":
                    robotExplore();
                    break;
                case "fastest":
                    robotFastest();
                    break;
                case "stop":
                    movementTextView.setText("Robot Stopped");
                    break;
                case "explore stop":
                    exploreStop();
                    break;

                case "fastest stop":
                    fastestStop();
                    break;
            }
            if (t1 != null) {
                char temp1 = t1.charAt(0);


                switch (temp1) {
                    case 'O': //eg: O(X,y,AWSDawsd)
                        if (autoMode == false)
                            obstacleInstruction.add(text);
                        else
                            getArrowObstacleCoord(text); //check for existing list if automode ==true
                        break;

                    case 'f':
                        displayFinalMDF(t1);
                        break;
                    case 't': //eg: t:f8007e00ff01fe03fc07f00ffc1ff83ffc7f00ee001c002000400000000000000007000e001f:(00,00,W):100000000010000000011c0000
                        tempMsg = t1.substring(2, t1.length());
                        obstaclelist.clear();
                        if (autoMode == false)
                            InstructionList.add(tempMsg);
                        else
                            mapDecoder(tempMsg);
                        break;
                }
            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiver6 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("bluetooth");
            getSupportActionBar().setSubtitle(text);
        }
    };

    private void displayFinalMDF(String t1) {
        String tempMsg = t1;
        String[] mdfstr = tempMsg.split(":");
        String MDFString1 = mdfstr[1].toString();
        String MDFString2 = mdfstr[2].toString();

        TextView MDF1 = findViewById(R.id.txtMDF1);
        TextView MDF2 = findViewById(R.id.txtMDF2);
        MDF1.setText("MDF1:" + MDFString1);
        MDF2.setText("MDF2:" + MDFString2);
    }

    private void fastestStop() {
        stopTimer1 = true;
        customHandler.removeCallbacks(null);
        stop1.setVisibility(View.GONE);
        start1.setVisibility(View.VISIBLE);
        start1.setText("Restart");
        time1.setText("00:00:000");
    }

    private void exploreStop() {
        stopTimer = true;
        customHandler.removeCallbacks(null);
        stop.setVisibility(View.GONE);
        start.setVisibility(View.VISIBLE);
        start.setText("Restart");
        time.setText("00:00:000");
    }

    private void robotFastest() {
        stopTimer = false;
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread1, 0);

        Intent messaging_intent = new Intent("outMsg");
        messaging_intent.putExtra("outgoingmsg", "fastest");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
        start.setVisibility(View.GONE);
        stop.setVisibility(View.VISIBLE);
    }

    private void robotExplore() {
        stopTimer1 = false;
        startTime1 = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);
        Intent messaging_intent = new Intent("outMsg");
        messaging_intent.putExtra("outgoingmsg", "explore");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
        start.setVisibility(View.GONE);
        stop.setVisibility(View.VISIBLE);
    }


    private void displayExplored(String exploredX, String exploredY) {
        Drawable explored = this.getResources().getDrawable(R.drawable.explored);
        int exploredPoint = -(((Integer.valueOf(exploredY) - 19) * 15) - Integer.valueOf(exploredX));
        exploredTV = findViewById(exploredPoint);
        exploredTV.setText("1");
        exploredmap.add(exploredPoint);

    }

    private void displayObstacle(String obstacleX, String obstacleY, String obstacleArrow) {
        Drawable upImage = this.getResources().getDrawable(R.drawable.up);
        Drawable obstacleImage = this.getResources().getDrawable(R.drawable.obstacle);
        int obstacleXValue = Integer.valueOf(obstacleX);
        int obstacleYValue = Integer.valueOf(obstacleY);

        int obstaclePoint = -(((obstacleYValue - 19) * 15) - obstacleXValue);
        TextView op = findViewById(obstaclePoint);

        if (obstacleArrow != null && obstacleCount <= 5) {
            //with arrow
            op.setText(obstacleArrow);
            op.setBackground(upImage);
            op.setTextColor(Color.parseColor("#FFFFFF"));
            op.setGravity(Gravity.CENTER);
            op.setGravity(Gravity.CENTER);
            obstacleCount++;
            arrowObstacles.add(obstaclePoint + "/" + obstacleArrow);
            Toast.makeText(getApplicationContext(), "Obstacle arrow created at " + obstaclePoint, Toast.LENGTH_SHORT).show();
        }

    }

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
                        Toast.makeText(getApplicationContext(), "Bluetooth is on", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(getApplicationContext(), "Bluetooth is turning on", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "Bluetooth is turning on", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Toast.makeText(getApplicationContext(), "Bluetooth is visible to other devices  ", Toast.LENGTH_SHORT).show();
                        break;
                }


            }
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                getSupportActionBar().setSubtitle("Connected");

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //test using AMD
                getSupportActionBar().setSubtitle("Not Connected");
                Intent reconnect_Intent = new Intent("reconnectMsg");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(reconnect_Intent);

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
            Toast.makeText(getApplicationContext(), "Bluetooth does not support on this device", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getApplicationContext(), "Making device discoverable for 300 seconds.", Toast.LENGTH_SHORT).show();
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

    //Exploration
    private Runnable updateTimerThread1 = new Runnable() {

        public void run() {
            timeInMilliseconds1 = SystemClock.uptimeMillis() - startTime1;

            updatedTime1 = timeSwapBuff1 + timeInMilliseconds1;

            int secs = (int) (updatedTime1 / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime1 % 1000);
            String localtime = "" + mins + ":" + String.format("%02d", secs)
                    + ":" + String.format("%03d", milliseconds);
            time1.setText(localtime);
            if (mins == 1) {
                stopTimer1 = true;
            }
            if (!stopTimer1)
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
            if (y == topLeftCorner || y == topLeftCorner + 1 || y == topLeftCorner + 2 || y == topLeftCorner + 15 || y == topLeftCorner + 16 || y == topLeftCorner + 17
                    || y == topLeftCorner + 30 || y == topLeftCorner + 31 || y == topLeftCorner + 32) {
                if (y == topLeftCorner + 16) {
                    t.setText("");
                    switch (direction) {
                        case "a":
                            if (currentDirection == "w") {
                                t.setBackground(leftImage);
                                currentDirection = "a";
                            } else if (currentDirection == "d") {
                                t.setBackground(upImage);
                                currentDirection = "w";
                            } else if (currentDirection == "s") {
                                t.setBackground(rightImage);
                                currentDirection = "d";
                            } else if (currentDirection == "a") {
                                t.setBackground(downImage);
                                currentDirection = "s";
                            }
                            break;
                        case "s":
                            if (currentDirection == "a") {
                                t.setBackground(rightImage);
                                currentDirection = "d";
                            } else if (currentDirection == "d") {
                                t.setBackground(leftImage);
                                currentDirection = "a";
                            } else if (currentDirection == "s") {
                                t.setBackground(upImage);
                                currentDirection = "w";
                            } else if (currentDirection == "w") {
                                t.setBackground(downImage);
                                currentDirection = "s";
                            }
                            break;
                        case "d":
                            if (currentDirection == "a") {
                                t.setBackground(upImage);
                                currentDirection = "w";
                            } else if (currentDirection == "d") {
                                t.setBackground(downImage);
                                currentDirection = "s";
                            } else if (currentDirection == "w") {
                                t.setBackground(rightImage);
                                currentDirection = "d";
                            } else if (currentDirection == "s") {
                                t.setBackground(leftImage);
                                currentDirection = "a";
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
        movementTextView.setText("Turning " + currentDirection);
    }

    public void robotMovement() {
        Drawable box = this.getResources().getDrawable(R.drawable.box);
        Drawable robot = this.getResources().getDrawable(R.drawable.robot);
        Drawable endpoint = this.getResources().getDrawable(R.drawable.endpoint);
        Drawable obstacleImage = this.getResources().getDrawable(R.drawable.obstacle);
        Drawable waypointImage = this.getResources().getDrawable(R.drawable.waypoint);

        Drawable upImage = this.getResources().getDrawable(R.drawable.up);
        Drawable downImage = this.getResources().getDrawable(R.drawable.down);
        Drawable leftImage = this.getResources().getDrawable(R.drawable.left);
        Drawable rightImage = this.getResources().getDrawable(R.drawable.right);

        switch (currentDirection) {
            case "w":
                if (topLeftCorner < 15)
                    break;
                else
                    topLeftCorner -= 15;
                break;
            case "s":
                if (topLeftCorner >= 255)
                    break;
                else
                    topLeftCorner += 15;
                break;
            case "d":
                if (topLeftCorner % 15 == 12)
                    break;
                else
                    topLeftCorner += 1;
                break;
            case "a":
                if (topLeftCorner % 15 == 0)
                    break;
                else
                    topLeftCorner -= 1;
                break;
        }
        String size = "";
        for (int i = 0; i < arrowObstacles.size(); i++) {
            size += arrowObstacles.get(i).toString() + "\n";
        }
        Log.d(TAG, size);
        for (int y = 0; y < 300; y++) {
            TextView t = findViewById(y);
            if (y == topLeftCorner || y == topLeftCorner + 1 || y == topLeftCorner + 2 || y == topLeftCorner + 15 || y == topLeftCorner + 16 || y == topLeftCorner + 17
                    || y == topLeftCorner + 30 || y == topLeftCorner + 31 || y == topLeftCorner + 32) {
                //if (topLeftCorner + y < topLeftCorner + 3)
                if (y == topLeftCorner + 16) {
                    t.setText("");
                    switch (currentDirection) {
                        case "w":
                            t.setBackground(upImage);
                            break;
                        case "s":
                            t.setBackground(downImage);
                            break;
                        case "a":
                            t.setBackground(leftImage);
                            break;
                        case "d":
                            t.setBackground(rightImage);
                            break;

                    }
                } else {
                    t.setBackground(robot);
                    t.setText("1");
                    t.setTextColor(Color.parseColor("#FF0000"));
                }

            }
        }
        for (int y = 0; y < 300; y++) {
            TextView t = findViewById(y);
            if (t.getTag().equals("Explored") || (t.getTag().equals("Robot")) || t.getTag().equals("GOAL")) {
                if (y == 12 || y == 13 || y == 14 || y == 27 || y == 28 || y == 29 || y == 42 || y == 43 || y == 44) {
                    t.setBackground(endpoint);
                    t.setText("");
                } else if (obstaclelist.size() > 0) {
                    if (obstaclelist.contains(y)) {
                        for (int j = 0; j < obstaclelist.size(); j++) {
                            if (y == obstaclelist.get(j)) {
                                if (arrowObstaclelist.size() > 0) {
                                    if (arrowObstaclelist.contains(obstaclelist.get(j))) {
                                        for (int temp1 = 0; temp1 < arrowObstacles.size(); temp1++) {
                                            String[] arrowsBoxID = arrowObstacles.get(temp1).split("/");
                                            if (Integer.valueOf(arrowsBoxID[0].toString()) == y) {
                                                t.setBackground(upImage);
                                                t.setTextColor(Color.parseColor("#FFFFFF"));
                                                t.setText(arrowsBoxID[1].toUpperCase());
                                            }
                                        }
                                    } else {
                                        t.setBackground(obstacleImage);
                                        t.setText("");
                                    }
                                } else {
                                    t.setBackground(obstacleImage);
                                    t.setText("");
                                }
                            }
                        }
                    }
                }
            }
            if (waypointList.contains(y)) {
                t.setBackground(waypointImage);
                t.setText("W");
            }


//                else {
//                    for (int i = 0; i < arrowObstacles.size(); i++) {
//                        String[] arrowsBoxID = arrowObstacles.get(i).split("/");
//                        if (y == Integer.parseInt(arrowsBoxID[0])) {
//                            t.setBackground(upImage);
//                            t.setTextColor(Color.parseColor("#FFFFFF"));
//                            t.setText(arrowsBoxID[1].toUpperCase());
//                        }
////                        else
////                            t.setBackground(box);
//                    }
//                }


        }
        movementTextView.setText("Moved " + currentDirection);
    }

    protected void setRobot(int boxid, String direction) {
        Drawable box = this.getResources().getDrawable(R.drawable.box);
        Drawable robotImage = this.getResources().getDrawable(R.drawable.robot);
        Drawable endpoint = this.getResources().getDrawable(R.drawable.endpoint);

        Drawable upImage = this.getResources().getDrawable(R.drawable.up);
        Drawable downImage = this.getResources().getDrawable(R.drawable.down);
        Drawable leftImage = this.getResources().getDrawable(R.drawable.left);
        Drawable rightImage = this.getResources().getDrawable(R.drawable.right);

        topLeftCorner = boxid;
        for (int i = 0; i < 300; i++) {
            TextView t = findViewById(i);
            if (i == topLeftCorner || i == topLeftCorner + 1 || i == topLeftCorner + 2 || i == topLeftCorner + 15 || i == topLeftCorner + 16 || i == topLeftCorner + 17
                    || i == topLeftCorner + 30 || i == topLeftCorner + 31 || i == topLeftCorner + 32) {
                if (i == topLeftCorner) {
                    t.setText("");
                    switch (direction) {
                        case "w":
                            t.setBackground(upImage);
                            currentDirection = "w";
                            break;
                        case "s":
                            t.setBackground(downImage);
                            currentDirection = "s";
                            break;
                        case "a":
                            t.setBackground(leftImage);
                            currentDirection = "a";
                            break;
                        case "d":
                            t.setBackground(rightImage);
                            currentDirection = "d";
                            break;
                    }
                }
                if (i == topLeftCorner + 16) {
                    switch (currentDirection) {
                        case "w":
                            t.setBackground(upImage);
                            break;
                        case "a":
                            t.setBackground(leftImage);
                            break;
                        case "s":
                            t.setBackground(downImage);
                            break;
                        case "d":
                            t.setBackground(rightImage);
                            break;
                    }
                    t.setText("");
                } else {
                    t.setBackground(robotImage);
                    t.setText("1");
                    t.setTextColor(Color.parseColor("#FF0000"));
                    t.setTag("Robot");
                }
            }
        }
    }


    protected void mapDecoder(String msg) {
        obstaclelist.clear();
        exploredmap.clear();
        obstaclemap.clear();

        Drawable box = this.getResources().getDrawable(R.drawable.box);
        Drawable robotImage = this.getResources().getDrawable(R.drawable.robot);
        Drawable endpoint = this.getResources().getDrawable(R.drawable.endpoint);
        Drawable obstacleImage = this.getResources().getDrawable(R.drawable.obstacle);

        Drawable upImage = this.getResources().getDrawable(R.drawable.up);
        Drawable downImage = this.getResources().getDrawable(R.drawable.down);
        Drawable leftImage = this.getResources().getDrawable(R.drawable.left);
        Drawable rightImage = this.getResources().getDrawable(R.drawable.right);


        String tempMsg = msg;
        String[] mdfstr1 = tempMsg.split(":");
        String bin = new BigInteger(mdfstr1[0].toString(), 16).toString(2);
        String robot = mdfstr1[1].toString();
        for (int temp = 2; temp < bin.length() - 2; temp++) { //because of the padded 11 infront and behind
            exploredmap.add(Integer.parseInt(bin.substring(temp, temp + 1)));
            //Log.d(TAG, mapdata.get(temp).toString());
        }
        String string = "";
        for (int temp = 0; temp < exploredmap.size(); temp++) {
            string += exploredmap.get(temp).toString();
        }
        //editmsg.setText(string);
        int counter = 0;
        for (int y = 19; y >= 0; y--) {
            for (int x = 0; x < 15; x++) {
                int boxid = (y * 15) + x;
                TextView t2 = findViewById(boxid);
                if (exploredmap.get(counter).toString().equals("1")) {
                    t2.setBackground(robotImage);
                    t2.setText(exploredmap.get(counter).toString());
                    t2.setTextColor(Color.parseColor("#000000"));
                    t2.setTag("Explored");
                    t2.setId(boxid);
                    t2.setTypeface(null, Typeface.NORMAL);
                    t2.setTag("Robot");
                } else {
                    t2.setText(exploredmap.get(counter).toString());
                    t2.setBackground(box);
                    t2.setId(boxid);
                    t2.setTextColor(Color.parseColor("#000000"));
                    t2.setTag("Unknown");
                    t2.setTypeface(null, Typeface.NORMAL);
                }
                counter++;

                if (boxid == 12 || boxid == 13 || boxid == 14 || boxid == 27 || boxid == 28 || boxid == 29 || boxid == 42 || boxid == 43 || boxid == 44) {
                    t2.setBackground(endpoint);
                    t2.setTag("GOAL");
                    t2.setText("");
                }
//
//                for (int i = 0; i < arrowObstacles.size(); i++) {
//                    String[] arrowsBoxID = arrowObstacles.get(i).split("/");
//                    if (boxid == Integer.parseInt(arrowsBoxID[0])) {
//                        t2.setBackground(upImage);
//                        t2.setTextColor(Color.parseColor("#FFFFFF"));
//                        t2.setText(arrowsBoxID[1].toUpperCase());
//                    }
//                }

            }
        }
        if (mdfstr1[1].toString().matches("^\\([01][0-9],[01][0-9],[AWSDawsd]\\)")) {
            //ob(xx,yy,N/S/E/W,u)
            try {
                int x1 = Integer.valueOf(mdfstr1[1].toString().substring(1, 3));
                int y1 = Integer.valueOf(mdfstr1[1].toString().substring(4, 6));
                String loc = mdfstr1[1].toString().substring(7, 8).toLowerCase();
                int boxid;
                //String arrow = tempMsg.substring(11, 12).toUpperCase();
                if ((x1 > 12) && (y1 < 2)) {
                    boxid = ((19 - 2) * 15) + 12;
                } else if ((x1 > 12) && (y1 >= 2)) {
                    boxid = ((19 - y1) * 15) + 12;
                } else if ((x1 <= 12) && (y1 < 2)) {
                    boxid = ((19 - 2) * 15) + x1;
                } else {
                    boxid = ((19 - y1) * 15) + x1;
                }
                setRobot(boxid, loc);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        String bin1 = new BigInteger(mdfstr1[2].toString(), 16).toString(2);
        Log.d(TAG, bin1);

        for (int temp = 1; temp < bin1.length(); temp++) {
            obstaclemap.add(Integer.parseInt(bin1.substring(temp, temp + 1)));
            //Log.d(TAG, mapdata.get(temp).toString());
        }
        String string1 = "";
        for (int temp = 0; temp < obstaclemap.size(); temp++) {
            string1 += obstaclemap.get(temp).toString();
        }
        Log.d(TAG, String.valueOf(string1));
        int tempint = 0;
        for (int i = 0; i < exploredmap.size(); i++) {
            //Log.d(TAG, exploredmap.get(i).toString());
            if (exploredmap.get(i).toString().equals("1") && exploredmap.get(i).toString().equals(obstaclemap.get(tempint).toString())) {
                int tempx = i % 15;
                int tempy = 19 - ((i - tempx) / 15);
                int tempyboxid = (tempy * 15) + tempx;
                obstaclelist.add(tempyboxid);
                if (tempint < obstaclemap.size()) {
                    tempint++;
                }
            } else if ((exploredmap.get(i).toString().equals("1") && obstaclemap.get(tempint).toString().equals("0"))) {
                if (tempint < obstaclemap.size()) {
                    tempint++;
                }
            }
        }
//            for (int i = 0; i < 300; i++) {
//                TextView t3 = findViewById(i);
//                for (int j = 0; j < obstaclelist.size(); j++) {
//                    if (i == obstaclelist.get(j)) {
//                        noArrowObstacles.add(i);
//                        t3.setBackground(obstacleImage);
//                        t3.setText("");
//                    }
//                }
//            }

        // }
        for (int i = 0; i < arrowObstacles.size(); i++) {
            String[] arrowsBoxID = arrowObstacles.get(i).split("/");
            arrowObstaclelist.add(Integer.parseInt(arrowsBoxID[0]));
        }

        for (int i = 0; i < 300; i++) {
            TextView t3 = findViewById(i);
            for (int j = 0; j < obstaclelist.size(); j++) {
                if (i == obstaclelist.get(j)) {
                    //     noArrowObstacles.add(i);// whats this for??
                    if (arrowObstaclelist.size() > 0) {
                        if (arrowObstaclelist.contains(obstaclelist.get(j))) {
                            for (int temp1 = 0; temp1 < arrowObstacles.size(); temp1++) {
                                String[] arrowsBoxID = arrowObstacles.get(temp1).split("/");
                                if (Integer.valueOf(arrowsBoxID[0].toString()) == i) {
                                    t3.setBackground(upImage);
                                    t3.setTextColor(Color.parseColor("#FFFFFF"));
                                    t3.setText(arrowsBoxID[1].toUpperCase());
                                }
                            }
                        } else {
                            t3.setBackground(obstacleImage);
                            t3.setText("");
                        }
                    } else {
                        t3.setBackground(obstacleImage);
                        t3.setText("");
                    }
                }
            }
        }
    }

    public void getArrowObstacleCoord(String text) {

        //obstacle regex
        String obstacleRegex = "(.*)(^O\\(0?1?[0-9],0?1?[0-9],[AWSDawsd]\\)$)(.*)";
        Pattern obstaclePattern = Pattern.compile(obstacleRegex);
        Matcher obstacleMatcher = obstaclePattern.matcher(text);
        String obstacleX = null;
        String obstacleY = null;
        String obstacleArrow = null;

        if (obstacleMatcher.find()) {
            //ob(xx,yy,N/S/E/W,u)
            try {

                Matcher matcher = Pattern.compile("[0-9A-za-z]+").matcher(text.substring(1, text.length()));
                switch (currentDirection) {
                    case "a":
                        direction = "left";
                        break;
                    case "w":
                        direction = "up";
                        break;
                    case "s":
                        direction = "down";
                        break;
                    case "d":
                        direction = "right";
                        break;
                }
                while (matcher.find()) {
                    receivedCoordinates++;
                    switch (receivedCoordinates) {
                        case 1:
                            obstacleX = matcher.group();
                            break;
                        case 2:
                            obstacleY = matcher.group();
                            break;
                        case 3:
                            obstacleArrow = matcher.group();
                            if (obstacleCount <= 5)
                                displayObstacle(obstacleX, obstacleY, obstacleArrow);

                            if (obstacleArrow != null) {
                                if (arrowCoordinates == null)
                                    arrowCoordinates = "S(" + obstacleX + "," + obstacleY + "," + obstacleArrow + ")";
                                else if (obstacleArrow != null && obstacleCount <= 5)
                                    arrowCoordinates += " \r\nS(" + obstacleX + "," + obstacleY + "," + obstacleArrow + ")";
                                TextView txtArrow = findViewById(R.id.txtArrow);
                                txtArrow.setText(arrowCoordinates);
                            }

                            break;


                    }
                    //reset to set the next obstacle pointjm
                    if (receivedCoordinates == 3)
                        receivedCoordinates = 0;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public void robotStart() {
        startXValue = findViewById(R.id.startXValue);
        startYValue = findViewById(R.id.startYValue);
        topLeftCorner = 255;
        currentDirection = "w";

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
        Drawable obstacleImage = this.getResources().getDrawable(R.drawable.obstacle);

        Drawable upImage = this.getResources().getDrawable(R.drawable.up);
        Drawable downImage = this.getResources().getDrawable(R.drawable.down);
        Drawable leftImage = this.getResources().getDrawable(R.drawable.left);
        Drawable rightImage = this.getResources().getDrawable(R.drawable.right);

        for (int y = 0; y < 300; y++) {
            TextView t = findViewById(y);
            if (!waypointList.contains(y)) {
                if (y == topLeftCorner || y == topLeftCorner + 1 || y == topLeftCorner + 2 || y == topLeftCorner + 15 || y == topLeftCorner + 16 || y == topLeftCorner + 17
                        || y == topLeftCorner + 30 || y == topLeftCorner + 31 || y == topLeftCorner + 32) {

                    if (y == topLeftCorner + 16) {
                        t.setText("");
                        switch (currentDirection) {
                            case "w":
                                t.setBackground(upImage);
                                break;
                            case "s":
                                t.setBackground(downImage);
                                break;
                            case "a":
                                t.setBackground(leftImage);
                                break;
                            case "d":
                                t.setBackground(rightImage);
                                break;

                        }
                    } else {
                        t.setBackground(robot);
                        t.setText("1");
                        t.setTextColor(Color.parseColor("#FF0000"));
                    }

                } else if (y == 12 || y == 13 || y == 14 || y == 27 || y == 28 || y == 29 || y == 42 || y == 43 || y == 44) {
                    t.setBackground(endpoint);
                    t.setText("");
                } else if (obstaclelist.contains(y) == true) //checking of obstacles with no arrows
                    t.setBackground(obstacleImage);
                else { //checking for obstacles with arrows
                    if (arrowObstacles.size() == 0) {
                        t.setText("");
                        t.setBackground(box);
                    }

                    for (int i = 0; i < arrowObstacles.size(); i++) {
                        String[] arrowsBoxID = arrowObstacles.get(i).split("/");
                        if (y == Integer.parseInt(arrowsBoxID[0])) {
                            t.setBackground(upImage);
                            t.setTextColor(Color.parseColor("#FFFFFF"));
                            t.setText(arrowsBoxID[1].toUpperCase());
                        } else {
                            t.setText("");
                            t.setBackground(box);
                        }

                    }
                }

            }

        }
    }
}