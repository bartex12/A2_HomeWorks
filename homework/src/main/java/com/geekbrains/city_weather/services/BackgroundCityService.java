package com.geekbrains.city_weather.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.Nullable;
import rest.OpenWeatherRepo;
import rest.weather_model.WeatherRequestRestModel;
import retrofit2.Response;

import static com.geekbrains.city_weather.constants.AppConstants.BROADCAST_CITY_ACTION;
import static com.geekbrains.city_weather.constants.AppConstants.IS_JSON_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.IS_RESPONS_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT;
import static com.geekbrains.city_weather.constants.AppConstants.LATITUDE;
import static com.geekbrains.city_weather.constants.AppConstants.LONGITUDE;

public class BackgroundCityService extends IntentService {

    private static final String TAG = "33333";

    //если создавать через Alt+Enter, будет BackgroundCityService(String name), его надо удалить
    public BackgroundCityService() {
        super("background_service_for_city");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "BackgroundCityService Язык системы= " + Locale.getDefault().getLanguage());

        double latitude = Objects.requireNonNull(intent).getExtras().getDouble(LATITUDE);
        double longitude = Objects.requireNonNull(intent.getExtras()).getDouble(LONGITUDE);

        String lat = String.valueOf(latitude);
        String lon = String.valueOf(longitude);

        Log.d(TAG, "BackgroundCityService onHandleIntent " +
                " Широта = " + lat + "  Долгота = " + lon);

        // создаём интент широковещательного сообщения с фильтром и ловим его в MainActivity
        Intent broadcastIntentCity = new Intent(BROADCAST_CITY_ACTION);

        Response<WeatherRequestRestModel> response = getCityResponse(lat, lon);
        Log.d(TAG, "BackgroundCityService response = " + response);

        //если телефон не может посылать запросы, response=null, обрабатываем эту ситуацию
        if (response != null) {
            //если удалось получить ответ от сервера делаем запрос прогноза и посылаем интент с ответом
            if (response.body() != null && response.isSuccessful()) {
                Log.d(TAG, "BackgroundCityService loadWeather OK");
                broadcastIntentCity.putExtra(JAVA_OBJECT, response.body());
                broadcastIntentCity.putExtra(IS_JSON_NULL, false);
                broadcastIntentCity.putExtra(IS_RESPONS_NULL, false);
                sendBroadcast(broadcastIntentCity);
                Log.d(TAG, "####### 1");
                //а если не удалось получить ответ для погоды по координатам, обычно
                // посылаем интент для обработки ошибки  ловим его в MainActivity
            } else {
                Log.d(TAG, "BackgroundCityService loadWeather NO");
                broadcastIntentCity.putExtra(IS_JSON_NULL, true);
                broadcastIntentCity.putExtra(IS_RESPONS_NULL, false);
                sendBroadcast(broadcastIntentCity);
                Log.d(TAG, "####### 3");
            }
            //если телефон не может посылать запросы, response=null
        } else {
            Log.d(TAG, "BackgroundCityService response = null");
            broadcastIntentCity.putExtra(IS_RESPONS_NULL, true);
            sendBroadcast(broadcastIntentCity);
            Log.d(TAG, "####### 4");
        }
    }

    private Response<WeatherRequestRestModel> getCityResponse(String latitude, String longitude) {
        //если надо получить сразу WeatherRequestRestModel, то надо .execute().body()
        Response<WeatherRequestRestModel> response = null;

        try {
            if (Locale.getDefault().getLanguage().equals("ru")) {
                response = OpenWeatherRepo.getSingleton()
                        .getAPI().loadCityLatLonRu(latitude, longitude,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric", "ru")
                        .execute();
            } else {
                response = OpenWeatherRepo.getSingleton()
                        .getAPI().loadCityLatLonEng(latitude, longitude,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric")
                        .execute();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
