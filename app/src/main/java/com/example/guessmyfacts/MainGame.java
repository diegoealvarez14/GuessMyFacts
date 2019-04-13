package com.example.guessmyfacts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MainGame extends AppCompatActivity {

    private static final String GUESSED_USER_KEY = "user";
    private static boolean noUsers = false;

    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String email;
    User guessCandidate;
    static Queue<User> candidates;
    static HashSet<String> usedCandidates;

    ArrayList<User.Question> currentQuestions = new ArrayList<User.Question>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent homeScreen = new Intent(this, HomeScreen.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        email = GoogleSignIn.getLastSignedInAccount(this).getEmail();

        candidates = new LinkedList<>();
        usedCandidates = new HashSet<>();
        final Button mainMenu = findViewById(R.id.toMainMenu);
        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(homeScreen);
                finish();
            }
        });
        refillCandidates();
    }



//    @Override
//    protected void onStart() {
//        super.onStart();
//        refillCandidates();
//    }

    public void refillCandidates() {
        // TODO IF Candidates is Empty, Create List of 10? Unused Candidates (I Think this works now)
        if(MainGame.candidates.isEmpty()) {

            // Need Synchronization otherwise can get weird race conditions between the two queries

            if(!checkNetworkConnection()){
               Toast.makeText(this,"Network Connection is lost. Please connect to a network to continue!",Toast.LENGTH_SHORT).show();
               // networkLost.show();
            }

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
                if(!checkNetworkConnection()){
                    Toast.makeText(getApplicationContext(),"Network Connection is lost. Please connect to a network to continue!",Toast.LENGTH_SHORT).show();

                }
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
                                        HashMap<User.Question, String> answers = new HashMap<User.Question, String>();
                                        for(User.Question q : User.Question.values()) {
                                            if(map.containsKey(q.getUserKey())) {
                                                answers.put(q, map.get(q.getUserKey()).toString());
                                            } else {
                                                // Empty Question
                                                System.out.println("Question: " + q.getStatsKey());
                                            }
                                        }
                                        User u = new User(document.getId(), answers,
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

    public boolean checkNetworkConnection(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void seeResults(View view){
        usedCandidates.add(guessCandidate.email);
        HashMap<String, Object> tempUser = new HashMap<String, Object>();
        tempUser.put("user", guessCandidate.email);

        if(!checkNetworkConnection()){
            Toast.makeText(this,"Network Connection is lost. Please connect to a network to continue!",Toast.LENGTH_SHORT).show();

        }

  //      while(!checkNetworkConnection()){}

        //Update Guesses Collection
        db.collection("users").document(email).collection("guesses")
                .document(guessCandidate.email).set(tempUser);
        //Update Stats

        String answer1 = ((TextView)findViewById(R.id.editText1)).getText().toString();
        // TODO: Check Case, Uniform to LowerCase
        String answer2 = ((TextView)findViewById(R.id.editText2)).getText().toString();
        // TODO: Check Case, Uniform to LowerCase
        String answer3 = ((TextView)findViewById(R.id.editText3)).getText().toString();
        HashMap<String, Object> stats = new HashMap<String, Object>();//TODO
        stats.put(currentQuestions.get(0).getStatsKey(), answer1);
        stats.put(currentQuestions.get(1).getStatsKey(), answer2);
        stats.put(currentQuestions.get(2).getStatsKey(), answer3);
        db.collection("users").document(guessCandidate.email)
                .collection("stats").document().set(stats);

        findViewById(R.id.editText1).setVisibility(View.INVISIBLE);
        findViewById(R.id.editText2).setVisibility(View.INVISIBLE);
        findViewById(R.id.editText3).setVisibility(View.INVISIBLE);
        findViewById(R.id.resultsButton).setVisibility(View.INVISIBLE);

        findViewById(R.id.result1).setVisibility(View.VISIBLE);
        findViewById(R.id.result1).setBackgroundColor(guessCandidate.getAnswer(User.Question.AGE).equals(answer1) ? Color.GREEN : Color.RED);

        findViewById(R.id.result2).setVisibility(View.VISIBLE);
        findViewById(R.id.result2).setBackgroundColor(guessCandidate.getAnswer(User.Question.COLOR).equals(answer2) ? Color.GREEN : Color.RED);

        findViewById(R.id.result3).setVisibility(View.VISIBLE);
        findViewById(R.id.result3).setBackgroundColor(guessCandidate.getAnswer(User.Question.HOBBY).equals(answer3) ? Color.GREEN : Color.RED);

        findViewById(R.id.nextButton).setVisibility(View.VISIBLE);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public void nextUser() {
        if(MainGame.candidates.isEmpty() && !noUsers) {
            refillCandidates();
        }

        // Returns Null if Empty, ie when noUsers = true
        guessCandidate = candidates.poll();
        currentQuestions.clear();

        currentQuestions = new ArrayList<User.Question>(Arrays.asList(User.Question.values()));
        Collections.shuffle(currentQuestions);
        currentQuestions.subList(0, 3);

        if(!MainGame.noUsers && guessCandidate != null) {
            ImageView image = findViewById(R.id.imageView);
            byte[] imageBytes = Base64.decode(guessCandidate.profile_pic, Base64.DEFAULT);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
            //int imageHeight = options.outHeight;
            //int imageWidth = options.outWidth;
            //String imageType = options.outMimeType;

            options.inSampleSize = calculateInSampleSize(options, 100, 100);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            decodedImage =  BitmapFactory.decodeByteArray(imageBytes, 0,imageBytes.length, options);

           // Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            image.setImageBitmap(decodedImage);
            TextView actual1 = findViewById(R.id.result1);
            EditText editText1 = findViewById(R.id.editText1);
            TextView actual2 = findViewById(R.id.result2);
            EditText editText2 = findViewById(R.id.editText2);
            TextView actual3 = findViewById(R.id.result3);
            EditText editText3 = findViewById(R.id.editText3);
            actual1.setText(guessCandidate.getAnswer(currentQuestions.get(0)));
            if(currentQuestions.get(0).isNumeric()) {
                editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            editText1.setHint(currentQuestions.get(0).getPrompt());
            actual2.setText(guessCandidate.getAnswer(currentQuestions.get(1)));
            if(currentQuestions.get(1).isNumeric()) {
                editText2.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            editText2.setHint(currentQuestions.get(1).getPrompt());
            actual3.setText(guessCandidate.getAnswer(currentQuestions.get(2)));
            if(currentQuestions.get(2).isNumeric()) {
                editText3.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            editText3.setHint(currentQuestions.get(2).getPrompt());

            findViewById(R.id.editText1).setVisibility(View.VISIBLE);
            ((EditText)findViewById(R.id.editText1)).getText().clear();
            findViewById(R.id.editText2).setVisibility(View.VISIBLE);
            ((EditText)findViewById(R.id.editText2)).getText().clear();
            findViewById(R.id.editText3).setVisibility(View.VISIBLE);
            ((EditText)findViewById(R.id.editText3)).getText().clear();
            findViewById(R.id.resultsButton).setVisibility(View.VISIBLE);

            actual1.setVisibility(View.INVISIBLE);
            actual2.setVisibility(View.INVISIBLE);
            actual3.setVisibility(View.INVISIBLE);
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
                        mAuth.signOut();
                        startActivity(login);
                        finish();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        final Intent homeScreen = new Intent(this, HomeScreen.class);
        startActivity(homeScreen);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.mainMenu).setEnabled(false);
        menu.findItem(R.id.mainMenu).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.logout) {
            signOut();
        }
        else if (id == R.id.updateProfile) {
            Intent updateProfile = new Intent(this, ProfileCreation.class);
            startActivity(updateProfile);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
