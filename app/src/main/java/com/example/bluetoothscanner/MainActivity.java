package com.example.bluetoothscanner;

import android.content.Intent;

import android.widget.Toast;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
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

import androidx.core.view.WindowInsetsCompat;

import android.util.Log;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothDevice;


public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter bluetoothAdapter;
    private TextView deviceInfos;
    private Button scanButton;
    private Boolean isScanning = false;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final int REQUEST_CODE_SCAN_PERMISSION = 2;
    //    private final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "MainActivity";
//    Switch mySwitch;
//    Intent enableBtIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        scanButton = findViewById(R.id.ScanButton);
        deviceInfos = findViewById(R.id.DeviceInfos);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!isScanning) {
                    if (checkBluetoothPermissions()) {
                        scanForDevices();
                        scanButton.setText("Stop Scan");
                        int color = Color.parseColor("#FFF44336"); // Parse the color code
                        scanButton.setBackgroundColor(color);
                    }
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

    private void scanForDevices() {
        devices.clear();
        deviceInfos.setText("Scanning...");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_CODE_SCAN_PERMISSION);
            return;
        }
        bluetoothAdapter.startLeScan(this);
//        deviceInfos.setText("Scanning Finied...");
    }

    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Toast.makeText(this, "inside onLeScan", Toast.LENGTH_LONG).show();
        if (!devices.contains(device)) {
            devices.add(device);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(this, "No scan permission, inside leScan", Toast.LENGTH_LONG).show();
                return;
            }
            String info = device.getName() + " - " + device.getAddress();
            deviceInfos.append("\n" + info);
        }
        String data = "Aswin";
        deviceInfos.append(data);
        Toast.makeText(this, data, Toast.LENGTH_LONG).show();
    }

    private void stopScan() {
        deviceInfos.setText("Device informations");
//        Log.d(TAG, "hello");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(this, "No Scan permission", Toast.LENGTH_LONG).show();
            return;
        }
        bluetoothAdapter.stopLeScan(this);
//        deviceInfos.setText("Hellooo");
//        Log.d(TAG, "hello2");
    }

    protected void onDestroy() {
        super.onDestroy();
        if (isScanning) {
            stopScan();
        }
    }
    private boolean checkBluetoothPermissions() {
        int fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED && coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSIONS);
            return false;
        } else {
            // Check if Bluetooth is enabled
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Bluetooth is disabled. Please enable it in settings to scan for devices.", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;  // Bluetooth is enabled, proceed with scanning
        }

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SCAN_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start scan
                scanForDevices();
            } else {
                // Permission denied, inform the user
                Toast.makeText(this, "Scan permission is required to find Bluetooth devices.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, start scanning
                scanForDevices();
            } else {
                // Permissions denied, inform the user
                Toast.makeText(this, "Location permissions are required for Bluetooth scanning.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}