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


    //=================================== begin getAllCityTemper  ================================
    //метод выбора массива температур для выбранного города
    public static String[] getAllCityTemper(SQLiteDatabase database, String city) {
        Log.d(TAG, "ForecastTable getAllCityTemper");
        String dataQuery = "SELECT " + COLUMN_TEMP
                + " FROM " + TABLE_NAME +  " WHERE " + COLUMN_CITY + " = ? " ;;
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
                + " FROM " + TABLE_NAME +  " WHERE " + COLUMN_CITY + " = ? " ;;
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
                + " FROM " + TABLE_NAME +  " WHERE " + COLUMN_CITY + " = ? " ;;
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


    //++++++++++++++++++++++++++++++++++ begin getOneCityForecast5Days +++++++++++++++++++++++++++
    //этот метод для вывода пятидневного прогноза погоды по заданному городу city
    public static DataForecast5Days[] getOneCityForecast5Days(SQLiteDatabase database, String city) {
        Log.d(TAG, "ForecastTable getOneCityForecast5Days");
        String dataQuery = "SELECT  * FROM " + TABLE_NAME
                +  " WHERE " + COLUMN_CITY + " = ? " ;
        Cursor cursor = database.rawQuery(dataQuery, new String[]{city});
        return getCityForecastFromForecastCursor(cursor);
    }

    //получаем массив объектов DataForecast5Days с погодными данными по городу
    private static DataForecast5Days[] getCityForecastFromForecastCursor(Cursor cursor) {
        Log.d(TAG, "ForecastTable getCityForecastFromForecastCursor size =" + cursor.getCount());

        DataForecast5Days[] dataForecast = null;

        if(cursor != null && cursor.moveToFirst()) {
            dataForecast = new DataForecast5Days[cursor.getCount()];
            do {
                int position = cursor.getPosition();
                dataForecast[position] = getForecastDataFromCurcor(cursor);
            }while (cursor.moveToNext());
        }
        try { Objects.requireNonNull(cursor).close(); } catch (Exception ignored) {}
        return dataForecast;
    }

    //получаем объект DataWeather из курсора
    private static DataForecast5Days getForecastDataFromCurcor(Cursor cursor) {
        // Узнаем индекс каждого столбца и Используем индекс для получения строки
        long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
        String cityName = cursor.getString(cursor.getColumnIndex(COLUMN_CITY));
        String temper = cursor.getString(cursor.getColumnIndex(COLUMN_TEMP));
        String lastUpdate = cursor.getString(cursor.getColumnIndex(COLUMN_DATA_UPDATE));
        String iconCod = cursor.getString(cursor.getColumnIndex(COLUMN_ICON));

        //создаём экземпляр класса DataWeather в конструкторе
        return new DataForecast5Days(id, cityName, temper, lastUpdate, iconCod);
    }
    //++++++++++++++++++++++++++++++++++ end getOneCityForecast5Days +++++++++++++++++++++++++++




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
