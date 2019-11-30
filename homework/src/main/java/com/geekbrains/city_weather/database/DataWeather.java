package com.geekbrains.city_weather.database;

public class DataWeather {

    private String cityName;
    private String country;
    private String latitude;
    private String longitude;
    private String lastUpdate;
    private String description;
    private String windSpeed;
    private String pressure;
    private String temperature;
    private String iconCod;
    private long updateSec;

    // конструктор без id
    public DataWeather(String cityName, String country, String latitude, String longitude,
                       String lastUpdate, String description, String windSpeed, String pressure,
                       String temperature, String iconCod, long updateSec){
        this.cityName = cityName;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdate = lastUpdate;
        this.description = description;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.temperature = temperature;
        this.iconCod = iconCod;
        this.updateSec = updateSec;
    }

    public static DataWeather getDataWeatherDefault(){
        return new DataWeather("Saint Petersburg", "RU",
                "60.0099509", "30.323831", "***",
                "***", "***","***", "***", "***", 0);
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountry() {
        return country;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
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


