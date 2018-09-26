package com.androiddeft.mdp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static java.lang.Thread.sleep;


public class DeviceListActivity extends Activity {
    BluetoothConnectionService mBluetoothConnection;
    TextView status;
    ListView listmsg;
    private static final String TAG = "DeviceListActivity";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayAdapter<String> NewDevicesArrayAdapter;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    //static HandleSearch handleSearch;
    BluetoothDevice btDevice, lastbtDevice;
    ArrayList<BluetoothDevice> arrayListPairedBluetoothDevices;
    ArrayList<BluetoothDevice> arrayListBluetoothDevices = null;

    MainActivity main = null;
    //ListItemClicked listItemClicked;

    private static final UUID mdpUUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);
        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        arrayListBluetoothDevices = new ArrayList<BluetoothDevice>();

        // Initialize the button to perform device discovery
        Button scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });
        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        NewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Get the local Bluetooth adapter
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        arrayListPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
        // Get a set of currently paired devices
        getPairedDevices();
        getNewDevice();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, filter);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver3, new IntentFilter("outMsg"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver4, new IntentFilter("reconnectMsg"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5, new IntentFilter("robustMsg"));

    }

    public void startConnection() {
        //Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection");
        startBTConnection(btDevice, mdpUUID);
        lastbtDevice = btDevice;
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection");
        mBluetoothConnection.startClient(device, uuid);
        mBluetoothConnection.startClient(device, uuid);
    }

    private void getNewDevice() {
        NewDevicesArrayAdapter.clear();
        NewDevicesArrayAdapter.notifyDataSetChanged();
        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = findViewById(R.id.new_devices);

        newDevicesListView.setAdapter(NewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(newDeviceClickListener);
    }

    private void getPairedDevices() {
        pairedDevicesArrayAdapter.clear();
        // Find and set up the ListView for paired devices
        ListView pairedListView = findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(PairedDeviceClickListener);
        Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                arrayListPairedBluetoothDevices.add(device);
            }
            pairedDevicesArrayAdapter.notifyDataSetChanged();
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (myBluetoothAdapter != null) {
            myBluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        unregisterReceiver(mBroadcastReceiver1);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */


    private void doDiscovery() {

        if (myBluetoothAdapter != null) {
            arrayListBluetoothDevices.clear();
        }
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mBroadcastReceiver1, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mBroadcastReceiver1, filter);
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (myBluetoothAdapter.isDiscovering()) {
            myBluetoothAdapter.cancelDiscovery();
        }
        //checkBTPermissions();
        // Request discover from BluetoothAdapter
        myBluetoothAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener PairedDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            myBluetoothAdapter.cancelDiscovery();
            btDevice = arrayListPairedBluetoothDevices.get(arg2);
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
//            Intent intent = new Intent();
//            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
//            setResult(Activity.RESULT_OK, intent);
            //startConnection();

            mBluetoothConnection = new BluetoothConnectionService(DeviceListActivity.this);
            startConnection();

            finish();


        }
    };

    private AdapterView.OnItemClickListener newDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            myBluetoothAdapter.cancelDiscovery();
            btDevice = arrayListBluetoothDevices.get(arg2);
            // Get the device MAC address, which is the last 17 chars in the View
            //Boolean isBonded = false;
            try {
                btDevice.createBond();
                sleep(5000);
                Log.d(TAG, "Device Name = " + btDevice.getName());
                Log.d(TAG, "Device Address = " + btDevice.getAddress());
                mBluetoothConnection = new BluetoothConnectionService(DeviceListActivity.this);
                startConnection();
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    NewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    //pairing
                    arrayListBluetoothDevices.add(device);
                }
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED");
                    btDevice = device;
                }
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING");
                }
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE");
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (NewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    NewDevicesArrayAdapter.add(noDevices);
                }
            }


        }
    };


    protected final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED");
                    btDevice = mDevice;
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE");
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String outgoingmsg = intent.getStringExtra("outgoingmsg");
            byte[] bytes = outgoingmsg.getBytes(Charset.defaultCharset());
            mBluetoothConnection.write(bytes);
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startBTConnection(lastbtDevice, mdpUUID);
            Toast.makeText(getApplicationContext(), "Bluetooth reconnected.", Toast.LENGTH_LONG).show();
        }
    };


    private final BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBluetoothConnection.stop();
            Toast.makeText(getApplicationContext(), "Bluetooth disconnected.", Toast.LENGTH_LONG).show();
        }
    };
}