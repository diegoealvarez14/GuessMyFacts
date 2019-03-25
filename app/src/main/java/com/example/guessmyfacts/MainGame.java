package com.example.guessmyfacts;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class MainGame extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String email = GoogleSignIn.getLastSignedInAccount(this).getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final TextView tAge = (TextView)findViewById(R.id.gameAge);
        final TextView tHobby = (TextView)findViewById(R.id.gameHobby);
        final TextView tColor = (TextView)findViewById(R.id.gameColor);

        DocumentReference docRef = db.collection("users").document(email);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> a = document.getData();
                        String age = a.get("AGE").toString();
                        Log.d("MainGame", "Age: " + age);
                        String color = a.get("COLOR").toString();
                        Log.d("MainGame", "Color: " + color);
                        String hobby = a.get("HOBBY").toString();
                        Log.d("MainGame", "Hobby: " + hobby);
                        tAge.setText(age);
                        tColor.setText(color);
                        tHobby.setText(hobby);
                    } else {
                        Log.d("MainGame", "No such document");
                    }
                } else {
                    Log.d("MainGame", "get failed with ", task.getException());
                }
            }
        });





    }

}
