package com.geekbrains.city_weather.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WeatherTable {

    private static final String TAG = "33333";

    private final static String TABLE_NAME = "weather";
    private final static String COLUMN_ID = "_id";
    private final static String COLUMN_CITY = "cityName";
    private final static String COLUMN_COUNTRY = "country";
    private final static String COLUMN_LAST_UPDATE = "lastUpdate";
    private final static String COLUMN_DESCRIPTION = "description";
    private final static String COLUMN_WIND_SPEED = "windSpeed";
    private final static String COLUMN_PRESSURE = "pressure";
    private final static String COLUMN_TEMPERATURE = "temperature";
    private final static String COLUMN_ICON = "iconCod";
    private final static String COLUMN_UPDATE_SEC = "updateSec";

    static void createTable(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CITY + " TEXT NOT NULL,"
                + COLUMN_COUNTRY + " TEXT NOT NULL,"
                + COLUMN_LAST_UPDATE + " TEXT NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT NOT NULL,"
                + COLUMN_WIND_SPEED + " TEXT NOT NULL,"
                + COLUMN_PRESSURE + " TEXT NOT NULL,"
                + COLUMN_TEMPERATURE + " TEXT NOT NULL,"
                + COLUMN_ICON + " TEXT NOT NULL,"
                + COLUMN_UPDATE_SEC + " REAL NOT NULL DEFAULT 0);");
    }

    static void onUpgrade(SQLiteDatabase database) {
        //обновлять пока не собираюсь
    }

    public static void addCityWeather(DataWeather dataWeather, SQLiteDatabase database) {

        ContentValues values = new ContentValues();

        values.put(COLUMN_CITY, dataWeather.getCityName());
        values.put(COLUMN_COUNTRY, dataWeather.getCountry());
        values.put(COLUMN_LAST_UPDATE, dataWeather.getLastUpdate());
        values.put(COLUMN_DESCRIPTION, dataWeather.getDescription());
        values.put(COLUMN_WIND_SPEED, dataWeather.getWindSpeed());
        values.put(COLUMN_PRESSURE, dataWeather.getPressure());
        values.put(COLUMN_TEMPERATURE, dataWeather.getTemperature());
        values.put(COLUMN_ICON, dataWeather.getIconCod());
        values.put(COLUMN_UPDATE_SEC, dataWeather.getUpdateSec());

        database.insert(TABLE_NAME, null, values);
    }

    public static void replaceCityWeather(String cityToEdit,
                                          DataWeather newDataWeather, SQLiteDatabase database) {
        Log.d(TAG, "WeatherTable replaceCityWeather");
        ContentValues values = new ContentValues();

        values.put(COLUMN_CITY, newDataWeather.getCityName());
        values.put(COLUMN_COUNTRY, newDataWeather.getCountry());
        values.put(COLUMN_LAST_UPDATE, newDataWeather.getLastUpdate());
        values.put(COLUMN_DESCRIPTION, newDataWeather.getDescription());
        values.put(COLUMN_WIND_SPEED, newDataWeather.getWindSpeed());
        values.put(COLUMN_PRESSURE, newDataWeather.getPressure());
        values.put(COLUMN_TEMPERATURE, newDataWeather.getTemperature());
        values.put(COLUMN_ICON, newDataWeather.getIconCod());
        values.put(COLUMN_UPDATE_SEC, newDataWeather.getUpdateSec());


        int updateNumber = database.update(TABLE_NAME, values,
                COLUMN_CITY + " = ? ",
                            new String[]{cityToEdit});

        Log.d(TAG, "WeatherTable replaceCityWeather updateNumber = " + updateNumber);
        Log.d(TAG, "WeatherTable replaceCityWeather long = "
                + getLastUpdate(database, cityToEdit));
    }

    public static void deleteCityWeather(String cityToDelete, SQLiteDatabase database) {
        database.delete(TABLE_NAME, COLUMN_CITY + " = " + cityToDelete, null);
    }

    public static void deleteAllDataFromCityWeather(SQLiteDatabase database) {
        database.delete(TABLE_NAME, null, null);
    }

    //******************************  begin getLastUpdate  *********************************************
    //метод для выбора из базы данных времени последнего обновления погоды для города city
    public static long getLastUpdate(SQLiteDatabase database, String city) {
        Log.d(TAG, "WeatherTable getLastUpdate");
        String dataQuery = "SELECT " + COLUMN_UPDATE_SEC
                         + " FROM " + TABLE_NAME
                         +  " WHERE " + COLUMN_CITY + " = ? " ;
        Cursor cursor = database.rawQuery(dataQuery, new String[]{city});
        return getLastUpdateFromCursor(cursor);
    }

    private static long getLastUpdateFromCursor(Cursor cursor){
        long updateSec = -1;

        if(cursor != null && cursor.moveToFirst()) {
            updateSec = cursor.getLong(cursor.getColumnIndex(COLUMN_UPDATE_SEC));
            Log.d(TAG, "WeatherTable getLastUpdateFromCursor updateSec = " + updateSec);
        }
        try { cursor.close(); } catch (Exception ignored) {}
        return updateSec;
    }
    //************************************* end  getLastUpdate  ************************************

    //=================================== begin getAllCitys  ================================
    //метод выбора списка городов
     public static ArrayList<String> getAllCitys(SQLiteDatabase database) {
         Log.d(TAG, "WeatherTable getAllCitys");
        String dataQuery = "SELECT " + COLUMN_CITY
                        + " FROM " + TABLE_NAME;
        Cursor cursor = database.rawQuery(dataQuery, null);
        return getCityFromCursor(cursor);
    }

    //обработка курсора для метода вывода списка городов getAllCitys()
    private static ArrayList<String> getCityFromCursor(Cursor cursor) {
        ArrayList<String> cityList = null;
        //попали на первую запись, плюс вернулось true, если запись есть
        if(cursor != null && cursor.moveToFirst()) {
            cityList = new ArrayList<>(cursor.getCount());

            do {
                cityList.add(cursor.getString(cursor.getColumnIndex(COLUMN_CITY)));
            } while (cursor.moveToNext());
        }

        try { cursor.close(); } catch (Exception ignored) {}
        return cityList == null ? new ArrayList<String>(0) : cityList;
    }
    //=================================== end getAllCitys  ================================

    //++++++++++++++++++++++++++++++++++ begin getOneCityWeatherLine +++++++++++++++++++++++++++
    //этот метод для вывода погоды по заданному городу city
    public static DataWeather getOneCityWeatherLine(SQLiteDatabase database, String city) {
        Log.d(TAG, "WeatherTable getOneCityWeatherLine");
        String dataQuery = "SELECT  * FROM " + TABLE_NAME
                        +  " WHERE " + COLUMN_CITY + " = ? " ;
        Cursor cursor = database.rawQuery(dataQuery, new String[]{city});
        return getCityWeatherFromWeatherCursor(cursor);
    }

    //получаем объект DataWeather с погодными данными по городу
    private static DataWeather getCityWeatherFromWeatherCursor(Cursor cursor) {

        DataWeather dataWeather = null;

        if(cursor != null && cursor.moveToFirst()) {
            dataWeather = getWeatherDataFromCurcor(cursor);
            Log.d(TAG, "WeatherTable getCityWeatherFromWeatherCursor dataWeather = " + dataWeather);
        }
        try { cursor.close(); } catch (Exception ignored) {}
        return dataWeather;
    }

    //получаем объект DataWeather из курсора
    private static DataWeather getWeatherDataFromCurcor(Cursor cursor) {
        // Узнаем индекс каждого столбца и Используем индекс для получения строки
        long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
        String cityName = cursor.getString(cursor.getColumnIndex(COLUMN_CITY));
        String country = cursor.getString(cursor.getColumnIndex(COLUMN_COUNTRY));
        String lastUpdate = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_UPDATE));
        String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
        String windSpeed = cursor.getString(cursor.getColumnIndex(COLUMN_WIND_SPEED));
        String pressure = cursor.getString(cursor.getColumnIndex(COLUMN_PRESSURE));
        String temperature = cursor.getString(cursor.getColumnIndex(COLUMN_TEMPERATURE));
        String iconCod = cursor.getString(cursor.getColumnIndex(COLUMN_ICON));
        long updateSec = cursor.getLong(cursor.getColumnIndex(COLUMN_UPDATE_SEC));

        //создаём экземпляр класса DataWeather в конструкторе
        return new DataWeather(id, cityName, country,
                lastUpdate, description, windSpeed,pressure,temperature,iconCod,updateSec);
    }
    //++++++++++++++++++++++++++++++++++ end getOneCityWeatherLine +++++++++++++++++++++++++++

    //этот метод возможно пригодится для вывода статистики  по городам
    public static List<DataWeather> getAllCityWeatherLines(SQLiteDatabase database) {

        String dataQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = database.rawQuery(dataQuery, null);
        return getResultFromWeatherCursor(cursor);
    }

    //получаем список объектов DataWeather
    private static List<DataWeather> getResultFromWeatherCursor(Cursor cursor) {
        Log.d(TAG, "WeatherTable getResultFromWeatherCursor");
        //список объектов с погодными данными DataWeather
        List<DataWeather> listOfDataWeather = null;

        if(cursor != null && cursor.moveToFirst()) {
            listOfDataWeather = new ArrayList<>(cursor.getCount());
            do {
                DataWeather dataWeather = getWeatherDataFromCurcor(cursor);
                //добавляем в список
                listOfDataWeather.add(dataWeather);

            } while (cursor.moveToNext());
            Log.d(TAG, "WeatherTable getResultFromWeatherCursor cursor.getCount() = " + cursor.getCount());
        }

        try { cursor.close(); } catch (Exception ignored) {}
        return listOfDataWeather == null ? new ArrayList<DataWeather>(0) : listOfDataWeather;
    }
}
