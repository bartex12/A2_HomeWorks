package com.geekbrains.city_weather.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.geekbrains.city_weather.R;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WeatherCardAdapter extends RecyclerView.Adapter<WeatherCardAdapter.CardViewHolder> {

    private ArrayList<DataForecast> dataForecast = new ArrayList<>();
    private Context context;

    public WeatherCardAdapter(Context context, ArrayList<DataForecast> data) {
        if (data != null) {
            dataForecast = data;
        }
        this.context = context;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_forecast, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.textViewDay.setText(dataForecast.get(position).day);
        holder.textViewIcon.setText(dataForecast.get(position).weatherIcon);
        holder.textViewTemper.setText(dataForecast.get(position).temp);
    }

    @Override
    public int getItemCount() {
        return dataForecast.size();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        TextView textViewDay;
        TextView textViewIcon;
        TextView textViewTemper;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDay = itemView.findViewById(R.id.textViewDay);
            textViewIcon = itemView.findViewById(R.id.textViewIcon);
            textViewTemper = itemView.findViewById(R.id.textViewTemper);
            Typeface weatherFont = Typeface.createFromAsset(
                    context.getAssets(), "fonts/weather.ttf");
            textViewIcon.setTypeface(weatherFont);
        }
    }
}
