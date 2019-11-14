package com.geekbrains.city_weather.database;

public class DataWeather {

    private long _id;
    private String cityName;
    private String country;
    private String lastUpdate;
    private String description;
    private String windSpeed;
    private String pressure;
    private String temperature;
    private String iconCod;
    private long updateSec;

    public DataWeather( ){
        //пустой конструктор
    }

    //полный конструктор
    public DataWeather(long _id, String cityName, String country, String lastUpdate,
                    String description, String windSpeed, String pressure,
                       String temperature, String iconCod, long updateSec){
        this._id = _id;
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

    public long get_id() {return _id; }
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
    public long getUpdateSec() {return updateSec; }

    }


