package com.geekbrains.city_weather.frag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.geekbrains.city_weather.R;
import com.geekbrains.city_weather.adapter.RecyclerViewCityAdapter;
import com.geekbrains.city_weather.database.DataWeather;
import com.geekbrains.city_weather.database.WeatherDataBaseHelper;
import com.geekbrains.city_weather.database.WeatherTable;
import com.geekbrains.city_weather.dialogs.DialogCityAdd;
import com.geekbrains.city_weather.events.AddItemEvent;
import com.geekbrains.city_weather.events.ChangeItemEvent;
import com.geekbrains.city_weather.services.BackgroundCoordByCityService;
import com.geekbrains.city_weather.singltones.CityCoordLab;
import com.geekbrains.city_weather.singltones.EventBus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import rest.weather_model.WeatherRequestRestModel;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.geekbrains.city_weather.constants.AppConstants.BROADCAST_COORD_ACTION;
import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.IS_JSON_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.IS_RESPONS_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT;
import static com.geekbrains.city_weather.constants.AppConstants.WEATHER_FRAFMENT_TAG;

public class ChooseCityFrag extends Fragment implements SensorEventListener {

    private static final String TAG = "33333";
    private boolean isExistWhetherFrag;  // Можно ли расположить рядом фрагмент с погодой
    private RecyclerView recyclerViewMarked; //RecyclerView для списка ранее выбранных городов
    private RecyclerViewCityAdapter recyclerViewCityAdapter; //адаптер для RecyclerView
    private SensorManager sensorManager;
    private Sensor sensorTemp;
    private Sensor sensorHumidity;
    private TextView textTempHere;
    private TextView textHumidity;
    private SQLiteDatabase database;
    private ServiceCoordsReceiver receiver = new ServiceCoordsReceiver();

    public static ChooseCityFrag newInstance() {
        return  new ChooseCityFrag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "ChooseCityFrag onCreateView");
        return inflater.inflate(R.layout.fragment_city_choose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "ChooseCityFrag onViewCreated");

        initDB();
        initSensors();
        initViews(view);
        initRecycledView();

        registerForContextMenu(recyclerViewMarked);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "ChooseCityFrag onActivityCreated");
        // Определение, можно ли будет расположить рядом данные в другом фрагменте
        isExistWhetherFrag = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getBus().register(this);
        Objects.requireNonNull(getActivity())
                .registerReceiver(receiver, new IntentFilter(BROADCAST_COORD_ACTION));
    }

    @Override
    public void onResume() {
        super.onResume();
        initRecycledView();
        Log.d(TAG, "ChooseCityFrag onResume recyclerViewCityAdapter = " + recyclerViewCityAdapter);
        if (recyclerViewCityAdapter!= null){
            recyclerViewCityAdapter.notifyDataSetChanged();
        }
        getPreferences();
        registerListenersOfSensors();
    }

    @Override
    public void onStop() {
        EventBus.getBus().unregister(this);
        Objects.requireNonNull(getActivity()).unregisterReceiver(receiver);
        super.onStop();
    }

    private void registerListenersOfSensors() {
        //регистрируем слушатель сенсора, при этом  слушатель - сам фрагмент
        sensorManager.registerListener(this, sensorTemp,
                SensorManager.SENSOR_DELAY_NORMAL);
        //регистрируем слушатель сенсора, при этом  слушатель - фрагмент
        sensorManager.registerListener(this, sensorHumidity,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void getPreferences() {
        //  !!!!  имя папки в телефоне com.geekbrains.a1l1_helloworld   !!!
        SharedPreferences prefSetting =
                getDefaultSharedPreferences(Objects.requireNonNull(getActivity()));
        //получаем из файла настроек состояние чекбоксов (Ключ не менять!)
        boolean isShowTempHumidHere = prefSetting.getBoolean("showSensors", true);
        Log.d(TAG, "WeatherFragment onResume isShowTempHumidHere = " + isShowTempHumidHere);
        showTempAndHumiditySensors(isShowTempHumidHere);
    }

    // показываем/скрываем данные о температуре/влажности
    private void showTempAndHumiditySensors(boolean isShowTempHumidHere) {
        if (isShowTempHumidHere) {
            textTempHere.setVisibility(View.VISIBLE);
            textHumidity.setVisibility(View.VISIBLE);
        } else {
            textTempHere.setVisibility(View.GONE);
            textHumidity.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "ChooseCityFrag onPause");
        // Если приложение свернуто, отключаем слушатели
        sensorManager.unregisterListener(this, sensorTemp);
        sensorManager.unregisterListener(this, sensorHumidity);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "ChooseCityFrag onDestroy");
        super.onDestroy();
    }

    //********************************** Жесть **************************************************
    //Действия для контекстного меню для пунктов списка RecyclerView во фрагменте
    // 1 в onViewCreated фрагмента пишем registerForContextMenu(recyclerViewMarked);
    // 2 делаем метод onContextItemSelected(MenuItem item) как обычно (см ниже)
    // 3 ViewHolder адаптера implements View.OnCreateContextMenuListener и реализуем
    // onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) интерфейса
    // 4 присваиваем слушатель адаптеру во ViewHolder: itemView.setOnCreateContextMenuListener(this);
    // 5  устанавливаем слушатель для долгих нажатий в onBindViewHolder адаптера - ловим позицию
    // holder.textView.setOnLongClickListener(new View.OnLongClickListener()
    //********************************************************************************************

    //********************************** Жесть 2 **************************************************
    //Действия для контекстного меню для списка RecyclerView во фрагменте при использовании OttoBus
    // 1 в onViewCreated фрагмента пишем registerForContextMenu(recyclerViewMarked);
    // 2 делаем метод onCreateContextMenu(...) как обычно (см ниже)
    // 3 делаем метод onContextItemSelected(...) как обычно (см ниже)
    // 4  устанавливаем слушатель для долгих нажатий в onBindViewHolder адаптера - ловим позицию
    // holder.textView.setOnLongClickListener(new View.OnLongClickListener()
    //*********************************************************************************************

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu,
                                    @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Objects.requireNonNull(getActivity()).getMenuInflater().inflate(R.menu.context_city_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        handleMenuItemClick(item);
        return super.onContextItemSelected(item);
    }

    private void initDB(){
        database = new WeatherDataBaseHelper(getActivity()).getWritableDatabase();
    }

    private void initSensors() {
        sensorManager = (SensorManager) Objects.requireNonNull(getActivity()).getSystemService(Context.SENSOR_SERVICE);
        sensorTemp = Objects.requireNonNull(sensorManager).getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensorHumidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        Log.d(TAG, "ChooseCityFrag initSensors sensorTemp = " + sensorTemp +
                " sensorHumidity = " + sensorHumidity);
    }

    //инициализация View
    private void initViews(View view) {

        textTempHere = view.findViewById(R.id.textTempHere);
        textHumidity = view.findViewById(R.id.texHumidityHere);
        if (sensorTemp == null){
            textTempHere.setText(Objects.requireNonNull(getActivity()).getResources().
                    getString(R.string.No_temperature_sensor));
        }
        if (sensorHumidity == null){
            textHumidity.setText(Objects.requireNonNull(getActivity()).getResources().
                    getString(R.string.No_humidity_sensor));
        }

        recyclerViewMarked = view.findViewById(R.id.recycledViewMarked);
    }

    //инициализация RecycledView
    private void initRecycledView() {
        Log.d(TAG, "ChooseCityFrag initRecycledView");
        //используем встроенный LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //реализуем интерфейс адаптера, в  его методе onCityClick получим имя города и его позицию
        RecyclerViewCityAdapter.OnCityClickListener onCityClickListener =
                new RecyclerViewCityAdapter.OnCityClickListener() {
                    @Override
                    public void onCityClick(String newCity) {
                        Log.d(TAG, "ChooseCityFrag initRecycledView onCityClick");
                        //получаем список городов из базы
                        ArrayList<String> ara = WeatherTable.getAllCitys(database);
                        Log.d(TAG, "ChooseCityFrag getActualDataOfCityWeather ara = " + ara.toString());
                        boolean isCityInDatabase = ara.contains(newCity);

                        //если текущий город есть в базе
                        if (isCityInDatabase) {
                            //получаем объект с погодными данными
                            DataWeather dataWeather = WeatherTable.getOneCityWeatherLine(database, newCity);
                            double latitude = Double.parseDouble(dataWeather.getLatitude());
                            double longitude = Double.parseDouble(dataWeather.getLongitude());
                            //делаем город текущим
                            CityCoordLab.setCurrentCity(newCity, latitude, longitude);
                            // показываем погоду в городе с учётом ориентации экрана
                            showCityWhetherWithOrientation();
                        } else {
                            //запускаем сервис, работающий в отдельном потоке, передаём туда текущий город
                            //для получения погодных данных
                            Intent intent = new Intent(getActivity(), BackgroundCoordByCityService.class);
                            intent.putExtra(CURRENT_CITY, newCity);
                            Objects.requireNonNull(getActivity()).startService(intent);
                        }
                    }
                };

        // вызываем конструктор адаптера, передаём базу данных
        recyclerViewCityAdapter = new RecyclerViewCityAdapter(database);
        // передаём ссылку на интерфейс чтобы отработать реакцию на выбор города в списке
        //второй вариант передать ссылку - сделать это в конструкторе адаптера - так было раньше
        recyclerViewCityAdapter.setOnCityClickListener(onCityClickListener);
        recyclerViewMarked.setLayoutManager(layoutManager);
        recyclerViewMarked.setAdapter(recyclerViewCityAdapter);
    }

    //обработка для контекстного меню
    private void handleMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.menu_add: {
                DialogFragment dialogFragment = new DialogCityAdd();
                dialogFragment.show(Objects.requireNonNull(getFragmentManager()), "addCity");
                break;
            }
            case R.id.menu_remove: {
                recyclerViewCityAdapter.removeElement();
                break;
            }
            case R.id.menu_clear: {
                recyclerViewCityAdapter.clearList();
                break;
            }
            case R.id.menu_cancel: {
                break;
            }
        }
    }

    // показываем погоду в городе с учётом ориентации экрана
    private void showCityWhetherWithOrientation() {
        Log.d(TAG, "ChooseCityFrag showCityWhetherWithOrientation ");
        isExistWhetherFrag = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        //если альбомная ориентация,то
        if (isExistWhetherFrag) {
            Log.d(TAG, "ChooseCityFrag showCityWhetherWithOrientation альбомная");
            setWeatherFragment(R.id.content_super_r);
            //а если портретная, то
        } else {
            Log.d(TAG, "ChooseCityFrag showCityWhetherWithOrientation портретная");
            setWeatherFragment(R.id.content_super);
        }
    }

    // Показать погоду во фрагменте в зависимости от  города и ориентации
    private void setWeatherFragment(int container_id) {
        Log.d(TAG, "ChooseCityFrag setWeatherFragment ");
        // создаем новый фрагмент с текущей позицией для вывода погоды
        WeatherFragment weatherFrag = WeatherFragment.newInstance();
        //и выполняем транзакцию по замене фрагмента
        FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        ft.replace(container_id, weatherFrag, WEATHER_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        ft.commit();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int type = event.sensor.getType();
        if (type == 13){
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Objects.requireNonNull(getActivity()).getResources()
                        .getString(R.string.temperature)).append(event.values[0]).append(" \u00B0C");
                textTempHere.setText(stringBuilder);
        }else if (type == 12){
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Objects.requireNonNull(getActivity()).getResources()
                        .getString(R.string.Relative_humidity)).append(event.values[0]).append(" %");
                textHumidity.setText(stringBuilder);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //реакция на событие ChangeItemEvent Событие создаётся в DialogCityChange
    @Subscribe
    @SuppressWarnings("unused")
    public void onChangeEvent(ChangeItemEvent event) {
        Log.d(TAG, "ChooseCityFrag onChangeEvent event.city =" + event.city);
        //добавляем город в список адаптера - если город не будет найден, он автоматически
        // удалится при обновлении списка из базы данных в конструкторе адаптера
        recyclerViewCityAdapter.addElement(event.city);

        //получаем список городов из базы
        ArrayList<String> ara = WeatherTable.getAllCitys(database);
        Log.d(TAG, "ChooseCityFrag getActualDataOfCityWeather ara = " + ara.toString());
        boolean isCityInDatabase = ara.contains(event.city);
        //если текущий город есть в базе
        if (isCityInDatabase) {
            //получаем объект с погодными данными
            DataWeather dataWeather = WeatherTable.getOneCityWeatherLine(database, event.city);
            double latitude = Double.parseDouble(dataWeather.getLatitude());
            double longitude = Double.parseDouble(dataWeather.getLongitude());
            //делаем город текущим
            CityCoordLab.setCurrentCity(event.city, latitude, longitude);
            // показываем погоду в городе с учётом ориентации экрана
            showCityWhetherWithOrientation();

        } else {
            //запускаем сервис, работающий в отдельном потоке, передаём туда текущий город
            //для получения погодных данных
            Intent intent = new Intent(getActivity(), BackgroundCoordByCityService.class);
            intent.putExtra(CURRENT_CITY, event.city);
            Objects.requireNonNull(getActivity()).startService(intent);
        }
    }

    //реакция на событие AddItemEvent Событие создаётся в DialogCityAdd
    @Subscribe
    @SuppressWarnings("unused")
    public void onAddEvent(AddItemEvent event) {
        Log.d(TAG, "ChooseCityFrag onAddEvent event.city =" + event.city);
        //добавляем город в список адаптера
        recyclerViewCityAdapter.addElement(event.city);
    }

    //приёмник широковещательного сообщения с фильтром BROADCAST_CITY_ACTION (см onStart)
    private class ServiceCoordsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.d(TAG, "MainActivity ServiceCityReceiver onReceive");
            //переходим в поток GUI
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //сначала смотрим, а удалось ли сервису получить JAVA объект
                    boolean is_JSON_null = intent.getBooleanExtra(IS_JSON_NULL, true);
                    boolean isResponceNull = intent.getBooleanExtra(IS_RESPONS_NULL, false);
                    //сначала смотрим, ответ от сервера равен null или нет
                    if (isResponceNull) {
                        Toast.makeText(getActivity(), R.string.tlf_problems,
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "ServiceCityReceiver: Возникли проблемы " +
                                "с отправкой запроса. Возможно нет интернета");
                    } else {
                        //если не удалось, то is_JSON_null = true
                        if (is_JSON_null) {
                            Toast.makeText(getActivity(), R.string.place_not_found,
                                    Toast.LENGTH_LONG).show();
                            //делаем текущим город Saint Petersburg
                            CityCoordLab.setCityDefault();
                            // показываем погоду в городе с учётом ориентации экрана
                            showCityWhetherWithOrientation();

                            //если JAVA объект получен, то получаем данные
                        } else {
                            //десериализуем объект WeatherRequestRestModel
                            WeatherRequestRestModel modelWeather = (WeatherRequestRestModel)
                                    Objects.requireNonNull(intent.getExtras())
                                            .getSerializable(JAVA_OBJECT);
                            String name = Objects.requireNonNull(modelWeather).name;
                            String country = Objects.requireNonNull(modelWeather).sys.country;
                            double latitude = modelWeather.coordinates.lat;
                            double longitude = modelWeather.coordinates.lon;
                            //получаем город
                            String currentCity = String.format(Locale.getDefault(),
                                    "%s, %s", name, country);
                            Log.d(TAG, "MainActivity ServiceCityReceiver currentCity =" +
                                    currentCity);

                            CityCoordLab.setCurrentCity(currentCity, latitude, longitude);

                            // показываем погоду в городе с учётом ориентации экрана
                            showCityWhetherWithOrientation();
                        }
                    }
                }
            });
        }


    }
}

