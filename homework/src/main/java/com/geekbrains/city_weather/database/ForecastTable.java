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
    public final static String COLUMN_DESCRIPTION = "description";
    public final static String COLUMN_TEMP = "temper";
    public final static String COLUMN_DATA_UPDATE = "dataUpdate";
    public final static String COLUMN_ICON = "iconCod";

    static void createTable(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CITY + " TEXT NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT NOT NULL,"
                + COLUMN_TEMP + " TEXT NOT NULL,"
                + COLUMN_DATA_UPDATE + " TEXT NOT NULL,"
                + COLUMN_ICON + " TEXT NOT NULL);");
    }

    static void onUpgrade(SQLiteDatabase database) {
        //обновлять пока не собираюсь
    }

    //добавление массива данных пятидневного прогноза для  заданного города city
    public static void addCityForecast(DataForecast[] dataForecast, SQLiteDatabase database, String city) {

        for (DataForecast forecast : dataForecast) {
            ContentValues values = new ContentValues();

            values.put(COLUMN_CITY, city);
            values.put(COLUMN_DESCRIPTION, forecast.getDescriptionNew());
            values.put(COLUMN_TEMP, forecast.getTempNew());
            values.put(COLUMN_DATA_UPDATE, forecast.getDayNew());
            values.put(COLUMN_ICON, forecast.getIconCodNew());

            database.insert(TABLE_NAME, null, values);
        }
    }

    //замена массива строк с данными прогноза на 5 дней для заданного города cityToEdit
    public static void replaceCityForecast(String cityToEdit,
                                           DataForecast[] newDataForecast, SQLiteDatabase database) {
        Log.d(TAG, "ForecastTable replaceCityForecast");

        //сначала получаем массив id нужных нам строк для замены данных с городом cityToEdit
        ArrayList<Integer> list = getAllCityIds(database, cityToEdit);
        int[] id = new int[list.size()];
        for(int i = 0; i < list.size(); i++){
            id[i] = list.get(i);
        }
        Log.d(TAG, "ForecastTable replaceCityForecast всего строк = " + id.length);

        //потом перебираем эти строки и меняем данные
        for (int i = 0; i<newDataForecast.length; i++){
            ContentValues values = new ContentValues();
            values.put(COLUMN_DESCRIPTION, newDataForecast[i].getDescriptionNew());
            values.put(COLUMN_TEMP, newDataForecast[i].getTempNew());
            values.put(COLUMN_DATA_UPDATE, newDataForecast[i].getDayNew());
            values.put(COLUMN_ICON, newDataForecast[i].getIconCodNew());

            database.update(TABLE_NAME, values, COLUMN_ID + " = ? ",
                    new String[]{String.valueOf(id[i])});
        }
    }

    //удаление прогноза на 5 дней для города с именем cityToDelete
    public static void deleteCityForecastByCity(String cityToDelete, SQLiteDatabase database) {

        database.delete(TABLE_NAME, COLUMN_CITY + " =? ", new String[]{cityToDelete});
    }

    //удаление всех записей из таблицы прогноза на 5 дней
    public static void deleteAllDataFromCityForecast(SQLiteDatabase database) {

        database.delete(TABLE_NAME, null, null);
    }

    //=================================== begin getAllCitysFromForecast  ================================
    //метод выбора списка городов
    public static ArrayList<String> getAllCitysFromForecast(SQLiteDatabase database) {
        Log.d(TAG, "ForecastTable getAllCitysFromForecast");
        String dataQuery = "SELECT DISTINCT " + COLUMN_CITY
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
        try { Objects.requireNonNull(cursor).close(); } catch (Exception ignored) {}
        return cityList == null ? new ArrayList<String>(0) : cityList;
    }
    //=================================== end getAllCitysFromForecast  ================================

    //=================================== begin getAllCityIds  ================================
    //метод выбора списка id для города city
    private static ArrayList<Integer> getAllCityIds(SQLiteDatabase database, String city) {
        Log.d(TAG, "ForecastTable getAllCityIds");
        String dataQuery = "SELECT " + COLUMN_ID
                + " FROM " + TABLE_NAME + " WHERE " + COLUMN_CITY + " = ? ";
        Cursor cursor = database.rawQuery(dataQuery, new String[]{city});
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

    //=================================== begin getArrayElementsForCityForecast  ================================
    //метод выбора массива элементов прогноза для выбранного города
    public static String[] getArrayElementsForCityForecast(SQLiteDatabase database,
                                                           String city, String column) {
        Log.d(TAG, "ForecastTable getArrayElementsForCityForecast");
        String dataQuery = "SELECT " + column
                + " FROM " + TABLE_NAME + " WHERE " + COLUMN_CITY + " = ? ";
        Cursor cursor = database.rawQuery(dataQuery, new String[]{city});
        return getElementsFromCursor(cursor, column);
    }

    //обработка курсора для метода выбора массива элементов прогноза для выбранного города
    private static String[] getElementsFromCursor(Cursor cursor, String column) {
        String[] elements = null;
        //попали на первую запись, плюс вернулось true, если запись есть
        if (cursor != null && cursor.moveToFirst()) {
            elements = new String[cursor.getCount()];

            do {
                elements[cursor.getPosition()] = cursor.getString(cursor.getColumnIndex(column));
            } while (cursor.moveToNext());
        }
        try {
            Objects.requireNonNull(cursor).close();
        } catch (Exception ignored) {
        }
        return elements;
    }
    //=================================== end getArrayElementsForCityForecast  ================================
}
