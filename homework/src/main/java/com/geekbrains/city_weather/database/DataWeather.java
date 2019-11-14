package com.geekbrains.city_weather.database;

public class DataWeather {

    private String cityName;
    private String country;
    private String lastUpdate;
    private String description;
    private String windSpeed;
    private String pressure;
    private String temperature;
    private String iconCod;
    private long updateSec;

    // конструктор без id
    public DataWeather(String cityName, String country, String lastUpdate,
                       String description, String windSpeed, String pressure,
                       String temperature, String iconCod, long updateSec){
        this.cityName = cityName;
        this.country = country;
        this.lastUpdate = lastUpdate;
        this.description = description;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.temperature = temperature;
        this.iconCod = iconCod;
        this.updateSec = updateSec;
    }

    public static DataWeather getDataWeatherDefault(){
        return  new DataWeather("Saint Petersburg", "RU", "***",
                "***", "***","***", "***", "***", 0);
    }

    public String getCityName() {
        return cityName;
    }
    public String getCountry() { return country; }
    public String getLastUpdate() {
        return lastUpdate;
    }
    public String getDescription() {
        return description;
    }
    public String getWindSpeed() {
        return windSpeed;
    }
    public String getPressure() {
        return pressure;
    }
    public String getTemperature() {
        return temperature;
    }
    public String getIconCod() {return iconCod; }
    long getUpdateSec() {return updateSec; }

    }


