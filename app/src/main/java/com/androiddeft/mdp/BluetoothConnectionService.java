package com.androiddeft.mdp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {

    private static final String TAG = "BluetoothService";
    private static final UUID mdpUUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String appName = "Bluetooth Communication";
    private final BluetoothAdapter myBluetoothAdapter;
    Context context;
    Context mContext;

    private String devicename = "";

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    private BluetoothSocket tempSocket;

    private ConnectedThread mConnectedThread;

    MainActivity mainact;

    public BluetoothConnectionService(Context context) {
        this.context = context;
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                //s2 to nexus
                tmp = myBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, mdpUUID);
                Log.d(TAG, "AcceptThread: Setting up Server using: " + mdpUUID);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            mmServerSocket = tmp;
            //mState = STATE_LISTEN;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try {
                Log.d(TAG, "run:RFCOM server socket start...");
                socket = mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection.");
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            if (socket != null) {
                connected(socket, mmDevice);
            }
            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        BluetoothSocket tmp = null;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            mmDevice = device;
            deviceUUID = uuid;
            try {
                //nexus to tablet
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        + deviceUUID);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
                devicename = mmDevice.getName();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }
        }

        public void run() {
            mmSocket = tmp;
            myBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
                connected(mmSocket, mmDevice);
            } catch (IOException e) {
                // Close the socket

                mmSocket = reconnect(tmp);
                Log.d(TAG, "ConnectThread: Reconnect");
            }
        }

        BluetoothSocket reconnect(BluetoothSocket tmp) {
            Class<?> class2 = tmp.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};

            Method m = null;
            try {
                m = class2.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};

                BluetoothSocket fbs = (BluetoothSocket) m.invoke(tmp.getRemoteDevice(), params);
                fbs.connect();
                connected(fbs, mmDevice);
                return fbs;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }


    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
        // Update UI title
        //updateUserInterfaceTitle();
    }

    /**
     * AcceptThread starts and sits waiting for a connection.
     * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     **/

    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: Started.");

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    /**
     * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     * receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);


                    Intent messaging_intent = new Intent("inMsg");
                    messaging_intent.putExtra("incomingmsg", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(messaging_intent);

                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            try {
                mmOutStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        mConnectedThread.write(out);
    }

    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        Log.d(TAG, "Disconnected");
    }

}