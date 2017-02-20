package com.barman.anuran.googlefitapp.fragments;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.wang.avi.AVLoadingIndicatorView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;

import static com.google.android.gms.fitness.data.DataType.TYPE_ACTIVITY_SAMPLES;
import static com.google.android.gms.fitness.data.DataType.TYPE_ACTIVITY_SEGMENT;
import static com.google.android.gms.fitness.data.DataType.TYPE_DISTANCE_DELTA;
import static com.google.android.gms.fitness.data.DataType.getAggregatesForInput;
import static com.google.android.gms.fitness.data.Field.FIELD_ACTIVITY;
import static com.google.android.gms.fitness.data.Field.FIELD_DISTANCE;
import static com.google.android.gms.fitness.data.Field.FIELD_DURATION;
import static com.google.android.gms.fitness.data.Field.FIELD_NUM_SEGMENTS;
import static java.text.DateFormat.getDateInstance;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Anuran on 2/6/2017.
 */

public class ActivityFragment extends Fragment {
   /* TextView todayCount;
    ListView list;
    List<StepCountModel> dataAct=new ArrayList<>();
    CustomAdapter customAdapter;
    AVLoadingIndicatorView avLoadingIndicatorView;
    File file;
    String path="/sdcard/myFile.txt";
    public ActivityFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customAdapter=new CustomAdapter(dataAct,getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.activity_fragment_layout,container,false);
        todayCount=(TextView)view.findViewById(R.id.todayCount);
        list=(ListView)view.findViewById(R.id.listActivity);
        list.setAdapter(customAdapter);
        avLoadingIndicatorView=(AVLoadingIndicatorView)view.findViewById(R.id.avi);
        int readPermission=ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission=ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(readPermission==PackageManager.PERMISSION_GRANTED && writePermission==PackageManager.PERMISSION_GRANTED){
            file=new File(path);
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            new InsertAndVerifyActivityDataTask().execute();
        }else{
            ActivityCompat.requestPermissions(getActivity(),new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},100);

        }
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100){
            if (grantResults.length>0){
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                    file=new File(path);
                    if(!file.exists()){
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    new InsertAndVerifyActivityDataTask().execute();
                }else{
                    Toast.makeText(getActivity(),"You must give permission to access your storage",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    return;
                }
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private  class InsertAndVerifyActivityDataTask extends AsyncTask<Void, Void, Void> {
        String result="";
        long total=0;
        int bucketSize;
        DataSet totalSet;
        int sessionSize;
        String content="";
        FileWriter fw=null; BufferedWriter bw;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avLoadingIndicatorView.show();
            try {
                fw = new FileWriter(file.getAbsoluteFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
            bw = new BufferedWriter(fw);
        }

        protected Void doInBackground(Void... params) {
            SessionReadRequest sessionReadRequest=readRequest();
            SessionReadResult sessionReadResult=Fitness.SessionsApi.readSession(MainActivity.mClient, sessionReadRequest)
                    .await(1, TimeUnit.MINUTES);
            if (sessionReadResult.getSessions().size()>0){
                sessionSize=sessionReadResult.getSessions().size();

            }


            DataReadRequest readRequest = queryFitnessStepData();
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(MainActivity.mClient, readRequest).await(1, TimeUnit.MINUTES);


            PendingResult<DailyTotalResult> result =
                    Fitness.HistoryApi.readDailyTotalFromLocalDevice(MainActivity.mClient, TYPE_ACTIVITY_SEGMENT);
            DailyTotalResult totalResult = result.await(30, SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                totalSet = totalResult.getTotal();
                if(!totalSet.isEmpty()){
                    for (DataPoint dataPoint:totalSet.getDataPoints()){
                       // total += Long.parseLong(dataPoint.getValue(FIELD_DURATION) + "");
                        //for (Field field:dataPoint.getDataType().getFields()){

                            total += (Integer.parseInt(dataPoint.getValue(FIELD_DURATION) + "") / 6000) % 60;

                        //}
                    }
                }

            } else {
                // handle failure
            }

            DateFormat dateFormat =  new SimpleDateFormat("dd/MM/yy  HH:mm:ss");
            dataAct.clear();
            if(dataReadResult.getBuckets().size() > 0){

                for (Bucket bucket : dataReadResult.getBuckets()) {
                   // if(bucket.getActivity().equals(FitnessActivities.WALKING_FITNESS)){
                        int tempDuration=0;
                        List<DataSet> dataSets = bucket.getDataSets();
                        for (DataSet dataSet1 : dataSets) {
                            for(DataPoint dataPoint:dataSet1.getDataPoints()) {
                                //for (Field field:dataPoint.getDataType().getFields()){
                                //long activeTime = bucket.getEndTime(TimeUnit.MINUTES) - bucket.getStartTime(TimeUnit.MINUTES);
                                content+="End Time: "+dateFormat.format(new Date(dataPoint.getEndTime(TimeUnit.MILLISECONDS)))+"\t Start Time: "+dateFormat.format(new Date(dataPoint.getEndTime(TimeUnit.MILLISECONDS)))+"\n";

                                tempDuration += (((dataPoint.getEndTime(TimeUnit.MILLISECONDS)-dataPoint.getStartTime(TimeUnit.MILLISECONDS)) / 6000) % 60);

                                // }
                                //tempDuration+=dataPoint.getEndTime(TimeUnit.MINUTES)-dataPoint.getStartTime(TimeUnit.MINUTES);
                                content+="\n=========Datapoint Ended=============\n";
                                try {
                                    bw.write(content);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            content+="\n=========DataSet Ended=============\n";
                            try {
                                bw.write(content);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //long activeTime = bucket.getEndTime(TimeUnit.MINUTES) - bucket.getStartTime(TimeUnit.MINUTES);
                        StepCountModel stepCountModel=new StepCountModel(tempDuration+" min "+bucket.getActivity(),dateFormat.format(new Date(bucket.getStartTime(TimeUnit.MILLISECONDS))),dateFormat.format(new Date(bucket.getEndTime(TimeUnit.MILLISECONDS))));
                        dataAct.add(stepCountModel);
                        content+="\n=========Bucket Ended=============\n";
                        try {
                            bw.write(content);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    //}
                }
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bucketSize=dataReadResult.getBuckets().size();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
           // total=(total/60000) % 60;
            Log.d("BUCKET",total+"");
            todayCount.setText("Total Activity Time today: "+total+" min");
            customAdapter.notifyDataSetChanged();
            avLoadingIndicatorView.hide();
            Toast.makeText(getActivity(),"Session size: "+sessionSize,Toast.LENGTH_SHORT).show();
            try {
                bw.write(content);
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Toast.makeText(MainActivity.this,datainsertionFailed+"",Toast.LENGTH_LONG).show();
            //Toast.makeText(getActivity(),bucketSize+"",Toast.LENGTH_LONG).show();
            //Toast.makeText(getActivity(),totalSet.getDataPoints().size()+"",Toast.LENGTH_SHORT).show();
        }
    }

    public static DataReadRequest queryFitnessStepData() {

        DateTimeZone timeZone = DateTimeZone.forID("Asia/Kolkata");
        DateTime today = new DateTime(timeZone).withTime(23,59,59,999);
        DateTime startDay = today.minusWeeks(1).withTimeAtStartOfDay();
        long endTime = today.getMillis();
        long startTime=startDay.getMillis();
        java.text.DateFormat dateFormat = getDateInstance();
        DataSource ACTIVITY_SEGMENT = new DataSource.Builder()
                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_activity_segment")
                .setAppPackageName("com.google.android.gms")
                .build();
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT,DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        return readRequest;
    }

    public static SessionReadRequest readRequest(){
        DateTimeZone timeZone = DateTimeZone.forID("Asia/Kolkata");
        DateTime today = new DateTime(timeZone).withTime(23,59,59,999);
        DateTime startDay = today.minusWeeks(1).withTimeAtStartOfDay();
        long endTime = today.getMillis();
        long startTime=startDay.getMillis();
        java.text.DateFormat dateFormat = getDateInstance();
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_ACTIVITY_SEGMENT)
                .setSessionName("sample-session-name")
                .build();

        return  readRequest;
    }



*/
}
