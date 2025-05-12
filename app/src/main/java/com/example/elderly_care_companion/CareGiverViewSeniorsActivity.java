package com.example.elderly_care_companion;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CareGiverViewSeniorsActivity extends AppCompatActivity {
    private static final String TAG = "CareGiverViewSeniors";
    private ListView seniorsListView;
    private EditText prescribeMedication, doseLevel, prescribeTime;
    private Button sendNotificationButton, backButton;
    private DatabaseHelper dbHelper;
    private String caregiverEmail;
    private String selectedSeniorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_giver_view_seniors);
        Log.d(TAG, "onCreate started");

        try {
            seniorsListView = findViewById(R.id.seniorsListView);
            dbHelper = new DatabaseHelper(this);
            caregiverEmail = getIntent().getStringExtra("USER_EMAIL");
            selectedSeniorEmail = getIntent().getStringExtra("SELECTED_SENIOR_EMAIL");
            Log.d(TAG, "Caregiver email: " + caregiverEmail + ", Selected senior email: " + selectedSeniorEmail);

            if (caregiverEmail == null) {
                Log.e(TAG, "USER_EMAIL is null");
                Toast.makeText(this, "Error: Caregiver email not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Add back button
            backButton = new Button(this);
            backButton.setText("Back");
            ((android.widget.LinearLayout) findViewById(R.id.rootLayout)).addView(backButton);
            backButton.setOnClickListener(v -> finish());

            if (selectedSeniorEmail != null) {
                // Show prescription UI and all health records for the selected senior
                prescribeMedication = new EditText(this);
                doseLevel = new EditText(this);
                prescribeTime = new EditText(this);
                sendNotificationButton = new Button(this);

                prescribeMedication.setHint("Prescribe Medication");
                doseLevel.setHint("Dose Level");
                prescribeTime.setHint("Prescribed Time (HH:MM)");
                sendNotificationButton.setText("Send Notification");

                ((android.widget.LinearLayout) findViewById(R.id.rootLayout)).addView(prescribeMedication);
                ((android.widget.LinearLayout) findViewById(R.id.rootLayout)).addView(doseLevel);
                ((android.widget.LinearLayout) findViewById(R.id.rootLayout)).addView(prescribeTime);
                ((android.widget.LinearLayout) findViewById(R.id.rootLayout)).addView(sendNotificationButton);

                sendNotificationButton.setOnClickListener(v -> sendNotification());
                loadHealthRecordsForSenior(selectedSeniorEmail);
            } else {
                // Show today's health records for all assigned seniors
                loadTodaysHealthRecordsForAllSeniors();
            }
            Log.d(TAG, "onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadTodaysHealthRecordsForAllSeniors() {
        try {
            List<String> seniors = dbHelper.getAssignedSeniors(caregiverEmail);
            Log.d(TAG, "Assigned seniors count: " + seniors.size());
            List<String> healthRecordItems = new ArrayList<>();
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Log.d(TAG, "Today's date: " + today);

            if (seniors.isEmpty()) {
                healthRecordItems.add("No seniors assigned to " + caregiverEmail);
            } else {
                healthRecordItems.add("Today's Health Records (" + today + "):");
                for (String seniorEmail : seniors) {
                    List<HealthRecord> records = dbHelper.getHealthRecordsForSenior(seniorEmail);
                    Log.d(TAG, "Records count for " + seniorEmail + ": " + records.size());
                    for (HealthRecord record : records) {
                        Log.d(TAG, "Record for " + seniorEmail + ": Date=" + record.getDate() + ", BP=" + record.getBp() + ", Time=" + record.getMedicationTime());
                    }
                    HealthRecord todayRecord = null;
                    for (HealthRecord record : records) {
                        if (record.getDate().equals(today)) {
                            todayRecord = record;
                            break;
                        }
                    }
                    if (todayRecord != null) {
                        String recordItem = "Senior: " + seniorEmail +
                                ", BP: " + todayRecord.getBp() +
                                ", Sugar: " + todayRecord.getSugarLevel() +
                                ", Temp: " + todayRecord.getTemperature() +
                                ", Pulse: " + todayRecord.getPulseRate() +
                                ", Med: " + todayRecord.getMedications() +
                                ", Dose: " + todayRecord.getDoseLevels() +
                                ", Time: " + todayRecord.getMedicationTime();
                        healthRecordItems.add(recordItem);
                    } else {
                        healthRecordItems.add("Senior: " + seniorEmail + " - No record for today");
                    }
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, healthRecordItems);
            seniorsListView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error loading today's health records: " + e.getMessage(), e);
            List<String> errorList = new ArrayList<>();
            errorList.add("Error loading today's health records");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, errorList);
            seniorsListView.setAdapter(adapter);
        }
    }

    private void loadHealthRecordsForSenior(String seniorEmail) {
        try {
            List<HealthRecord> records = dbHelper.getHealthRecordsForSenior(seniorEmail);
            Log.d(TAG, "Health records count for " + seniorEmail + ": " + records.size());

            if (records.isEmpty()) {
                List<String> emptyList = new ArrayList<>();
                emptyList.add("No health records available for " + seniorEmail);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, emptyList);
                seniorsListView.setAdapter(adapter);
                return;
            }

            List<String> details = new ArrayList<>();
            details.add("Health Records for " + seniorEmail + ":");
            for (HealthRecord record : records) {
                details.add("Date: " + record.getDate());
                details.add("BP: " + record.getBp());
                details.add("Sugar Level: " + record.getSugarLevel());
                details.add("Temperature: " + record.getTemperature());
                details.add("Pulse Rate: " + record.getPulseRate());
                details.add("Medication: " + record.getMedications());
                details.add("Dose: " + record.getDoseLevels());
                details.add("Medication Time: " + record.getMedicationTime());
                details.add("Status: " + record.getStatus());
                details.add(""); // Empty line for readability
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, details);
            seniorsListView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error loading health records: " + e.getMessage(), e);
            List<String> errorList = new ArrayList<>();
            errorList.add("Error loading health records");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, errorList);
            seniorsListView.setAdapter(adapter);
        }
    }

    private void sendNotification() {
        try {
            String medication = prescribeMedication.getText().toString().trim();
            String dose = doseLevel.getText().toString().trim();
            String time = prescribeTime.getText().toString().trim();

            if (selectedSeniorEmail == null || medication.isEmpty() || dose.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.insertPrescription(selectedSeniorEmail, medication, dose, time);
            if (success) {
                Toast.makeText(this, "Notification sent to " + selectedSeniorEmail + "!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to send notification.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage(), e);
            Toast.makeText(this, "Error sending notification", Toast.LENGTH_SHORT).show();
        }
    }
}

