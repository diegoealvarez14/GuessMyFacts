package com.example.guessmyfacts;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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
import java.util.HashMap;

public class Stats extends AppCompatActivity {


    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String email;

    private HashMap<User.Question, HashMap<String, Integer>> statsMap =
            new HashMap<User.Question, HashMap<String, Integer>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        email = GoogleSignIn.getLastSignedInAccount(this).getEmail();

        for(User.Question question : User.Question.values()) {
            statsMap.put(question, new HashMap<String, Integer>());
        }

        synchronized (db) {
            db.collection("users").document(email).collection("stats").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for(QueryDocumentSnapshot data : task.getResult()) {
                                for(User.Question question : User.Question.values()) {
                                    if(data.getData().containsKey(question.getStatsKey())) {
                                        String response = data.getData().get(question.getStatsKey()).toString();
                                        HashMap<String, Integer> temp = statsMap.get(question);
                                        if(temp.containsKey(response)) {
                                            temp.put(response, temp.get(response) + 1);
                                        } else {
                                            temp.put(response, 1);
                                        }
                                    } else {
                                        // Hasn't answered question yet
                                    }
                                }
                            }
                            PieChart ageChart = findViewById(R.id.AgeChart);
                            ageChart.animateXY(800, 800);
                            ageChart.setEntryLabelTextSize(36);
                            ageChart.getDescription().setEnabled(false);

                            ArrayList<String> ages = new ArrayList<String>(statsMap.get(User.Question.AGE).keySet());
                            ArrayList<PieEntry> ageCounts = new ArrayList<PieEntry>();
                            int i = 0;
                            for(Integer count : statsMap.get(User.Question.AGE).values()) {
                                ageCounts.add(new PieEntry(count, ages.get(i).toString()));
                                i++;
                            }

                            PieDataSet ageDataSet = new PieDataSet(ageCounts, "");
                            ageDataSet.setDrawValues(false);
                            ageDataSet.setSliceSpace(4);

                            ArrayList<Integer> pieChartColors = new ArrayList<>();
                            pieChartColors.add(Color.rgb(187, 0, 0));
                            pieChartColors.add(Color.GRAY);
                            pieChartColors.add(Color.LTGRAY);
                            pieChartColors.add(Color.DKGRAY);
                            ageDataSet.setColors(pieChartColors);

                            Legend ageLegend = ageChart.getLegend();
                            ageLegend.setForm(Legend.LegendForm.CIRCLE);
                            ageLegend.setTextSize(24);
                            ageLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                            ageLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);

                            PieData ageData = new PieData(ageDataSet);
                            ageChart.setData(ageData);
                            ageChart.invalidate();

                            // Color
                            PieChart colorChart = findViewById(R.id.ColorChart);
                            colorChart.animateXY(800, 800);
                            colorChart.setEntryLabelTextSize(36);
                            colorChart.getDescription().setEnabled(false);

                            ArrayList<String> colors = new ArrayList<String>(statsMap.get(User.Question.COLOR).keySet());
                            ArrayList<PieEntry> colorCounts = new ArrayList<PieEntry>();
                            i = 0;
                            for(Integer count : statsMap.get(User.Question.COLOR).values()) {
                                colorCounts.add(new PieEntry(count, colors.get(i).toString()));
                                i++;
                            }

                            PieDataSet colorDataSet = new PieDataSet(colorCounts, "");
                            colorDataSet.setDrawValues(false);
                            colorDataSet.setSliceSpace(4);

                            colorDataSet.setColors(pieChartColors);

                            Legend colorLegend = colorChart.getLegend();
                            colorLegend.setForm(Legend.LegendForm.CIRCLE);
                            colorLegend.setTextSize(24);
                            colorLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                            colorLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);

                            PieData colorData = new PieData(colorDataSet);
                            colorChart.setData(colorData);
                            colorChart.invalidate();

                            //Hobby
                            // Color
                            PieChart hobbyChart = findViewById(R.id.HobbyChart);
                            hobbyChart.animateXY(800, 800);
                            //hobbyChart.setEntryLabelTextSize(36);
                            hobbyChart.getDescription().setEnabled(false);

                            ArrayList<String> hobbies = new ArrayList<String>(statsMap.get(User.Question.HOBBY).keySet());
                            ArrayList<PieEntry> hobbyCounts = new ArrayList<PieEntry>();
                            i = 0;
                            for(Integer count : statsMap.get(User.Question.HOBBY).values()) {
                                hobbyCounts.add(new PieEntry(count, hobbies.get(i).toString()));
                                i++;
                            }

                            PieDataSet hobbyDataSet = new PieDataSet(hobbyCounts, "");
                            hobbyDataSet.setDrawValues(false);
                            hobbyDataSet.setSliceSpace(4);

                            hobbyDataSet.setColors(pieChartColors);

                            Legend hobbyLegend = hobbyChart.getLegend();
                            hobbyLegend.setForm(Legend.LegendForm.CIRCLE);
                            //hobbyLegend.setTextSize(14);
                            hobbyLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                            hobbyLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);

                            PieData hobbyData = new PieData(hobbyDataSet);
                            hobbyChart.setData(hobbyData);
                            hobbyChart.invalidate();
                        }
                    });
        }

        //TODO Move small value into other category



        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }



    @Override
    public void onBackPressed() {
        final Intent homeScreen = new Intent(this, HomeScreen.class);
        startActivity(homeScreen);
        finish();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.logout).setEnabled(false);
        menu.findItem(R.id.logout).setVisible(false);
        menu.findItem(R.id.updateProfile).setEnabled(false);
        menu.findItem(R.id.updateProfile).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.mainMenu) {
            Intent homeScreen = new Intent(this, HomeScreen.class);
            startActivity(homeScreen);
            finish();
        }

//        if (id == R.id.logout) {
//            signOut();
//        }else if (id == R.id.updateProfile) {
//            Intent updateProfile = new Intent(this, ProfileCreation.class);
//            startActivity(updateProfile);
//            finish();
//        }

        return super.onOptionsItemSelected(item);
    }

}
