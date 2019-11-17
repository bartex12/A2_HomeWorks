package com.geekbrains.city_weather.adapter;

import android.graphics.drawable.Drawable;

public class DataForecastNew {

    private String descriptionNew;
    private String tempNew;
    private String dayNew;
    private Drawable weatherIconNew;

    public DataForecastNew(String descriptionNew, String tempNew, String dayNew, Drawable weatherIconNew) {
        this.descriptionNew = descriptionNew;
        this.tempNew = tempNew;
        this.dayNew = dayNew;
        this.weatherIconNew = weatherIconNew;
    }

    public String getDescriptionNew() {
        return descriptionNew;
    }

    public String getTempNew() {
        return tempNew;
    }

    public String getDayNew() {
        return dayNew;
    }

    public Drawable getWeatherIconNew() {
        return weatherIconNew;
    }
}
