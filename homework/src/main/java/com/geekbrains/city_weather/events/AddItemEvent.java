package com.geekbrains.city_weather.events;

public class AddItemEvent {
    public String city;

    public AddItemEvent(String city) {
        this.city = city;
    }
}
