package com.geekbrains.city_weather.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.geekbrains.city_weather.R;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rest.OpenWeatherRepo;
import rest.entities.WeatherRequestRestModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.geekbrains.city_weather.constants.AppConstants.BROADCAST_WEATHER_ACTION;
import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.IS_JSON_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT;


public class BackgroundWeatherService extends IntentService {

    private static final String TAG = "33333";

    //если создавать через Alt+Enter, будет BackgroundWeatherService(String name), его надо удалить
    public BackgroundWeatherService() {
        super("background_service_for_city_weather");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        //получаем текущий город из интента
        final String currentCity = Objects.requireNonNull(intent).getStringExtra(CURRENT_CITY);
        //делаем запрос и получаем ответ от сервера
        OpenWeatherRepo.getSingleton().getAPI().loadWeatherEng(currentCity,
                "80bb32e4a0db84762bb04ab2bd724646", "metric")
                .enqueue(new Callback<WeatherRequestRestModel>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherRequestRestModel> call,
                                           @NonNull Response<WeatherRequestRestModel> response) {
                        Log.d(TAG, "BackgroundWeatherService loadWeatherRu" );
//          отправляем уведомление о завершении сервиса во фрагмент WeatherFragment
//          там создаём  private class ServiceFinishedReceiver extends BroadcastReceiver,
//          который регистрируем в onStart с фильтром  BROADCAST_WEATHER_ACTION и  в его методе
//          оnReceive обрабатываем погодные данные
//          Для этого создаём интент широковещательного сообщения с фильтром
                        Intent broadcastIntent = new Intent(BROADCAST_WEATHER_ACTION);
                        //если удалось получить ответ от сервера посылаем интент с ответом
                        if (response.body() != null && response.isSuccessful()) {
                              broadcastIntent.putExtra(JAVA_OBJECT, response.body());
                              broadcastIntent.putExtra(CURRENT_CITY, currentCity);
                              broadcastIntent.putExtra(IS_JSON_NULL, false);
                              sendBroadcast(broadcastIntent);
                              //а если не удалось получить ответ- посылаем интент для обработки ошибки
                        } else {
                            broadcastIntent.putExtra(CURRENT_CITY, currentCity);
                            broadcastIntent.putExtra(IS_JSON_NULL, true);
                            sendBroadcast(broadcastIntent);
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherRequestRestModel> call, Throwable t) {
                        Toast.makeText(getBaseContext(), getString(R.string.network_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
