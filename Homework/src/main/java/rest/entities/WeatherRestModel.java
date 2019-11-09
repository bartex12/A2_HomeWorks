package rest.entities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WeatherRestModel implements Serializable {
    @SerializedName("id") public int id;
    @SerializedName("main") public String main;
    @SerializedName("description") public String description;
    @SerializedName("icon") public String icon;

}
