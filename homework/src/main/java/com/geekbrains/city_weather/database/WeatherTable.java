package com.geekbrains.city_weather.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class WeatherTable {

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

    static void createTable(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CITY + " TEXT NOT NULL,"
                + COLUMN_COUNTRY + " TEXT NOT NULL,"
                + COLUMN_LAST_UPDATE + " TEXT NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT NOT NULL,"
                + COLUMN_WIND_SPEED + " REAL NOT NULL,"
                + COLUMN_PRESSURE + " REAL NOT NULL,"
                + COLUMN_TEMPERATURE + " REAL NOT NULL);");
    }

    static void onUpgrade(SQLiteDatabase database) {
        //обновлять пока не собираюсь
    }

    static void addCityWeather(DataWeather dataWeather, SQLiteDatabase database) {

        ContentValues values = new ContentValues();

        values.put(COLUMN_CITY, dataWeather.getCityName());
        values.put(COLUMN_COUNTRY, dataWeather.getCountry());
        values.put(COLUMN_LAST_UPDATE, dataWeather.getLastUpdate());
        values.put(COLUMN_DESCRIPTION, dataWeather.getDescription());
        values.put(COLUMN_WIND_SPEED, dataWeather.getWindSpeed());
        values.put(COLUMN_PRESSURE, dataWeather.getPressure());
        values.put(COLUMN_TEMPERATURE, dataWeather.getTemperature());
        values.put(COLUMN_ICON, dataWeather.getIconCod());

        database.insert(TABLE_NAME, null, values);
    }

    static void replaceCityWeather(String cityToEdit,
                                          DataWeather newDataWeather, SQLiteDatabase database) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_CITY, newDataWeather.getCityName());
        values.put(COLUMN_COUNTRY, newDataWeather.getCountry());
        values.put(COLUMN_LAST_UPDATE, newDataWeather.getLastUpdate());
        values.put(COLUMN_DESCRIPTION, newDataWeather.getDescription());
        values.put(COLUMN_WIND_SPEED, newDataWeather.getWindSpeed());
        values.put(COLUMN_PRESSURE, newDataWeather.getPressure());
        values.put(COLUMN_TEMPERATURE, newDataWeather.getTemperature());
        values.put(COLUMN_ICON, newDataWeather.getIconCod());

        database.update(TABLE_NAME, values, COLUMN_CITY + "=" + cityToEdit, null);
    }

    static void deleteCityWeather(String cityToDelete, SQLiteDatabase database) {
        database.delete(TABLE_NAME, COLUMN_CITY + " = " + cityToDelete, null);
    }

    static void deleteAllDataFromCityWeather(SQLiteDatabase database) {
        database.delete(TABLE_NAME, null, null);
    }


    //этот метод для вывода погоды по заданному городу city
    static DataWeather getOneCityWeatherLine(SQLiteDatabase database, String city) {

        String dataQuery = "SELECT  * FROM " + TABLE_NAME +  " WHERE " + COLUMN_CITY + " = " + city;
        Cursor cursor = database.rawQuery(dataQuery, null);
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
        float windSpeed = cursor.getFloat(cursor.getColumnIndex(COLUMN_WIND_SPEED));
        float pressure = cursor.getFloat(cursor.getColumnIndex(COLUMN_PRESSURE));
        float temperature = cursor.getFloat(cursor.getColumnIndex(COLUMN_TEMPERATURE));
        int iconCod = cursor.getInt(cursor.getColumnIndex(COLUMN_ICON));

        //создаём экземпляр класса DataWeather в конструкторе
        return new DataWeather(id, cityName, country,
                lastUpdate, description, windSpeed,pressure,temperature,iconCod);
    }

    //этот метод возможно пригодится для вывода статистики  по городам
    static List<DataWeather> getAllCityWeatherLines(SQLiteDatabase database) {

        String dataQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = database.rawQuery(dataQuery, null);
        return getResultFromWeatherCursor(cursor);
    }

    //получаем список объектов DataWeather
    private static List<DataWeather> getResultFromWeatherCursor(Cursor cursor) {
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
