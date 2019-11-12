package com.geekbrains.city_weather.database;

public class DataWeather {

    private long _id;
    private String cityName;
    private String country;
    private String lastUpdate;
    private String description;
    private float windSpeed;
    private float pressure;
    private float temperature;
    private int iconCod;

    public DataWeather( ){
        //пустой конструктор
    }

    //основной конструктор
    public DataWeather(long _id, String cityName, String country, String lastUpdate,
                    String description, float windSpeed, float pressure, float temperature, int iconCod){
        this._id = _id;
        this.cityName = cityName;
        this.country = country;
        this.lastUpdate = lastUpdate;
        this.description = description;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.temperature = temperature;
        this.iconCod = iconCod;
    }

    //спец конструктор
    public DataWeather(String cityName, String country, String lastUpdate,
                       String description, float windSpeed, float pressure, float temperature, int iconCod){
        this.cityName = cityName;
        this.country = country;
        this.lastUpdate = lastUpdate;
        this.description = description;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.temperature = temperature;
        this.iconCod = iconCod;
    }

    public DataWeather getDataWeatherDefault(){
        return  new DataWeather("***", "***", "***",
                "***", 999,0, 999, 999);
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

    public float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public int getIconCod() {
        return iconCod;
    }

    public void setIconCod(int iconCod) {
        this.iconCod = iconCod;
    }
}
