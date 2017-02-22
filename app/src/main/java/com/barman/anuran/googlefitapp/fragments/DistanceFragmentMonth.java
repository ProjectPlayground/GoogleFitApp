package com.barman.anuran.googlefitapp.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.barman.anuran.googlefitapp.R;
import com.barman.anuran.googlefitapp.activities.MainActivityWithSpinner;
import com.barman.anuran.googlefitapp.adapter.CustomAdapter;
import com.barman.anuran.googlefitapp.model.StepCountModel;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.wang.avi.AVLoadingIndicatorView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.fitness.data.DataType.TYPE_DISTANCE_DELTA;
import static com.google.android.gms.fitness.data.Field.FIELD_DISTANCE;
import static java.text.DateFormat.getDateInstance;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Anuran on 2/6/2017.
 */

public class DistanceFragmentMonth extends Fragment {
    TextView todayCount;
    ListView list;
    List<StepCountModel> data=new ArrayList<>();
    CustomAdapter customAdapter;
    AVLoadingIndicatorView avLoadingIndicatorView;
    public DistanceFragmentMonth(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customAdapter=new CustomAdapter(data,getActivity(),2);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.distance_fragment_layout,container,false);
        todayCount=(TextView)view.findViewById(R.id.todayCount);
        avLoadingIndicatorView=(AVLoadingIndicatorView)view.findViewById(R.id.avi);
        list=(ListView)view.findViewById(R.id.listDistance);
        list.setAdapter(customAdapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new InsertAndVerifyDistanceDataTask().execute();
    }

    private  class InsertAndVerifyDistanceDataTask extends AsyncTask<Void, Void, Void> {
        String result="";
        long total;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avLoadingIndicatorView.show();
        }

        protected Void doInBackground(Void... params) {
            // Create a new dataset and insertion request.
            // DataSet dataSet = insertFitnessData();

            // [START insert_dataset]
            // Then, invoke the History API to insert the data and await the result, which is
            // possible here because of the {@link AsyncTask}. Always include a timeout when calling
            // await() to prevent hanging that can occur from the service being shutdown because
            // of low memory or other conditions.
            /*com.google.android.gms.common.api.Status insertStatus =
                    Fitness.HistoryApi.insertData(mClient, dataSet)
                            .await(1, TimeUnit.MINUTES);

            // Before querying the data, check to see if the insertion succeeded.
            if (!insertStatus.isSuccess()) {
                Log.i(TAG, "There was a problem inserting the dataset.");
                datainsertionFailed=true;
                return null;
            }*/

            // At this point, the data has been inserted and can be read.
            // [END insert_dataset]

            // Begin by creating the query.
            DataReadRequest readRequest = queryFitnessStepData();

            // [START read_dataset]
            // Invoke the History API to fetch the data with the query and await the result of
            // the read request.
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(MainActivityWithSpinner.mClient, readRequest).await(1, TimeUnit.MINUTES);
            // [END read_dataset]

            PendingResult<DailyTotalResult> result =
                    Fitness.HistoryApi.readDailyTotalFromLocalDevice(MainActivityWithSpinner.mClient, TYPE_DISTANCE_DELTA);
            DailyTotalResult totalResult = result.await(30, SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                total = totalSet.isEmpty()
                        ? 0
                        : (long)totalSet.getDataPoints().get(0).getValue(FIELD_DISTANCE).asFloat();
            } else {
                // handle failure
            }

            // For the sake of the sample, we'll print the data so we can see what we just added.
            // In general, logging fitness information should be avoided for privacy reasons.
            DateFormat dateFormat =  new SimpleDateFormat("HH:mm:ss");
            DateFormat dateFormatFull =  new SimpleDateFormat("dd/MM/yy");
            DateFormat dateFormatTemp=new SimpleDateFormat("E");
            data.clear();
            if(dataReadResult.getBuckets().size() > 0){
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet1 : dataSets) {
                        for(DataPoint dataPoint:dataSet1.getDataPoints()){
                            for (Field field:dataPoint.getDataType().getFields()){
                                String dayOfWeek=dateFormatTemp.format(new Date(dataPoint.getStartTime(TimeUnit.MILLISECONDS)));
                                Calendar cal=Calendar.getInstance();
                                cal.setTime(new Date(dataPoint.getStartTime(TimeUnit.MILLISECONDS)));
                                int day=cal.get(Calendar.DAY_OF_MONTH);
                                String month=getMonth(cal.get(Calendar.MONTH));
                                String fullDate=dayOfWeek+", "+day+" "+month;
                                StepCountModel stepCountModel=new StepCountModel(String.format("%.2f",Float.parseFloat(dataPoint.getValue(field)+"")/1000)+"",fullDate,dateFormat.format(new Date(dataPoint.getStartTime(TimeUnit.MILLISECONDS))),dateFormat.format(new Date(dataPoint.getEndTime(TimeUnit.MILLISECONDS))));
                                data.add(stepCountModel);
                                // result+="Field: "+field.getName()+"Value: "+dataPoint.getValue(field)+"Start: "+dateFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS))+"End: "+dateFormat.format(dataPoint.getEndTime(TimeUnit.MILLISECONDS))+"\n";
                            }
                        }
                    }
                }
            }
            /*for (DataSet dataSet1:dataReadResult.getDataSets()){
                for(DataPoint dataPoint:dataSet1.getDataPoints()){
                    for (Field field:dataPoint.getDataType().getFields()){
                        result+="Field: "+field.getName()+"Value: "+dataPoint.getValue(field)+"\n";
                    }
                }
            }*/
            // result=dataReadResult.toString();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            todayCount.setText("Total Distance covered today: "+total/1000+" km");
            customAdapter.notifyDataSetChanged();
            avLoadingIndicatorView.hide();
            // Toast.makeText(MainActivity.this,datainsertionFailed+"",Toast.LENGTH_LONG).show();
            //Toast.makeText(MainActivity.this,result,Toast.LENGTH_LONG).show();
        }
    }

    public static DataReadRequest queryFitnessStepData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        /*Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();*/

        DateTimeZone timeZone = DateTimeZone.forID("Asia/Kolkata");
        DateTime today = new DateTime(timeZone).withTime(23,59,59,500);
        DateTime startDay = today.minusMonths(1).withTimeAtStartOfDay();
        long endTime = today.getMillis();
        long startTime=startDay.getMillis();
        DateFormat dateFormat = getDateInstance();

       /* DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_DISTANCE_DELTA)
                .setType(DataSource.TYPE_RAW)
                .setStreamName("estimated_distance")
                .setAppPackageName("com.google.android.gms")
                .build();*/
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA,DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }

    public void runAsync(){
        new InsertAndVerifyDistanceDataTask().execute();
    }

    public String getMonth(int i){
        String month="";
        switch (i){
            case 0:
                month="Jan";
                break;
            case 1:
                month="Feb";
                break;
            case 2:
                month="Mar";
                break;
            case 3:
                month="Apr";
                break;
            case 4:
                month="May";
                break;
            case 5:
                month="June";
                break;
            case 6:
                month="July";
                break;
            case 7:
                month="Aug";
                break;
            case 8:
                month="Sept";
                break;
            case 9:
                month="Oct";
                break;
            case 10:
                month="Nov";
                break;
            case 11:
                month="Dec";
                break;
        }
        return month;
    }
}
