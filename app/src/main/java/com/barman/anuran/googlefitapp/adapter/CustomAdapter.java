package com.barman.anuran.googlefitapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.barman.anuran.googlefitapp.R;
import com.barman.anuran.googlefitapp.model.StepCountModel;

import java.util.List;

/**
 * Created by Anuran on 1/30/2017.
 */

public class CustomAdapter extends BaseAdapter {
    List<StepCountModel> myData;
    Context context;
    int type;
    private int lastPosition = -1;
    public CustomAdapter( List<StepCountModel> myData,Context context,int type) {
         this.myData = myData;
        this.context = context;
        this.type=type;
    }

    @Override
    public int getCount() {
        return myData.size();
    }

    @Override
    public Object getItem(int position) {
        return myData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.step_row_single_item,null);
        switch (type){
            case 0:
                view=inflater.inflate(R.layout.step_row_single_item,parent,false);
                RelativeLayout meter=(RelativeLayout)view.findViewById(R.id.meter);
                LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (Integer.parseInt(myData.get(position).getSteps())>=15000){
                    params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                }else if (Integer.parseInt(myData.get(position).getSteps())>=10000){
                    params=new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.WRAP_CONTENT);

                }else  if (Integer.parseInt(myData.get(position).getSteps())>=5000){
                    params=new LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.WRAP_CONTENT);

                }else  if (Integer.parseInt(myData.get(position).getSteps())>=1000){
                    params=new LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.WRAP_CONTENT);

                }else {
                    params=new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT);
                }
                params.setMargins(10,0,10,0);
                meter.setLayoutParams(params);
                break;
            case 1:
                view=inflater.inflate(R.layout.calories_row_single_item,parent,false);
                RelativeLayout meter2=(RelativeLayout)view.findViewById(R.id.meter);
                LinearLayout.LayoutParams params2=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (Float.parseFloat(myData.get(position).getSteps())>=5000){
                    params2=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                }else if (Float.parseFloat(myData.get(position).getSteps())>=2000){
                    params2=new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.WRAP_CONTENT);

                }else if (Float.parseFloat(myData.get(position).getSteps())>=1500){
                    params2=new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.WRAP_CONTENT);

                } else if (Float.parseFloat(myData.get(position).getSteps())>=1000){
                    params2=new LinearLayout.LayoutParams(380, LinearLayout.LayoutParams.WRAP_CONTENT);

                }else  if (Float.parseFloat(myData.get(position).getSteps())>=500){
                    params2=new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT);

                }else {
                    params2=new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT);

                }/*else {
                    params3=new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT);
                }*/
                params2.setMargins(10,0,10,0);
                meter2.setLayoutParams(params2);
                break;
            case 2:
                view=inflater.inflate(R.layout.step_row_single_item,parent,false);
                RelativeLayout meter3=(RelativeLayout)view.findViewById(R.id.meter);
                LinearLayout.LayoutParams params3=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (Float.parseFloat(myData.get(position).getSteps())>=1.5){
                    params3=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                }else if (Float.parseFloat(myData.get(position).getSteps())>=1){
                    params3=new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.WRAP_CONTENT);

                }else  if (Float.parseFloat(myData.get(position).getSteps())>=0.5){
                    params3=new LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.WRAP_CONTENT);

                }else  if (Float.parseFloat(myData.get(position).getSteps())>=0.2){
                    params3=new LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.WRAP_CONTENT);

                }/*else {
                    params3=new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT);
                }*/
                params3.setMargins(10,0,10,0);
                meter3.setLayoutParams(params3);
                break;
        }

        TextView stepCount=(TextView)view.findViewById(R.id.stepCount);
        TextView startDate=(TextView)view.findViewById(R.id.startDate);
        TextView endDate=(TextView)view.findViewById(R.id.endDate);
        TextView date=(TextView)view.findViewById(R.id.dateText);
        date.setText(myData.get(position).getFullDate());
        switch (type){
            case 0:
                stepCount.setText(myData.get(position).getSteps()+" steps");
                break;
            case 1:
                stepCount.setText(myData.get(position).getSteps()+" cal");
                break;
            case 2:
                stepCount.setText(myData.get(position).getSteps()+" km");
                break;
        }
        startDate.setText(myData.get(position).getStartTime());
        endDate.setText(myData.get(position).getEndTime());
        Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        view.startAnimation(animation);
        lastPosition = position;
        return view;
    }


}
