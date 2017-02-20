package com.barman.anuran.googlefitapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.barman.anuran.googlefitapp.R;
import com.barman.anuran.googlefitapp.graphFragment.CaloriesGraph;
import com.barman.anuran.googlefitapp.graphFragment.DistanceGraph;
import com.barman.anuran.googlefitapp.graphFragment.StepGraph;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

/**
 * Created by Anuran on 2/20/2017.
 */

public class ThreeGraphActivity extends AppCompatActivity {
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    public static GoogleApiClient mClient;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.three_graph_layout);
        mClient=null;
        buildFitnessClient();
        fragmentManager=getSupportFragmentManager();
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
                                    fragmentTransaction=fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.stepGraphContainer,new StepGraph());
                                    fragmentTransaction.commit();

                                    fragmentTransaction=fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.caloriesGraphContainer,new CaloriesGraph());
                                    fragmentTransaction.commit();

                                    fragmentTransaction=fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.distanceGraphContainer,new DistanceGraph());
                                    fragmentTransaction.commit();
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
                                    ThreeGraphActivity.this.findViewById(R.id.main_activity_view),
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
