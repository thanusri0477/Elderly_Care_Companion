package com.example.elderly_care_companion;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class SeniorCitizenViewMedicationsActivity extends AppCompatActivity {
    private ListView medicationsListView;
    private DatabaseHelper dbHelper;
    private String seniorEmail; // Changed from seniorId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senior_citizen_view_medications);

        medicationsListView = findViewById(R.id.medicationsListView);
        seniorEmail = getIntent().getStringExtra("USER_EMAIL");

        dbHelper = new DatabaseHelper(this);
        List<String> medications = dbHelper.getAssignedMedications(seniorEmail);

        if (medications.isEmpty()) {
            Toast.makeText(this, "No medications assigned!", Toast.LENGTH_SHORT).show();
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, medications);
            medicationsListView.setAdapter(adapter);
        }
    }
}
