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
import com.google.android.gms.fitness.data.DataSource;
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

import static com.google.android.gms.fitness.data.DataType.TYPE_CALORIES_EXPENDED;
import static com.google.android.gms.fitness.data.Field.FIELD_CALORIES;
import static java.text.DateFormat.getDateInstance;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Anuran on 1/30/2017.
 */

public class CaloriFragmentMonth extends Fragment {
    ListView list;
    List<StepCountModel> datax=new ArrayList<>();
    CustomAdapter customAdapter;
    TextView todayCount;
    AVLoadingIndicatorView avLoadingIndicatorView;
    public CaloriFragmentMonth() {
    }

    public CaloriFragmentMonth getCaloriInstance(){
        CaloriFragmentMonth caloriFragment=new CaloriFragmentMonth();
        return caloriFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customAdapter=new CustomAdapter(datax,getActivity(),1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.calories_fragment_layout,container,false);
        todayCount=(TextView)view.findViewById(R.id.todayCount);
        avLoadingIndicatorView=(AVLoadingIndicatorView)view.findViewById(R.id.avi);
        list=(ListView)view.findViewById(R.id.list);
        list.setAdapter(customAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
            new InsertAndVerifyDataTask().execute();

    }

    public  class InsertAndVerifyDataTask extends AsyncTask<Void, Void, Void> {
        String result="";
        long total;
        boolean datainsertionFailed=false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avLoadingIndicatorView.show();
        }

        protected Void doInBackground(Void... params) {

            DataReadRequest readRequest = queryFitnessData();

            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(MainActivityWithSpinner.mClient, readRequest).await(1, TimeUnit.MINUTES);
            PendingResult<DailyTotalResult> result =
                    Fitness.HistoryApi.readDailyTotalFromLocalDevice(MainActivityWithSpinner.mClient, TYPE_CALORIES_EXPENDED);
            DailyTotalResult totalResult = result.await(30, SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                total = totalSet.isEmpty()
                        ? 0
                        : (long)totalSet.getDataPoints().get(0).getValue(FIELD_CALORIES).asFloat();
            } else {
                // handle failure
            }
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            DateFormat dateFormatFull = new SimpleDateFormat("dd/MM/yy");
            DateFormat dateFormatTemp=new SimpleDateFormat("E");
            datax.clear();
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
                                StepCountModel stepCountModel=new StepCountModel(String.format("%.2f",Float.parseFloat(dataPoint.getValue(field)+""))+"",fullDate,dateFormat.format(new Date(dataPoint.getStartTime(TimeUnit.MILLISECONDS))),dateFormat.format(new Date(dataPoint.getEndTime(TimeUnit.MILLISECONDS))));
                                datax.add(stepCountModel);
                                //result+="Field: "+field.getName()+"Value: "+dataPoint.getValue(field)+"Start: "+dateFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS))+"End: "+dateFormat.format(dataPoint.getEndTime(TimeUnit.MILLISECONDS))+"\n";
                            }
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            todayCount.setText("Total Calories expended today: "+total+" cal");
            customAdapter.notifyDataSetChanged();
            avLoadingIndicatorView.hide();
        }
    }


    /**
     * Return a {@link DataReadRequest} for all step count changes in the past week.
     */
    public static DataReadRequest queryFitnessData() {
        DateTimeZone timeZone = DateTimeZone.forID("Asia/Kolkata");
        DateTime today = new DateTime(timeZone).withTime(23,59,59,900);
        DateTime startDay = today.minusMonths(1).withTimeAtStartOfDay();
        long endTime = today.getMillis();
        long startTime=startDay.getMillis();
        DateFormat dateFormat = getDateInstance();

        DataSource ESTIMATED_CALORIES_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setType(DataSource.TYPE_RAW)
                .setStreamName("estimated_calories")
                .build();
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED,DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }

    public void runAsync(){
        new InsertAndVerifyDataTask().execute();
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
