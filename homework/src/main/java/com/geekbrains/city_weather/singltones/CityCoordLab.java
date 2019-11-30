package com.geekbrains.city_weather.singltones;

import static com.geekbrains.city_weather.constants.AppConstants.DEFAULT_CITY;

public class CityCoordLab {

    private static String currentCity;
    private static double latitude;
    private static double longitude;
    private static CityCoordLab cityCoordLab;

    //закрытый конструктор
    private CityCoordLab(String city, double latitude, double longitude) {
        CityCoordLab.currentCity = city;
        CityCoordLab.latitude = latitude;
        CityCoordLab.longitude = longitude;
    }

    //статический метод синглтона
    public static CityCoordLab getInstance(String city, double latitude, double longitude) {
        if (cityCoordLab == null) {
            cityCoordLab = new CityCoordLab(city, latitude, longitude);
        }
        return cityCoordLab;
    }

    //получение текущего города
    public static String getCity() {
        return currentCity;
    }

    //получение текущего города
    public static double getLatitude() {
        return latitude;
    }

    //получение текущего города
    public static double getLongitude() {
        return longitude;
    }

    public static void setCurrentCity(String city, double lat, double lon) {
        CityCoordLab.currentCity = city;
        CityCoordLab.latitude = lat;
        CityCoordLab.longitude = lon;
    }

    public static void setCityDefault() {
        CityCoordLab.currentCity = DEFAULT_CITY;
        CityCoordLab.latitude = 60.0099509;
        CityCoordLab.longitude = 30.323831;
    }
}
