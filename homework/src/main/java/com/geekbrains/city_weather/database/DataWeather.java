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
    private int iconCod;
    private int isLastCity;

    public DataWeather( ){
        //пустой конструктор
    }

    //полный конструктор
    public DataWeather(long _id, String cityName, String country, String lastUpdate,
                    String description, String windSpeed, String pressure,
                       String temperature, int iconCod, int isLastCity){
        this._id = _id;
        this.cityName = cityName;
        this.country = country;
        this.lastUpdate = lastUpdate;
        this.description = description;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.temperature = temperature;
        this.iconCod = iconCod;
        this.isLastCity = isLastCity;
    }

    // конструктор без id
    public DataWeather(String cityName, String country, String lastUpdate,
                       String description, String windSpeed, String pressure,
                       String temperature, int iconCod, int isLastCity){
        this.cityName = cityName;
        this.country = country;
        this.lastUpdate = lastUpdate;
        this.description = description;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.temperature = temperature;
        this.iconCod = iconCod;
        this.isLastCity = isLastCity;
    }

    public DataWeather getDataWeatherDefault(){
        return  new DataWeather("Saint Petersburg", "RU", "***",
                "***", "***","***", "***", 999, 0);
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

    public int getIconCod() {
        return iconCod;
    }

    public void setIconCod(int iconCod) {
        this.iconCod = iconCod;
    }

    public int getLastCity() {
        return isLastCity;
    }

    public void setLastCity(int lastCity) {
        isLastCity = lastCity;
    }
}
