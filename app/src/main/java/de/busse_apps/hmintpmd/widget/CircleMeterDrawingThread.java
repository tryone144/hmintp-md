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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Message;
import android.view.SurfaceHolder;

import de.busse_apps.hmintpmd.widget.CircleMeterView.CircleMeterData;
import de.busse_apps.hmintpmd.widget.CircleMeterView.CircleMeterHandler;

public class CircleMeterDrawingThread extends Thread {

    private static final String SIS_CURRENT_DEGREE = "de.busse_apps.hmintpmd.widget.CircleMeterDrawingThread.mCurDegree";
    private static final String SIS_CURRENT_VALUE = "de.busse_apps.hmintpmd.widget.CircleMeterDrawingThread.mCurValue";

    private double mMaxValue;
    private double mCurValue;

    private double mMaxDegree;
    private double mCurDegree;

    private int mDegreePerSecond;

    private long mLastTime;

    private boolean mRun = false;

    private SurfaceHolder mHolder;
    private CircleMeterHandler mHandler;

    private int mArcWidth;
    private int mBorderWidth;

    private int mColorLow;
    private int mColorMid;
    private int mColorHi;

    private Paint mPaintEmpty;
    private Paint mPaintGraph;
    private Paint mPaintBorder;

    private RectF mDrawingBounds;

    public CircleMeterDrawingThread(SurfaceHolder holder, CircleMeterHandler handler, CircleMeterData data) {
        mHolder = holder;
        mHandler = handler;
        mDrawingBounds = new RectF();

        mArcWidth = data.arcWidth;
        mBorderWidth = data.borderWidth;
        mMaxValue = data.maxValue;
        mMaxDegree = data.maxDegree;
        mDegreePerSecond = data.degreePerSecond;

        mColorLow = data.colorLow;
        mColorMid = data.colorMid;
        mColorHi = data.colorHi;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);

        mPaintBorder = new Paint(paint);
        mPaintBorder.setStrokeWidth(mArcWidth + mBorderWidth * 2);
        mPaintBorder.setARGB(255, 0, 0, 0);

        paint.setStrokeWidth(mArcWidth);

        mPaintEmpty = new Paint(paint);
        mPaintEmpty.setARGB(255, 255, 255, 255);

        mPaintGraph = new Paint(paint);
        mPaintGraph.setColor(mColorLow);
    }

    public boolean isRunning() {
        return mRun;
    }

    public void setRunning(boolean running) {
        mRun = running;
    }

    public synchronized void restoreState(Bundle savedState) {
        synchronized (mHolder) {
            mCurDegree = savedState.getDouble(SIS_CURRENT_DEGREE);
            mCurValue = savedState.getDouble(SIS_CURRENT_VALUE);
        }
    }

    public void complete() {
        mCurDegree = mMaxDegree;
        mCurValue = mMaxValue;
    }

    @Override
    public void run() {
        mLastTime = System.currentTimeMillis() + 200;
        while (mRun) {
            Canvas canvas = null;
            try {
                canvas = mHolder.lockCanvas(null);
                synchronized (mHolder) {
                    doAnimate();
                    doUpdate();
                    doDraw(canvas);
                }
            } catch (NullPointerException ignored) {
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (canvas != null) {
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public Bundle saveState(Bundle map) {
        synchronized (mHolder) {
            if (map != null) {
                map.putDouble(SIS_CURRENT_DEGREE, mCurDegree);
                map.putDouble(SIS_CURRENT_VALUE, mCurValue);
            }
        }
        return map;
    }

    public void pause() {
        setRunning(false);
        //clearCanvas();
    }

    public void setDrawingBounds(Rect bounds) {
        // synchronized to make sure these all change atomically
        synchronized (mHolder) {
            mDrawingBounds.set(bounds);
        }
    }

// --Commented out by Inspection START (24.01.15 20:43):
//    private void clearCanvas() {
//        Canvas canvas = null;
//        try {
//            canvas = mHolder.lockCanvas(null);
//            synchronized (mHolder) {
//                canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
//            }
//        } catch (NullPointerException e) {
//        	// ignore
//        } finally {
//            // do this in a finally so that if an exception is thrown
//        	// during the above, we don't leave the Surface in an
//        	// inconsistent state
//        	if (canvas != null) {
//            	mHolder.unlockCanvasAndPost(canvas);
//        	}
//        }
//    }
// --Commented out by Inspection STOP (24.01.15 20:43)

    private void doUpdate() {
        if (mMaxDegree == 0.0) {
            mCurValue = 0.0;
            postProgressUpdate();
            return;
        }
        mCurValue = mMaxValue * (mCurDegree / mMaxDegree);
        postProgressUpdate();
    }

    private void doAnimate() {
        long now = System.currentTimeMillis();

        if (mLastTime > now) {
            return;
        }

        double elapsed = (now - mLastTime) / 1000.0;

        mCurDegree += mDegreePerSecond * elapsed;
        mLastTime = now;

        if (mCurDegree > mMaxDegree) {
            mCurDegree = mMaxDegree;
            doUpdate();
            postDrawingFinished();
            return;
        }

        if (mCurDegree > 360) {
            mCurDegree = 360;
            postDrawingFinished();
        }

    }

    private void doDraw(Canvas canvas) {
        float curDegree = (float) mCurDegree;

        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

        canvas.drawArc(mDrawingBounds, 0, 360, false, mPaintBorder);
        canvas.drawArc(mDrawingBounds, 0, 360, false, mPaintEmpty);

        mPaintGraph.setColor(mColorLow);
        canvas.drawArc(mDrawingBounds, -90, curDegree, false, mPaintGraph);
        mPaintGraph.setColor(mColorMid);
        canvas.drawArc(mDrawingBounds, 30, curDegree - 120 < 0 ? 0 : curDegree - 120, false, mPaintGraph);
        mPaintGraph.setColor(mColorHi);
        canvas.drawArc(mDrawingBounds, 150, curDegree - 240 < 0 ? 0 : curDegree - 240, false, mPaintGraph);
    }

    public void redraw() {
        if (!mRun) {
            Canvas canvas = null;
            try {
                canvas = mHolder.lockCanvas(null);
                synchronized (mHolder) {
                    doDraw(canvas);
                }
            } catch (NullPointerException e) {
                // ignore
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (canvas != null) {
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void postProgressUpdate() {
        Message msg = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt(CircleMeterHandler.KEY_EVENT, CircleMeterHandler.EVENT_UPDATE);
        b.putDouble(CircleMeterHandler.KEY_VALUE, mCurValue);
        b.putDouble(CircleMeterHandler.KEY_DEGREE, mCurDegree);
        msg.setData(b);
        mHandler.sendMessage(msg);
    }

    private void postDrawingFinished() {
        setRunning(false);

        Message msg = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt(CircleMeterHandler.KEY_EVENT, CircleMeterHandler.EVENT_FINISHED);
        msg.setData(b);
        mHandler.sendMessage(msg);
    }
}
