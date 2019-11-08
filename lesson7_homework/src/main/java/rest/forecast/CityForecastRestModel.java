package rest.forecast;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CityForecastRestModel implements Serializable {
    @SerializedName("id") public long id;
    @SerializedName("name") public String name;
    @SerializedName("coord") public CoordForecastRestModel coord;

}
