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

public class CustomTemp extends View {

    private static final String TAG = "33333";
    private Paint paint;
    private int radius = 100;
    private int color = Color.BLACK;
    private boolean pressed = false;
    private View.OnClickListener listener;

    public CustomTemp(Context context) {
        super(context);
        initView();
    }

    //если через макет, то нужен такой конструктор для добавления аттрибутов
    public CustomTemp(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        initView();
    }

    //если через макет, то нужен такой конструктор для добавления аттрибутов и стилей
    public CustomTemp(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        paint.setStrokeWidth(10);
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

        canvas.drawCircle(radius, radius, radius, paint);
//        if(pressed) {
//            canvas.drawCircle(radius, radius, radius/10, paint);
//        } else {
//            canvas.drawCircle(radius, radius, radius, paint);
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) { // Нажали
            pressed = true;
            invalidate();           // Перерисовка элемента
            if (listener != null) listener.onClick(this);
        } else if(action == MotionEvent.ACTION_UP) { // Отпустили
            pressed = false;
            invalidate();           // Перерисовка элемента
        }
        return true;
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener lis)
    {
        listener = lis;
    }
}