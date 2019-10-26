package com.geekbrains.city_weather.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.geekbrains.city_weather.R;

import androidx.annotation.Nullable;

public class CustomSlogan extends View {

    private static final String TAG = "33333";
    private Paint paint;
    private int radius = 100;
    private int color = Color.BLACK;
    //private boolean pressed = false;
    private View.OnClickListener listener;

    public CustomSlogan(Context context) {
        super(context);
        initView();
    }

    //если через макет, то нужен такой конструктор для добавления аттрибутов
    public CustomSlogan(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        initView();
    }

    //если через макет, то нужен такой конструктор для добавления аттрибутов и стилей
    public CustomSlogan(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        initView();
    }

    // Обработка параметров в xml
    private void initAttr(Context context, AttributeSet attrs){
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomView,
                0, 0);
        setRadius(typedArray.getInt(R.styleable.CustomView_cv_Radius, 50));
        setColor(typedArray.getColor(R.styleable.CustomView_cv_Color, Color.BLUE));
        typedArray.recycle();
    }

    private void initView(){
        Log.d(TAG, "Constructor");
        paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
    }

    public void setRadius(int radius){
        this.radius = radius;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        super.onDraw(canvas);

        canvas.drawCircle(radius, radius, (float)(radius/1.2), paint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) { // Нажали
            invalidate();           // Перерисовка элемента
            if (listener != null) listener.onClick(this);
        }
        return true;
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener lis)
    {
        listener = lis;
    }
}