package rest.forecast_model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WeatherForecastRestModel implements Serializable {
    @SerializedName("id") public int id;
    @SerializedName("main") public String main;
    @SerializedName("description") public String description;
    @SerializedName("icon") public String icon;

}
