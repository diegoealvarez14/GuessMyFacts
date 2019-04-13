package com.example.guessmyfacts;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        email = GoogleSignIn.getLastSignedInAccount(this).getEmail();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();


        Button startGame = (findViewById(R.id.startGame));
        final Intent profileCreation = new Intent(this, MainGame.class);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                while(!checkNetworkConnection()) {
                    Toast t = Toast.makeText(getApplicationContext(), "Connection lost!",Toast.LENGTH_LONG);
                    t.show();
                }

                startActivity(profileCreation);
                finish();
            }
        });

        Button statsPage = (findViewById(R.id.stats));
        final Intent stats = new Intent(this, Stats.class);
        statsPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(stats);
                finish();
            }
        });
    }

    public boolean checkNetworkConnection(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

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
        //Do nothing
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
