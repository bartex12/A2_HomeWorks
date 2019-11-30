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

import static com.geekbrains.city_weather.constants.AppConstants.BROADCAST_COORD_ACTION;
import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.IS_JSON_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.IS_RESPONS_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT;

public class BackgroundCoordByCityService extends IntentService {

    private static final String TAG = "33333";

    public BackgroundCoordByCityService() {
        super("background_coord_by_city_service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "BackgroundCoordByCityService Язык системы= " + Locale.getDefault().getLanguage());

        String currentCity = Objects.requireNonNull(Objects.requireNonNull(intent).getExtras())
                .getString(CURRENT_CITY);
        Log.d(TAG, "BackgroundCoordByCityService onHandleIntent " +
                " currentCity = " + currentCity);

        // создаём интент широковещательного сообщения с фильтром и ловим его в MainActivity
        Intent broadcastIntentCoords = new Intent(BROADCAST_COORD_ACTION);

        Response<WeatherRequestRestModel> response = getCoordsResponse(currentCity);
        Log.d(TAG, "BackgroundCoordByCityService response = " + response);

        //если телефон не может посылать запросы, response=null, обрабатываем эту ситуацию
        if (response != null) {
            //если удалось получить ответ от сервера посылаем интент с ответом
            if (response.body() != null && response.isSuccessful()) {
                Log.d(TAG, "BackgroundCoordByCityService loadWeather OK");
                broadcastIntentCoords.putExtra(JAVA_OBJECT, response.body());
                broadcastIntentCoords.putExtra(IS_JSON_NULL, false);
                broadcastIntentCoords.putExtra(IS_RESPONS_NULL, false);
                sendBroadcast(broadcastIntentCoords);
                Log.d(TAG, "%%%%%%% 1");
                //а если не удалось получить ответ для погоды по городу , обычно
                // посылаем интент для обработки ошибки  ловим его в ChooseCityFrag
            } else {
                Log.d(TAG, "BackgroundCoordByCityService loadWeather NO");
                broadcastIntentCoords.putExtra(IS_JSON_NULL, true);
                broadcastIntentCoords.putExtra(IS_RESPONS_NULL, false);
                sendBroadcast(broadcastIntentCoords);
                Log.d(TAG, "%%%%%%% 3");
            }
            //если телефон не может посылать запросы, response=null
        } else {
            Log.d(TAG, "BackgroundCoordByCityService response = null");
            broadcastIntentCoords.putExtra(IS_RESPONS_NULL, true);
            sendBroadcast(broadcastIntentCoords);
            Log.d(TAG, "%%%%%%% 4");
        }
    }

    private Response<WeatherRequestRestModel> getCoordsResponse(String currentCity) {
        //если надо получить сразу WeatherRequestRestModel, то надо .execute().body()
        Response<WeatherRequestRestModel> response = null;

        try {
            if (Locale.getDefault().getLanguage().equals("ru")) {
                response = OpenWeatherRepo.getSingleton()
                        .getAPI().loadWeatherRu(currentCity,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric", "ru")
                        .execute();
            } else {
                response = OpenWeatherRepo.getSingleton()
                        .getAPI().loadWeatherEng(currentCity,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric")
                        .execute();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
