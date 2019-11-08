package rest.forecast;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import rest.entities.WeatherRestModel;

public class ListForecastRestModel implements Serializable {
    @SerializedName("dt") public long dt;
    @SerializedName("main") public MainForecastRestModel main;
    @SerializedName("weather") public WeatherForecastRestModel[] weather;
    @SerializedName("clouds") public CloudsForecastRestModel clouds;
    @SerializedName("wind") public WindForecastRestModel wind;
    @SerializedName("sys") public SysForecastRestModel sys;
    @SerializedName("dt_txt") public DateForecastRestModel dt_txt;
}
