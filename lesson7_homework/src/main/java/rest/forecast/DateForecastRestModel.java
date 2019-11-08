package rest.forecast;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DateForecastRestModel implements Serializable {
    @SerializedName("dt_txt") public String dt_txt;
}
