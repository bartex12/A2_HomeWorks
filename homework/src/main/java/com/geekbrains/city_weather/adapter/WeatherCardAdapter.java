package com.geekbrains.city_weather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.geekbrains.city_weather.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WeatherCardAdapter extends RecyclerView.Adapter<WeatherCardAdapter.CardViewHolder> {

    private ArrayList<DataForecastNew> dataForecast = new ArrayList<>();
    private Context context;

    public WeatherCardAdapter(Context context, ArrayList<DataForecastNew> data) {
        if (data != null) {
            dataForecast = data;
        }
        this.context = context;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_forecast_new, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.textViewDescr.setText(dataForecast.get(position).getDescriptionNew());
        holder.textViewTemper.setText(dataForecast.get(position).getTempNew());
        holder.textViewDay.setText(dataForecast.get(position).getDayNew());
        holder.imageView.setImageDrawable(dataForecast.get(position).getIconDraw());
    }

    @Override
    public int getItemCount() {
        return dataForecast.size();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        TextView textViewDescr;
        TextView textViewTemper;
        TextView textViewDay;
        ImageView imageView;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescr = itemView.findViewById(R.id.textViewDescrNew);
            textViewTemper = itemView.findViewById(R.id.textViewTemperNew);
            textViewDay = itemView.findViewById(R.id.textViewDayNew);
            imageView = itemView.findViewById(R.id.imageViewIcon);
        }
    }
}
