package com.example.guessmyfacts;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

    private HashMap<Integer, Integer> ageMap = new HashMap<Integer, Integer>();
    private HashMap<String, Integer> colorMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> hobbyMap = new HashMap<String, Integer>();


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

        synchronized (db) {
            db.collection("users").document(email).collection("stats").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for(QueryDocumentSnapshot data : task.getResult()) {
                                int age = Integer.parseInt(data.getData().get("age").toString());
                                String color = data.getData().get("color").toString();
                                String hobby = data.getData().get("hobby").toString();
                                if(ageMap.containsKey(age)) {
                                    ageMap.put(age, ageMap.get(age) + 1);
                                } else {
                                    ageMap.put(age, 1);
                                }
                                if(colorMap.containsKey(color)) {
                                    colorMap.put(color, colorMap.get(color) + 1);
                                } else {
                                    colorMap.put(color, 1);
                                }
                                if(hobbyMap.containsKey(hobby)) {
                                    hobbyMap.put(hobby, hobbyMap.get(hobby) + 1);
                                } else {
                                    hobbyMap.put(hobby, 1);
                                }
                            }
                            PieChart ageChart = findViewById(R.id.AgeChart);
                            ageChart.animateXY(800, 800);
                            ageChart.setEntryLabelTextSize(36);
                            ageChart.getDescription().setEnabled(false);

                            ArrayList<Integer> ages = new ArrayList<Integer>(ageMap.keySet());
                            ArrayList<PieEntry> ageCounts = new ArrayList<PieEntry>();
                            int i = 0;
                            for(Integer count : ageMap.values()) {
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

                            ArrayList<String> colors = new ArrayList<String>(colorMap.keySet());
                            ArrayList<PieEntry> colorCounts = new ArrayList<PieEntry>();
                            i = 0;
                            for(Integer count : colorMap.values()) {
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

                            ArrayList<String> hobbies = new ArrayList<String>(hobbyMap.keySet());
                            ArrayList<PieEntry> hobbyCounts = new ArrayList<PieEntry>();
                            i = 0;
                            for(Integer count : hobbyMap.values()) {
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



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
