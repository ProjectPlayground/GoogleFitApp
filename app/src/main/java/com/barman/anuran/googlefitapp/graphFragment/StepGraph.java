package com.barman.anuran.googlefitapp.graphFragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.barman.anuran.googlefitapp.activities.GraphActivity;
import com.barman.anuran.googlefitapp.R;
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
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
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

import lecho.lib.hellocharts.model.PointValue;

import static java.text.DateFormat.getDateInstance;

/**
 * Created by Anuran on 2/15/2017.
 */

public class StepGraph extends Fragment {
    LineChart lineChart;
    List<Entry> dataSteps=new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.step_graph,container,false);
        lineChart=(LineChart) view.findViewById(R.id.chart);
        lineChart.setNoDataText("Please wait.Loading your Chart.");
        Description description=new Description();
        description.setText("");
        lineChart.setDescription(description);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new InsertAndVerifyStepDataTask().execute();
    }


    private  class InsertAndVerifyStepDataTask extends AsyncTask<Void, Void, Void> {
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
            //Toast.makeText(GraphActivity.this, "toast 2", Toast.LENGTH_SHORT).show();
        }

        protected Void doInBackground(Void... params) {
            DataReadRequest readRequest=queryFitnessStepData();
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(ThreeGraphActivity.mClient, readRequest).await(1, TimeUnit.MINUTES);

            DateFormat dateFormatTemp=new SimpleDateFormat("E");
            dataSteps.clear();
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
                                PointValue pointValue=new PointValue(gap,Float.parseFloat(dataPoint.getValue(field)+""));
                                Entry entry=new Entry(gap,Float.parseFloat(dataPoint.getValue(field)+""));
                                dataSteps.add(entry);
                                gap+=1f;
                                dates.add(fullDate.substring(fullDate.indexOf(" ")+1));

                            }
                        }
                    }

                }
            }else {
                Log.d("DATASET","dataset is empty");
            }
            LineDataSet lineDataSet=new LineDataSet(dataSteps,"Steps");
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            List<ILineDataSet> dataSets=new ArrayList<>();
            dataSets.add(lineDataSet);
            lineData=new LineData(dataSets);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            lineChart.getAxisLeft().setDrawGridLines(false);
            lineChart.getXAxis().setDrawGridLines(false);
            lineChart.setData(lineData);
            YAxis yAxis=lineChart.getAxisRight();
            yAxis.setEnabled(false);
            lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            lineChart.getXAxis().setValueFormatter(formatter);
            lineChart.getXAxis().setGranularity(1);
            if(dataSteps.size()>0){
                lineChart.invalidate();
            }else{
                lineChart.setTouchEnabled(false);
                lineChart.setNoDataText("No Chart Data Available");
            }

        }
    }

    public DataReadRequest queryFitnessStepData() {

        DateTimeZone timeZone = DateTimeZone.forID("Asia/Kolkata");
        DateTime today = new DateTime(timeZone).withTime(23,59,59,500);
        DateTime startDay = today.minusWeeks(1).withTimeAtStartOfDay();
        long endTime = today.getMillis();
        long startTime=startDay.getMillis();
        java.text.DateFormat dateFormat = getDateInstance();

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
