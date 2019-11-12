package com.geekbrains.city_weather.singltones;

import java.util.ArrayList;
import java.util.Collections;

public class CityListLab {

    private static ArrayList<String> cityMarked;
    private static CityListLab cityListLab;

    //статический метод синглета
    public static CityListLab getInstance(ArrayList<String> cityMarked){
        if (cityListLab == null){
            cityListLab = new CityListLab(cityMarked);
        }
        return cityListLab;
    }

    //закрытый конструктор
    private CityListLab(ArrayList<String> cityMarked){
        CityListLab.cityMarked = cityMarked;
    }

    //получение списка данных
    public static ArrayList<String> getCitysList(){
        return cityMarked;
    }

    //добавление данных
    public static void addCity(String city){
        if (isNotCityInList(city)){
            cityMarked.add(city);
            Collections.sort(cityMarked);
        }
    }

    //добавление данных
    public static void addCityInPosition(int position, String city){
        if (isNotCityInList(city)){
            cityMarked.add(position, city);
        }
    }

    //удаление данных из списка по названию
    public static void removeSity(String city){
        cityMarked.remove(city);
    }

    //очистка синглтона для замены списка
    public static void clearCityListLab(){
        cityMarked.clear();
        cityListLab = null;
    }

    private static boolean isNotCityInList(String city) {
        for (int i = 0; i < cityMarked.size(); i++) {
            if (cityMarked.get(i).toUpperCase().equals(city.toUpperCase())) {
                return false;
            }
        }
        return true;
    }

}
