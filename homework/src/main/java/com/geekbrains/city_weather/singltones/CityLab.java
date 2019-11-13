package com.geekbrains.city_weather.singltones;

import static com.geekbrains.city_weather.constants.AppConstants.DEFAULT_CITY;

public class CityLab {

    private static String currentCity;
    private static CityLab cityLab;

    //статический метод синглтона
    public static CityLab getInstance(String city){
        if (cityLab == null){
            cityLab = new CityLab(city);
        }
        return cityLab;
    }

    //закрытый конструктор
    private CityLab(String city){
        currentCity = city;
    }

    //получение текущего города
    public static String getCity(){
        return currentCity;
    }

    public static void setCurrentCity(String currentCity) {
        CityLab.currentCity = currentCity;
    }

    public static void setCityDefault() {
        CityLab.currentCity = DEFAULT_CITY;
    }
}
