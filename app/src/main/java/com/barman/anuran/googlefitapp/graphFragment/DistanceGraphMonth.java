package com.barman.anuran.googlefitapp.graphFragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.barman.anuran.googlefitapp.R;
import com.barman.anuran.googlefitapp.activities.MainActivityWithSpinner;
import com.barman.anuran.googlefitapp.activities.ThreeGraphActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA;
import static com.google.android.gms.fitness.data.Field.FIELD_STEPS;
import static java.text.DateFormat.getDateInstance;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Anuran on 2/21/2017.
 */

public class DistanceGraphMonth extends Fragment {
    LineChart distanceChart;
    List<Entry> dataDistance=new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.distance_graph,container,false);
        distanceChart=(LineChart)view.findViewById(R.id.chart);
        distanceChart.setNoDataText("Please wait.Loading your Chart");
        Description description=new Description();
        description.setText("");
        distanceChart.setDescription(description);
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
        LineData lineData;
        List<String> dates=new ArrayList<>();
        IAxisValueFormatter formatter=new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dates.get((int)value);
            }
        };
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(Void... params) {

            DataReadRequest readRequest = queryFitnessCaloriesData();

            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(ThreeGraphActivity.mClient, readRequest).await(50, TimeUnit.SECONDS);
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
            DateFormat dateFormat =  new SimpleDateFormat("HH:mm:ss");
            DateFormat dateFormatFull=new SimpleDateFormat("dd/MM/yy");
            DateFormat dateFormatTemp=new SimpleDateFormat("E");
            dataDistance.clear();
            float gap=0;
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
                                Entry entry=new Entry(gap,Float.parseFloat(dataPoint.getValue(field)+"")/1000);
                                dataDistance.add(entry);
                                dates.add(fullDate.substring(fullDate.indexOf(" ")+1));
                                gap+=1f;

                                // result+="Field: "+field.getName()+"Value: "+dataPoint.getValue(field)+"Start: "+dateFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS))+"End: "+dateFormat.format(dataPoint.getEndTime(TimeUnit.MILLISECONDS))+"\n";
                            }
                        }
                    }

                }
            }else{
                Log.d("DATASET","dataset empty");
            }

            LineDataSet lineDataSet=new LineDataSet(dataDistance,"Distance (in KM)");
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            List<ILineDataSet> dataSets=new ArrayList<>();
            dataSets.add(lineDataSet);
            lineData=new LineData(dataSets);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            distanceChart.getAxisLeft().setDrawGridLines(false);
            distanceChart.getXAxis().setDrawGridLines(false);
            distanceChart.setData(lineData);
            YAxis yAxis=distanceChart.getAxisRight();
            yAxis.setEnabled(false);
            distanceChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            distanceChart.getXAxis().setValueFormatter(formatter);
            distanceChart.getXAxis().setGranularity(1);
            distanceChart.setVisibleXRangeMaximum(5);
            if(dataDistance.size()>0){
                distanceChart.invalidate();
            }else{
                distanceChart.setTouchEnabled(false);
                distanceChart.setNoDataText("No Chart Data Available");
            }


        }
    }




    public static DataReadRequest queryFitnessCaloriesData() {

        DateTimeZone timeZone = DateTimeZone.forID("Asia/Kolkata");
        DateTime today = new DateTime(timeZone).withTime(23,59,59,500);
        DateTime startDay = today.minusMonths(1).withTimeAtStartOfDay();
        long endTime = today.getMillis();
        long startTime=startDay.getMillis();
        java.text.DateFormat dateFormat = getDateInstance();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
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
