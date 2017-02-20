package com.barman.anuran.googlefitapp.fragments;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.barman.anuran.googlefitapp.R;
import com.barman.anuran.googlefitapp.activities.GraphActivity;
import com.barman.anuran.googlefitapp.activities.MainActivityWithSpinner;
import com.barman.anuran.googlefitapp.activities.ThreeGraphActivity;
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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.fitness.data.DataType.TYPE_CALORIES_EXPENDED;
import static com.google.android.gms.fitness.data.DataType.TYPE_DISTANCE_DELTA;
import static com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA;
import static com.google.android.gms.fitness.data.Field.FIELD_CALORIES;
import static com.google.android.gms.fitness.data.Field.FIELD_DISTANCE;
import static com.google.android.gms.fitness.data.Field.FIELD_STEPS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Anuran on 2/10/2017.
 */

public class TodayFragment extends Fragment {
    //CircleProgressView circleProgressView,circleProgressView2,circleProgressView3;
    TextView steps,calories,distance,datepickerText,seeGraphDetails;
    AVLoadingIndicatorView avLoadingIndicatorView;
    LinearLayout linearLayout;
    DatePicker datePicker;
    Calendar calendar;
    int dDay,dMonth,dYear,eDay,eMonth,eYear;
    String dateString;
    TextView dateTextCalendar,monthTextCalendar;
    RelativeLayout calendarLayout;
    //ImageView stepImage,caloriesImage,distanceImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar=Calendar.getInstance();
        dDay=calendar.get(Calendar.DAY_OF_MONTH);
        dMonth=calendar.get(Calendar.MONTH);
        dYear=calendar.get(Calendar.YEAR);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.today_reading_layout,container,false);
        seeGraphDetails=(TextView)view.findViewById(R.id.seeGraphDetails);
        calendarLayout=(RelativeLayout)view.findViewById(R.id.datePickerCalendar);
        dateTextCalendar=(TextView)view.findViewById(R.id.dateTextCalendar);
        monthTextCalendar=(TextView)view.findViewById(R.id.monthTextCalendar);
        linearLayout=(LinearLayout)view.findViewById(R.id.todayResultContainer);
        avLoadingIndicatorView=(AVLoadingIndicatorView)view.findViewById(R.id.avi);
        steps=(TextView)view.findViewById(R.id.stepCount);
        calories=(TextView)view.findViewById(R.id.caloriesCount);
        distance=(TextView)view.findViewById(R.id.DistanceCount);
        datepickerText=(TextView)view.findViewById(R.id.datePicker);

        Calendar cal=Calendar.getInstance();
        int todayDate=cal.get(Calendar.DAY_OF_MONTH);
        String todayDay=getMonth(cal.get(Calendar.MONTH));
        dateTextCalendar.setText(todayDate+"");
        monthTextCalendar.setText(todayDay);

        seeGraphDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),ThreeGraphActivity.class);
                startActivity(intent);
            }
        });

        datepickerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog datePickerDialog=new DatePickerDialog(getActivity(),
                        myDateListener, dYear, dMonth, dDay);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-604800000);
                datePickerDialog.show();
            }
        });
        calendarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog=new DatePickerDialog(getActivity(),
                        myDateListener, dYear, dMonth, dDay);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-604800000);
                datePickerDialog.show();
            }
        });
        steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityWithSpinner.spinner.setSelection(1);
            }
        });
        calories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityWithSpinner.spinner.setSelection(2);
            }
        });
        distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityWithSpinner.spinner.setSelection(3);
            }
        });

        new getTodayStepCountTask().execute();
        new getTodayCaloriesTask().execute();
        new getTodayDistanceTask().execute();
        return view;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    eDay=arg3;
                    eMonth=arg2+1;
                    eYear=arg1;
                    if(eDay<10 && eMonth<10){
                        dateString="0"+eDay+"/"+"0"+eMonth+"/"+eYear;
                    }else if(eDay<10){
                        dateString="0"+eDay+"/"+eMonth+"/"+eYear;
                    }else if(eMonth<10){
                        dateString=eDay+"/"+"0"+eMonth+"/"+eYear;
                    }else{
                        dateString=eDay+"/"+eMonth+"/"+eYear;
                    }
                    datepickerText.setText(dateString);
                    dateTextCalendar.setText(eDay+"");
                    monthTextCalendar.setText(getMonth(arg2));
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
                    DateTime dateTime = formatter.parseDateTime(dateString);
                    DateTime thatDayEndTime=dateTime.withTime(23,59,59,999);
                    avLoadingIndicatorView.show();
                    new getStepCount().execute(dateTime.getMillis(),thatDayEndTime.getMillis());
                    new getCalories().execute(dateTime.getMillis(),thatDayEndTime.getMillis());
                    new getDistance().execute(dateTime.getMillis(),thatDayEndTime.getMillis());
                }
            };



    public class getTodayStepCountTask extends AsyncTask<Void,Void,Void>{

        long total;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //circleProgressView.spin();
            avLoadingIndicatorView.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            PendingResult<DailyTotalResult> result =
                    Fitness.HistoryApi.readDailyTotalFromLocalDevice(MainActivityWithSpinner.mClient, TYPE_STEP_COUNT_DELTA);
            DailyTotalResult totalResult = result.await(30, SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                total = totalSet.isEmpty()
                        ? 0
                        : totalSet.getDataPoints().get(0).getValue(FIELD_STEPS).asInt();
            } else {
                // handle failure

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //circleProgressView.setValueAnimated((float)total);
            //circleProgressView.stopSpinning();
            setSteps(total);
        }
    }

    public class getTodayCaloriesTask extends AsyncTask<Void,Void,Void>{

        long total;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // circleProgressView2.spin();
        }

        @Override
        protected Void doInBackground(Void... params) {

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
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //circleProgressView2.setValueAnimated((float)total);
            //circleProgressView2.stopSpinning();
            setCalories(total);
        }
    }

    public class getTodayDistanceTask extends AsyncTask<Void,Void,Void>{

        long total;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //circleProgressView3.spin();
        }

        @Override
        protected Void doInBackground(Void... params) {

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
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //circleProgressView3.setValueAnimated(Float.parseFloat(String.format("%.2f",(float)total/1000)));
            //circleProgressView3.stopSpinning();
            setDistance(Float.parseFloat(String.format("%.2f",(float)total/1000)));
            avLoadingIndicatorView.hide();
            linearLayout.setVisibility(View.VISIBLE);
        }
    }

    public void setSteps(long total){
        ValueAnimator animator = ValueAnimator.ofFloat(0,(float)total);
        animator.setObjectValues(0, total);
        animator.setDuration(2000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                steps.setText("" + String.format("%.0f",Float.parseFloat(animation.getAnimatedValue()+"")));
            }
        });
        animator.start();
    }
    public void setCalories(long total){
        ValueAnimator animator = ValueAnimator.ofFloat(0,(float)total);
        animator.setObjectValues(0, total);
        animator.setDuration(2000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                calories.setText("" + String.format("%.0f",Float.parseFloat(animation.getAnimatedValue()+"")));
            }
        });
        animator.start();
    }
    public void setDistance(float total){
        ValueAnimator animator = ValueAnimator.ofFloat(0,(float)total);
        animator.setObjectValues(0, total);
        animator.setDuration(2000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                distance.setText("" + String.format("%.2f",Float.parseFloat(animation.getAnimatedValue()+"")));
            }
        });
        animator.start();
    }

    public class getStepCount extends AsyncTask<Long,Void,String>{
        String step="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Long... params) {
            String stepCount=getStepCount(params[0],params[1]);
            return stepCount;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals(""))
                result="0";
            setSteps(Long.parseLong(result));
        }
    }

    public class getCalories extends AsyncTask<Long,Void,String>{
        String step="";
        @Override
        protected String doInBackground(Long... params) {
            String stepCount=getCalories(params[0],params[1]);
            return stepCount;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals(""))
                result="0";
            setCalories((long)Float.parseFloat(result));
        }
    }
    public class getDistance extends AsyncTask<Long,Void,String>{
        String step="";
        @Override
        protected String doInBackground(Long... params) {
            String stepCount=getDistance(params[0],params[1]);
            return stepCount;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals(""))
                result="0";
            setDistance(Float.parseFloat(result));
            if(avLoadingIndicatorView.isShown())
                avLoadingIndicatorView.hide();
        }
    }


    public String getStepCount(long startTime,long endTime){
        String steps="";
        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(MainActivityWithSpinner.mClient, readRequest).await(1, TimeUnit.MINUTES);

        if(dataReadResult.getBuckets().size() > 0){
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet1 : dataSets) {
                    for(DataPoint dataPoint:dataSet1.getDataPoints()){
                        for (Field field:dataPoint.getDataType().getFields()){
                            steps=dataPoint.getValue(field)+"";
                        }
                    }
                }

            }
        }
        return steps;
    }

    public String getCalories(long startTime,long endTime){
        String steps="";
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(MainActivityWithSpinner.mClient, readRequest).await(1, TimeUnit.MINUTES);

        if(dataReadResult.getBuckets().size() > 0){
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet1 : dataSets) {
                    for(DataPoint dataPoint:dataSet1.getDataPoints()){
                        for (Field field:dataPoint.getDataType().getFields()){
                            steps=String.format("%.2f",Float.parseFloat(dataPoint.getValue(field)+""))+"";
                        }
                    }
                }

            }
        }
        return steps;
    }

    public String getDistance(long startTime,long endTime){
        String steps="";
        float total=0;
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(MainActivityWithSpinner.mClient, readRequest).await(1, TimeUnit.MINUTES);

        if(dataReadResult.getBuckets().size() > 0){
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet1 : dataSets) {
                    for(DataPoint dataPoint:dataSet1.getDataPoints()){
                        for (Field field:dataPoint.getDataType().getFields()){
                           // steps=String.format("%.2f",Float.parseFloat(dataPoint.getValue(field)+"")/1000)+"";
                           // Log.d("STEPS",steps);
                            total+=Float.parseFloat(dataPoint.getValue(field)+"");
                        }
                    }
                }

            }
            steps=String.format("%.2f",total/1000);
        }
        return steps;
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
