package com.barman.anuran.googlefitapp.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.barman.anuran.googlefitapp.fragments.CaloriFragment;
import com.barman.anuran.googlefitapp.fragments.CaloriFragmentMonth;
import com.barman.anuran.googlefitapp.fragments.DistanceFragment;
import com.barman.anuran.googlefitapp.R;
import com.barman.anuran.googlefitapp.fragments.DistanceFragmentMonth;
import com.barman.anuran.googlefitapp.fragments.StepFragmentMonth;
import com.barman.anuran.googlefitapp.utils.SharedPrefManager;
import com.barman.anuran.googlefitapp.fragments.StepFragment;
import com.barman.anuran.googlefitapp.fragments.TodayFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.OnDataPointListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anuran on 2/10/2017.
 */

public class MainActivityWithSpinner extends AppCompatActivity {
    public static Spinner spinner;
    List<String> fragments=new ArrayList<>();
    LinearLayout container;
    public static final String TAG = "BasicSensorsApi";
    public static GoogleApiClient mClient = null;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    private OnDataPointListener mListener;
    ToggleButton toggleButton,dayMonthToggle;
    TextView toggleText;
    boolean stepRec,disRec,stepRecStop,disRecStop;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_with_spinner);
        spinner=(Spinner)findViewById(R.id.spinner);
        dayMonthToggle=(ToggleButton)findViewById(R.id.day_month_toggle);
        toggleText=(TextView)findViewById(R.id.toggleText);
        toggleButton=(ToggleButton)findViewById(R.id.toggleButton1);
        if(new SharedPrefManager(MainActivityWithSpinner.this).getTracking()){
         //   toggleText.setText("Fitness Tracking: ON");
            toggleButton.setChecked(true);
        }else{
         //   toggleText.setText("Fitness Tracking: OFF");
            toggleButton.setChecked(false);
        }
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    startRecording();
                }else{
                    stopRecording();
                }
            }
        });
        container=(LinearLayout)findViewById(R.id.container);
        fragments.add("Today's Fitness Reading");
        fragments.add("Steps");
        fragments.add("Calories");
        fragments.add("Distance");

        fragmentManager=getSupportFragmentManager();
        fragmentTransaction=fragmentManager.beginTransaction();


        if (!checkPermissions()) {
            requestPermissions();
        }else{
            buildFitnessClient();
        }

        dayMonthToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    switch (spinner.getSelectedItemPosition()){
                        case 1:
                            fragmentTransaction=fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container,new StepFragment());
                            fragmentTransaction.commit();
                            break;
                        case 2:
                            fragmentTransaction=fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container,new CaloriFragment());
                            fragmentTransaction.commit();
                            break;
                        case 3:
                            fragmentTransaction=fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container,new DistanceFragment());
                            fragmentTransaction.commit();
                            break;
                    }
                }else{
                    switch (spinner.getSelectedItemPosition()){
                        case 1:
                            fragmentTransaction=fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container,new StepFragmentMonth());
                            fragmentTransaction.commit();
                            break;
                        case 2:
                            fragmentTransaction=fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container,new CaloriFragmentMonth());
                            fragmentTransaction.commit();
                            break;
                        case 3:
                            fragmentTransaction=fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container,new DistanceFragmentMonth());
                            fragmentTransaction.commit();
                            break;
                    }
                }
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        dayMonthToggle.setVisibility(View.GONE);
                        fragmentTransaction=fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container,new TodayFragment());
                        fragmentTransaction.commit();
                        break;
                    case 1:
                        dayMonthToggle.setVisibility(View.VISIBLE);
                        dayMonthToggle.setChecked(true);
                        fragmentTransaction=fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container,new StepFragment());
                        fragmentTransaction.commit();
                        break;
                    case 2:
                        dayMonthToggle.setVisibility(View.VISIBLE);
                        dayMonthToggle.setChecked(true);
                        fragmentTransaction=fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container,new CaloriFragment());
                        fragmentTransaction.commit();
                        break;
                    case 3:
                        dayMonthToggle.setVisibility(View.VISIBLE);
                        dayMonthToggle.setChecked(true);
                        fragmentTransaction=fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container,new DistanceFragment());
                        fragmentTransaction.commit();
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    private void buildFitnessClient() {
        if (mClient == null && checkPermissions()) {
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
                                    ArrayAdapter adapter=new ArrayAdapter(MainActivityWithSpinner.this,android.R.layout.simple_spinner_item,fragments);
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
                           Toast.makeText(MainActivityWithSpinner.this,"Exception while connecting to Google Play services: " +
                                   result.getErrorMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .build();
            mClient.connect();
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BODY_SENSORS);
        return permissionState == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

        } else {
            Log.i(TAG, "Requesting permission");


        }
        ActivityCompat.requestPermissions(MainActivityWithSpinner.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.BODY_SENSORS},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                buildFitnessClient();
            } else {


            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient.stopAutoManage(this);
        mClient.disconnect();
    }

    public void startRecording(){

        Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                stepRec=true;
                            } else {
                                stepRec=true;
                                Toast.makeText(MainActivityWithSpinner.this,"Recording started",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivityWithSpinner.this,"Recording faced some issues.Try again.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_DISTANCE_CUMULATIVE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                disRec=true;
                                new SharedPrefManager(MainActivityWithSpinner.this).setTracking(true);
                            } else {
                                new SharedPrefManager(MainActivityWithSpinner.this).setTracking(true);
                                disRec=true;
                                Toast.makeText(MainActivityWithSpinner.this,"Recording started for Distance",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivityWithSpinner.this,"Distance  Recording faced some issues.Try again.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_CALORIES_EXPENDED)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                disRec=true;
                                new SharedPrefManager(MainActivityWithSpinner.this).setTracking(true);
                            } else {
                                new SharedPrefManager(MainActivityWithSpinner.this).setTracking(true);
                                disRec=true;
                                Toast.makeText(MainActivityWithSpinner.this,"Recording started for Calories",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivityWithSpinner.this,"Distance  Recording faced some issues.Try again.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void stopRecording(){
        Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            stepRecStop=true;
                            new SharedPrefManager(MainActivityWithSpinner.this).setTracking(false);
                            Toast.makeText(MainActivityWithSpinner.this,"Fitness recording successfully stopped.",Toast.LENGTH_SHORT).show();
                        } else {
                            // Subscription not removed
                            Toast.makeText(MainActivityWithSpinner.this,"Something went wrong while stopping the recording service.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_DISTANCE_CUMULATIVE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            disRecStop=true;
                            new SharedPrefManager(MainActivityWithSpinner.this).setTracking(false);
                            Toast.makeText(MainActivityWithSpinner.this,"Fitness recording successfully stopped.",Toast.LENGTH_SHORT).show();
                        } else {
                            // Subscription not removed
                            Toast.makeText(MainActivityWithSpinner.this,"Something went wrong while stopping the recording service.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_CALORIES_EXPENDED)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            disRecStop=true;
                            new SharedPrefManager(MainActivityWithSpinner.this).setTracking(false);
                            Toast.makeText(MainActivityWithSpinner.this,"Fitness recording successfully stopped.",Toast.LENGTH_SHORT).show();
                        } else {
                            // Subscription not removed
                            Toast.makeText(MainActivityWithSpinner.this,"Something went wrong while stopping the recording service.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
