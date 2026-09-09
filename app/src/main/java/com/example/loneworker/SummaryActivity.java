package com.example.loneworker;

import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileInputStream;
import org.json.JSONObject;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SummaryActivity extends AppCompatActivity {
    private String savedMinutes = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView summaryValue = findViewById(R.id.summaryValue);
        Button submitButton = findViewById(R.id.submitButton);

        try {
            File file = new File(getFilesDir(), "travel_info.json");
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String json = new String(data, "UTF-8");
            JSONObject obj = new JSONObject(json);
            savedMinutes = obj.getString("minutes");
            summaryValue.setText(savedMinutes + " Minutes");
        } catch (Exception e) {
            e.printStackTrace();
            summaryValue.setText("Error loading data");
        }

        submitButton.setOnClickListener(v -> showConfirmationDialog());
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Lone Worker Submission");
        
        int batteryLevel = 19; // getBatteryPercentage();
        String message = "Check in interval: " + savedMinutes + " minutes\n" +
                         "Current Battery: " + batteryLevel + "%";
        builder.setMessage(message);
        
        builder.setPositiveButton("CONFIRM", (dialog, which) -> {
            if (batteryLevel < 20) {
                showBatteryWarning(batteryLevel);
            } else {
                proceedToStatus();
            }
        });

        builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int getBatteryPercentage() {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    private void showBatteryWarning(int level) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Low Battery Warning");
        builder.setMessage("Your battery is at " + level + "%. Please charge your device to ensure it remains active until the end of your session.");
        builder.setPositiveButton("OK", (dialog, which) -> proceedToStatus());
        builder.show();
    }

    private void proceedToStatus() {
        Toast.makeText(SummaryActivity.this, "Submission Confirmed", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SummaryActivity.this, StatusActivity.class);
        startActivity(intent);
        finish();
    }
}