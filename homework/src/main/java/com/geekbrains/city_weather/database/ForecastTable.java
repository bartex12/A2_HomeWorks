package com.geekbrains.city_weather.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.geekbrains.city_weather.adapter.DataForecastNew;

import java.util.ArrayList;
import java.util.Objects;

public class ForecastTable {
    private static final String TAG = "33333";

    private final static String TABLE_NAME = "forecast";
    private final static String COLUMN_ID= "_id";
    private final static String COLUMN_CITY = "city";
    private final static String COLUMN_DESCRIPTION = "description";
    private final static String COLUMN_TEMP = "temper";
    private final static String COLUMN_DATA_UPDATE = "dataUpdate";
    private final static String COLUMN_ICON = "iconCod";

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
    public static void addCityForecast(DataForecastNew[] dataForecast, SQLiteDatabase database, String city) {

        for (DataForecastNew forecast : dataForecast) {
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
                                           DataForecastNew[] newDataForecast, SQLiteDatabase database) {
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
            values.put(COLUMN_DESCRIPTION, newDataForecast[i].getDescriptionNew());
            values.put(COLUMN_TEMP, newDataForecast[i].getTempNew());
            values.put(COLUMN_DATA_UPDATE, newDataForecast[i].getDayNew());
            values.put(COLUMN_ICON, newDataForecast[i].getIconCodNew());

            database.update(TABLE_NAME, values,
                    COLUMN_CITY + " = ? " + " AND " + COLUMN_ID + " =? " ,
                    new String[]{cityToEdit, String.valueOf(id[i])});
        }
    }

    //удаление прогноза на 5 дней для города с именем cityToDelete
    public static void deleteCityForecastByCity(String cityToDelete, SQLiteDatabase database) {

        database.delete(TABLE_NAME, COLUMN_CITY + " =? ", new String[]{cityToDelete});
    }

    //удаление пвсех записей из таблицы прогноза на 5 дней
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
    //метод выбора списка id
    private static ArrayList<Integer> getAllCityIds(SQLiteDatabase database) {
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

    //=================================== begin getAllCityTemper  ================================
    //метод выбора массива описаний для выбранного города
    public static String[] getAllCityDescription(SQLiteDatabase database, String city) {
        Log.d(TAG, "ForecastTable getAllCityDescription");
        String dataQuery = "SELECT " + COLUMN_DESCRIPTION
                + " FROM " + TABLE_NAME + " WHERE " + COLUMN_CITY + " = ? ";
        Cursor cursor = database.rawQuery(dataQuery, new String[]{city});
        return getDescriptionFromCursor(cursor);
    }

    //обработка курсора для метода вывода списка описаний getAllCityDescription()
    private static String[] getDescriptionFromCursor(Cursor cursor) {
        String[] descr = null;
        //попали на первую запись, плюс вернулось true, если запись есть
        if (cursor != null && cursor.moveToFirst()) {
            descr = new String[cursor.getCount()];

            do {
                int position = cursor.getPosition();
                descr[position] = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
            } while (cursor.moveToNext());
        }

        try {
            Objects.requireNonNull(cursor).close();
        } catch (Exception ignored) {
        }
        return descr;
    }
    //=================================== end getAllCityTemper  ================================


    //=================================== begin getAllCityTemper  ================================
    //метод выбора массива температур для выбранного города
    public static String[] getAllCityTemper(SQLiteDatabase database, String city) {
        Log.d(TAG, "ForecastTable getAllCityTemper");
        String dataQuery = "SELECT " + COLUMN_TEMP
                + " FROM " + TABLE_NAME +  " WHERE " + COLUMN_CITY + " = ? ";
        Cursor cursor = database.rawQuery(dataQuery, new String[]{city});
        return getTempersFromCursor(cursor);
    }

    //обработка курсора для метода вывода списка городов getAllCitys()
    private static String[] getTempersFromCursor(Cursor cursor) {
        String[] tempers = null;
        //попали на первую запись, плюс вернулось true, если запись есть
        if(cursor != null && cursor.moveToFirst()) {
            tempers = new String[cursor.getCount()];

            do {
                int position = cursor.getPosition();
                tempers[position] = cursor.getString(cursor.getColumnIndex(COLUMN_TEMP));
            } while (cursor.moveToNext());
        }

        try { Objects.requireNonNull(cursor).close(); } catch (Exception ignored) {}
        return tempers;
    }
    //=================================== end getAllCityTemper  ================================

    //=================================== begin getAllCityDays  ================================
    //метод выбора массива дат прогноза для выбранного города
    public static String[] getAllCityDays(SQLiteDatabase database, String city) {
        Log.d(TAG, "ForecastTable getAllCityDays");
        String dataQuery = "SELECT " + COLUMN_DATA_UPDATE
                + " FROM " + TABLE_NAME +  " WHERE " + COLUMN_CITY + " = ? ";
        Cursor cursor = database.rawQuery(dataQuery, new String[]{city});
        return getDaysFromCursor(cursor);
    }

    //обработка курсора для метода вывода списка городов getAllCitys()
    private static String[] getDaysFromCursor(Cursor cursor) {
        String[] days = null;
        //попали на первую запись, плюс вернулось true, если запись есть
        if(cursor != null && cursor.moveToFirst()) {
            days = new String[cursor.getCount()];

            do {
                int position = cursor.getPosition();
                days[position] = cursor.getString(cursor.getColumnIndex(COLUMN_DATA_UPDATE));
            } while (cursor.moveToNext());
        }

        try { Objects.requireNonNull(cursor).close(); } catch (Exception ignored) {}
        return days;
    }
    //=================================== end getAllCityDays  ================================

    //=================================== begin getAllCityIcons  ================================
    //метод выбора массива иконок  прогноза для выбранного города
    public static String[] getAllCityIcons(SQLiteDatabase database, String city) {
        Log.d(TAG, "ForecastTable getAllCityIcons");
        String dataQuery = "SELECT " + COLUMN_ICON
                + " FROM " + TABLE_NAME +  " WHERE " + COLUMN_CITY + " = ? ";
        Cursor cursor = database.rawQuery(dataQuery, new String[]{city});
        return getIconsFromCursor(cursor);
    }

    //обработка курсора для метода вывода списка городов getAllCitys()
    private static String[] getIconsFromCursor(Cursor cursor) {
        String[] icons = null;
        //попали на первую запись, плюс вернулось true, если запись есть
        if(cursor != null && cursor.moveToFirst()) {
            icons = new String[cursor.getCount()];

            do {
                int position = cursor.getPosition();
                icons[position] = cursor.getString(cursor.getColumnIndex(COLUMN_ICON));
            } while (cursor.moveToNext());
        }

        try { Objects.requireNonNull(cursor).close(); } catch (Exception ignored) {}
        return icons;
    }
    //=================================== end getAllCityIcons  ================================
}
