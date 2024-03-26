package com.example.bluetoothscanner;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

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
    private boolean firstTime = true;
    ArrayAdapter<String> adapter;



    private ArrayList<BluetoothDevice> devices = new ArrayList<>();

    private Map<String, DeviceInfo> uniqueDevices = new TreeMap<>();
    private Map<Long, Map<String, DeviceInfo>> scans = new TreeMap<>(Collections.reverseOrder());


    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            addToListView();
            mHandler.postDelayed(this, 6000);
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            byte[] scanRecord = result.getScanRecord().getBytes();

            runOnUiThread(() -> {
                uniqueDevices.put(device.getAddress(), new DeviceInfo(rssi, device.getAddress(), System.currentTimeMillis()));
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
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        mHandler = new Handler(Looper.getMainLooper());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);


//        displayPairedDevices();
        scanButton.setOnClickListener(v -> {
            if (!isScanning) {
                startScan();
                if (firstTime) {
//                    addToListView();
                    firstTime = false;
                }
            } else {
                stopScan();
            }
            isScanning = !isScanning;
        });
    }



    private void startScan() {

        scanButton.setText("Stop Scan");
        int color = Color.parseColor("#FFF44336"); // Parse the color code
        scanButton.setBackgroundColor(color);

        bluetoothLeScanner.startScan(mScanCallback);

        mHandler.post(mRunnable); // Start the Runnable
    }
    private void stopScan() {
        scanButton.setText("Start Scan");
        int color = Color.parseColor("#FF4CAF50"); // Parse the color code
        scanButton.setBackgroundColor(color);

        mHandler.removeCallbacks(mRunnable); // Stop the Runnable

        bluetoothLeScanner.stopScan(mScanCallback);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }




    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, request next permission
                permissionIndex++;
                requestNextPermission();
            } else {
                // Permission was denied. You can handle it here.
                new AlertDialog.Builder(this)
                        .setMessage("This permission is important for the app.")
                        .setPositiveButton("Open Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
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

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{permissions[permissionIndex]}, REQUEST_CODE);
                }

            } else {
                permissionIndex++;
                requestNextPermission();
            }
        }
    }

    private void addToListView() {
//        listView.setAdapter(null);
        adapter.clear();    // this is required, otherwise list will be duplicated

        if (uniqueDevices.size() == 0) {
            showInListView();
            return;
        }

        long timestamp = System.currentTimeMillis();
        Map<String, DeviceInfo> uniqueDevicesCopy = new TreeMap<>(uniqueDevices);

        scans.put(timestamp, uniqueDevicesCopy);
//        uniqueDevices.clear();

        showInListView();

    }

    private  void showInListView() {
        for (Map.Entry<Long, Map<String, DeviceInfo>> scan : scans.entrySet()) {
//            String item = String.valueOf(scan.getKey()) + "\n";
            long diff = System.currentTimeMillis() - scan.getKey();
            double diffInMinutesDouble = (double) diff / (1000 * 6);
            int diffInMinutes = (int) Math.floor(diffInMinutesDouble);

            String item = String.valueOf(diffInMinutes) + " min ago\n";
            for (Map.Entry<String, DeviceInfo> entry : scan.getValue().entrySet()) {
                String address = entry.getKey();
                DeviceInfo rssi = entry.getValue();

                item += rssi.getRssi() + " -- " + address + "\n";
            }
            item += "\n";
            adapter.add(item);
        }
        adapter.notifyDataSetChanged();
        uniqueDevices.clear();
        devices.clear();
    }
}