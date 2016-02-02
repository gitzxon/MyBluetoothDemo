package com.zxon.mybluetoothdemo.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothUuid;
import android.bluetooth.client.pbap.BluetoothPbapClient;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;

import com.zxon.mybluetoothdemo.util.LogUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by leon on 16/2/1.
 */
public class BluetoothService extends Service {

    public static final int STATE_SCANNING = 1;
    public static final int STATE_NONE = 2;
    public static final int STATE_CONNECTING = 3;
    public static final int STATE_CONNECTED = 4;
    private static final UUID MY_UUID_SECURE = UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB");


    public static BluetoothPbapClient sPbapClient;
    private static BluetoothAdapter sBluetoothAdapter;
    public static List<BluetoothDevice> sDevicesAvailable;
    public static BluetoothServiceHandler sHandler = new BluetoothServiceHandler();
    public static BluetoothSocket sBluetoothSocket;
    public static Context sContext;

    private int mState = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(getClass().getSimpleName() + " onCreate ");

        if (sBluetoothAdapter == null) {
            sBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        sContext = this;
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
        if (sBluetoothSocket != null && sBluetoothSocket.isConnected()) {
            try {
                sBluetoothSocket.close();
            } catch (IOException e) {
                LogUtil.d("in onDestroy, sBluetoothSocket.close failed !");
                e.printStackTrace();
            }
        }
        super.onDestroy();
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

    public void establishPbap(String address) {
        // Get the BluetoothDevice object
        BluetoothAdapter adapter = getsBluetoothAdapter();
        BluetoothDevice device = adapter.getRemoteDevice(address);
        if (sPbapClient == null) {
            sPbapClient = new BluetoothPbapClient(device, sHandler);
            sPbapClient.connect();
            LogUtil.d("after sPbapClient.connect()");
        }
    }


    public void showAllAvailableUuids(BluetoothDevice device) {
        LogUtil.d("-------------");

        ParcelUuid[] uuids = device.getUuids();
        if (uuids == null || uuids.length == 0) {
            LogUtil.d("get uuids failed");
        } else {
            for (ParcelUuid uuid : uuids) {
                LogUtil.d("uuid is : " + uuid);
            }
            LogUtil.d("-------------");
        }
    }

    public void showBondedState(BluetoothDevice device){
        // bond state
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDED: {
                LogUtil.d("BONDED");
            }
            break;
            case BluetoothDevice.BOND_NONE: {
                LogUtil.d("NONE");
            }
            break;
            case BluetoothDevice.BOND_BONDING: {
                LogUtil.d("BONDING");
            }
            break;
            default: {
            }
        }
    }

    public void showDiscoveryState(){
        if (sBluetoothAdapter.isDiscovering()) {
            LogUtil.d("@@@ it is discovering @@@ ");
            sBluetoothAdapter.cancelDiscovery();
        } else {
            LogUtil.d("@@@ it is not discovering @@@");
        }
    }

    public void establishSocket(String address) {
        BluetoothAdapter adapter = getsBluetoothAdapter();
        BluetoothDevice device = adapter.getRemoteDevice(address);


        ParcelUuid uuid = BluetoothUuid.Handsfree;

        showAllAvailableUuids(device);
        showBondedState(device);
        showDiscoveryState();

        try {
            LogUtil.d("try to createRfcommSocketToServiceRecord");
            sBluetoothAdapter.cancelDiscovery();


            sBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid.getUuid());

            try {
                sBluetoothSocket.connect();
            } catch (IOException e) {
                LogUtil.d("sBLuetoothSocket.connect() is failed -------from teh IOException");
                try {
                    sBluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
            LogUtil.d("state of sBluetoothSocket : " + sBluetoothSocket.isConnected());

        } catch (IOException e) {
            LogUtil.d("createRfcommSocketToServiceRecord failed");
            e.printStackTrace();
        }

    }

    public void placeCall() {
        getHeadsetClient();
    }

    public void getHeadsetClient() {
        BluetoothProfile.ServiceListener listener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                LogUtil.d("BluetoothProfile.ServiceListener onServiceConnected");
                if (profile == BluetoothProfile.HEADSET) {
                    if (proxy == null) {
                        LogUtil.d("the proxy is null ! ");
                        return;
                    } else {
                        LogUtil.d("the proxy is not null ! ");
                    }

                    BluetoothHeadset headset = (BluetoothHeadset) proxy;

                    List<BluetoothDevice> connectedList = headset.getConnectedDevices();
                    if (connectedList.size() == 0) {
                        LogUtil.d("the connectedList is of size 0 ! ");
                        return;
                    }

                    BluetoothDevice device = connectedList.get(0);

                    boolean isAudioConnected = headset.isAudioConnected(device);
                    LogUtil.d("isAudioConnected : " + isAudioConnected);

                    boolean isVoiceRecognitionEnable = headset.startVoiceRecognition(device);
                    LogUtil.d("isVoiceRecognitionEnable : " + isVoiceRecognitionEnable);
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                LogUtil.d("onServiceDisconnected");
            }
        };

        if (sBluetoothAdapter.getProfileProxy(sContext, listener, BluetoothProfile.HEADSET)) {
            LogUtil.d("Get the profile proxy object associated with the profile --- success ! ");
        } else {
            LogUtil.d("getProfileProxy failed ! ");
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
