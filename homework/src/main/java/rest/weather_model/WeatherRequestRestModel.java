package rest.weather_model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WeatherRequestRestModel implements Serializable {
    @SerializedName("coord") public CoordRestModel coordinates;
    @SerializedName("weather") public WeatherRestModel[] weather;
    @SerializedName("base") public String base;
    @SerializedName("main")public MainRestModel main;
    @SerializedName("visibility") public int visibility;
    @SerializedName("wind")public WindRestModel wind;
    @SerializedName("clouds")public CloudsRestModel clouds;
    @SerializedName("dt") public long dt;
    @SerializedName("sys")public SysRestModel sys;
    @SerializedName("timezone") public int timezone;
    @SerializedName("id") public long id;
    @SerializedName("name") public String name;
    @SerializedName("cod") public int cod;
}
