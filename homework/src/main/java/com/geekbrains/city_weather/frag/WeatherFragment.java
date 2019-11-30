package com.geekbrains.city_weather.frag;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.geekbrains.city_weather.R;
import com.geekbrains.city_weather.adapter.DataForecast;
import com.geekbrains.city_weather.adapter.WeatherCardAdapterNew;
import com.geekbrains.city_weather.database.DataWeather;
import com.geekbrains.city_weather.database.ForecastTable;
import com.geekbrains.city_weather.database.WeatherDataBaseHelper;
import com.geekbrains.city_weather.database.WeatherTable;
import com.geekbrains.city_weather.services.BackgroundWeatherService;
import com.geekbrains.city_weather.singltones.CityCoordLab;
import com.geekbrains.city_weather.singltones.CityLab;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import rest.forecast_model.ForecastRequestRestModel;
import rest.weather_model.WeatherRequestRestModel;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.geekbrains.city_weather.constants.AppConstants.BROADCAST_WEATHER_ACTION;
import static com.geekbrains.city_weather.constants.AppConstants.CITY_FRAFMENT_TAG;
import static com.geekbrains.city_weather.constants.AppConstants.IS_JSON_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.IS_RESPONS_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT_FORECAST;
import static com.geekbrains.city_weather.constants.AppConstants.LAST_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.LATITUDE;
import static com.geekbrains.city_weather.constants.AppConstants.LONGITUDE;
import static com.geekbrains.city_weather.constants.AppConstants.WEATHER_FRAFMENT_TAG;
import static com.geekbrains.city_weather.database.ForecastTable.COLUMN_DATA_UPDATE;
import static com.geekbrains.city_weather.database.ForecastTable.COLUMN_DESCRIPTION;
import static com.geekbrains.city_weather.database.ForecastTable.COLUMN_ICON;
import static com.geekbrains.city_weather.database.ForecastTable.COLUMN_TEMP;

/**
 *  Варианты получения погодных данных в приложении:
 * 1) на уроке A2L1 JSONObject получали в отдельном потоке через методы sdk - HttpURLConnection и т д
 * а обрабатывали в потоке GUI через handler.post(new Runnable()
 *
 * 2) на уроке A2L3 JSONObject получали через сервис, который сам создавал отдельный поток,
 * передавали в интенте широковещательного сообщения в виде строки в место обработки
 * воссоздавали JSONObject из строки и обрабатывали в потоке GUI
 *
 * 3) на уроке A2L5 погодные данные получаем через сервис сразу в виде JAVA объекта
 * с помощью запроса к погодному серверу через библиотеку Retrofit с конвертором GSON,
 * сериализуем ответ и передаём его  в интенте широковещательного сообщения в место обработки,
 * где десериализуем и обрабатывали в потоке GUI
 *
 * 4) на уроке A2L6 погодные данные пишем в базу данных и читаем их из базы,
 * если с момента повторного обращения за погодными данными того же города прошло менее часа
 *
 * 5) на уроке A2L8 погодные данные при старте приложения берём для местоположения устройства
 * если даны разрешения на определение местоположения.  А если не даны- то берутся данные
 * для последнего города при предыдущем обращении
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

    private String[] descriptions = new String[5];
    private String[] dates = new String[5];
    private String[] temperuteres = new String[5];
    private String[] iconArray = new String[5];
    private String[] descriptionsModel = new String[5];
    private String[] datesModel = new String[5];
    private String[] temperuteresModel = new String[5];
    private String[] iconArrayModel = new String[5];
    private Drawable[] iconArrayNewModel = new Drawable[5];
    private Drawable[] iconArrayNew = new Drawable[5];
    private ImageView imageView;
    //когда сервис BackgroundWeatherService отправляет уведомление о завершении
    //мы его получаем и в  методе onReceive обрабатываем погодные данные
    private ServiceFinishedReceiver receiver = new ServiceFinishedReceiver();
    private SQLiteDatabase database;


    public WeatherFragment() {
        // Required empty public constructor
    }

    public static WeatherFragment newInstance() {
        return new WeatherFragment();
    }

    @Override
    @SuppressLint("Recycle")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "WeatherFragment onCreateView");
        return inflater.inflate(R.layout.fragment_whether, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "WeatherFragment onAttach" );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "WeatherFragment onViewCreated" );

        initDB();
        initViews(view);
        getActualDataOfCityWeather();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "WeatherFragment onStart");
        //регистрируем примник широковещательных сообщений с фильтром BROADCAST_WEATHER_ACTION
        Objects.requireNonNull(getActivity())
                .registerReceiver(receiver, new IntentFilter(BROADCAST_WEATHER_ACTION));
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "WeatherFragment onResume");
        //  !!!!  имя папки в телефоне com.geekbrains.a1l1_helloworld   !!!
        SharedPreferences prefSetting =
                getDefaultSharedPreferences(Objects.requireNonNull(getActivity()));
        //получаем из файла настроек состояние чекбоксов Ключ не менять!
        boolean isShowCheckboxes = prefSetting.getBoolean("showCheckBoxes", true);
        Log.d(TAG, "WeatherFragment onResume isShowCheckboxes = " + isShowCheckboxes);

        // показываем/скрываем данные о ветре и давлении
        showWindAndPressure(isShowCheckboxes);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "WeatherFragment onStop");
        Objects.requireNonNull(getActivity()).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "WeatherFragment onDestroy");
        SharedPreferences defaultPrefs =
                PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity()));
        saveLastCity(defaultPrefs);
        super.onDestroy();
    }

    private void saveLastCity(SharedPreferences preferences){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_CITY, CityLab.getCity());
        editor.apply();
    }

    private void initDB(){
        database = new WeatherDataBaseHelper(getActivity()).getWritableDatabase();
    }

    private void initViews(View view) {
        recyclerViewForecast = view.findViewById(R.id.recyclerViewForecast);
        cityTextView = view.findViewById(R.id.greetingsTextView);
        textViewLastUpdate = view.findViewById(R.id.textViewLastUpdate);
        textViewWhether = view.findViewById(R.id.textViewWhether);
        textViewTemper = view.findViewById(R.id.textViewTemper);
        if (Objects.requireNonNull(getActivity()).getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) {
            //в портретной ориентации устанавливаем отступы
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) textViewTemper
                    .getLayoutParams();
            mlp.setMargins(0, 50, 0, 80);
        }
        textViewWind = view.findViewById(R.id.textViewWind);
        textViewPressure = view.findViewById(R.id.textViewPressure);
        imageView = view.findViewById(R.id.imageView);
    }

    //если последнее обновление в базе не найдено - идем на погодный сайт, а если
    // последнее обновление было не более часа  назад - берём данные из базы
    private void getActualDataOfCityWeather(){

        //получаем текущий город из синглтона - куда город попал из Preferences
        String currentCity = CityCoordLab.getCity();
        Log.d(TAG, "WeatherFragment getActualDataOfCityWeather currentCity = " + currentCity);
        //получаем список городов из базы
        ArrayList<String> ara = WeatherTable.getAllCitys(database);
        Log.d(TAG, "WeatherFragment getActualDataOfCityWeather ara = " + ara.toString());
        boolean isCityInDatabase = ara.contains(currentCity);
        Log.d(TAG, "WeatherFragment getActualDataOfCityWeather isCityInDatabase = " + isCityInDatabase);

        //если текущий город есть в базе
        if (isCityInDatabase){
            //получаем время последнего обновления погоды по этому городу
            long lastUpdateOfCityWeather = WeatherTable.getLastUpdate(database, currentCity);
            //вычисляем разницу в мс между текущим временем и  временем последнего обновления
            long delta = System.currentTimeMillis()/1000 - lastUpdateOfCityWeather;
            Log.d(TAG, "*** WeatherFragment getDataOfCityWeather /1000 = "
                    + System.currentTimeMillis()/1000);
            Log.d(TAG, "*** WeatherFragment getDataOfCityWeather lastUpdateOfCityWeather = "
                    + lastUpdateOfCityWeather);
            Log.d(TAG, "*** WeatherFragment getDataOfCityWeather delta = " + delta);

            //  если прошло больше заданного времени (1 час) с последнего обновления
            if (delta>3600){
                //запускаем сервис, работающий в отдельном потоке, передаём туда текущий город
                //для получения погодных данных
                Intent intent = new Intent(getActivity(), BackgroundWeatherService.class);
                intent.putExtra(LATITUDE, CityCoordLab.getLatitude());
                intent.putExtra(LONGITUDE, CityCoordLab.getLongitude());
                Objects.requireNonNull(getActivity()).startService(intent);
                //иначе  берём данные из базы
            }else{
                Log.d(TAG, "***********  WeatherFragment getDataOfCityWeather  ************");
                // получаем из базы погодные данные для текущего города
                getDataWetherForCity(currentCity);
                getDataforecastForCity(currentCity);
            }
        }else {
            //запускаем сервис, работающий в отдельном потоке, передаём туда текущий город
            //для получения погодных данных
            Intent intent = new Intent(getActivity(), BackgroundWeatherService.class);
            intent.putExtra(LATITUDE, CityCoordLab.getLatitude());
            intent.putExtra(LONGITUDE, CityCoordLab.getLongitude());
            Objects.requireNonNull(getActivity()).startService(intent);
        }
    }

    //получаем данные погоды из базы и показываем их на экране
    private void getDataWetherForCity(String currentCity) {
        DataWeather dataWeather = WeatherTable.getOneCityWeatherLine( database, currentCity);
        Log.d(TAG, "WeatherFragment getDataWetherForCity dataWeather = " + dataWeather);
        cityTextView.setText(String.format(Locale.getDefault(),"%s, %s",
                dataWeather.getCityName(),dataWeather.getCountry()));
        textViewLastUpdate.setText(dataWeather.getLastUpdate());
        textViewWhether.setText(dataWeather.getDescription());
        textViewWind.setText(dataWeather.getWindSpeed());
        textViewPressure.setText(dataWeather.getPressure());
        textViewTemper.setText(dataWeather.getTemperature());
        Drawable drawable = getIconFromIconCod(dataWeather.getIconCod());
        imageView.setImageDrawable(drawable);
    }

    //получаем данные прогноза из базы и запускаем с ними RecyclerView
    private void getDataforecastForCity(String currentCity){

        descriptions = ForecastTable.getArrayElementsForCityForecast(database,
                currentCity, COLUMN_DESCRIPTION);
        dates = ForecastTable.getArrayElementsForCityForecast(database,
                currentCity, COLUMN_DATA_UPDATE);
        temperuteres = ForecastTable.getArrayElementsForCityForecast(database,
                currentCity, COLUMN_TEMP);
        iconArray = ForecastTable.getArrayElementsForCityForecast(database,
                currentCity, COLUMN_ICON);

        iconArrayNew = getIconsArrayForecast(iconArray);  //Drawable

        Log.d(TAG, "+++  getDataforecastForCity descriptions = " + descriptions[0]);
        Log.d(TAG, "+++  getDataforecastForCity dates = " + dates[0]);
        Log.d(TAG, "+++  getDataforecastForCity temperuteres = " + temperuteres[0]);
        Log.d(TAG, "+++  getDataforecastForCity icon = " + iconArray[0]);

         //запускаем RecyclerView с данными из базы данных
        initRecyclerViewWithDatabaseData();
    }

    // Показать погоду во фрагменте в альбомной ориентации
    private void showCityWhetherLand() {

        // создаем новый фрагмент с текущей позицией для вывода погоды
        WeatherFragment weatherFrag = WeatherFragment.newInstance();
        // ... и выполняем транзакцию по замене фрагмента
        FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        ft.replace(R.id.content_super_r, weatherFrag, WEATHER_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        ft.commit();
        Log.d(TAG, "WeatherFragment onCityChange Фрагмент = " +
                getFragmentManager().findFragmentById(R.id.content_super));
    }

    // создаем новый фрагмент со списком ранее выбранных городов
    private void setChooseCityFrag() {
        ChooseCityFrag chooseCityFrag = ChooseCityFrag.newInstance();
        FragmentTransaction ft = Objects.requireNonNull(getActivity())
                .getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_super, chooseCityFrag, CITY_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        ft.commit();
    }

    //получаем погодные  из модели погоды для города modelWeather.name
    private void renderWeather(WeatherRequestRestModel modelWeather) {
        try {
            setPlaceName(modelWeather.name, modelWeather.sys.country);
            String lastUpdate = setUpdatedText(modelWeather.dt);
            Log.e(TAG, "**--** setUpdated lastUpdate= " + lastUpdate);
            setDescription(modelWeather.weather[0].description);
            String windSpeed = setWind(modelWeather.wind.speed);
            String pressure = setPressure(modelWeather.main.pressure);
            String temperature = setCurrentTemp(modelWeather.main.temp);

            Drawable drawable = getIconFromIconCod(modelWeather.weather[0].icon);
            imageView.setImageDrawable(drawable);

            //добавляем или заменяем погодные данные для города modelWeather.name
            addOrReplaceCityWeather(modelWeather, lastUpdate, windSpeed, pressure, temperature);

            //записываем город в синглтон - делаем его текущим
            CityLab.setCurrentCity(modelWeather.name);

        } catch (Exception exc) {
            exc.printStackTrace();
            Log.e(TAG, "One or more fields not found in the JSON data");
        }
    }

    private void addOrReplaceCityWeather(WeatherRequestRestModel modelWeather, String lastUpdate,
                                         String windSpeed, String pressure, String temperature) {
        //получаем класс DataWeather
        DataWeather dataWeather = new DataWeather(modelWeather.name,
                modelWeather.sys.country,lastUpdate, modelWeather.weather[0].description,
                windSpeed, pressure, temperature, modelWeather.weather[0].icon, modelWeather.dt );
        Log.e(TAG, "addOrReplaceCityWeather modelWeather.dt = " + modelWeather.dt);

        ArrayList<String> ara = WeatherTable.getAllCitys(database);
        boolean isCityInBase = ara.contains(modelWeather.name);
        if (isCityInBase){
            WeatherTable.replaceCityWeather(modelWeather.name, dataWeather, database);
            Log.e(TAG, "addOrReplaceCityWeather isCityInBase = true ");
        }else {
            WeatherTable.addCityWeather(dataWeather, database);
            Log.e(TAG, "addOrReplaceCityWeather isCityInBase = false");
        }
    }

    //получаем погодные данные пятидневного прогноза ИЗ МОДЕЛИ ПРОГНОЗА
    private void renderForecast(ForecastRequestRestModel modelForecast) {
        descriptionsModel = getDescrArray(modelForecast);
        datesModel = getDateArray(modelForecast);
        temperuteresModel = getTempArray(modelForecast);
        iconArrayModel = getIconsArray(modelForecast);  //String - коды иконок
        iconArrayNewModel = getIconsArrayForecast(iconArrayModel);  //Drawable - изображения иконок

        //добавляем или изменяем данные прогноза в базе данных для города modelForecast.city.name
        addOrReplaceCityForecast(modelForecast);
        //теперь, после формирования данных для адаптера, инициализируем сам адаптер
        initRecyclerViewWithDataFromModel();
    }

    private void addOrReplaceCityForecast(ForecastRequestRestModel modelForecast){
        //получаем массив объектов класса DataForecast
        DataForecast[] dataForecastsNew = getDataForecastsFromModel();
        ArrayList<String> ara = ForecastTable.getAllCitysFromForecast(database);
        boolean isCityInBase = ara.contains(modelForecast.city.name);
        if (isCityInBase){
            Log.e(TAG, "addOrReplaceCityWeather replaceCityForecast");
            ForecastTable.replaceCityForecast(modelForecast.city.name, dataForecastsNew, database);
        }else {
            Log.e(TAG, "addOrReplaceCityWeather addCityForecast");
            ForecastTable.addCityForecast(dataForecastsNew, database, modelForecast.city.name);
        }
    }

    //получение даты для прогноза на 5 дней
    private String[] getDateArray(ForecastRequestRestModel modelForecast) {
        Log.e(TAG, "getDateArray list.length = " + modelForecast.list.length);

        //форматируем, чтобы был день недели и числос месяцем
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMM", Locale.getDefault());
        long dateTime;
        String[] dateTimeArray = new String[5];
        for (int i = 0; i < dateTimeArray.length; i++) {
            dateTime = modelForecast.list[7 + 8 * i].dt;
            dateTimeArray[i] = dateFormat.format(new Date(dateTime * 1000));
        }
        Log.e(TAG, "**-** dateTimeArray[0] = " + dateTimeArray[0]);
        return dateTimeArray;
    }


    //получение температуры для прогноза на 5 дней
    private String[] getTempArray(ForecastRequestRestModel modelForecast){
        Log.e(TAG, "getTempArray list.length = " + modelForecast.list.length);
        double[] temper = new double[5];
        String[] tempArray = new String[5];
        for (int i = 0; i < temper.length; i++) {
            temper[i] =  modelForecast.list[7 + 8*i].main.temp;
            tempArray[i] = String.format(Locale.getDefault(),
                    "%.1f", temper[i]) + "\u2103";
        }
        Log.e(TAG, "temper.length = " + temper.length);
        return tempArray;
    }

    //получение описания погоды для прогноза на 5 дней
    private String[] getDescrArray(ForecastRequestRestModel modelForecast) {

        String[] descrArray = new String[5];
        for (int i = 0; i < descrArray.length; i++) {
            descrArray[i] = modelForecast.list[7 + 8 * i].weather[0].description;
        }
        Log.e(TAG, "descrArray.length = " + descrArray.length);
        return descrArray;
    }

    //получение массива id для прогноза на 5 дней
    private String[] getIconsArray(ForecastRequestRestModel modelForecast) {
        Log.e(TAG, "getIconsArray list.length = " + modelForecast.list.length);
        String[] icons = new String[5];
        for (int i = 0; i < icons.length; i++) {
            icons[i] = modelForecast.list[7 + 8 * i].weather[0].icon;
        }
        Log.e(TAG, "icons.length = " + icons.length);
        return icons;
    }

    //загрузка данных из модели в адаптер списка прогноза на 5 дней
    private void initRecyclerViewWithDataFromModel() {

        DataForecast[] data = getDataForecastsFromModel();

        ArrayList<DataForecast> list = new ArrayList<>(data.length);
        list.addAll(Arrays.asList(data));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        WeatherCardAdapterNew cardAdapter = new WeatherCardAdapterNew(getActivity(), list);

        recyclerViewForecast.setLayoutManager(layoutManager);
        recyclerViewForecast.setAdapter(cardAdapter);
    }

    //загрузка данных из базы данных в адаптер списка прогноза на 5 дней
    private void initRecyclerViewWithDatabaseData() {

        DataForecast[] data = getDataForecastsFromDatabase();

        ArrayList<DataForecast> list = new ArrayList<>(data.length);
        list.addAll(Arrays.asList(data));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        WeatherCardAdapterNew cardAdapter = new WeatherCardAdapterNew(getActivity(), list);

        recyclerViewForecast.setLayoutManager(layoutManager);
        recyclerViewForecast.setAdapter(cardAdapter);
    }

    private DataForecast[] getDataForecastsFromModel() {
        return new DataForecast[]{
                new DataForecast(descriptionsModel[0], temperuteresModel[0], datesModel[0],
                        iconArrayModel[0], iconArrayNewModel[0]),
                new DataForecast(descriptionsModel[1], temperuteresModel[1], datesModel[1],
                        iconArrayModel[1], iconArrayNewModel[1]),
                new DataForecast(descriptionsModel[2], temperuteresModel[2], datesModel[2],
                        iconArrayModel[2], iconArrayNewModel[2]),
                new DataForecast(descriptionsModel[3], temperuteresModel[3], datesModel[3],
                        iconArrayModel[3], iconArrayNewModel[3]),
                new DataForecast(descriptionsModel[4], temperuteresModel[4], datesModel[4],
                        iconArrayModel[4], iconArrayNewModel[4])
        };
    }

    private DataForecast[] getDataForecastsFromDatabase() {
        return new DataForecast[]{
                new DataForecast(descriptions[0], temperuteres[0], dates[0],
                        iconArray[0], iconArrayNew[0]),
                new DataForecast(descriptions[1], temperuteres[1], dates[1],
                        iconArray[1], iconArrayNew[1]),
                new DataForecast(descriptions[2], temperuteres[2], dates[2],
                        iconArray[2], iconArrayNew[2]),
                new DataForecast(descriptions[3], temperuteres[3], dates[3],
                        iconArray[3], iconArrayNew[3]),
                new DataForecast(descriptions[4], temperuteres[4], dates[4],
                        iconArray[4], iconArrayNew[4])
        };
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

    private void setPlaceName(String name, String country) {
        cityTextView.setText(String.format(Locale.getDefault(), "%s, %s",name,country));
    }

    private String setUpdatedText(long dt) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String updateOn = dateFormat.format(new Date(dt * 1000));
        String updatedText = Objects.requireNonNull(getActivity()).getResources()
                .getString(R.string.lastUpdate) + updateOn;
        textViewLastUpdate.setText(updatedText);
        return updatedText;
    }

    //описание
    private void setDescription(String description){
        StringBuilder builder = new StringBuilder(description);
        //выставляем первый символ заглавным, если это буква
        if (Character.isAlphabetic(description.codePointAt(0)))
            builder.setCharAt(0, Character.toUpperCase(description.charAt(0)));
        textViewWhether.setText(builder.toString());
    }

    private String setWind(float wind){
        String windSpeed = Objects.requireNonNull(getActivity()).getString(R.string.windSpeed);
        String ms = getActivity().getString(R.string.ms);
        String windText = windSpeed + " " + wind + " " + ms;
        textViewWind.setText(windText);
        return windText;
    }

    private String setPressure(float pressure) {
        String press = Objects.requireNonNull(getActivity()).getString(R.string.press);
        String hPa = getActivity().getString(R.string.hPa);
        String pressureText = press + " " + pressure + " " + hPa;
        textViewPressure.setText(pressureText);
        return pressureText;
    }

    private String setCurrentTemp(float temper){
        String currentText = String.format(Locale.getDefault(), "%.1f",temper) + "\u2103";
        textViewTemper.setText(currentText);
        return currentText;
    }

    //получение массива символов иконок  для отображения в пятидневном прогнозе погоды
    private Drawable[] getIconsArrayForecast(String[] iconCod) {

        Drawable[] icons = new Drawable[5];
        try {
            for (int i = 0; i < icons.length; i++) {
                //получаем рисунок иконки из ресурсов в зависимости от кода иконки
                icons[i] = getDrawable(iconCod[i]);
            }
            Log.e(TAG, "icons.length = " + icons.length);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return icons;
    }

    //получение изображений иконок в зависимости от кода иконки на сайте openweathermap.org
    // для вывода иконок при обращении к базе данных на устройстве
    private Drawable getIconFromIconCod(String iconCod) {
        //получаем рисунок иконки из ресурсов в зависимости от кода иконки
        Drawable drawable = getDrawable(iconCod);
        //еслиальбомная ориентация, не показываем рисунок иконки на экране
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageView.setVisibility(View.GONE);
        }
        return drawable;
    }

    private Drawable getDrawable(String iconCod) {
        Drawable drawable;

        switch (iconCod) {
            case "01d":
            case "01n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.clear_sky_01d_);
                break;
            case "02d":
            case "02n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.few_clouds_02d_);
                break;
            case "03d":
            case "03n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.scattered_clouds_03d_);
                break;
            case "04d":
            case "04n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.broken_clouds_04d_);
                break;
            case "09d":
            case "09n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.shower_rain_09d_);
                break;
            case "10d":
            case "10n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.rain_10d_);
                break;
            case "11d":
            case "11n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.thunderstorm_11d_);
                break;
            case "13d":
            case "13n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.snow_13d_);
                break;
            case "50d":
            case "50n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.mist_50d_);
                break;
            default:
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.what);
        }
        return drawable;
    }

    private class ServiceFinishedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.d(TAG, "WeatherFragment ServiceFinishedReceiver onReceive" );
            //переходим в поток GUI
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //сначала смотрим, а удалось ли сервису получить JAVA объект
                    boolean is_JSON_null =  intent.getBooleanExtra(IS_JSON_NULL, true);
                    boolean isResponceNull =  intent.getBooleanExtra(IS_RESPONS_NULL, false);
                    //сначала смотрим, ответ от сервера равен null или нет
                    if (isResponceNull){
                        Toast.makeText(getActivity(), getActivity().getResources()
                                        .getString(R.string.tlf_problems),Toast.LENGTH_LONG).show();
                        Log.e(TAG, "ServiceFinishedReceiver: Возникли проблемы " +
                                "с отправкой запроса. Возможно нет интернета");
                    }else {
                        //если не удалось, то is_JSON_null = true
                        if (is_JSON_null){
                            Toast.makeText(getActivity(), R.string.place_not_found,
                                    Toast.LENGTH_LONG).show();

                            CityCoordLab.setCityDefault();  //делаем текущим город Saint Petersburg

                            if (Objects.requireNonNull(getActivity()).getResources().getConfiguration()
                                    .orientation  == Configuration.ORIENTATION_LANDSCAPE){
                                //показываем фрагмент с погодой с городом по умолчанию
                                showCityWhetherLand();
                                //перегружаем фрагмент со списком для обновления списка
                                //поскольку в базу данных город не заносился, в списке его не будет
                                setChooseCityFrag();
                            }else {
                                //показываем фрагмент со списком
                                setChooseCityFrag();
                            }
                            //если JAVA объект получен, то получаем данные
                        }else {
                            //десериализуем объект WeatherRequestRestModel
                            WeatherRequestRestModel modelWeather = (WeatherRequestRestModel)
                                    Objects.requireNonNull(intent.getExtras())
                                            .getSerializable(JAVA_OBJECT);
                            Log.d(TAG, "WeatherFragment ServiceFinishedReceiver " +
                                    " lat = " + Objects.requireNonNull(modelWeather).coordinates.lat +
                                    " lon = " + Objects.requireNonNull(modelWeather).coordinates.lon +
                                    " name = " + Objects.requireNonNull(modelWeather).name);

                            //обрабатываем данные и выводим на экран если всё OK, заносим данные в базу
                            renderWeather(modelWeather);

                            ForecastRequestRestModel modelForecast =(ForecastRequestRestModel)
                                    Objects.requireNonNull(intent.getExtras())
                                            .getSerializable(JAVA_OBJECT_FORECAST);
                            Log.d(TAG, "WeatherFragment ServiceFinishedReceiver modelForecast =" +
                                    modelForecast);
                            renderForecast(modelForecast);
                        }
                    }
                }
            });
        }
    }
}
//TODO 1 hPa = 0.75006375541921 mmHg