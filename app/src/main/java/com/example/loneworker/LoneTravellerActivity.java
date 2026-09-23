package com.example.loneworker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import org.json.JSONObject;

public class LoneTravellerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lone_traveller);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextInputEditText travelMinutesInput = findViewById(R.id.travelMinutesInput);
        MaterialButton completeButton = findViewById(R.id.completeButton);

        travelMinutesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                if ("0".equals(input) || "00".equals(input) || "000".equals(input)) {
                    s.clear();
                    Toast.makeText(LoneTravellerActivity.this, "Minutes must be greater than 0", Toast.LENGTH_SHORT).show();
                }
            }
        });

        completeButton.setOnClickListener(v -> {
            String minutesStr = travelMinutesInput.getText() != null ? travelMinutesInput.getText().toString().trim() : "";
            if (minutesStr.isEmpty()) {
                Toast.makeText(LoneTravellerActivity.this, "Please enter minutes", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int minutes = Integer.parseInt(minutesStr);
                if (minutes <= 0) {
                    Toast.makeText(LoneTravellerActivity.this, "Minutes must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveToJson(String.valueOf(minutes));
            } catch (NumberFormatException e) {
                Toast.makeText(LoneTravellerActivity.this, "Please enter a valid number of minutes", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(LoneTravellerActivity.this, SummaryActivity.class);
            startActivity(intent);
        });
    }

    private void saveToJson(String minutes) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("minutes", minutes);
            String jsonString = jsonObject.toString();

            File file = new File(getFilesDir(), "travel_info.json");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}