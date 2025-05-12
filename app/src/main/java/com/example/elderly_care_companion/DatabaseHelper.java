package com.example.elderly_care_companion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ElderlyCare.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_ASSIGNED_CAREGIVER = "assigned_caregiver";

    private static final String TABLE_HEALTH = "health_records";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_BP = "bp";
    private static final String COLUMN_SUGAR = "sugar_level";
    private static final String COLUMN_TEMPERATURE = "temperature";
    private static final String COLUMN_PULSE = "pulse_rate";
    private static final String COLUMN_MEDICATION = "medication";
    private static final String COLUMN_DOSE = "dose_level";
    private static final String COLUMN_MEDICATION_TIME = "medication_time";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_DATE = "record_date";
    private static final String COLUMN_CAREGIVER = "caregiver_id";

    private static final String TABLE_PRESCRIPTIONS = "prescriptions";
    private static final String COLUMN_PRESCRIPTION_SENIOR_ID = "seniorId";
    private static final String COLUMN_PRESCRIPTION_MEDICATION = "medication";
    private static final String COLUMN_PRESCRIPTION_DOSE = "dose";
    private static final String COLUMN_PRESCRIPTION_TIME = "time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_ROLE + " TEXT, " +
                COLUMN_ASSIGNED_CAREGIVER + " TEXT)";

        String createHealthTable = "CREATE TABLE " + TABLE_HEALTH + " (" +
                COLUMN_USER_ID + " TEXT, " + // Changed to TEXT
                COLUMN_BP + " TEXT, " +
                COLUMN_SUGAR + " TEXT, " +
                COLUMN_TEMPERATURE + " TEXT, " +
                COLUMN_PULSE + " TEXT, " +
                COLUMN_MEDICATION + " TEXT, " +
                COLUMN_DOSE + " TEXT, " +
                COLUMN_MEDICATION_TIME + " TEXT, " +
                COLUMN_STATUS + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_CAREGIVER + " TEXT)"; // Changed to TEXT, removed foreign key for simplicity

        String createPrescriptionsTable = "CREATE TABLE " + TABLE_PRESCRIPTIONS + " (" +
                COLUMN_PRESCRIPTION_SENIOR_ID + " TEXT, " +
                COLUMN_PRESCRIPTION_MEDICATION + " TEXT, " +
                COLUMN_PRESCRIPTION_DOSE + " TEXT, " +
                COLUMN_PRESCRIPTION_TIME + " TEXT)";

        db.execSQL(createUsersTable);
        db.execSQL(createHealthTable);
        db.execSQL(createPrescriptionsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HEALTH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESCRIPTIONS);
        onCreate(db);
    }

    public boolean registerUser(String name, String email, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        values.putNull(COLUMN_ASSIGNED_CAREGIVER);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public List<String> getCaregivers() {
        List<String> caregivers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_EMAIL + " FROM " + TABLE_USERS + " WHERE " + COLUMN_ROLE + "='Caregiver'", null);

        if (cursor.moveToFirst()) {
            do {
                caregivers.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.d("DatabaseHelper", "Fetched caregivers: " + caregivers.toString());
        return caregivers;
    }

    public String getUserRole(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ROLE}, COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(0);
            cursor.close();
            db.close();
            return role;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }

    public boolean insertHealthRecord(HealthRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, record.getUserId());
        values.put(COLUMN_BP, record.getBp());
        values.put(COLUMN_SUGAR, record.getSugarLevel());
        values.put(COLUMN_TEMPERATURE, record.getTemperature());
        values.put(COLUMN_PULSE, record.getPulseRate());
        values.put(COLUMN_MEDICATION, record.getMedications());
        values.put(COLUMN_DOSE, record.getDoseLevels());
        values.put(COLUMN_MEDICATION_TIME, record.getMedicationTime());
        values.put(COLUMN_STATUS, record.getStatus());
        values.put(COLUMN_DATE, record.getDate());
        values.put(COLUMN_CAREGIVER, record.getCaregiverId());

        Log.d("DatabaseHelper", "Inserting health record - Senior: " + record.getUserId() + ", Caregiver: " + record.getCaregiverId() + ", Date: " + record.getDate());
        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_HEALTH, null, values);
            Log.d("DatabaseHelper", "Health record insert result: " + result + " for senior: " + record.getUserId());
        } catch (SQLException e) {
            Log.e("DatabaseHelper", "Error inserting health record: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }
        return result != -1;
    }

    public String getCaregiverIdForSenior(String userEmail) { // Renamed for clarity
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ASSIGNED_CAREGIVER + " FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + "=?", new String[]{userEmail});

        if (cursor != null && cursor.moveToFirst()) {
            String caregiverEmail = cursor.getString(0);
            cursor.close();
            db.close();
            return caregiverEmail != null ? caregiverEmail : "N/A";
        }
        if (cursor != null) cursor.close();
        db.close();
        return "N/A";
    }

    public List<String> getAssignedSeniors(String caregiverEmail) {
        List<String> seniors = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COLUMN_USER_ID + " FROM " + TABLE_HEALTH +
                " WHERE " + COLUMN_CAREGIVER + "=?", new String[]{caregiverEmail});

        if (cursor.moveToFirst()) {
            do {
                seniors.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.d("DatabaseHelper", "Fetched assigned seniors for caregiver " + caregiverEmail + ": " + seniors.toString());
        return seniors;
    }

    public List<HealthRecord> getHealthRecordsForSenior(String seniorEmail) {
        List<HealthRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HEALTH + " WHERE " + COLUMN_USER_ID + "=?", new String[]{seniorEmail});

        if (cursor.moveToFirst()) {
            do {
                HealthRecord record = new HealthRecord(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BP)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUGAR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEMPERATURE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PULSE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEDICATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEDICATION_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAREGIVER))
                );
                records.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return records;
    }

    public List<String> getAssignedMedications(String seniorEmail) {
        List<String> medications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_PRESCRIPTION_MEDICATION + ", " + COLUMN_PRESCRIPTION_DOSE + ", " + COLUMN_PRESCRIPTION_TIME +
                " FROM " + TABLE_PRESCRIPTIONS + " WHERE " + COLUMN_PRESCRIPTION_SENIOR_ID + "=?", new String[]{seniorEmail});
        if (cursor.moveToFirst()) {
            do {
                String med = cursor.getString(0);
                String dose = cursor.getString(1);
                String time = cursor.getString(2);
                medications.add("Medication: " + med + ", Dose: " + dose + ", Time: " + time);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return medications;
    }

    public boolean insertPrescription(String seniorEmail, String medication, String dose, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRESCRIPTION_SENIOR_ID, seniorEmail);
        values.put(COLUMN_PRESCRIPTION_MEDICATION, medication);
        values.put(COLUMN_PRESCRIPTION_DOSE, dose);
        values.put(COLUMN_PRESCRIPTION_TIME, time);

        long result = db.insert(TABLE_PRESCRIPTIONS, null, values);
        db.close();
        return result != -1;
    }

    public void assignCaregiverToSenior(String seniorEmail, String caregiverEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ASSIGNED_CAREGIVER, caregiverEmail);
        db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{seniorEmail});
        db.close();
    }

    public String getCaregiverEmailByName(String caregiverName) { // Changed to return email
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_EMAIL + " FROM " + TABLE_USERS + " WHERE " + COLUMN_NAME + "=? AND " + COLUMN_ROLE + "='Caregiver'", new String[]{caregiverName});

        if (cursor != null && cursor.moveToFirst()) {
            String caregiverEmail = cursor.getString(0);
            cursor.close();
            db.close();
            return caregiverEmail;
        }
        if (cursor != null) cursor.close();
        db.close();
        return "N/A";
    }
}

