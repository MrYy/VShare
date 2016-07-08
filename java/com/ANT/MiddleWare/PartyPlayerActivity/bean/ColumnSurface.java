package com.ANT.MiddleWare.PartyPlayerActivity.bean;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by zxc on 2016/7/8.
 */
public class ColumnSurface extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = ColumnSurface.class.getSimpleName();
    private final SurfaceHolder sfh;
    private final Paint mPaint;
    private int screenH;
    private int screenW;
    private Thread th;
    private int columnW;
    private int marginW = 40;
    private Canvas canvas;
    //速度单位为 kbit/s，最高50MB带宽估计
    private int maxSpeed = 5000;

    public ColumnSurface(Context context) {
        this(context, null);
    }

    public ColumnSurface(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColumnSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sfh = getHolder();
        sfh.addCallback(this);
        mPaint = new Paint();
        setFocusable(true);
        th = new Thread(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenH = this.getHeight();
        screenW = this.getWidth();
        columnW = screenW * 1 / 12;
        Log.d(TAG, "screen height:" + String.valueOf(screenH));
        th.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {
        while (true) {
            canvas = sfh.lockCanvas();
            canvas.drawColor(Color.WHITE);
            mPaint.setStrokeWidth(3);
            mPaint.setColor(Color.BLACK);
            canvas.drawLine(marginW - 10, 0, marginW - 10, screenH, mPaint);
            canvas.drawLine(0, screenH - marginW + 10, 2 * marginW + 4 * columnW, screenH - marginW + 10, mPaint);
            float h = 0;
            for (int i = 0; i < 3; i++) {
                switch (i) {
                    case 0:
                        //3G下载
                        mPaint.setColor(Color.BLUE);
                        h = StatisticsFactory.getInstance(StatisticsFactory.Type.gReceive).getSpeed();
                        break;
                    case 1:
                        //wifi 下载
                        mPaint.setColor(Color.GREEN);
                        h = StatisticsFactory.getInstance(StatisticsFactory.Type.wifiReceive).getSpeed();
                        break;
                    case 2:
                        //wifi 上传
                        mPaint.setColor(Color.YELLOW);
                        h = StatisticsFactory.getInstance(StatisticsFactory.Type.wifiSend).getSpeed();
                        break;
                }
//                Log.d("TAG", "spped" + String.valueOf(h));
                canvas.drawRect(marginW + i * columnW, (1 - h / maxSpeed) * screenH - marginW, marginW + (i + 1) * columnW, screenH - marginW, mPaint);
            }
            sfh.unlockCanvasAndPost(canvas);
            try {
                TimeUnit.MILLISECONDS.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
