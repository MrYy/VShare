package com.ANT.MiddleWare.PartyPlayerActivity.util;

import android.graphics.Canvas;

public interface IDanmukuItem {
    void doDraw(Canvas canvas);

    void setTextSize(int sizeInDip);

    void setTextColor(int colorResId);

    void setStartPosition(int x, int y);

    void setSpeedFactor(float factor);

    float getSpeedFactor();

    boolean isOut();

    boolean willHit(IDanmukuItem runningItem);

    void release();

    int getWidth();

    int getHeight();

    int getCurrX();

    int getCurrY();
}
