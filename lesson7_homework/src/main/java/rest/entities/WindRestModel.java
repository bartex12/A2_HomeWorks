package rest.entities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WindRestModel implements Serializable {
    @SerializedName("speed") public float speed;
    @SerializedName("deg") public float deg;
}
