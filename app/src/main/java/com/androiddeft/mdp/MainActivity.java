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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
    ArrayList<Integer> noArrowObstacles = new ArrayList<Integer>(); //without arrows
    ArrayList<Integer> arrowObstacles = new ArrayList<Integer>(); //with arrows

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

    //explored
    String exploredX = null;
    String exploredY = null;
    TextView exploredTV;
    ArrayList<Integer> exploredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        Toast.makeText(getApplicationContext(), "Waypoint coordinates removed.", Toast.LENGTH_LONG).show();
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

                topLeftCorner = 255;
                currentDirection = "w";

                robotStart();
                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "fastest");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
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

        //Exploration
        start1 = findViewById(R.id.startButton1);
        stop1 = findViewById(R.id.stopButton1);
        time1 = findViewById(R.id.timerValue1);
        start1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "explore");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
                stopTimer1 = false;
                startTime1 = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread1, 0);
                start1.setVisibility(View.GONE);
                stop1.setVisibility(View.VISIBLE);

            }
        });

        stop1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                stopTimer1 = true;
                customHandler.removeCallbacks(null);
                stop1.setVisibility(View.GONE);
                start1.setVisibility(View.VISIBLE);
                start1.setText("Restart");
                time1.setText("00:00:000");

            }
        });

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


                    refresh.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "In auto mode", Toast.LENGTH_LONG).show();

                } else {
                    // The toggle is disabled
                    autoMode = false;
                    refresh.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "In manual mode", Toast.LENGTH_LONG).show();

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

                Toast.makeText(getApplicationContext(), "Refreshed.", Toast.LENGTH_LONG).show();

            }
        });


        //Directions
        movementTextView = findViewById(R.id.movementTextView);
        up = findViewById(R.id.upButton);

        up.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (autoMode == true)
                    robotMovement();
                else
                    manualList.add("w");

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bw");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
            }
        });
        up.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (autoMode == true)
                    robotMovement();
                else
                    manualList.add("w");

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bw");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
            }
        }));
        down = findViewById(R.id.downButton);

        down.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (autoMode == true)
                    robotRotate("s");
                else
                    manualList.add("s");

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bs");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

            }
        });

        down.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (autoMode == true)
                    robotRotate("s");
                else
                    manualList.add("s");

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bs");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
            }
        }));

        left = findViewById(R.id.leftButton);

        left.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (autoMode == true)
                    robotRotate("a");
                else
                    manualList.add("a");

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "ba");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

            }
        });

        left.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (autoMode == true)
                    robotRotate("a");
                else
                    manualList.add("a");

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "ba");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
            }
        }));
        right = findViewById(R.id.rightButton);

        right.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (autoMode == true)
                    robotRotate("d");
                else
                    manualList.add("d");

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bd");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

            }
        });
        right.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (autoMode == true)
                    robotRotate("d");
                else
                    manualList.add("d");

                Intent messaging_intent = new Intent("outMsg");
                messaging_intent.putExtra("outgoingmsg", "bd");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);
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
    }

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String obstacleX = null;
            String obstacleY = null;
            String obstacleArrow = null;
            incomingMessage = intent.getStringExtra("incomingmsg");
            Toast.makeText(getApplicationContext(), "Received: " + incomingMessage,
                    Toast.LENGTH_SHORT).show();

            //obstacle regex
            String obstacleRegex = "(.*)(^O\\(0?1?[0-9],0?1?[0-9],([^0]|[^1]?)\\)$)(.*)";
            Pattern obstaclePattern = Pattern.compile(obstacleRegex);
            Matcher obstacleMatcher = obstaclePattern.matcher(incomingMessage);

            //MDF1 regex
            String MDF1Regex = "(.*)(^MDF1)(.*)";
            Pattern MDF1Pattern = Pattern.compile(MDF1Regex);
            Matcher MDF1Matcher = MDF1Pattern.matcher(incomingMessage);

            //MDF2 regex
            String MDF2Regex = "(.*)(^MDF2)(.*)";
            Pattern MDF2Pattern = Pattern.compile(MDF2Regex);
            Matcher MDF2Matcher = MDF2Pattern.matcher(incomingMessage);

            //Explored regex
            String exploredRegex = "(.*)(^E\\(0?1?[0-9],0?1?[0-9]\\)$)(.*)";
            Pattern exploredPattern = Pattern.compile(exploredRegex);
            Matcher exploredMatcher = exploredPattern.matcher(incomingMessage);

            //display obstacle with arrows
            TextView txtArrow = findViewById(R.id.txtArrow);

            if (incomingMessage.equals("s") || incomingMessage.equals("d") || incomingMessage.equals("a"))
                robotRotate(incomingMessage);
            else if (incomingMessage.equals("w"))
                robotMovement();
            else if (incomingMessage.equals("stop"))
                movementTextView.setText("Stopped");
            else if (MDF1Matcher.find()) { //Eg: MDF1ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
                MDF1Value = incomingMessage.substring(4, incomingMessage.length());
                TextView MDF1 = findViewById(R.id.txtMDF1);
                MDF1.setText(MDF1Value);

            } else if (MDF2Matcher.find()) { //Eg: MDF2ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
                MDF2Value = incomingMessage.substring(4, incomingMessage.length());
                TextView MDF2 = findViewById(R.id.txtMDF2);
                MDF2.setText(MDF2Value);

            } else if (exploredMatcher.find()) {
                //compare and ensure that it is O(x,y)
                Matcher matcher = Pattern.compile("[0-9]+").matcher(incomingMessage);
                while (matcher.find()) {
                    receivedCoordinates++;
                    switch (receivedCoordinates) {
                        case 1:
                            exploredX = matcher.group();
                            break;
                        case 2:
                            exploredY = matcher.group();
                            break;

                    }
                    //reset to set the next explored point
                    if (receivedCoordinates == 2) {
                        receivedCoordinates = 0;

                        displayExplored(exploredX, exploredY);
                    }
                }
            } else if (obstacleMatcher.find()) {
                //compare and ensure that it is O(x,y)
                Matcher matcher = Pattern.compile("[0-9]+").matcher(incomingMessage);
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
                            displayObstacle(obstacleX, obstacleY, obstacleArrow);

                            if (obstacleArrow.equals("1")) {
                                if (arrowCoordinates == null)
                                    arrowCoordinates = "S(" + obstacleX + "," + obstacleY + "," + direction + ")";
                                else
                                    arrowCoordinates += " \r\nS(" + obstacleX + "," + obstacleY + "," + direction + ")";

                                txtArrow.setText(arrowCoordinates);
                            }

                            break;


                    }
                    //reset to set the next obstacle pointjm
                    if (receivedCoordinates == 3)
                        receivedCoordinates = 0;
//                    Toast.makeText(getApplicationContext(), "Obstacle:" + obstacleX + "," + obstacleY + "," + obstacleArrow, Toast.LENGTH_LONG).show();


                }
            }
        }
    };

    private void displayExplored(String exploredX, String exploredY) {
        Drawable explored = this.getResources().getDrawable(R.drawable.explored);
        int exploredPoint = -(((Integer.valueOf(exploredY) - 19) * 15) - Integer.valueOf(exploredX));
        exploredTV = findViewById(exploredPoint);
        exploredTV.setText("1");
        exploredList.add(exploredPoint);

    }

    private void displayObstacle(String obstacleX, String obstacleY, String obstacleArrow) {
        Drawable upImage = this.getResources().getDrawable(R.drawable.up);
        Drawable obstacleImage = this.getResources().getDrawable(R.drawable.obstacle);
        int obstacleXValue = Integer.valueOf(obstacleX);
        int obstacleYValue = Integer.valueOf(obstacleY);

        int obstaclePoint = -(((obstacleYValue - 19) * 15) - obstacleXValue);
        TextView op = findViewById(obstaclePoint);

        if (obstacleArrow.equals("1") && obstacleCount <= 5) {
            //with arrow
            op.setText("U");
            op.setBackground(upImage);
            op.setTextColor(Color.parseColor("#FFFFFF"));
            op.setGravity(Gravity.CENTER);
            obstacleCount++;
            arrowObstacles.add(obstaclePoint);
            Toast.makeText(getApplicationContext(), "Obstacle arrow created at " + obstaclePoint, Toast.LENGTH_LONG).show();
        } else {
            //without arrow
            op.setBackground(obstacleImage);
            noArrowObstacles.add(obstaclePoint);
            Toast.makeText(getApplicationContext(), "Obstacle created at " + obstaclePoint, Toast.LENGTH_LONG).show();
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


            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

                //test using AMD
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
            if (!waypointList.contains(y)) {
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


        }
        movementTextView.setText("Turning " + currentDirection);
    }

    public void robotMovement() {
        Drawable box = this.getResources().getDrawable(R.drawable.box);
        Drawable robot = this.getResources().getDrawable(R.drawable.robot);
        Drawable endpoint = this.getResources().getDrawable(R.drawable.endpoint);
        Drawable obstacleImage = this.getResources().getDrawable(R.drawable.obstacle);

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

        for (int y = 0; y < 300; y++) {
            TextView t = findViewById(y);
            if (selectedWaypoint == false) {
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

                } else if (y == 12 || y == 13 || y == 14 || y == 27 || y == 28 || y == 29 || y == 42 || y == 43 || y == 44)
                    t.setBackground(endpoint);
                else if (arrowObstacles.contains(y)) {
                    t.setBackground(upImage);
                    t.setText("U");
                    t.setTextColor(Color.parseColor("#FFFFFF"));
                    t.setGravity(Gravity.CENTER);
                }
                else if (noArrowObstacles.contains(y))
                    t.setBackground(obstacleImage);
                else {
                    t.setBackground(box);
                }


            } else if (!waypointList.contains(y) && selectedWaypoint == true) {
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

                } else if (y == 12 || y == 13 || y == 14 || y == 27 || y == 28 || y == 29 || y == 42 || y == 43 || y == 44)
                    t.setBackground(endpoint);
                else if (arrowObstacles.contains(y)) {
                    t.setBackground(upImage);
                    t.setText("U");
                } else if (noArrowObstacles.contains(y))
                    t.setBackground(obstacleImage);
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
                } else if (arrowObstacles.contains(y) == false && noArrowObstacles.contains(y) == false) {
                    t.setText("");
                    t.setBackground(box);

                }
            }

        }

    }

}