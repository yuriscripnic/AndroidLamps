package physis.androidlamps;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

class BluetoothController {
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private String address = "XX:XX:XX:XX:XX:XX";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private InputStream inStream = null;
    private OutputStream outStream = null;
    private Handler handler = null;
    private byte delimiter = '\n';
    private boolean stopWorker = false;
    private int readBufferPosition = 0;
    private byte[] readBuffer = new byte[1024];
    private Context context;
    private String lastMessage =null;
    private String CHAR_SET = "US-ASCII";

    private static final String TAG = "BluetoothController";
    public BluetoothController(Context context){
        this.context = context;
        this.handler =  new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1) lastMessage = (String) msg.obj;
            }
        };
    }

    private void CheckBt() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "Bluetooth Disabled !",
                    Toast.LENGTH_SHORT).show();
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(context,
                    "Bluetooth null !", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void connect() {
        CheckBt();
        Log.d(TAG, address);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        Log.d(TAG, "Connecting to ... " + device);
        mBluetoothAdapter.cancelDiscovery();
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            btSocket.connect();
            Log.d(TAG, "Connection made.");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.d(TAG, "Unable to end the connection");
            }
            Log.d(TAG, "Socket creation failed");
        }

        beginListenForData();
    }

    public void write(String data) {
        try {
            byte[] bytes = data.getBytes(CHAR_SET);
            outStream.write(bytes);
        } catch (IOException e) { }
    }

    public String read(){
        String lm = lastMessage;
        lastMessage = null;
        return lm;
    }

    public int avaliable(){
        if(lastMessage != null && lastMessage.length()>0){
            return lastMessage.length();
        }
        return 0;
    }

    public void close(){
        try {
            btSocket.close();
        } catch (IOException e) {
        }
    }


    private void beginListenForData()   {
        try {
            inStream = btSocket.getInputStream();
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
        }

        Thread workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = inStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, CHAR_SET);
                                    readBufferPosition = 0;
                                    handler.obtainMessage(1,data).sendToTarget();
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

}
