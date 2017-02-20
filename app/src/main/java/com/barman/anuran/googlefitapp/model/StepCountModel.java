package com.barman.anuran.googlefitapp.model;

/**
 * Created by Anuran on 1/30/2017.
 */

public class StepCountModel {
    public String steps;
    public String startTime;
    public String endTime;
    public String fullDate;

    public StepCountModel(String steps, String fullTime,String startTime, String endTime) {
        this.steps = steps;
        this.startTime = startTime;
        this.endTime = endTime;
        this.fullDate=fullTime;
    }

    public String getFullDate() {
        return fullDate;
    }

    public void setFullDate(String fullDate) {
        this.fullDate = fullDate;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
