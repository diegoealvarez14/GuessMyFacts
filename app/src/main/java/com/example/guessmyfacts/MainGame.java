package com.example.guessmyfacts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

public class MainGame extends AppCompatActivity {

    private static final String GUESSED_USER_KEY = "user";
    private static boolean noUsers = false;

    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String email;
    User guessCandidate;
    static Queue<User> candidates = new LinkedList<User>();
    static HashSet<String> usedCandidates = new HashSet<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        email = GoogleSignIn.getLastSignedInAccount(this).getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        final TextView tAge = findViewById(R.id.gameAge);
//        final TextView tHobby = findViewById(R.id.gameHobby);
//        final TextView tColor = findViewById(R.id.gameColor);

        DocumentReference docRef = db.collection("users").document(email);

//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Map<String, Object> a = document.getData();
//                        String age = a.get("AGE").toString();
//                        Log.d("MainGame", "Age: " + age);
//                        String color = a.get("COLOR").toString();
//                        Log.d("MainGame", "Color: " + color);
//                        String hobby = a.get("HOBBY").toString();
//                        Log.d("MainGame", "Hobby: " + hobby);
//                        tAge.setText(age);
//                        tColor.setText(color);
//                        tHobby.setText(hobby);
//                    } else {
//                        Log.d("MainGame", "No such document");
//                    }
//                } else {
//                    Log.d("MainGame", "get failed with ", task.getException());
//                }
//            }
//        });
        refillCandidates();
    }

    public void refillCandidates() {
        // TODO IF Candidates is Empty, Create List of 10? Unused Candidates (I Think this works now)
        if(MainGame.candidates.isEmpty()) {

            // Need Synchronization otherwise can get weird race conditions between the two queries
            synchronized (db) {
                db.collection("users").document(email).collection("guesses").get().addOnCompleteListener(
                        new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for(QueryDocumentSnapshot document : task.getResult()) {
                                    if(!usedCandidates.contains(document.get("user"))) {
                                        usedCandidates.add(document.get("user").toString());
                                    }
                                }

                                System.out.println(usedCandidates.toString()); //TODO REMOVE for Prod
                            }
                        }
                );

                //veda's changes
                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                MainGame.candidates = new LinkedList<User>();
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d("doc :", document.getId() + " => " + document.getData());
                                        Map<String, Object> map = document.getData();
                                        User u = new User(document.getId(),Integer.parseInt(map.get("AGE").toString()),
                                                map.get("COLOR").toString(),
                                                map.get("HOBBY").toString(),
                                                map.get("PROFILE-PIC").toString());
                                        if(!u.email.equals(email) && !usedCandidates.contains(u.email)) {
                                            MainGame.candidates.add(u);
                                            if(MainGame.candidates.size() == 10) {
                                                break;
                                            }
                                        }
                                    }
                                    if(MainGame.candidates.isEmpty()) {
                                        // Empty after searching all users means there are none left
                                        MainGame.noUsers = true;
                                    }
                                    nextUser();
                                } else {
                                    Log.d("error", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
            }
    }

    public void seeResults(View view){

        usedCandidates.add(guessCandidate.email);
        HashMap<String, Object> tempUser = new HashMap<String, Object>();
        tempUser.put("user", guessCandidate.email);
        //Update Guesses Collection
        db.collection("users").document(email).collection("guesses")
                .document(guessCandidate.email).set(tempUser);
        //Update Stats
        int age = Integer.parseInt(((TextView)findViewById(R.id.editText)).getText().toString());
        // TODO: Check Case, Uniform to LowerCase
        String color = ((TextView)findViewById(R.id.editText2)).getText().toString();
        // TODO: Check Case, Uniform to LowerCase
        String hobby = ((TextView)findViewById(R.id.editText3)).getText().toString();
        HashMap<String, Object> stats = new HashMap<String, Object>();
        stats.put("age", age);
        stats.put("color", color);
        stats.put("hobby", hobby);
        db.collection("users").document(guessCandidate.email)
                .collection("stats").document().set(stats);

        findViewById(R.id.editText).setVisibility(View.INVISIBLE);
        findViewById(R.id.editText2).setVisibility(View.INVISIBLE);
        findViewById(R.id.editText3).setVisibility(View.INVISIBLE);
        findViewById(R.id.resultsButton).setVisibility(View.INVISIBLE);

        findViewById(R.id.result1).setVisibility(View.VISIBLE);
        findViewById(R.id.result1).setBackgroundColor(guessCandidate.age == age ? Color.GREEN : Color.RED);

        findViewById(R.id.result2).setVisibility(View.VISIBLE);
        findViewById(R.id.result2).setBackgroundColor(guessCandidate.color.equals(color) ? Color.GREEN : Color.RED);

        findViewById(R.id.result3).setVisibility(View.VISIBLE);
        findViewById(R.id.result3).setBackgroundColor(guessCandidate.hobby.equals(hobby) ? Color.GREEN : Color.RED);

        findViewById(R.id.nextButton).setVisibility(View.VISIBLE);
    }

    public void nextUser() {
        if(MainGame.candidates.isEmpty() && !noUsers) {
            refillCandidates();
        }

        // Returns Null if Empty, ie when noUsers = true
        guessCandidate = candidates.poll();

        if(!MainGame.noUsers && guessCandidate != null) {
            ImageView image = findViewById(R.id.imageView);
            byte[] imageBytes = Base64.decode(guessCandidate.profile_pic, Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            image.setImageBitmap(decodedImage);
            ((TextView)findViewById(R.id.result1)).setText(Integer.toString(guessCandidate.age));
            ((TextView)findViewById(R.id.result2)).setText(guessCandidate.color);
            ((TextView)findViewById(R.id.result3)).setText(guessCandidate.hobby);
            findViewById(R.id.editText).setVisibility(View.VISIBLE);
            ((EditText)findViewById(R.id.editText)).getText().clear();
            findViewById(R.id.editText2).setVisibility(View.VISIBLE);
            ((EditText)findViewById(R.id.editText2)).getText().clear();
            findViewById(R.id.editText3).setVisibility(View.VISIBLE);
            ((EditText)findViewById(R.id.editText3)).getText().clear();
            findViewById(R.id.resultsButton).setVisibility(View.VISIBLE);

            findViewById(R.id.result1).setVisibility(View.INVISIBLE);
            findViewById(R.id.result2).setVisibility(View.INVISIBLE);
            findViewById(R.id.result3).setVisibility(View.INVISIBLE);
            findViewById(R.id.nextButton).setVisibility(View.INVISIBLE);
        } else {
            // Null Candidate Means No More Users in Database to Guess
        }
    }

    public void nextUser(View view) {
        nextUser();
    }

//    private void setPic() {
//        // Get the dimensions of the View
//        int targetW = imageView.getWidth();
//        int targetH = imageView.getHeight();
//
//        // Get the dimensions of the bitmap
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(currentPhotoPath, boptions);
//        int photoW = bmOptions.outWidth;
//        int photoH = bmOptions.outHeight;
//
//        // Determine how much to scale down the image
//        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
//
//        // Decode the image file into a Bitmap sized to fill the View
//        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;
//
//        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, boptions);
//        imageView.setImageBitmap(bitmap);
//    }

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
            //Intent settings = new Intent(this, Settings.class);
            //startActivity(settings);
            finish();
        }
        else if (id == R.id.logout) {
            signOut();
            mAuth.signOut();
        }
//        else if (id == R.id.delete) {
//            db.collection("users").document(email).delete();
//        }

        return super.onOptionsItemSelected(item);
    }

}
