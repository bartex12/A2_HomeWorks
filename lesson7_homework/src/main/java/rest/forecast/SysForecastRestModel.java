package rest.forecast;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SysForecastRestModel implements Serializable {
    @SerializedName("pod") public String pod;
}
