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

    public DataWeather getDataWeatherDefault(){
        return  new DataWeather("Saint Petersburg", "RU", "***",
                "***", "***","***", "***", "***", 0);
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getIconCod() {
        return iconCod;
    }

    public void setIconCod(String iconCod) {
        this.iconCod = iconCod;
    }

    public long getUpdateSec() {
        return updateSec;
    }

    public void setUpdateSec(long updateSec) {
        this.updateSec = updateSec;
    }
}
