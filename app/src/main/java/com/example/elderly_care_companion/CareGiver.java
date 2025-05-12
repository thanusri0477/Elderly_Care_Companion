package com.example.elderly_care_companion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class CareGiver extends AppCompatActivity {
    private static final String TAG = "CareGiver";
    private TextView seniorId;
    private EditText prescribeMedication, doseLevel, prescribeTime;
    private Spinner seniorListSpinner;
    private Button sendNotificationButton, viewSeniorsButton;
    private ListView assignedSeniorsListView;
    private DatabaseHelper dbHelper;
    private String userEmail;
    private boolean isInitialLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_giver);
        Log.d(TAG, "onCreate started");

        try {
            dbHelper = new DatabaseHelper(this);
            userEmail = getIntent().getStringExtra("USER_EMAIL");
            Log.d(TAG, "User email: " + userEmail);

            if (userEmail == null) {
                Log.e(TAG, "USER_EMAIL is null");
                Toast.makeText(this, "Error: User email not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            seniorId = findViewById(R.id.seniorId);
            prescribeMedication = findViewById(R.id.prescribeMedication);
            doseLevel = findViewById(R.id.doseLevel);
            prescribeTime = findViewById(R.id.prescribeTime);
            seniorListSpinner = findViewById(R.id.seniorListSpinner);
            sendNotificationButton = findViewById(R.id.sendNotificationButton);
            viewSeniorsButton = findViewById(R.id.viewSeniorsButton);
            assignedSeniorsListView = findViewById(R.id.assignedSeniorsListView);
            Log.d(TAG, "UI components initialized");

            loadSeniors();

            List<String> seniors = dbHelper.getAssignedSeniors(userEmail);
            Log.d(TAG, "Assigned seniors count: " + seniors.size());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, seniors);
            assignedSeniorsListView.setAdapter(adapter);

            seniorListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (isInitialLoad) {
                        isInitialLoad = false;
                        Log.d(TAG, "Skipping initial spinner selection");
                        return;
                    }
                    String selectedSeniorEmail = parent.getItemAtPosition(position).toString();
                    Log.d(TAG, "Selected senior: " + selectedSeniorEmail);
                    Intent intent = new Intent(CareGiver.this, CareGiverViewSeniorsActivity.class);
                    intent.putExtra("USER_EMAIL", userEmail);
                    intent.putExtra("SELECTED_SENIOR_EMAIL", selectedSeniorEmail);
                    startActivity(intent);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG, "No senior selected in spinner");
                }
            });

            sendNotificationButton.setOnClickListener(v -> sendNotification());
            viewSeniorsButton.setOnClickListener(v -> {
                Log.d(TAG, "View Assigned Seniors button clicked");
                Intent intent = new Intent(CareGiver.this, CareGiverViewSeniorsActivity.class);
                intent.putExtra("USER_EMAIL", userEmail);
                // Not passing SELECTED_SENIOR_EMAIL to indicate "view all"
                startActivity(intent);
                Log.d(TAG, "Intent launched for CareGiverViewSeniorsActivity");
            });
            Log.d(TAG, "onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadSeniors() {
        try {
            List<String> seniors = new ArrayList<>(dbHelper.getAssignedSeniors(userEmail));
            Log.d(TAG, "Loading seniors, count: " + seniors.size());

            if (seniors.isEmpty()) {
                Toast.makeText(this, "No seniors assigned!", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, seniors);
            seniorListSpinner.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error loading seniors: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to load seniors", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotification() {
        try {
            String selectedSenior = seniorListSpinner.getSelectedItem() != null ? seniorListSpinner.getSelectedItem().toString() : "";
            String medication = prescribeMedication.getText().toString().trim();
            String dose = doseLevel.getText().toString().trim();
            String time = prescribeTime.getText().toString().trim();

            if (selectedSenior.isEmpty() || medication.isEmpty() || dose.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.insertPrescription(selectedSenior, medication, dose, time);
            if (success) {
                Toast.makeText(this, "Notification sent!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to send notification.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage(), e);
            Toast.makeText(this, "Error sending notification", Toast.LENGTH_SHORT).show();
        }
    }
}




