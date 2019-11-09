package com.geekbrains.city_weather.singltones;

import com.squareup.otto.Bus;

public class EventBus {
    private static Bus bus = null;

    public static Bus getBus() {
        if(bus == null) {
            bus = new Bus();
        }

        return bus;
    }
}
