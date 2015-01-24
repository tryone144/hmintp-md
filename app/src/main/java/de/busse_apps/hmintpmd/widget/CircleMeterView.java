package de.busse_apps.hmintpmd.widget;

/*
 * Copyright 2015 Bernd Busse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import de.busse_apps.hmintpmd.R;

public class CircleMeterView extends SurfaceView implements SurfaceHolder.Callback {
    
    public static final int MAX_DEGREE = 360;
    
    private SurfaceHolder mHolder;
    private CircleMeterData mData;
    private CircleMeterDrawingThread mThread;
    
    private Rect mDrawingRect = new Rect();
    
    private CircleMeterHandler mCircleMeterHandler;
    private CircleMeterCallback mCallback;
    
    private Bundle mThreadState;
    private boolean isSurfaceCreated;
    
    public CircleMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        if (!isInEditMode()) {
            setZOrderOnTop(true);
        }
        
        mData = new CircleMeterData();
        
        TypedArray styled = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CircleMeterView, 0, 0);
        try {
            mData.degreePerSecond = styled.getInteger(
                    R.styleable.CircleMeterView_degree_per_second, 90);
            mData.arcWidth = styled
                    .getDimensionPixelSize(R.styleable.CircleMeterView_arc_width, 10);
            mData.borderWidth = styled.getDimensionPixelSize(
                    R.styleable.CircleMeterView_border_width, 2);
            mData.colorLow = styled.getColor(R.styleable.CircleMeterView_color_low, getResources()
                    .getColor(R.color.circle_meter_green));
            mData.colorMid = styled.getColor(R.styleable.CircleMeterView_color_mid, getResources()
                    .getColor(R.color.circle_meter_orange));
            mData.colorHi = styled.getColor(R.styleable.CircleMeterView_color_hi, getResources()
                    .getColor(R.color.circle_meter_red));
        } finally {
            styled.recycle();
        }
        mData.maxValue = 0;
        mData.maxDegree = 0;
        
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        
        mCircleMeterHandler = new CircleMeterHandler(this);
        
        mThread = new CircleMeterDrawingThread(mHolder, mCircleMeterHandler, mData);
        
        isSurfaceCreated = false;
    }
    
    public CircleMeterDrawingThread getThread() {
        return mThread;
    }
    
    public void setCallback(Object callback) {
        try {
            mCallback = (CircleMeterCallback) callback;
        } catch (ClassCastException e) {
            throw new ClassCastException(callback.toString()
                    + " must implement CircleMeterCallback!");
        }
    }
    
    public void setDegreePerSecond(int speed) {
        mData.degreePerSecond = speed;
        restartThread();
    }
    
    public int getDegreePerSecond() {
        return mData.degreePerSecond;
    }
    
    public void setArcWidth(int width) {
        mData.arcWidth = width;
        restartThread();
    }
    
    public int getArcWidth() {
        return mData.arcWidth;
    }
    
    public void setBorderWidth(int width) {
        mData.borderWidth = width;
        restartThread();
    }
    
    public int getBorderWidth() {
        return mData.borderWidth;
    }
    
    public void setMaxValue(double value) {
        mData.maxValue = value;
        restartThread();
    }
    
    public double getMaxValue() {
        return mData.maxValue;
    }
    
    public void setMaxDegree(double degree) {
        mData.maxDegree = degree;
        restartThread();
    }
    
    public double getMaxDegree() {
        return mData.maxDegree;
    }
    
    public void setColorLow(int color) {
        mData.colorLow = color;
    }
    
    public int getColorLow() {
        return mData.colorLow;
    }
    
    public void setColorMid(int color) {
        mData.colorMid = color;
    }
    
    public int getColorMid() {
        return mData.colorMid;
    }
    
    public void setColorHi(int color) {
        mData.colorHi = color;
    }
    
    public int getColorHi() {
        return mData.colorHi;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We are allowed to change the view's width
        boolean resizeWidth = false;
        
        // We are allowed to change the view's height
        boolean resizeHeight = false;
        
        int width = Integer.MAX_VALUE;
        int height = Integer.MAX_VALUE;
        
        int widthSize;
        int heightSize;
        
        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        
        resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
        resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
        
        if (resizeWidth || resizeHeight) {
            // We may change at least one Dimension
            
            // Get the max possible width/height given our constraints
            widthSize = resolveSize(Integer.MAX_VALUE, widthMeasureSpec);
            heightSize = resolveSize(Integer.MAX_VALUE, heightMeasureSpec);
            
            // See what our actual aspect ratio is
            float actualAspect = (float) (widthSize - getPaddingLeft() - getPaddingRight())
                    / (heightSize - getPaddingTop() - getPaddingBottom());
            
            if (Math.abs(actualAspect - 1.0f) > 0.0000001) {
                boolean done = false;
                
                // Try adjusting width to be proportional to height
                if (resizeWidth) {
                    int newWidth = heightSize - getPaddingTop() - getPaddingBottom()
                            + getPaddingLeft() + getPaddingRight();
                    // Allow the width to outgrow its original estimate if
                    // height is fixed.
                    if (!resizeHeight) {
                        widthSize = resolveAdjustedSize(newWidth, Integer.MAX_VALUE,
                                widthMeasureSpec);
                    }
                    if (newWidth <= widthSize) {
                        widthSize = newWidth;
                        done = true;
                    }
                }
                // Try adjusting height to be proportional to width
                if (!done && resizeHeight) {
                    int newHeight = widthSize - getPaddingLeft() - getPaddingRight()
                            + getPaddingTop() + getPaddingBottom();
                    // Allow the height to outgrow its original estimate if
                    // width is fixed.
                    if (!resizeWidth) {
                        heightSize = resolveAdjustedSize(newHeight, Integer.MAX_VALUE,
                                heightMeasureSpec);
                    }
                    if (newHeight <= heightSize) {
                        heightSize = newHeight;
                    }
                }
            }
        } else {
            // We are not allowed to change dimensions
            widthSize = resolveSize(width, widthMeasureSpec);
            heightSize = resolveSize(height, heightMeasureSpec);
        }
        
        setMeasuredDimension(widthSize, heightSize);
    }
    
    private void configureBounds() {
        int padding = mData.borderWidth + mData.arcWidth / 2;
        int left = getPaddingLeft() + padding;
        int top = getPaddingTop() + padding;
        int right = getPaddingRight() + padding;
        int bottom = getPaddingBottom() + padding;
        
        int width = getWidth() - left - right;
        int height = getHeight() - top - bottom;
        
        if (width >= height) {
            int diff = (width - height) / 2;
            bottom = height + top;
            right = width + left - diff;
            left = left + diff;
        } else {
            int diff = (height - width) / 2;
            right = width + left;
            bottom = height + top - diff;
            top = top + diff;
        }
        
        mDrawingRect.set(left, top, right, bottom);
    }
    
    private int resolveAdjustedSize(int desiredSize, int maxSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                // Parent says we can be as big as we want. Just don't be larger
                // than max size imposed on ourselves.
                result = Math.min(desiredSize, maxSize);
                break;
            case MeasureSpec.AT_MOST:
                // Parent says we can be as big as we want, up to specSize.
                // Don't be larger than specSize, and don't be larger than
                // the max size imposed on ourselves.
                result = Math.min(Math.min(desiredSize, specSize), maxSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }
    
    private void doDrawingFinished() {
        if (mCallback != null) {
            mCallback.onDrawingFinished();
        }
    }
    
    private void doProgressUpdate(double value, double degree) {
        if (mCallback != null) {
            mCallback.onProgressUpdate(value, degree);
        }
    }
    
    private void restartThread() {
        if (mThread.isRunning()) {
            mThread.pause();
            mThreadState = mThread.saveState(new Bundle());
        }
        
        mThread = new CircleMeterDrawingThread(mHolder, mCircleMeterHandler, mData);
        
        if (mThreadState != null) {
            mThread.restoreState(mThreadState);
        }
        if (isSurfaceCreated) {
            mThread.setRunning(true);
            mThread.start();
        }
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        configureBounds();
        mThread.setDrawingBounds(mDrawingRect);
        mThread.redraw();
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceCreated = true;
        if (mThread.getState() == Thread.State.TERMINATED) {
            mThread = new CircleMeterDrawingThread(mHolder, mCircleMeterHandler, mData);
        }
        if (mThreadState != null) {
            mThread.restoreState(mThreadState);
        }
        
        mThread.setRunning(true);
        mThread.start();
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceCreated = false;
        boolean retry = true;
        mThread.setRunning(false);
        
        mThreadState = mThread.saveState(new Bundle());
        
        while (retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
    
    /**
     * public Interface for callback actions
     */
    public interface CircleMeterCallback {
        public void onDrawingFinished();
        
        public void onProgressUpdate(double value, double degree);
    }

    /**
     * public static Handler for callback actions
     */
    public static class CircleMeterHandler extends Handler {
        public static final String KEY_EVENT = "de.busse_apps.hmintpmd.common.CircleMeterView.CircleMeterHandler.eventType";
        public static final String KEY_VALUE = "de.busse_apps.hmintpmd.common.CircleMeterView.CircleMeterHandler.value";
        public static final String KEY_DEGREE = "de.busse_apps.hmintpmd.common.CircleMeterView.CircleMeterHandler.degree";
        
        public static final int EVENT_FINISHED = 1;
        public static final int EVENT_UPDATE = 2;
        
        private final WeakReference<CircleMeterView> mCircleMeterView;
        
        public CircleMeterHandler(CircleMeterView view) {
            mCircleMeterView = new WeakReference<CircleMeterView>(view);
        }
        
        @Override
        public void handleMessage(Message m) {
            CircleMeterView view = mCircleMeterView.get();
            if (view != null) {
                switch (m.getData().getInt(KEY_EVENT)) {
                    case EVENT_FINISHED:
                        view.doDrawingFinished();
                        break;
                    case EVENT_UPDATE:
                        double value = m.getData().getDouble(KEY_VALUE);
                        double degree = m.getData().getDouble(KEY_DEGREE);
                        view.doProgressUpdate(value, degree);
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    /**
     * public DataHolder for easy use of SavedInstanceState Bundle 
     */
    public class CircleMeterData {
        
        public double maxValue;
        public double maxDegree;
        public int degreePerSecond;
        public int arcWidth;
        public int borderWidth;
        
        public int colorLow;
        public int colorMid;
        public int colorHi;
    }
}
