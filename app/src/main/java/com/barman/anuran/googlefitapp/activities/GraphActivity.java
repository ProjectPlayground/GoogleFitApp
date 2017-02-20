package com.barman.anuran.googlefitapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.barman.anuran.googlefitapp.R;
import com.barman.anuran.googlefitapp.graphFragment.CaloriesGraph;
import com.barman.anuran.googlefitapp.graphFragment.DistanceGraph;
import com.barman.anuran.googlefitapp.graphFragment.StepGraph;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

import java.util.ArrayList;
import java.util.List;

import static java.text.DateFormat.getDateInstance;

/**
 * Created by Anuran on 2/15/2017.
 */

public class GraphActivity extends AppCompatActivity {
    Spinner spinner;
    List<String> fragments=new ArrayList<>();
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    public static GoogleApiClient mClient;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_layout);
        spinner=(Spinner)findViewById(R.id.spinnerForGraph);
        mClient=null;
        buildFitnessClient();
        fragments.add("Steps");
        fragments.add("Calories");
        fragments.add("Distance");
        /*ArrayAdapter adapter=new ArrayAdapter(GraphActivity.this,android.R.layout.simple_spinner_item,fragments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);*/
        fragmentManager=getSupportFragmentManager();
        fragmentTransaction=fragmentManager.beginTransaction();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        fragmentTransaction=fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container,new StepGraph());
                        fragmentTransaction.commit();
                        break;
                    case 1:
                        fragmentTransaction=fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container,new CaloriesGraph());
                        fragmentTransaction.commit();
                        break;
                    case 2:
                        fragmentTransaction=fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container,new DistanceGraph());
                        fragmentTransaction.commit();
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public void buildFitnessClient() {
        if (mClient == null) {
            mClient = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.SESSIONS_API)
                    .addApi(Fitness.RECORDING_API)
                    .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                    .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    ArrayAdapter adapter=new ArrayAdapter(GraphActivity.this,android.R.layout.simple_spinner_item,fragments);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spinner.setAdapter(adapter);
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        // Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        // Log.i(TAG,
                                        //    "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }
                    )
                    .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Snackbar.make(
                                    GraphActivity.this.findViewById(R.id.main_activity_view),
                                    "Exception while connecting to Google Play services: " +
                                            result.getErrorMessage(),
                                    Snackbar.LENGTH_INDEFINITE).show();
                        }
                    })
                    .build();
            mClient.connect();
        }
    }







}
