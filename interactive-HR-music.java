package com.Nulody.NupianoV2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.Nulody.NupianoV2.MiBLE_UUID.HeartRateService;
import com.Nulody.NupianoV2.support_source.MusicNote4d;
import static com.Nulody.NupianoV2.support_source.FileOP.m_nBMP;

import java.util.List;


/**
 * Created by Chani on 2022/03/28.
 */
public class Fragment3 extends Fragment {

    private TextView deviceText;
    private TextView heraRateText;
    private TextView textView_ageTV;
    private TextView textView_age;
    private TextView textView_HRrestTV;
    private TextView textView_HRrest;
    private TextView textView_HRmaxTV;
    private TextView textView_HRmax;
    private TextView textView_OrBPMTV;
    private TextView textView_OrBPM;
    private TextView textView_NewBPMTV;
    private TextView textView_NewBPM;
    private Button connectButton;
    private Button startButton;
    private Button stopButton;

    private Intent serviceIntent;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    BluetoothAdapter bluetoothAdapter;


    private String mDeviceName;
    private String mDeviceAddress;

    private float mHR;
    private int HRmax;
    private int HRrest;
    private int m;
    int m_newtempo = 60;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment3, container, false);

        mDeviceName = getActivity().getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = getActivity().getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);

        heraRateText = view.findViewById(R.id.textView_HR);
        deviceText = view.findViewById(R.id.textView_device_BLE);
        textView_ageTV = view.findViewById(R.id.textView_ageTV);
        textView_age = view.findViewById(R.id.textView_age);
        textView_HRrestTV = view.findViewById(R.id.textView_HRrestTV);
        textView_HRrest = view.findViewById(R.id.textView_HRrest);
        textView_HRmaxTV = view.findViewById(R.id.textView_HRmaxTV);
        textView_HRmax = view.findViewById(R.id.textView_HRmax);
        textView_OrBPMTV = view.findViewById(R.id.textView_OrBPMTV);
        textView_OrBPM = view.findViewById(R.id.textView_OrBPM);
        textView_NewBPMTV = view.findViewById(R.id.textView_NewBPMTV);
        textView_NewBPM = view.findViewById(R.id.textView_NewBPM);
        connectButton = view.findViewById(R.id.button_connect_BLE);
        startButton = view.findViewById(R.id.button_start);
        stopButton = view.findViewById(R.id.button_stop);


        initializeObjects();
        initListeners();

        if (m_nBMP > 0) {
            textView_OrBPM.setText(String.valueOf(m_nBMP));
            textView_NewBPM.setText(String.valueOf(m_nBMP));
        }

        if (mDeviceAddress != null)
            deviceText.setText(mDeviceAddress + " Connected");

        Spinner spinner1 = view.findViewById(R.id.spinner_mode);

        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(getContext()
                , R.array.array_mode, android.R.layout.simple_spinner_item);
        spinner1.setAdapter(adapter1);

        spinner1.setOnItemSelectedListener(modelis);

        // Inflate the layout for this fragment
        return view;
    }

    public AdapterView.OnItemSelectedListener modelis = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            switch (i) {
                case 0:
                    if (m_nBMP > 0) { m=0; }
                    break;
                case 1:
                    if (mHR > 0  && m_nBMP > 0) { m=1; }
                    break;
                case 2:
                    if (mHR > 0 && m_nBMP > 0) { m=2; }
                    break;
                default:
                    if (m_nBMP > 0) { m=0; }
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    private void initListeners() {
        connectButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                getBoundedDevice();
                int age = Integer.parseInt(textView_age.getText().toString());
                HRmax = Math.toIntExact(Math.round(206.9 - (0.67 * age)));
                textView_HRmax.setText(String.valueOf(HRmax));
                HRrest = Integer.parseInt(textView_HRrest.getText().toString());
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                startService();
                int age = Integer.parseInt(textView_age.getText().toString());
                HRmax = Math.toIntExact(Math.round(206.9 - (0.67 * age)));
                textView_HRmax.setText(String.valueOf(HRmax));
                HRrest = Integer.parseInt(textView_HRrest.getText().toString());
            }

        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().stopService(serviceIntent);
            }
        });
    }

    private void startService() {
        serviceIntent = new Intent(getActivity(), HeartRateService.class);
        if (mDeviceAddress != null && bluetoothAdapter.isEnabled()) {
            Toast.makeText(getActivity(), "Connecting", Toast.LENGTH_SHORT).show();
            serviceIntent.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
            getActivity().startService(serviceIntent);

        } else {
            Toast.makeText(getActivity(), "No Mi band connected, Pair device first", Toast.LENGTH_SHORT).show();
        }
    }

    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @SuppressLint("MissingPermission")
    void getBoundedDevice() {
        Toast.makeText(getActivity(), "Checking available devices", Toast.LENGTH_SHORT).show();

        if (!bluetoothAdapter.isEnabled()) {
            deviceText.setText("Turn on the bluetooth");
            Toast.makeText(getActivity(), "Turn on the bluetooth to scan", Toast.LENGTH_SHORT).show();
            return;
        }
        BluetoothManager btManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        @SuppressLint("MissingPermission")
        List<BluetoothDevice> ConnectedDevice = btManager.getConnectedDevices(BluetoothProfile.GATT);;
        for (BluetoothDevice cd : ConnectedDevice) {
            if (cd.getName().contains("Amazfit")) {
                mDeviceAddress=cd.getAddress();
                deviceText.setText(cd.getName()+" connected");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("hr");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);

    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister local broadcast
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context contextBLE, Intent intentBLE) {

            if (intentBLE.getAction().equals("hr")) {
                heraRateText.setText(intentBLE.getStringExtra("hr"));
                String date = intentBLE.getStringExtra("hr");
                mHR = Math.abs(Integer.parseInt(date));
                switch (m){
                    case 0:
                        if (m_nBMP > 0) {
                            MusicNote4d.SetBPM(m_nBMP);
                            textView_OrBPM.setText(String.valueOf(m_nBMP));
                            textView_NewBPM.setText(String.valueOf(m_nBMP));
                        }
                        break;
                    case 1:
                        textView_OrBPM.setText(String.valueOf(m_nBMP));
                        double enhance = Math.round(((1 - Math.exp(-mHR / (HRmax - mHR))) * 2.1) * 10.0) / 10.0;
                        m_newtempo = (int) (enhance * m_nBMP);
                        MusicNote4d.SetBPM(m_newtempo);
                        textView_NewBPM.setText(String.valueOf(m_newtempo));
                        break;
                    case 2:
                        textView_OrBPM.setText(String.valueOf(m_nBMP));
                        double relax = Math.round(HRrest / mHR * 10.0) / 10.0;
                        m_newtempo = (int) (relax * m_nBMP);
                        MusicNote4d.SetBPM(m_newtempo);
                        textView_NewBPM.setText(String.valueOf(m_newtempo));
                        break;
                    default:
                        MusicNote4d.SetBPM(60);
                        break;
                }
            }
        }
    };

}
