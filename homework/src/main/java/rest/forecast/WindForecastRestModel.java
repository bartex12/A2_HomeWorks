package rest.forecast;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WindForecastRestModel implements Serializable {
    @SerializedName("speed") public float speed;
    @SerializedName("deg") public float deg;
}
