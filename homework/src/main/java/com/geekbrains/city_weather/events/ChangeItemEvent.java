package com.geekbrains.city_weather.events;

public class ChangeItemEvent {
    public String city;

    public ChangeItemEvent(String city) {
        this.city = city;
    }
}
