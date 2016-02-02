package com.zxon.mybluetoothdemo.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.client.pbap.BluetoothPbapClient;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.zxon.mybluetoothdemo.util.LogUtil;

import java.util.List;

/**
 * Created by leon on 16/2/1.
 */
public class BluetoothService extends Service {

    public static final int STATE_SCANNING = 1;
    public static final int STATE_NONE = 2;
    public static final int STATE_CONNECTING = 3;
    public static final int STATE_CONNECTED = 4;


    public static BluetoothPbapClient sPbapClient;
    private static BluetoothAdapter sBluetoothAdapter;
    public static List<BluetoothDevice> sDevicesAvailable;
    public static BluetoothServiceHandler sHandler = new BluetoothServiceHandler();

    private int mState = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(getClass().getSimpleName() + " onCreate ");

        if (sBluetoothAdapter == null) {
            sBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.d(getClass().getSimpleName() + " onBind ");
        return new BluetoothServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(getClass().getSimpleName() + " onStartCommand ");


        return super.onStartCommand(intent, flags, startId);
    }

    public boolean isBluetoothEnabled() {
        return getsBluetoothAdapter().isEnabled();
    }

    public int startScanningDevices() {
        if (!getsBluetoothAdapter().isDiscovering()) {
            getsBluetoothAdapter().startDiscovery();
        } else {
            // do nothing
        }
        return 0;
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (getsBluetoothAdapter().getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public BluetoothService() {
        super();
    }

    public static BluetoothAdapter getsBluetoothAdapter() {
        return sBluetoothAdapter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void connect(String address) {
        // Get the BluetoothDevice object
        BluetoothAdapter adapter = getsBluetoothAdapter();
        BluetoothDevice device = adapter.getRemoteDevice(address);
        if (sPbapClient == null) {
            sPbapClient = new BluetoothPbapClient(device, sHandler);
            sPbapClient.connect();
            LogUtil.d("after sPbapClient.connect()");
        }

    }

    public void getPhoneBook() {
        if (sPbapClient != null && sPbapClient.getState() == BluetoothPbapClient.ConnectionState.CONNECTED) {
            LogUtil.d("pulling the PhoneBook, it may take a long time ! ");
            sPbapClient.pullPhoneBook(BluetoothPbapClient.PB_PATH);
        } else {
            LogUtil.d("-----------------------------");
            LogUtil.d("sPbapClient is not ready ! ");
            LogUtil.d(sPbapClient);
            LogUtil.d(sPbapClient.getState());
            LogUtil.d("-----------------------------");

        }
    }

    public class BluetoothServiceBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }

    }

    public static class BluetoothServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            LogUtil.d(msg);
            switch (msg.what) {
                case BluetoothPbapClient.EVENT_PULL_PHONE_BOOK_DONE: {
                    LogUtil.d("EVENT_PULL_PHONE_BOOK_DONE");
                    sPbapClient.disconnect();
                }
                break;
                case BluetoothPbapClient.EVENT_SESSION_CONNECTED: {
                    LogUtil.d("EVENT_SESSION_CONNECTED");
                }
                break;
                case BluetoothPbapClient.EVENT_SESSION_DISCONNECTED: {
                    LogUtil.d("EVENT_SESSION_DISCONNECTED");
                }
                break;
                default: {
                }
            }
        }
    }
}
