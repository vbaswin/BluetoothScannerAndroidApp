package com.example.bluetoothscanner;

import android.content.Intent;

import java.util.Set;

import android.content.IntentFilter;

import android.nfc.Tag;
import android.view.ViewGroup;
import android.widget.Toast;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.widget.Button;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import androidx.activity.EdgeToEdge;

import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;

import android.Manifest;

import androidx.core.view.ViewCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner; // for API level > 18
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;

import androidx.core.view.WindowInsetsCompat;

import android.content.BroadcastReceiver;

import android.util.Log;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothDevice;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_DISCOVER_BLUETOOTH = 2;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 100;


    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private TextView deviceInfos;
    private Button scanButton;
    private boolean isScanning = false;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();


    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "inside onScanResult");
            BluetoothDevice device = result.getDevice();
            if (!devices.contains(device)) {
                devices.add(device);
                String info = device.getName() + " - " + device.getAddress();
                updateDeviceInfo(info);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        deviceInfos = findViewById(R.id.DeviceInfos);
        scanButton = findViewById(R.id.ScanButton);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.e(TAG, "Bluetooth not supported on this device");
            finish();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            // Request to enable Bluetooth if it's disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            // Bluetooth is enabled, check and request location permission
            checkLocationPermission();
        }

        displayPairedDevices();
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning) {
                    startScan();
                    scanButton.setText("Stop Scan");
                    int color = Color.parseColor("#FFF44336"); // Parse the color code
                    scanButton.setBackgroundColor(color);
                } else {
                    stopScan();
                    scanButton.setText("Start Scan");
                    int color = Color.parseColor("#FF4CAF50"); // Parse the color code
                    scanButton.setBackgroundColor(color);
                }
                isScanning = !isScanning;
            }
        });
    }

    private void startScan() {
//        Log.d(TAG, "Inside start scan");
        devices.clear();
        displayPairedDevices();
        deviceInfos.append("\n\nAvailable Devices\n\n");
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        Log.d(TAG, "BluetoothLeScanner: " + bluetoothLeScanner); // Verify BluetoothLeScanner object
        bluetoothLeScanner.startScan(scanCallback);
        Log.d(TAG, "Scan started with callback: " + scanCallback); // Verify callback registration
    }

    private void stopScan() {
//        Log.d(TAG, "Inside stop scan");
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
        }
        displayPairedDevices();
    }

    private void updateDeviceInfo(final String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceInfos.append("\n" + info);
            }
        });
    }
    private void displayPairedDevices() {
        deviceInfos.setText("");
        deviceInfos.append("Paired Devices\n\n");
        if (bluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

            for (BluetoothDevice device : devices) {
                deviceInfos.append("\n Device : " + device.getName() + " , " + device);
            }
        } else {
            Log.d(TAG, "Turn On bluetooth to get paired devices");
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION_PERMISSION);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start scanning
                startScan();
            } else {
                Log.d(TAG, "fine location permission denied");
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        stopScan();
    }


}

//
//public class MainActivity extends AppCompatActivity {
//    private BluetoothAdapter bluetoothAdapter;
//    private TextView deviceInfos;
//    private Button scanButton;
//    private Boolean notScanning = true;
//    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
//    //    private static final int REQUEST_CODE_PERMISSIONS = 1;
////    private static final int REQUEST_CODE_SCAN_PERMISSION = 2;
////
//    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
//    private static final int REQUEST_DISCOVER_BLUETOOTH = 2;
//    private static final int REQUEST_CODE_LOCATION_PERMISSION = 100;
//
//
//    private static final String TAG = "MainActivity";
//
//    private BluetoothLeScanner bluetoothLeScanner;
//    private boolean scanning;
//    private Handler handler = new Handler();
////    private LeDeviceListAdapter leDeviceListAdapter = new LeDeviceListAdapter();
//
//
//    // Stops scanning after 10 seconds.
//    private static final long SCAN_PERIOD = 10000;
//
//    private ScanCallback leScanCallback =
//            new ScanCallback() {
//                @Override
//                public void onScanResult(int callbackType, ScanResult result) {
//                    Log.d(TAG, "inside onScanResult");
//                    super.onScanResult(callbackType, result);
//
//                    deviceInfos.append(result.getDevice().getName());
////                    leDeviceListAdapter.notifyDataSetChanged();
//                }
//            };
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//
//        });
//
//        scanButton = findViewById(R.id.ScanButton);
//        deviceInfos = findViewById(R.id.DeviceInfos);
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_CODE_LOCATION_PERMISSION);
//        }
//
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
//
//        Intent intent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
//        startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
//
//
//        deviceInfos.setText("");
//        deviceInfos.append("Paired Devices\n\n");
//        if (bluetoothAdapter.isEnabled()) {
//            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
//
//            for (BluetoothDevice device : devices) {
//                deviceInfos.append("\n Device : " + device.getName() + " , " + device);
//            }
//        } else {
//            Log.d(TAG, "Turn On bluetooth to get paired devices");
//        }
//        deviceInfos.append("\n\nAvailable Devices\n\n");
//
//        scanButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                if (notScanning) {
//
//                    scanButton.setText("Stop Scan");
//                    int color = Color.parseColor("#FFF44336"); // Parse the color code
//                    scanButton.setBackgroundColor(color);
//
//
////                    bluetoothAdapter.startDiscovery();
//
//
////                    if (!bluetoothAdapter.isDiscovering()) {
////
////                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
////                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
////                        startActivityForResult(discoverableIntent, REQUEST_DISCOVER_BLUETOOTH);
////                        devices.clear();
////                        bluetoothAdapter.startDiscovery();
////                    }
//                    bluetoothLeScanner.startScan(leScanCallback);
////                    if (bluetoothAdapter.isEnabled()) {
////                        Log.d(TAG, "Adapter enabled");
////                    } else
////                        Log.d(TAG, "Adapter enabled");
//                    if (bluetoothAdapter.isDiscovering()) {
//                        Log.d(TAG, "isDiscovering");
//                    } else
//                        Log.d(TAG, "not discovering");
//                } else {
//                    bluetoothAdapter.cancelDiscovery();
//                    scanButton.setText("Start Scan");
//                    int color = Color.parseColor("#FF4CAF50"); // Parse the color code
//                    scanButton.setBackgroundColor(color);
//                }
//                notScanning = !notScanning;
//            }
//        });
//
//
//        // Register for broadcasts when a device is discovered.
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(receiver, filter);
//
//    }
//    private void scanLeDevice() {
//        if (!scanning) {
//            // Stops scanning after a predefined scan period.
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    scanning = false;
//                    bluetoothLeScanner.stopScan(leScanCallback);
//                }
//            }, SCAN_PERIOD);
//
//            scanning = true;
//            bluetoothLeScanner.startScan(leScanCallback);
//        } else {
//            scanning = false;
//            bluetoothLeScanner.stopScan(leScanCallback);
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        switch (requestCode) {
//            case REQUEST_ENABLE_BLUETOOTH:
//                if (resultCode == RESULT_OK) {
//                    Log.d(TAG, "Bluetooth is on");
//                } else
//                    Log.d(TAG, "Bluetooth is off");
//                break;
//        }
//    }
//    private final BroadcastReceiver receiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "inside onReceive");
//            String action = intent.getAction();
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                // Discovery has found a device. Get the BluetoothDevice
//                // object and its info from the Intent.
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//                deviceInfos.append("Name: " + deviceName + ", MAC Address: " + deviceHardwareAddress + "\n");
//            }
//        }
//    };
//
//    protected void onDestroy() {
//        super.onDestroy();
//
//        unregisterReceiver(receiver);
//
//        // optional and not recommended
////        bluetoothAdapter.disable();     // disable bluetooth when app stops
//    }
//
//}