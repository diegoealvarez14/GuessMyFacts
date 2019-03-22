package com.example.guessmyfacts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class ProfileCreation extends AppCompatActivity {

    private FirebaseAuth mAuth;
    String email;

    public GoogleSignInClient mGoogleSignInClient;
    public static String AGE_KEY = "AGE";
    public static String COLOR_KEY = "COLOR";
    public static String HOBBY_KEY = "HOBBY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        mAuth = FirebaseAuth.getInstance();
        email = GoogleSignIn.getLastSignedInAccount(this).getEmail();

        Button updateButton = findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
            }
        });



        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });


    }

    @Override
    protected void onStart() {

        super.onStart();
        //profile creation page should be displayed when the user logs in for the first time
        //add code here to navigate away to game activity directly if this is not the first time the user is logging in
    }

    private void signOut() {
        final Intent login = new Intent(this, Login.class);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(login);
                        finish();
                    }
                });
    }


    private void update(){
        final Intent game = new Intent(this, MainGame.class);
        EditText editTextAge = (EditText) findViewById(R.id.editTextAge);
        EditText editTextColor = (EditText) findViewById(R.id.editTextColor);
        EditText editTextHobby = (EditText) findViewById(R.id.editTextHobby);





        String age = editTextAge.getText().toString();
        String color = editTextColor.getText().toString();
        String hobby = editTextHobby.getText().toString();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put(AGE_KEY, age);
        user.put(COLOR_KEY, color);
        user.put(HOBBY_KEY, hobby);
        db.collection("users").document(email).update(user);

        startActivity(game);
//        finish();
    }


    private void submit(){
        final Intent game = new Intent(this, MainGame.class);
        EditText editTextAge = (EditText) findViewById(R.id.editTextAge);
        EditText editTextColor = (EditText) findViewById(R.id.editTextColor);
        EditText editTextHobby = (EditText) findViewById(R.id.editTextHobby);




        String age = editTextAge.getText().toString();
        String color = editTextColor.getText().toString();
        String hobby = editTextHobby.getText().toString();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put(AGE_KEY, age);
        user.put(COLOR_KEY, color);
        user.put(HOBBY_KEY, hobby);

        db.collection("users").document(email).set(user)
                .addOnSuccessListener(new OnSuccessListener < Void > () {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d("TAG", e.toString());
                    }
                });

//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//
//                        userProfileObject = documentReference.getId();
//                        prof.setUserProfileObject(userProfileObject);
//
//                        Log.d("ProfileCreation", "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("ProfileCreation", "Error adding document", e);
//                    }
//                });;


        startActivity(game);
//        finish();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {


            return true;
        }
        else if (id == R.id.logout) {
            signOut();
            mAuth.signOut();
        }
        else if (id == R.id.delete) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(email).delete();
        }

        return super.onOptionsItemSelected(item);
    }



}
