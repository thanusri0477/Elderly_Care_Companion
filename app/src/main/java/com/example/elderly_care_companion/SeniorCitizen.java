package com.example.elderly_care_companion;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SeniorCitizen extends AppCompatActivity {
    private EditText bp, sugarLevel, temperature, pulseRate, doseLevel;
    private TextView caregiverId, medicationTime;
    private Spinner medicationSpinner, caregiverSpinner;
    private Button submitButton, pickTimeButton, viewMedicationsButton;
    private DatabaseHelper dbHelper;
    private String userEmail; // Changed from userId to userEmail for clarity
    private String selectedTime = "";
    private String selectedCaregiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senior_citizen);

        dbHelper = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        if (userEmail == null) {
            Log.e("SeniorCitizen", "USER_EMAIL is null");
            Toast.makeText(this, "Error: User email not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bp = findViewById(R.id.bp);
        sugarLevel = findViewById(R.id.sugarLevel);
        temperature = findViewById(R.id.temperature);
        pulseRate = findViewById(R.id.pulseRate);
        doseLevel = findViewById(R.id.doseLevel);
        medicationTime = findViewById(R.id.medicationTime);
        pickTimeButton = findViewById(R.id.pickTimeButton);
        submitButton = findViewById(R.id.submitHealthRecord);
        viewMedicationsButton = findViewById(R.id.viewMedicationsButton);
        medicationSpinner = findViewById(R.id.medicationSpinner);
        caregiverSpinner = findViewById(R.id.caregiverSpinner);

        String[] medications = {
                "Aspirin", "Paracetamol", "Metformin", "Insulin", "Atorvastatin",
                "Amlodipine", "Furosemide", "Omeprazole", "Lansoprazole", "Simvastatin"
        };
        ArrayAdapter<String> medicationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, medications);
        medicationSpinner.setAdapter(medicationAdapter);

        List<String> caregivers = dbHelper.getCaregivers();
        if (caregivers.isEmpty()) {
            caregivers.add("No caregivers available");
        }
        ArrayAdapter<String> caregiverAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, caregivers);
        caregiverSpinner.setAdapter(caregiverAdapter);

        caregiverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCaregiverName = parent.getItemAtPosition(position).toString();
                Log.d("SeniorCitizen", "Selected caregiver: " + selectedCaregiverName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCaregiverName = null;
                Log.d("SeniorCitizen", "No caregiver selected");
            }
        });

        pickTimeButton.setOnClickListener(v -> showTimePicker());
        submitButton.setOnClickListener(v -> saveHealthRecord());
        viewMedicationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(SeniorCitizen.this, SeniorCitizenViewMedicationsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            selectedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
            medicationTime.setText(selectedTime);
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void saveHealthRecord() {
        String bpValue = bp.getText().toString().trim();
        String sugar = sugarLevel.getText().toString().trim();
        String temp = temperature.getText().toString().trim();
        String pulse = pulseRate.getText().toString().trim();
        String dose = doseLevel.getText().toString().trim();
        String medication = medicationSpinner.getSelectedItem().toString();
        String time = medicationTime.getText().toString().trim();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String caregiverEmail = selectedCaregiverName; // Now an email, e.g., alice@gmail.com

        Log.d("SeniorCitizen", "Saving health record - User: " + userEmail + ", Caregiver: " + caregiverEmail + ", Date: " + date + ", Time: " + time);

        if (bpValue.isEmpty() || sugar.isEmpty() || temp.isEmpty() || pulse.isEmpty() || caregiverEmail == null || caregiverEmail.isEmpty() || caregiverEmail.equals("No caregivers available")) {
            Log.e("SeniorCitizen", "Validation failed - Empty fields or no caregiver selected");
            Toast.makeText(this, "Please fill all health fields and select a caregiver", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assign the caregiver to the senior in the users table
        dbHelper.assignCaregiverToSenior(userEmail, caregiverEmail);

        HealthRecord record = new HealthRecord(userEmail, bpValue, sugar, temp, pulse, medication, dose, time, "Pending", date, caregiverEmail);
        boolean success = dbHelper.insertHealthRecord(record);
        if (success) {
            Log.d("SeniorCitizen", "Health record submitted successfully for user: " + userEmail);
            Toast.makeText(this, "Health Record Submitted", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("SeniorCitizen", "Failed to submit health record for user: " + userEmail + ", caregiver: " + caregiverEmail);
            Toast.makeText(this, "Failed to submit health record", Toast.LENGTH_SHORT).show();
        }
    }
}

