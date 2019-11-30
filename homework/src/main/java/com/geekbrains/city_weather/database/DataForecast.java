package com.geekbrains.city_weather.database;

import android.graphics.drawable.Drawable;

public class DataForecast {

    private String descriptionNew;
    private String tempNew;
    private String dayNew;
    private String iconCodNew;
    private Drawable iconDraw;

    public DataForecast(String descriptionNew, String tempNew,
                        String dayNew, String iconCodNew, Drawable iconDraw) {
        this.descriptionNew = descriptionNew;
        this.tempNew = tempNew;
        this.dayNew = dayNew;
        this.iconCodNew = iconCodNew;
        this.iconDraw = iconDraw;
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

    public String getIconCodNew() {
        return iconCodNew;
    }

    public Drawable getIconDraw() {
        return iconDraw;
    }
}