package com.dkao.realtimecircularprogressbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Real-time android view component that can be used to show a round progress bar.
 * It can be customized with size, stroke size, colors and text etc.
 *
 * Progress change is displayed to the user with as little delay as possible,
 * since there are no animations and UI is drawn separately from the main Android UI thread.
 *
 * Written by:
 * Dennis Kao, June 2018
 *      github.com/dennis-kao
 *
 * Code sources:
 * Kristoffer Matsson, http://kmdev.se, for Canvas, circle and text code
 *      https://github.com/korre/android-circular-progress-bar/blob/master/app/src/main/java/se/kmdev/circularprogressbar/CircularProgressBar.java
 *
 * Chryssa Aliferi, for Thread and SurfaceHolder code
 *      https://examples.javacodegeeks.com/android/core/ui/surfaceview/android-surfaceview-example/
 */
public class RealtimeCircularProgressBar extends SurfaceView implements Runnable {

    //  SurfaceHolder
    private SurfaceHolder surfaceHolder;

    //  Distances and measurements
    private int mViewWidth;
    private int mViewHeight;
    private final float mStartAngle = -90;      // Always start from top (default is: "3 o'clock on a watch.")
    private float mMaxSweepAngle = 360;         // Max degrees to sweep = full circle
    private int mStrokeWidth = 40;              // Width of outline
    private int mMaxProgress = 100;             // Max progress to use
    private int circleGap = 10;
    private int unitTextGap = 120;

    private String unitText = "steps";

    //  Text drawing options
    private boolean mDrawText = true;           // Set to true if progress text should be drawn
    private boolean mRoundedCorners = true;     // Set to true if rounded corners should be applied to outline ends

    //  Colors
    private int mProgressColor = Color.RED;   // Outline color
    private int mTextColor = Color.BLACK;       // Progress text color
    private int backgroundCircleColor = Color.parseColor("#B3d3d3d3");  // 70% transparent grey

    //  Font(s)
    private Typeface font;

    //  mPaint is reused to setup all drawn elements on canvas
    private Paint mPaint;                       // Allocate paint outside onDraw to avoid unnecessary object creation

    //  Thread logic
    private Thread thread = null;
    volatile boolean running = false;

    //  Progress data
    private int count = 0;
    private int progress = 0;

    public RealtimeCircularProgressBar(Context context) {
        this(context, null);
        init(context);
    }

    public RealtimeCircularProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public RealtimeCircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        initMeasurments();
        surfaceHolder = getHolder();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        font = Typeface.createFromAsset(context.getAssets(), "font/robotocondensed_regular.ttf");

        this.setZOrderOnTop(true);
        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    public void onResumeSurfaceView() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void onPauseSurfaceView() {
        running = false;

        try {
            thread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while(running) {

            if(!surfaceHolder.getSurface().isValid()) //  only update UI when steps text change
                continue;

            Canvas canvas = surfaceHolder.lockCanvas();
            onDraw(canvas);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        initMeasurments();
        super.onDraw(canvas);

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //clear the previous frame

        drawBackgroundArc(canvas);
        drawProgressArc(canvas);

        if (mDrawText) {
            drawText(canvas);
            drawUnitText(canvas);
        }
    }

    private void initMeasurments() {
        mViewWidth = getWidth();
        mViewHeight = getHeight();
    }

    private void drawProgressArc(Canvas canvas) {

        final int diameter = Math.min(mViewWidth, mViewHeight);
        final float pad = mStrokeWidth / 2f;
        final RectF outerOval = new RectF(pad, pad, diameter - pad, diameter - pad);

        mPaint.setColor(mProgressColor);
        mPaint.setStrokeWidth(mStrokeWidth - circleGap);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(mRoundedCorners ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(outerOval, mStartAngle, calcSweepAngleFromProgress(progress), false, mPaint);
    }

    private void drawBackgroundArc(Canvas canvas){

        final int diameter = Math.min(mViewWidth, mViewHeight);
        final float pad = mStrokeWidth / 2f;
        final RectF outerOval = new RectF(pad, pad, diameter - pad, diameter - pad);

        mPaint.setColor(backgroundCircleColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(outerOval, mStartAngle, mMaxSweepAngle, false, mPaint);
    }

    private void drawText(Canvas canvas) {
        mPaint.setTextSize(Math.min(mViewWidth, mViewHeight) / 5f);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStrokeWidth(0);
        mPaint.setColor(mTextColor);
        mPaint.setTypeface(font);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2)) ;

        canvas.drawText(Integer.toString(count), xPos, yPos, mPaint);
    }

    private void drawUnitText(Canvas canvas) {
        mPaint.setTextSize(Math.min(mViewWidth, mViewHeight) / 15f);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStrokeWidth(0);
        mPaint.setColor(mTextColor);
        mPaint.setTypeface(font);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // Center text
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2)) + unitTextGap;
        canvas.drawText(unitText, xPos, yPos, mPaint);
    }

    private float calcSweepAngleFromProgress(int progress) {
        return (mMaxSweepAngle / mMaxProgress) * progress;
    }

    private int calcProgressFromSweepAngle(float sweepAngle) {
        return (int) ((sweepAngle * mMaxProgress) / mMaxSweepAngle);
    }

    public void setProgress(int prog, int c) {
        count = c;
        progress = prog;
    }

    public void setProgress(int prog) {
        progress = prog;
    }

    public void setProgressColor(int color) {
        mProgressColor = color;
        invalidate();
    }

    public void setProgressWidth(int width) {
        mStrokeWidth = width;
        invalidate();
    }

    public void setTextColor(int color) {
        mTextColor = color;
        invalidate();
    }

    public void showProgressText(boolean show) {
        mDrawText = show;
        invalidate();
    }

    public void setCircleGap(int g) {
        circleGap = g;
        invalidate();
    }

    public void setNumText(int n) {
        count = n;
        invalidate();
    }

    public void setUnitTextGap(int unitTextGap) {
        this.unitTextGap = unitTextGap;
    }

    public void setUnitText(String unitText) {
        this.unitText = unitText;
    }

    /**
     * Toggle this if you don't want rounded corners on progress bar.
     * Default is true.
     * @param roundedCorners true if you want rounded corners of false otherwise.
     */
    public void useRoundedCorners(boolean roundedCorners) {
        mRoundedCorners = roundedCorners;
        invalidate();
    }
}