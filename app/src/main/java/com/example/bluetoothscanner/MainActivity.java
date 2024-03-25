package com.example.bluetoothscanner;

import android.content.DialogInterface;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.IntentFilter;

import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import android.os.Looper;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_DISCOVER_BLUETOOTH = 2;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 100;

    private Handler mHandler;

    private int permissionIndex = 0;
    private static final int REQUEST_CODE = 1;
    private String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private TextView deviceInfos;
    private Button scanButton;
    private ListView listView;
    private boolean isScanning = false;
    ArrayAdapter<String> adapter;


    private ArrayList<BluetoothDevice> devices = new ArrayList<>();

    private Map<String, DeviceInfo> uniqueDevices = new HashMap<>();
    private Map<Long, Map<String, DeviceInfo>> scans = new HashMap<>();


//    private Runnable mRunnable = new Runnable() {
//        @Override
//        public void run() {
//            startScan();
////            bluetoothAdapter.startLeScan(mLeScanCallback);
//            mHandler.postDelayed(this, 6000);
//        }
//    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Inside onLeScan");
//                            deviceInfos.append(device.getName() + " " + device.getAddress() + "\n");
                            String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
//                            deviceInfos.append( timestamp + " -- " +
//                                     rssi + " -- " +
//                                     device.getAddress() + "\n\n");
//                            uniqueDevices.put(device.getAddress(), new DeviceInfo(rssi, device.getAddress()), timestamp);
                            adapter.add(String.valueOf(rssi)  + " " +  device.getAddress());
                            adapter.notifyDataSetChanged();
                        }
//                            mLeDeviceListAdapter.addDevice(device);
//                            mLeDeviceListAdapter.notifyDataSetChanged();
//                        }
                    });
                }
            };


    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestNextPermission();

//        deviceInfos = findViewById(R.id.DeviceInfos);
        scanButton = findViewById(R.id.ScanButton);
        listView = findViewById(R.id.listView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.e(TAG, "Bluetooth not supported on this device");
            finish();
            return;
        }
        if (!isLocationEnabled()) {
            // Request to enable Location Services if it's disabled
            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(enableLocationIntent);
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Request to enable Bluetooth if it's disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

//        mHandler = new Handler(Looper.getMainLooper());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listView.setAdapter(adapter);

//        displayPairedDevices();
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning) {
//                    mHandler.post(mRunnable); // Start the Runnable
                    startScan();
                } else {
                    stopScan();
//                    mHandler.removeCallbacks(mRunnable); // Stop the Runnable
                }
                isScanning = !isScanning;
            }
        });
    }




    private void startScan() {
        Log.d(TAG, "Inside start scan");
        devices.clear();

        scanButton.setText("Stop Scan");
        int color = Color.parseColor("#FFF44336"); // Parse the color code
        scanButton.setBackgroundColor(color);
        bluetoothAdapter.startLeScan(mLeScanCallback);

        long timestamp = System.currentTimeMillis();
        uniqueDevices = new HashMap<>();
        scans.put(timestamp, uniqueDevices);

//        mHandler.post(mRunnable); // Start the Runnable
    }

    private void stopScan() {
        Log.d(TAG, "Inside stop scan");

        scanButton.setText("Start Scan");
        int color = Color.parseColor("#FF4CAF50"); // Parse the color code
        scanButton.setBackgroundColor(color);

//        mHandler.removeCallbacks(mRunnable); // Stop the Runnable
        bluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }




    private void updateDeviceInfo(final String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceInfos.append("\n" + info);
            }
        });
    }

//    private void displayPairedDevices() {
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
//    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION_PERMISSION);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, request next permission
                permissionIndex++;
                requestNextPermission();
            } else {
                // Permission was denied. You can handle it here.
                new AlertDialog.Builder(this)
                        .setMessage("This permission is important for the app.")
                        .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();

            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
//        stopScan();
    }

    private void requestNextPermission() {
        if (permissionIndex < permissions.length) {
            if (ContextCompat.checkSelfPermission(this, permissions[permissionIndex]) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request for permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[permissionIndex])) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    // You can show a dialog or a toast here.
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this, new String[]{permissions[permissionIndex]}, REQUEST_CODE);
//                    requestNextPermission();
                }

                } else {
                permissionIndex++;
                requestNextPermission();
            }
        }
//        while (ContextCompat.checkSelfPermission(this, permissions[permissionIndex]) != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted, request for permission
//            ActivityCompat.requestPermissions(this, new String[]{permissions[permissionIndex]}, REQUEST_CODE);
//        }
//        // Permission is granted, move to next permission
//        permissionIndex++;
//        if (permissionIndex < permissions.length) {
//            requestNextPermission();
//        }
    }

}