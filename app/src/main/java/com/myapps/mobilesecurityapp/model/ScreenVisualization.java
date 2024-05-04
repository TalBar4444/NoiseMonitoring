package com.myapps.mobilesecurityapp.model;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ScreenVisualization extends View {
    private static final int MAX_AMPLITUDE = 100;
    private ColorScheme colorScheme;
    private int amplitudes;
    private int width,height;

    public ScreenVisualization(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        colorScheme = new ColorScheme();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w;
        this.height = h;
        this.amplitudes = this.height;
    }


    public void addAmplitude(int amplitude){
        invalidate();
        float scaledHeight = ((float) amplitude/MAX_AMPLITUDE) * (height -1);
        amplitudes = (int) (height - scaledHeight);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        colorScheme.shuffle();
        canvas.drawPaint(colorScheme.CanvasPaint);
        canvas.drawLine((float) width / 2, height, (float) width / 2, amplitudes, colorScheme.LinePaint); // Draw line based on amplitudes
    }
}
