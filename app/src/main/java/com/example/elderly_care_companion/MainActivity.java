package com.example.elderly_care_companion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText emailField, passwordField;
    private Button loginButton;
    private TextView registerLink;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        emailField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        loginButton.setOnClickListener(v -> loginUser());
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String enteredEmail = emailField.getText().toString().trim();
        String enteredPassword = passwordField.getText().toString().trim();

        if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
            showToast("Enter a valid email");
            return;
        }

        Log.d("LOGIN", "Attempting login for: " + enteredEmail);
        String role = dbHelper.getUserRole(enteredEmail, enteredPassword);

        if (role != null) {
            Log.d("LOGIN", "Retrieved role: " + role);
            showToast("Login Successful");

            Intent intent;
            role = role.toLowerCase().trim();

            if (role.equals("senior citizen")) {
                intent = new Intent(MainActivity.this, SeniorCitizen.class);
            } else if (role.equals("caregiver")) {
                intent = new Intent(MainActivity.this, CareGiver.class);
            } else {
                showToast("Invalid Role: " + role);
                return;
            }

            intent.putExtra("USER_EMAIL", enteredEmail);
            startActivity(intent);
            Log.d("LOGIN", "Navigating to: " + intent.getComponent().getClassName());

            finish();
        } else {
            Log.e("LOGIN_FAILED", "Invalid credentials for email: " + enteredEmail);
            showToast("Invalid Credentials");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
