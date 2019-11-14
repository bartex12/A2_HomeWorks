package com.geekbrains.city_weather.database;

public class DataForecast5Days {

    long id;
    String city;
    String dataUpdate;
    String weatherIcon;
    String temp;

    public DataForecast5Days(long id, String city, String temp, String dataUpdate, String weatherIcon) {
        this.id = id;
        this.city = city;
        this.temp = temp;
        this.dataUpdate = dataUpdate;
        this.weatherIcon = weatherIcon;
    }

    public String getDataUpdate() {
        return dataUpdate;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public String getTemp() {
        return temp;
    }

    public long getId() {
        return id;
    }

    public String getCity() {
        return city;
    }
}
