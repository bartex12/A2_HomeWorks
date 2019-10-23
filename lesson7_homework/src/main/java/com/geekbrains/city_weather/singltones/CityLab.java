package com.geekbrains.city_weather.singltones;

public class CityLab {

    private static String currentCity;
    private static CityLab cityLab;

    //статический метод синглета
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

}
