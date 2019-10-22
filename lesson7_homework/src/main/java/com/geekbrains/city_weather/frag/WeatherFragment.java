package com.geekbrains.city_weather.frag;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.geekbrains.city_weather.MainActivity;
import com.geekbrains.city_weather.R;
import com.geekbrains.city_weather.adapter.DataForecast;
import com.geekbrains.city_weather.adapter.WeatherCardAdapter;
import com.geekbrains.city_weather.data_loader.CityWeatherDataLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.geekbrains.city_weather.constants.AppConstants.CITY_MARKED;
import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY_DETAIL;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {
    private static final String TAG = "33333";

    private RecyclerView recyclerViewForecast;
    private TextView cityTextView;
    private TextView textViewLastUpdate;
    private TextView textViewWhether;
    private TextView textViewTemper;
    private TextView textViewWind;
    private TextView textViewPressure;
    private TextView textViewIcon;
    private Handler handler = new Handler();
    private String[] dates = new String[5];
    private double[] temperuteres = new double[5];
    private int[] idArray = new int[5];
    private String[] iconArray = new String[5];
    private long sunrise;
    private long sunset;

    public WeatherFragment() {
        // Required empty public constructor
    }

    public static WeatherFragment newInstance(String city, ArrayList<String> cityMarked) {
        WeatherFragment fragment = new WeatherFragment();
        // Передача параметра
        Bundle args = new Bundle();
        args.putString("city", city);
        args.putStringArrayList("cityMarked", cityMarked);
        fragment.setArguments(args);
        return fragment;
    }

    // Получить город из аргументов public - он используется ещё где то
    public String getCity() {
        return Objects.requireNonNull(getArguments()).getString("city", "Moscow");
    }

    // Получить список из аргументов public - он используется ещё где то
    public ArrayList<String> getCityMarkedArray() {
        return Objects.requireNonNull(getArguments()).getStringArrayList("cityMarked");
    }

    @Override
    @SuppressLint("Recycle")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_whether, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initFonts();
        updateWeatherData(getCity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "WeatherFragment onResume");

        SharedPreferences prefSetting = androidx.preference.PreferenceManager
                .getDefaultSharedPreferences(Objects.requireNonNull(getActivity()));
        //получаем из файла настроек количество знаков после запятой
        boolean isShowCheckboxes = prefSetting.getBoolean("showCheckBoxes", true);
        Log.d(TAG, "WeatherFragment initViews isShowCheckboxes = " + isShowCheckboxes);

        // показываем/скрываем данные о ветре и давлении
        showWindAndPressure(isShowCheckboxes);
    }

    private void initViews(View view) {
        recyclerViewForecast = view.findViewById(R.id.recyclerViewForecast);
        cityTextView = view.findViewById(R.id.greetingsTextView);
        textViewLastUpdate = view.findViewById(R.id.textViewLastUpdate);
        textViewWhether = view.findViewById(R.id.textViewWhether);
        textViewTemper = view.findViewById(R.id.textViewTemper);
        textViewWind = view.findViewById(R.id.textViewWind);
        textViewPressure = view.findViewById(R.id.textViewPressure);
        textViewIcon = view.findViewById(R.id.textViewIcon);
    }

    private void initFonts() {
        Typeface weatherFont = Typeface.createFromAsset(
                Objects.requireNonNull(getActivity()).getAssets(), "fonts/weather.ttf");
        textViewIcon.setTypeface(weatherFont);
    }

    //TODO перенести всё в отдельный класс
    //получаем погодные данные с сервера  в JSON формате
    private void updateWeatherData(final String city) {
        new Thread() {
            @Override
            public void run() {
                final JSONObject jsonObject = CityWeatherDataLoader.getJSONData(city);
                final JSONObject jsonObjectForecast = CityWeatherDataLoader.getJSONDataForecast(city);
                if (jsonObject == null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //TODO если город не обнаружен и телефон в альбомной ориентации,
                            // нужно выводить картинку на эту тему - смущённый чел
                            Toast.makeText(getActivity(), R.string.place_not_found,
                                    Toast.LENGTH_LONG).show();
                            ArrayList<String> cityMarked = getCityMarkedArray();
                            cityMarked.remove(city);
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.putExtra(CURRENT_CITY_DETAIL, "Moscow");
                            intent.putExtra(CITY_MARKED, cityMarked);
                            startActivity(intent);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            renderWeather(jsonObject);
                            renderForecast(jsonObjectForecast);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderForecast(JSONObject jsonObjectForecast) {
        try {
            dates = getDateArray(jsonObjectForecast);
            temperuteres = getTempArray(jsonObjectForecast);
            idArray = getIdArray(jsonObjectForecast);
            sunrise = getSunrise(jsonObjectForecast);
            sunset = getSunset(jsonObjectForecast);
            iconArray = getIconsArray(idArray, sunrise, sunset);
            //после формирования данных для адаптера инициализируем сам адаптер
            initRecyclerView();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Ошибка в renderForecast");
        }
    }

    private void renderWeather(JSONObject jsonObject) {

        try {
            JSONObject details = jsonObject.getJSONArray("weather").getJSONObject(0);
            JSONObject main = jsonObject.getJSONObject("main");
            JSONObject wind = jsonObject.getJSONObject("wind");

            setPlaceName(jsonObject);
            setUpdatedText(jsonObject);
            setDescription(details);
            setWind(wind);
            setPressure(main);
            setCurrentTemp(main);

            setWeatherIcon(details.getInt("id"),
                    jsonObject.getJSONObject("sys").getLong("sunrise") * 1000,
                    jsonObject.getJSONObject("sys").getLong("sunset") * 1000);
        } catch (Exception exc) {
            exc.printStackTrace();
            Log.e(TAG, "One or more fields not found in the JSON data");
        }
    }

    //получение даты для прогноза на 5 дней
    private String[] getDateArray(JSONObject jsonObjectForecast) throws JSONException {
        Log.e(TAG, "getDateArray");
        DateFormat dateFormat = DateFormat.getDateInstance();
        long dateTime;
        String[] dateTimeArray = new String[5];
        for (int i = 0; i < dateTimeArray.length; i++) {
            dateTime = jsonObjectForecast.getJSONArray("list").
                    getJSONObject(7 + 8 * i).getLong("dt");
            dateTimeArray[i] = dateFormat.format(new Date(dateTime * 1000));
        }
        Log.e(TAG, "dateTimeArray.length = " + dateTimeArray.length);
        return dateTimeArray;
    }

    //получение температуры для прогноза на 5 дней
    private double[] getTempArray(JSONObject jsonObjectForecast) throws JSONException {
        Log.e(TAG, "getTempArray");
        double[] temper = new double[5];
        for (int i = 0; i < temper.length; i++) {
            JSONObject list = jsonObjectForecast.getJSONArray("list").getJSONObject(7 + 8 * i);
            temper[i] = list.getJSONObject("main").getDouble("temp");
        }
        Log.e(TAG, "temper.length = " + temper.length);
        return temper;
    }

    //получение массива id для прогноза на 5 дней
    private int[] getIdArray(JSONObject jsonObjectForecast) throws JSONException {
        Log.e(TAG, "getIdArray");
        int[] id = new int[5];
        for (int i = 0; i < id.length; i++) {
            JSONObject list = jsonObjectForecast.getJSONArray("list").getJSONObject(7 + 8 * i);
            id[i] = list.getJSONArray("weather").getJSONObject(0).getInt("id");
            Log.e(TAG, "id[i] = " + id[i]);
        }
        Log.e(TAG, "id.length = " + id.length);
        return id;
    }

    //получение времени  восход для прогноза на 5 дней
    private long getSunrise(JSONObject jsonObjectForecast) throws JSONException {
        Log.e(TAG, "getSunrise");
        long sunrise = jsonObjectForecast.getJSONObject("city").getLong("sunrise");
        Log.e(TAG, "sunrise = " + sunrise);
        return sunrise;
    }

    //получение времени  заката для прогноза на 5 дней
    private long getSunset(JSONObject jsonObjectForecast) throws JSONException {
        Log.e(TAG, "getSunset");
        long sunset = jsonObjectForecast.getJSONObject("city").getLong("sunset");
        Log.e(TAG, "sunset = " + sunset);
        return sunset;
    }

    //загрузка данных в адаптер списка прогноза на 5 дней
    private void  initRecyclerView(){
        //иконки
        DataForecast[] data = new DataForecast[] {
                new DataForecast(dates[0], iconArray[0],
                        String.format(Locale.getDefault(),
                                "%.1f", temperuteres[0]) + "\u2103"),
                new DataForecast(dates[1], iconArray[1],
                        String.format(Locale.getDefault(),
                                "%.1f", temperuteres[1]) + "\u2103"),
                new DataForecast(dates[2], iconArray[2],
                        String.format(Locale.getDefault(),
                                "%.1f", temperuteres[2]) + "\u2103"),
                new DataForecast(dates[3], iconArray[3],
                        String.format(Locale.getDefault(),
                                "%.1f", temperuteres[3]) + "\u2103"),
                new DataForecast(dates[4], iconArray[4],
                        String.format(Locale.getDefault(),
                                "%.1f", temperuteres[4]) + "\u2103")};

        ArrayList<DataForecast> list = new ArrayList<>(data.length);
        list.addAll(Arrays.asList(data));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        WeatherCardAdapter cardAdapter = new WeatherCardAdapter(getActivity(), list);

        recyclerViewForecast.setLayoutManager(layoutManager);
        recyclerViewForecast.setAdapter(cardAdapter);
    }

    // показываем/скрываем данные о ветре и давлении
    private void showWindAndPressure(boolean isShowCheckboxes) {
        if (isShowCheckboxes) {
            textViewWind.setVisibility(View.VISIBLE);
            textViewPressure.setVisibility(View.VISIBLE);
        } else {
            textViewWind.setVisibility(View.GONE);
            textViewPressure.setVisibility(View.GONE);
        }
    }

    private void setPlaceName(JSONObject jsonObject) throws JSONException {
        String cityText = jsonObject.getString("name").toUpperCase() + ", "
                + jsonObject.getJSONObject("sys").getString("country");
        //выводим строки в текстовых полях
        cityTextView.setText(cityText);
    }

    private void setUpdatedText(JSONObject jsonObject) throws JSONException {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String updateOn = dateFormat.format(new Date(jsonObject.getLong("dt") * 1000));
        String updatedText = Objects.requireNonNull(getActivity()).getResources()
                .getString(R.string.lastUpdate) + updateOn;
        textViewLastUpdate.setText(updatedText);
    }

    private void setDescription(JSONObject details) throws JSONException {
        String descriptionText = details.getString("description").toUpperCase();
        textViewWhether.setText(descriptionText);
    }

    private void setWind(JSONObject jsonObject) throws JSONException {
        String wind = jsonObject.getString("speed");
        String windSpeed = Objects.requireNonNull(getActivity()).getString(R.string.windSpeed);
        String ms = getActivity().getString(R.string.ms);
        String windText = windSpeed + " " + wind + " " + ms;
        textViewWind.setText(windText);
    }

    private void setCurrentTemp(JSONObject main) throws JSONException {
        String currentTextText = String.format(Locale.getDefault(), "%.1f",
                main.getDouble("temp")) + "\u2103";
        textViewTemper.setText(currentTextText);
    }

    private void setPressure(JSONObject main) throws JSONException {
        String pressure = main.getString("pressure");
        String press = Objects.requireNonNull(getActivity()).getString(R.string.press);
        String hPa = getActivity().getString(R.string.hPa);
        String pressureText = press + " " + pressure + " " + hPa;
        textViewPressure.setText(pressureText);
    }

    private String[] getIconsArray(int[] actualId, long sunrise, long sunset) {

        String[] icons = new String[5];
        try {
            for (int i = 0; i < icons.length; i++) {
                int id = actualId[i] / 100;

                if (actualId[i] == 800) {
                    long currentTime = new Date().getTime();
                    if (currentTime >= sunrise && currentTime < sunset) {
                        //icon = "\u2600";
                        icons[i] = getString(R.string.weather_sunny);
                    } else {
                        icons[i] = getString(R.string.weather_clear_night);
                    }
                } else {
                    switch (id) {
                        case 2: {
                            icons[i] = getString(R.string.weather_thunder);
                            break;
                        }
                        case 3: {
                            icons[i] = getString(R.string.weather_drizzle);
                            break;
                        }
                        case 5: {
                            icons[i] = getString(R.string.weather_rainy);
                            break;
                        }
                        case 6: {
                            icons[i] = getString(R.string.weather_snowy);
                            break;
                        }
                        case 7: {
                            icons[i] = getString(R.string.weather_foggy);
                            break;
                        }
                        case 8: {
                            //icon = "\u2601";
                            icons[i] = getString(R.string.weather_cloudy);
                            break;
                        }
                    }
                }

            }
            Log.e(TAG, "icons.length = " + icons.length);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return icons;
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";

        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                //icon = "\u2600";
                icon = getString(R.string.weather_sunny);
            } else {
                icon = getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2: {
                    icon = getString(R.string.weather_thunder);
                    break;
                }
                case 3: {
                    icon = getString(R.string.weather_drizzle);
                    break;
                }
                case 5: {
                    icon = getString(R.string.weather_rainy);
                    break;
                }
                case 6: {
                    icon = getString(R.string.weather_snowy);
                    break;
                }
                case 7: {
                    icon = getString(R.string.weather_foggy);
                    break;
                }
                case 8: {
                    //icon = "\u2601";
                    icon = getString(R.string.weather_cloudy);
                    break;
                }
            }
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            textViewIcon.setVisibility(View.GONE);
        } else {
            textViewIcon.setText(icon);
        }
    }

}
//1 hPa = 0.75006375541921 mmHg