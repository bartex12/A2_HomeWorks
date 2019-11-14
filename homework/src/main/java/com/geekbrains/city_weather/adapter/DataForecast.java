package com.geekbrains.city_weather.adapter;

public class DataForecast {
    String day;
    String weatherIcon;
    String temp;

    public DataForecast(String day, String weatherIcon, String temp) {
        this.day = day;
        this.weatherIcon = weatherIcon;
        this.temp = temp;
    }

    public String getDay() {
        return day;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public String getTemp() {
        return temp;
    }
}
