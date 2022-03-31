package com.Nulody.NupianoV2.MiBLE_UUID;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.Nulody.NupianoV2.Fragment3;

import java.util.List;

public class HeartRateService extends Service {

    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;
    BluetoothAdapter bluetoothAdapter;

    BluetoothGattCharacteristic hrChar;
    BluetoothGattCharacteristic stepChar;


    private final IBinder binder = new LocalBinder();

    public static final String NOTIFICATION_CHANNEL = "my_channel";
    public static final int NOTIFICATION_ID = 5;

//    public static final String HR_ACTION="id.aashari.code.miband2.HR";
//    public static final String HEART_RATE="HEART_RATE";


    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public IBinder onBind(Intent intentBLE) {
        return binder;
    }

    @Override
    public void onCreate() {
        initializeObjects();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL, "Heart Rate Service", NotificationManager.IMPORTANCE_LOW));
        }
    }

    public class LocalBinder extends Binder {
        HeartRateService getService() {
            // Return this instance of LocalService so clients can call public methods
            return HeartRateService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startConnecting(intent);
        return super.onStartCommand(intent, flags, startId);

    }


    @SuppressLint("MissingPermission")
    void startConnecting(Intent intent) {
        String address = intent.getStringExtra(Fragment3.EXTRAS_DEVICE_ADDRESS);
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        Log.v("test", "Connecting to " + address);
        Log.v("test", "Device name " + bluetoothDevice.getName());
        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);
    }

    @SuppressLint("MissingPermission")
    void stateConnected() {
        bluetoothGatt.discoverServices();
        listenHeartRate();
        Log.d("connected", "bluetoothGatt");
    }

    @SuppressLint("MissingPermission")
    void stateDisconnected() {
        bluetoothGatt.disconnect();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        Log.d("disconnected", "bluetoothGatt");
        super.onDestroy();
    }


    @SuppressLint("MissingPermission")
    public void listenHeartRate() {

        BluetoothGattCharacteristic hrm = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service).getCharacteristic(CustomBluetoothProfile.HeartRate.measurementCharacteristic);
        BluetoothGattDescriptor descriptor = hrm.getDescriptor(CustomBluetoothProfile.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
        bluetoothGatt.setCharacteristicNotification(hrm, true);

    }

    //每11秒命令量測
    private final Handler mHandler = new Handler();
    Runnable runnable = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            //傳送量測命令
            BluetoothGattCharacteristic hmc = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service).getCharacteristic(CustomBluetoothProfile.HeartRate.controlCharacteristic);
            hmc.setValue(new byte[]{0x15, 0x01, 0x01});
            bluetoothGatt.writeCharacteristic(hmc);
            mHandler.postDelayed(this, 11000);
        }
    };


    public void ReadStep() {

        BluetoothGattCharacteristic stp = bluetoothGatt.getService(CustomBluetoothProfile.Basic.service).getCharacteristic(CustomBluetoothProfile.Basic.stepCharacteristic);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothGatt.readCharacteristic(stp);
//        bluetoothGatt.setCharacteristicNotification(stp, true);

    }

    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.v("test", "onConnectionStateChange");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                stateConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stateDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.v("test", "onServicesDiscovered");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();

                for (BluetoothGattService bluetoothGattService : services) {
                    Log.d(TAG, "onServicesDiscovered service: " + bluetoothGattService.getUuid());
                    List<BluetoothGattCharacteristic> charc = bluetoothGattService.getCharacteristics();

                    for (BluetoothGattCharacteristic charac : charc) {
                        if (charac.getUuid().equals(CustomBluetoothProfile.HeartRate.measurementCharacteristic)) {
                            Log.d(TAG, "hrChar found!");
                            //设备 震动特征值
                            hrChar = charac;
                            listenHeartRate();
                            mHandler.postDelayed(runnable, 11000);
                        }
                        if (charac.getUuid().equals(CustomBluetoothProfile.Basic.stepCharacteristic)) {
                            Log.d(TAG, "stepchar found!");
                            //设备 步数
                            stepChar = charac;
                            //ReadStep();
                        }
                    }
                }
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.v("test", "onCharacteristicRead");
            if (characteristic == stepChar) {
                byte[] ab = characteristic.getValue();
                if (ab == null) {
                    Log.v("test", "realtime steps: value is null");
                    return;
                }
                if (ab.length == 13) {
                    int steps = ab[1] & 0xff | (ab[2] & 0xff) << 8;
                    sendBroadCast("step", steps + "");
                    int distance = ab[5] & 0xff | (ab[6] & 0xff) << 8 | ab[7] & 0xff | (ab[8] & 0xff) << 24;
                    //int distance = ((((ab[5] & 255) | ((ab[6] & 255) << 8)) | (ab[7] & 16711680)) | ((ab[8] & 255) << 24));
                    sendBroadCast("distance", distance + "");
                    int calories = ab[9] & 0xff | (ab[10] & 0xff) << 8 | ab[11] & 0xff | (ab[12] & 0xff) << 24;
                    sendBroadCast("calories", calories + "");
                }
            }
            //txtByte.setText(Arrays.toString(data));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v("test", "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.v("test", "onCharacteristicChanged");

            if (characteristic == hrChar) {
                byte[] data = characteristic.getValue();
                sendBroadCast("hr" , data[1]+"");
            }
//            Log.v("rate", Arrays.toString(data));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.v("test", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.v("test", "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.v("test", "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.v("test", "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.v("test", "onMtuChanged");
        }

    };

    public void sendBroadCast(String type, String data)
    {
        Intent intentBLE = new Intent();
        intentBLE.setAction(type);
        intentBLE.putExtra(type,data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentBLE);
    }
}
