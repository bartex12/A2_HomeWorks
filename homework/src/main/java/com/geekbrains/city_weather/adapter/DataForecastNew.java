package com.geekbrains.city_weather.adapter;

import android.graphics.drawable.Drawable;

public class DataForecastNew {

    private String descriptionNew;
    private String tempNew;
    private String dayNew;
    private Drawable weatherIconNew;
    private String iconCodNew;

    public DataForecastNew(String descriptionNew, String tempNew,
                           String dayNew, Drawable weatherIconNew, String iconCodNew) {
        this.descriptionNew = descriptionNew;
        this.tempNew = tempNew;
        this.dayNew = dayNew;
        this.weatherIconNew = weatherIconNew;
        this.iconCodNew = iconCodNew;
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

    public String getIconCodNew() {
        return iconCodNew;
    }
}
