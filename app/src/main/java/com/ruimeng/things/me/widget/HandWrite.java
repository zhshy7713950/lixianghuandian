package com.ruimeng.things.me.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import wongxd.utils.utilcode.util.ScreenUtils;

public class HandWrite extends View {
    Paint paint = null;
    Bitmap originalBitmap = null;
    Bitmap new1_Bitmap = null;
    Bitmap new2_Bitmap = null;
    float startX = 0, startY = 0;
    float clickX = 0, clickY = 0;
    boolean isMove = true;
    boolean isClear = false;
    int color = Color.BLUE;
    float strokeWidth = 10.0f;

    public HandWrite(Context context, AttributeSet attrs) {
        super(context, attrs);
        //生成纯白的bitmap
        ColorDrawable drawable = new ColorDrawable(Color.parseColor("#AA0000"));
        Bitmap bitmap = Bitmap.createBitmap(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenWidth(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);

        originalBitmap = bitmap;
        new1_Bitmap = Bitmap.createBitmap(originalBitmap);
    }

    public void clear() {
        isClear = true;
        new2_Bitmap = Bitmap.createBitmap(originalBitmap);
        invalidate();
    }

    public void black() {
        isClear = false;
        strokeWidth = 10.0f;
        color = Color.BLACK;
    }

    public void red() {
        isClear = false;
        strokeWidth = 10.0f;
        color = Color.RED;
    }

    public void blue() {
        isClear = false;
        strokeWidth = 10.0f;
        color = Color.BLUE;
    }

    public void brush() {
        strokeWidth = 20.0f;
    }

    public void eraser() {
        color = Color.WHITE;
        strokeWidth = 80.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(HandWriting(new1_Bitmap), 0, 0, null);
    }

    public Bitmap HandWriting(Bitmap o_Bitmap) {
        Canvas canvas = null;
        if (isClear) {
            canvas = new Canvas(new2_Bitmap);
        } else {
            canvas = new Canvas(o_Bitmap);
        }
        paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        if (isMove) {
            canvas.drawLine(startX, startY, clickX, clickY, paint);
        }
        startX = clickX;
        startY = clickY;
        if (isClear) {
            return new2_Bitmap;
        }
        return o_Bitmap;
    }


    /**
     * 对View进行截图
     */
    public static Bitmap doViewSnapshot(View view) {
        //使控件可以进行缓存
        view.setDrawingCacheEnabled(true);
        //获取缓存的 Bitmap
        Bitmap drawingCache = view.getDrawingCache();
        //复制获取的 Bitmap
        drawingCache = Bitmap.createBitmap(drawingCache);
        //关闭视图的缓存
        view.setDrawingCacheEnabled(false);

        return drawingCache;
    }


    /**
     * 对View进行截图
     */
    public Bitmap doSnapshot() {
        //使控件可以进行缓存
        this.setDrawingCacheEnabled(true);
        //获取缓存的 Bitmap
        Bitmap drawingCache = this.getDrawingCache();
        //复制获取的 Bitmap
        drawingCache = Bitmap.createBitmap(drawingCache);
        //关闭视图的缓存
        this.setDrawingCacheEnabled(false);

        return drawingCache;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        clickX = event.getX();
        clickY = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isMove = false;
            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            isMove = true;
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }
}