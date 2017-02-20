package com.barman.anuran.googlefitapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.barman.anuran.googlefitapp.activities.MainAct;
import com.barman.anuran.googlefitapp.R;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.concurrent.TimeUnit;

/**
 * Created by Anuran on 2/3/2017.
 */

public class StepFragMini extends Fragment {
    TextView stepTv;
    public StepFragMini() {
    }

    public static StepFragMini getInstance(){
        StepFragMini stepFragMini=new StepFragMini();
        return stepFragMini;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.step_frag_mini,container,false);
        stepTv=(TextView)view.findViewById(R.id.stepCountTV);


        if(MainAct.mApiClient.isConnected()){
            connectForFitnessData();
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    public  void connectForFitnessData(){
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes( DataType.TYPE_STEP_COUNT_CUMULATIVE )
                .setDataSourceTypes( DataSource.TYPE_DERIVED )
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                    if( DataType.TYPE_STEP_COUNT_CUMULATIVE.equals( dataSource.getDataType() ) ) {
                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(MainAct.mApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {

        SensorRequest request = new SensorRequest.Builder()
                .setDataSource( dataSource )
                .setDataType( dataType )
                .setSamplingRate( 2, TimeUnit.SECONDS )
                .build();

        Fitness.SensorsApi.add(MainAct.mApiClient, request, new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for( final Field field : dataPoint.getDataType().getFields() ) {
                    final Value value = dataPoint.getValue( field );
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stepTv.setText(value+"");
                            //Toast.makeText(getApplicationContext(), "Field: " + field.getName() + " Value: " + value, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        })
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.e( "GoogleFit", "SensorApi successfully added" );
                        }
                    }
                });
    }

}
