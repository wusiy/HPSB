package com.sunland.cpocr.view;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.sunland.cpocr.R;


public final class LPRfinderView extends View {
    private static final long ANIMATION_DELAY = 50;
    private final Paint paint;
    private final Paint paintLine;
    private final int maskColor;
    private final int frameColor;
    private final int laserColor;
    private int ShrinkSize;
    private Paint mTextPaint;
    private String mText;
    private Rect frame;

    int w, h;
    private boolean mSerial = false;


    public int getShrinkSize() {
        return ShrinkSize;
    }

    public void setShrinkSize(int shrinkSize) {
        ShrinkSize = shrinkSize;
    }

    public LPRfinderView(Context context, int w, int h) {
        this(context, w, h, false);
    }

    public LPRfinderView(Context context, int w, int h, boolean serial) {
        super(context);
        this.w = w;
        this.h = h;
        this.mSerial = serial;
        paint = new Paint();
        paintLine = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        frameColor = resources.getColor(R.color.viewfinder_frame);
        laserColor = resources.getColor(R.color.viewfinder_laser);
    }


    @Override
    public void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int t;
        int b;
        int l;
        int r;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            l = 10;
            r = width - 10;
            int ntmpH = (r - l) * 58 / 88;
            if (mSerial)
                t = 10;
            else
                t = (height - ntmpH) / 2;
            b = t + ntmpH;
        } else {
            t = height * 1 / 5;
            b = height * 4 / 5;
            int ntmpW = (b - t) * 88 / 68;
            if (mSerial) {
                l = 10;
                r = l + ntmpW;
            } else {
                l = (width - ntmpW) / 2;
                r = width - l;
            }
        }

//		if(mSerial) {
//			l = 10;               //----------org
//			r = width - 10;
//			int ntmpH = (r - l) * 58 / 88;
//			//t = (height-ntmpH)/2;
//			t = 10;
//			b = t + ntmpH;
//		}else{
//			l = (int)(width * 0.2);
//			r =(int)(width * 0.8);
//			int ntmpH = (r - l) * 58 / 66;
//			t = (height - ntmpH) / 2;
//			b = t + ntmpH;
//		}

        if (frame == null) {
            frame = new Rect(l, t, r, b);
        }
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, frame.right+ 310, frame.top, paint);

        canvas.drawRect(0, frame.top, frame.left + 70, frame.bottom , paint);

        canvas.drawRect(frame.right  - ShrinkSize + 100, frame.top, frame.right + 310, frame.bottom ,
                paint);
        canvas.drawRect(0, frame.bottom , frame.right + 310, height, paint);

        paintLine.setColor(frameColor);
        paintLine.setStrokeWidth(5);
        paintLine.setAntiAlias(true);

        int num = (r - l) / 10;
        canvas.drawLine(l - 8 + ShrinkSize + 70, t, l + num + ShrinkSize + 70, t, paintLine);
        canvas.drawLine(l + ShrinkSize + 70, t, l + ShrinkSize + 70, t + num, paintLine);

        canvas.drawLine(r + 8 - ShrinkSize + 100, t, r - num - ShrinkSize + 100, t, paintLine);
        canvas.drawLine(r - ShrinkSize + 100, t, r - ShrinkSize + 100, t + num, paintLine);

        canvas.drawLine(l - 8 + ShrinkSize + 70, b, l + num + ShrinkSize + 70, b, paintLine);
        canvas.drawLine(l + ShrinkSize + 70, b, l + ShrinkSize + 70, b - num, paintLine);

        canvas.drawLine(r + 8 - ShrinkSize + 100, b, r - num - ShrinkSize + 100, b, paintLine);
        canvas.drawLine(r - ShrinkSize + 100, b, r - ShrinkSize + 100, b - num, paintLine);

        paintLine.setColor(laserColor);
        paintLine.setAlpha(100);
        paintLine.setStrokeWidth(3);
        paintLine.setAntiAlias(true);

        canvas.drawLine(l + ShrinkSize + 70, t + num, l + ShrinkSize + 70, b - num, paintLine); //左

        canvas.drawLine(r - ShrinkSize + 100, t + num, r - ShrinkSize + 100, b - num, paintLine);//右

        canvas.drawLine(l + num + ShrinkSize + 70, t, r - num - ShrinkSize + 100, t, paintLine);

        canvas.drawLine(l + num + ShrinkSize + 70, b, r - num - ShrinkSize + 100, b, paintLine);


//	     mText = "请将车牌置于框内";
//	     mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//	     mTextPaint.setStrokeWidth(3);
//	     mTextPaint.setTextSize(50);
//	     mTextPaint.setColor(frameColor);
//	      mTextPaint.setTextAlign(Paint.Align.CENTER);
//	     canvas.drawText(mText,w/2,h/2, mTextPaint);
        if (frame == null) {
            return;
        }

        postInvalidateDelayed(ANIMATION_DELAY);
    }
}
