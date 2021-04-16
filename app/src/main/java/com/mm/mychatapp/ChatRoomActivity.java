package com.mm.mychatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    private EditText etEnterMessage;
    private ScrollView scrollViewMessages;
    private LinearLayout linearMessages;
    private String userId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        etEnterMessage = findViewById(R.id.etEnterMessage);
        scrollViewMessages = findViewById(R.id.scrollViewMessages);
        linearMessages = findViewById(R.id.linearMessages);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.MY_PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getString("uid", "");
        username = prefs.getString("username", "N/A");

        getMessages();
    }

    private void getMessages() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("messages");
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                if (map != null) {
                    String message = map.get("message").toString();
                    String timestamp = map.get("timestamp").toString();
                    String userName = map.get("username").toString();
                    if (userName.equals(username)) {
                        addMessageBox("You:\n" + message, 1, timestamp);
                    } else {
                        addMessageBox(userName + ":\n" + message, 2, timestamp);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ChatRoomActivity.this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendMessagePressed(View view) {
        if (etEnterMessage.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter message...", Toast.LENGTH_SHORT).show();
            return;
        }
        saveMessageToFirebase();
    }

    private void saveMessageToFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("messages");
        Map<String, Object> message = new HashMap<>();
        message.put("message", etEnterMessage.getText().toString());
        message.put("uid", userId);
        message.put("timestamp", ServerValue.TIMESTAMP);
        message.put("username", username);
        myRef.push().setValue(message);
        etEnterMessage.setText("");
    }

    public void addMessageBox(String message, int type, String timestamp) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(getResources().getColor(R.color.black));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 10, 50, 10);
        textView.setLayoutParams(lp);
        textView.setBackgroundResource(type == 1 ? R.drawable.row_self_message : R.drawable.row_other_message);
        linearMessages.addView(textView);
        scrollViewMessages.fullScroll(View.FOCUS_DOWN);
    }

}