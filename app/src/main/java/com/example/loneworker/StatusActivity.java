package com.example.loneworker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.json.JSONObject;

public class StatusActivity extends AppCompatActivity {
    private TextView nextCheckInText;
    private TextView lastCheckInText;
    private TextView drainStatusText;
    private int intervalMinutes = 0;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy hh:mm:ss a", Locale.US);
    
    private final Set<Integer> alertedThresholds = new HashSet<>();
    private LocationManager locationManager;
    private GnssStatus.Callback gnssCallback;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (int) ((level / (float) scale) * 100);
            
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == BatteryManager.BATTERY_STATUS_FULL;
            
            checkBatteryThresholds(batteryPct, isCharging);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nextCheckInText = findViewById(R.id.nextCheckInText);
        lastCheckInText = findViewById(R.id.lastCheckInText);
        drainStatusText = findViewById(R.id.drainStatusText);
        LinearLayout checkInAction = findViewById(R.id.checkInAction);
        LinearLayout checkOutAction = findViewById(R.id.checkOutAction);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        setupGnssMonitoring();

        loadInterval();
        updateTimes();

        checkInAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTimes();
            }
        });

        checkOutAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StatusActivity.this, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupGnssMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gnssCallback = new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                    int satelliteCount = status.getSatelliteCount();
                    int strongSignals = 0;
                    for (int i = 0; i < satelliteCount; i++) {
                        if (status.getCn0DbHz(i) >= 30.0) { // 30 dB-Hz is generally considered a decent signal
                            strongSignals++;
                        }
                    }

                    // If less than 4 satellites have a strong signal, GPS radio works harder
                    if (satelliteCount > 0 && strongSignals < 3) {
                        updateDrainStatus(true);
                    } else {
                        updateDrainStatus(false);
                    }
                }
            };
        }
    }

    private void updateDrainStatus(boolean isHighDrain) {
        runOnUiThread(() -> {
            if (isHighDrain) {
                drainStatusText.setText("Battery Drain: HIGH (Weak GPS Signal)");
                drainStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                drainStatusText.setText("Battery Drain: Normal");
                drainStatusText.setTextColor(getResources().getColor(R.color.teal_700));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && gnssCallback != null) {
                locationManager.registerGnssStatusCallback(gnssCallback, null);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(batteryReceiver);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && gnssCallback != null) {
            locationManager.unregisterGnssStatusCallback(gnssCallback);
        }
    }

    private void checkBatteryThresholds(int batteryPct, boolean isCharging) {
        int[] thresholds = {20, 15, 10, 5};
        for (int threshold : thresholds) {
            if (batteryPct <= threshold && !alertedThresholds.contains(threshold)) {
                if (!isCharging) {
                    alertedThresholds.add(threshold);
                    showBatteryAlert(batteryPct, isCharging);
                }
                break; 
            }
        }
    }

    private void showBatteryAlert(int currentLevel, boolean isCharging) {
        String chargingStatus = isCharging ? " (Charging)" : " (NOT Charging)";
        new AlertDialog.Builder(this)
            .setTitle("Critical Battery Alert")
            .setMessage("Your battery has dropped to " + currentLevel + "%" + chargingStatus + ". Please connect to a charger immediately.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void loadInterval() {
        try {
            File file = new File(getFilesDir(), "travel_info.json");
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
                String json = new String(data, "UTF-8");
                JSONObject obj = new JSONObject(json);
                intervalMinutes = Integer.parseInt(obj.getString("minutes"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTimes() {
        Date now = new Date();
        String lastTime = "Last check-in Time: " + sdf.format(now);
        lastCheckInText.setText(lastTime);

        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.MINUTE, intervalMinutes);
        String nextTime = "Next check-in Time: " + sdf.format(cal.getTime());
        nextCheckInText.setText(nextTime);
    }
}