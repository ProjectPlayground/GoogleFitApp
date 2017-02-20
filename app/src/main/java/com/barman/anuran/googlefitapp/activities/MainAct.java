package com.barman.anuran.googlefitapp.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.barman.anuran.googlefitapp.R;
import com.barman.anuran.googlefitapp.fragments.CalFragMini;
import com.barman.anuran.googlefitapp.fragments.StepFragMini;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;

/**
 * Created by teehalf on 27-Jan-17.
 */

public class MainAct extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    public static GoogleApiClient mApiClient;
    Button start,stop;
    Chronometer chronometer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        start=(Button)findViewById(R.id.btnStart);
        stop=(Button)findViewById(R.id.btnStop);
        chronometer=(Chronometer)findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
                chronometer.start();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                chronometer.stop();
            }
        });

        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        int permission2 = ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BODY_SENSORS);
        if (permission == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED) {
            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.SENSORS_API)
                    .addApi(Fitness.RECORDING_API)
                    .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                    .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mApiClient.connect();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.BODY_SENSORS}, 100);
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100){
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mApiClient = new GoogleApiClient.Builder(this)
                            .addApi(Fitness.SENSORS_API)
                            .addApi(Fitness.RECORDING_API)
                            .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                            .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                            .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
                    mApiClient.connect();
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
         StepFragMini.getInstance().connectForFitnessData();
         CalFragMini.getInstance().connectForCaloriesData();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.stepContainer, new StepFragMini());
        fragmentTransaction.add(R.id.calContainer, new CalFragMini());
        fragmentTransaction.commit();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(MainAct.this,"API is not connected",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainAct.this,"API connection failed",Toast.LENGTH_SHORT).show();
        if (!authInProgress) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(MainAct.this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {

            }
        } else {
            Log.e("GoogleFit", "authInProgress");
        }
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                if (!mApiClient.isConnecting() && !mApiClient.isConnected()) {
                    mApiClient.connect();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.e("GoogleFit", "RESULT_CANCELED");
            }
        } else {
            Log.e("GoogleFit", "requestCode NOT request_oauth");
        }
    }


    public void startRecording(){
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                            } else {
                                Toast.makeText(MainAct.this,"Recording started",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainAct.this,"Recording faced some issues.Try again.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_DISTANCE_CUMULATIVE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                            } else {
                                Toast.makeText(MainAct.this,"Recording started for Distance",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainAct.this,"Distance  Recording faced some issues.Try again.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void stopRecording(){
        Fitness.RecordingApi.unsubscribe(mApiClient, DataType.TYPE_ACTIVITY_SAMPLE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Toast.makeText(MainAct.this,"Fitness recording successfully stopped.",Toast.LENGTH_SHORT).show();
                        } else {
                            // Subscription not removed
                            Toast.makeText(MainAct.this,"Something went wrong while stopping the recording service.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        Fitness.RecordingApi.unsubscribe(mApiClient, DataType.TYPE_ACTIVITY_SAMPLE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Toast.makeText(MainAct.this,"Fitness recording successfully stopped.",Toast.LENGTH_SHORT).show();
                        } else {
                            // Subscription not removed
                            Toast.makeText(MainAct.this,"Something went wrong while stopping the recording service.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
