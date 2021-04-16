package com.mm.mychatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity: ";
    public static final String MY_PREFS_NAME = "prefs_chat";
    private EditText etUsername;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        etUsername = findViewById(R.id.etUsername);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (null != currentUser) {
            Log.d(TAG, "already signed in :success");
            openHomeActivity();
        }
    }

    public void loginPressed(View view) {
        if (etUsername.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter username...", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInAnonymously:success");
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    saveUserToFirebase(user);
                }
                openHomeActivity();
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInAnonymously:failure", task.getException());
                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToFirebase(FirebaseUser user) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("username", etUsername.getText().toString());
        editor.putString("uid", user.getUid());
        editor.apply();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        Map<String, String> users = new HashMap<>();
        users.put("username", etUsername.getText().toString());
        users.put("uid", user.getUid());
        myRef.child(user.getUid()).setValue(users);
    }

    private void openHomeActivity() {
        startActivity(new Intent(LoginActivity.this, ChatRoomActivity.class));
    }
}