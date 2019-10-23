package com.geekbrains.city_weather.singltones;


import java.util.ArrayList;

public class CityLab {

    public static ArrayList<String> cityMarked;
    private static CityLab cityLab;

    //статический метод синглета
    public static CityLab getInstance(ArrayList<String> cityMarked){
        if (cityLab == null){
            cityLab = new CityLab(cityMarked);
        }
        return cityLab;
    }

    //закрытый конструктор
    private CityLab(ArrayList<String> cityMarked){
        this.cityMarked = cityMarked;
//        cityMarked = new ArrayList<>();
//        addCity("Saint Petersburg");
    }

    //получение списка данных
    public static ArrayList<String> getCitysList(){
        return cityMarked;
    }

    //добавление данных
    public static void addCity(String city){
        if (isNotCityInList(city)){
            cityMarked.add(city);
        }
    }

    //удаление данных из списка
    public static ArrayList<String> removeSity(String city){
        cityMarked.remove(city);
        return cityMarked;
    }

    //очистка списка
    public static void clearSityList(){
        cityMarked.clear();
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
