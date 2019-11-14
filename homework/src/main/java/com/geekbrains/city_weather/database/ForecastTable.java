package com.geekbrains.city_weather.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.geekbrains.city_weather.adapter.DataForecast;

import java.util.ArrayList;
import java.util.Objects;

public class ForecastTable {
    private static final String TAG = "33333";

    private final static String TABLE_NAME = "forecast";
    private final static String COLUMN_ID= "_id";
    private final static String COLUMN_CITY = "city";
    private final static String COLUMN_TEMP = "temper";
    private final static String COLUMN_DATA_UPDATE = "dataUpdate";
    private final static String COLUMN_ICON = "iconCod";


    static void createTable(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CITY + " TEXT NOT NULL,"
                + COLUMN_TEMP + " TEXT NOT NULL,"
                + COLUMN_DATA_UPDATE + " TEXT NOT NULL,"
                + COLUMN_ICON + " TEXT NOT NULL);");
    }

    static void onUpgrade(SQLiteDatabase database) {
        //обновлять пока не собираюсь
    }

    //добавление массива данных пятидневного прогноза для  заданного города city
    public static void addCityForecast(DataForecast[] dataForecast, SQLiteDatabase database, String city) {

        for (int i = 0; i<dataForecast.length; i++){

            ContentValues values = new ContentValues();

            values.put(COLUMN_CITY, city);
            values.put(COLUMN_TEMP, dataForecast[i].getTemp());
            values.put(COLUMN_DATA_UPDATE, dataForecast[i].getDay());
            values.put(COLUMN_ICON, dataForecast[i].getWeatherIcon());

            database.insert(TABLE_NAME, null, values);
        }
    }

    //=================================== begin getAllCityIds  ================================
    //метод выбора списка городов
    public static ArrayList<Integer> getAllCityIds(SQLiteDatabase database) {
        Log.d(TAG, "ForecastTable getAllCityIds");
        String dataQuery = "SELECT " + COLUMN_ID
                + " FROM " + TABLE_NAME;
        Cursor cursor = database.rawQuery(dataQuery, null);
        return getIdsFromCursor(cursor);
    }

    //обработка курсора для метода вывода списка городов getAllCitys()
    private static ArrayList<Integer> getIdsFromCursor(Cursor cursor) {
        ArrayList<Integer> idList = null;
        //попали на первую запись, плюс вернулось true, если запись есть
        if(cursor != null && cursor.moveToFirst()) {
            idList = new ArrayList<>(cursor.getCount());

            do {
                idList.add(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            } while (cursor.moveToNext());
        }

        try { Objects.requireNonNull(cursor).close(); } catch (Exception ignored) {}
        return idList == null ? new ArrayList<Integer>(0) : idList;
    }
    //=================================== end getAllCityIds  ================================


    //замена массива строк с данными прогноза на 5 дней для заданного города cityToEdit
    public static void replaceCityForecast(String cityToEdit,
                                          DataForecast[] newDataForecast, SQLiteDatabase database) {
        Log.d(TAG, "ForecastTable replaceCityForecast");

        //сначала получаем массив id нужных нам строк для замены данных с городом cityToEdit
        ArrayList<Integer> list = getAllCityIds(database);
        int[] id = new int[list.size()];
        for(int i = 0; i < list.size(); i++){
            id[i] = list.get(i);
        }

        //потом перебираем эти строки и меняем данные
        for (int i = 0; i<newDataForecast.length; i++){
            ContentValues values = new ContentValues();
            values.put(COLUMN_TEMP, newDataForecast[i].getTemp());
            values.put(COLUMN_DATA_UPDATE, newDataForecast[i].getDay());
            values.put(COLUMN_ICON, newDataForecast[i].getWeatherIcon());

            database.update(TABLE_NAME, values,
                    COLUMN_CITY + " = ? " + " AND " + COLUMN_ID + " =? " ,
                    new String[]{cityToEdit, String.valueOf(id[i])});
        }
    }

    public static void deleteCityForecastByCity(String cityToDelete, SQLiteDatabase database) {

        database.delete(TABLE_NAME, COLUMN_CITY + " =? ", new String[]{cityToDelete});
    }

    public static void deleteAllDataFromCityForecast(SQLiteDatabase database) {

        database.delete(TABLE_NAME, null, null);
    }

}
