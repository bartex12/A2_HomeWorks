package rest.weather_model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SysRestModel implements Serializable {
    @SerializedName("type") public int type;
    @SerializedName("id") public int id;
    @SerializedName("message") public float message;
    @SerializedName("country") public String country;
    @SerializedName("sunrise") public long sunrise;
    @SerializedName("sunset") public long sunset;

}
