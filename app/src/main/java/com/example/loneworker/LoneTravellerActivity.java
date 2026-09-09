package com.example.loneworker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import org.json.JSONObject;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String minutes = travelMinutesInput.getText().toString();
                if (minutes.isEmpty()) {
                    Toast.makeText(LoneTravellerActivity.this, "Please enter minutes", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveToJson(minutes);

                Intent intent = new Intent(LoneTravellerActivity.this, SummaryActivity.class);
                startActivity(intent);
            }
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