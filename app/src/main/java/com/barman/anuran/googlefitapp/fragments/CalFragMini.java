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
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Anuran on 2/3/2017.
 */

public class CalFragMini extends Fragment {
    TextView calTv;
    MainAct mainAct;

    public CalFragMini() {

    }
    public static CalFragMini getInstance(){
        CalFragMini calFragMini=new CalFragMini();
        return calFragMini;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.cal_frag_mini,container,false);
        calTv=(TextView)view.findViewById(R.id.caloriesTV);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(MainAct.mApiClient.isConnected()){
            connectForCaloriesData();
        }
    }


    private void registerFitnessDataListenerForCalories(DataSource dataSource, DataType dataType) {

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
                            calTv.setText(value+"");
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

    public void connectForCaloriesData(){
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes( DataType.TYPE_DISTANCE_CUMULATIVE )
                .setDataSourceTypes( DataSource.TYPE_RAW )
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                    if( DataType.TYPE_DISTANCE_CUMULATIVE.equals( dataSource.getDataType() ) ) {
                        registerFitnessDataListenerForCalories(dataSource, DataType.TYPE_DISTANCE_CUMULATIVE);
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(MainAct.mApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }




    public static void saveUserHeight(int heightCentimiters) {
        // to post data
        float height = ((float) heightCentimiters) / 100.0f;
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataSet heightDataSet = createDataForRequest(
                DataType.TYPE_HEIGHT,    // for height, it would be DataType.TYPE_HEIGHT
                DataSource.TYPE_RAW,
                height,                  // weight in kgs
                startTime,              // start time
                endTime,                // end time
                TimeUnit.MILLISECONDS                // Time Unit, for example, TimeUnit.MILLISECONDS
        );

        com.google.android.gms.common.api.Status heightInsertStatus =
                Fitness.HistoryApi.insertData(MainAct.mApiClient, heightDataSet)
                        .await(1, TimeUnit.MINUTES);
    }

    public static void saveUserWeight(float weight) {
        // to post data
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataSet weightDataSet = createDataForRequest(
                DataType.TYPE_WEIGHT,    // for height, it would be DataType.TYPE_HEIGHT
                DataSource.TYPE_RAW,
                weight,                  // weight in kgs
                startTime,              // start time
                endTime,                // end time
                TimeUnit.MILLISECONDS                // Time Unit, for example, TimeUnit.MILLISECONDS
        );

        com.google.android.gms.common.api.Status weightInsertStatus =
                Fitness.HistoryApi.insertData(MainAct.mApiClient, weightDataSet)
                        .await(1, TimeUnit.MINUTES);
    }

    public static DataSet createDataForRequest(DataType dataType,
                                               int dataSourceType,
                                               Object values,
                                               long startTime,
                                               long endTime,
                                               TimeUnit timeUnit) {
        DataSource dataSource = new DataSource.Builder()
                .setDataType(dataType)
                .setType(dataSourceType)
                .build();

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint().setTimeInterval(startTime, endTime, timeUnit);

        if (values instanceof Integer) {
            dataPoint = dataPoint.setIntValues((Integer) values);
        } else {
            dataPoint = dataPoint.setFloatValues((Float) values);
        }

        dataSet.add(dataPoint);

        return dataSet;
    }
}
