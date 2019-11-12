package com.geekbrains.city_weather.events;

import com.geekbrains.city_weather.database.DataWeather;

public class GetWeatherEvent {

    public DataWeather dataWeather;

    public GetWeatherEvent(DataWeather dataWeather) {
        this.dataWeather = dataWeather;
    }
}
