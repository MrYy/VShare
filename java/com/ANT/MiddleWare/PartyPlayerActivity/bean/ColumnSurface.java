package com.ANT.MiddleWare.PartyPlayerActivity.bean;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ANT.MiddleWare.PartyPlayerActivity.util.StatisticsActivity;

import java.util.concurrent.TimeUnit;

/**
 * Created by zxc on 2016/7/8.
 */
public class ColumnSurface extends SurfaceView implements SurfaceHolder.Callback {
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
    private RectF rectf;
    private int rectMargin;

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
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenH = this.getHeight();
        screenW = this.getWidth();
        columnW = screenW * 1 / 12;
        final float scale = getResources().getDisplayMetrics().density;
        Log.d(TAG, "screen height:" + String.valueOf(screenH));
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    canvas = sfh.lockCanvas();
                    if (canvas == null) {
                        Thread.yield();
                        break;
                    }
                    canvas.drawColor(Color.WHITE);
                    mPaint.setStrokeWidth(3);
                    mPaint.setColor(Color.BLACK);
                    canvas.drawLine(marginW - 10, 0, marginW - 10, screenH, mPaint);
                    canvas.drawLine(0, screenH - marginW + 10, screenW, screenH - marginW + 10, mPaint);
                    float h = 0;
                    int interval = 25;
                    for (int i = 0; i < 3; i++) {
                        Message msg = new Message();
                        switch (i) {
                            case 0:
                                //3G下载
                                mPaint.setColor(Color.BLUE);
                                h = StatisticsFactory.getInstance(StatisticsFactory.Type.gReceive).getSpeed();
                                msg.what = StatisticsActivity.gR;
                                break;
                            case 1:
                                //wifi 下载
                                mPaint.setColor(Color.GREEN);
                                h = StatisticsFactory.getInstance(StatisticsFactory.Type.wifiReceive).getSpeed();
                                msg.what = StatisticsActivity.wR;
                                break;
                            case 2:
                                //wifi 上传
                                mPaint.setColor(Color.YELLOW);
                                h = StatisticsFactory.getInstance(StatisticsFactory.Type.wifiSend).getSpeed();
                                msg.what = StatisticsActivity.wS;
                                break;
                        }
                        msg.arg1 = (int) h;
                        StatisticsActivity.mHandler.sendMessage(msg);
                        rectMargin = 100;
                        canvas.drawRect(rectMargin + i * columnW + i*interval, (1 - h / maxSpeed) * screenH - marginW, rectMargin + (i + 1) * columnW+i*interval, screenH - marginW, mPaint);
                    }
                    mPaint.reset();
                    String text = "传输速度/kbps";
                    mPaint.setStrokeWidth(3);
                    mPaint.setTextSize(18*scale);
                    mPaint.setAntiAlias(true);
                    canvas.drawText(text,marginW+10,(int) (40 * scale + 0.5f),mPaint);
                    sfh.unlockCanvasAndPost(canvas);
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


}
