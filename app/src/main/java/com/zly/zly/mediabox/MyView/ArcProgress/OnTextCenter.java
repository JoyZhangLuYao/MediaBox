package com.zly.zly.mediabox.MyView.ArcProgress;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by caizepeng on 16/9/8.
 */
public class OnTextCenter implements ArcProgress.OnCenterDraw {
    private int textColor = Color.parseColor("#99E4592C");
    private int textSize = 40;

    public OnTextCenter(int textColor, int textSize) {
        this.textColor = textColor;
        this.textSize = textSize;
    }
    public OnTextCenter(){
        super();
    }
    @Override
    public void draw(Canvas canvas, RectF rectF, float x, float y, float strokeWidth, int progress) {
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStrokeWidth(35);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        String progressStr = String.valueOf(progress+"％");
        float textX = x-(textPaint.measureText(progressStr)/2);
        float textY = y-((textPaint.descent()+textPaint.ascent())/2);
        canvas.drawText(progressStr,textX,textY,textPaint);
    }
}
