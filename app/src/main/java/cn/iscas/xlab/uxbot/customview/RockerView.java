/*
 * Copyright 2017 lisongting
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
package cn.iscas.xlab.uxbot.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import cn.iscas.xlab.uxbot.R;


/**
 * Created by lisongting on 2017/9/28.
 * Xbot的遥控器---游戏摇杆式
 *
 */

public class RockerView extends View {
    public static final String TAG = "RockerView";

    private static final int DEFAULT_AREA_SIZE = 400;
    private static final int DEFAULT_ROCKER_RADIUS = DEFAULT_AREA_SIZE / 8;

    private Paint mAreaBackgroundPaint;
    private Paint mRockerPaint;

    private Point mRockerPosition;
    private Point mCenterPoint;

    private int mAreaRadius;
    private int mRockerRadius;

    private OnAngleChangeListener mAngleListener;
    private OnDirectionChangeListener mDirectionListener;

    // 360°平分8份的边缘角度
    private static final double ANGLE_8D_OF_0P = 22.5;
    private static final double ANGLE_8D_OF_1P = 67.5;
    private static final double ANGLE_8D_OF_2P = 112.5;
    private static final double ANGLE_8D_OF_3P = 157.5;
    private static final double ANGLE_8D_OF_4P = 202.5;
    private static final double ANGLE_8D_OF_5P = 247.5;
    private static final double ANGLE_8D_OF_6P = 292.5;
    private static final double ANGLE_8D_OF_7P = 337.5;

    private Bitmap mAreaBitmap;
    private Bitmap mRockerBitmap;

    private int measureWidth, measureHeight;


    //标志当前的摇杆是否可用，如果未连接Ros服务器，则不可用
    private boolean isAvailable = false;

    private Bitmap unavailableBitmap;

    public RockerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttributeSet(context,attrs);

        mAreaBackgroundPaint = new Paint();
        mAreaBackgroundPaint.setAntiAlias(true);

        mRockerPaint = new Paint();
        mRockerPaint.setAntiAlias(true);
        mCenterPoint = new Point();
        mRockerPosition = new Point();



    }

    private void initAttributeSet(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RockerView);

        Drawable areaBackground = typedArray.getDrawable(R.styleable.RockerView_areaBackground);
        if (areaBackground instanceof GradientDrawable) {
            mAreaBitmap = drawableToBitmap(areaBackground);
        }

        Drawable rockerBackground = typedArray.getDrawable(R.styleable.RockerView_rockerBackground);
        if (rockerBackground instanceof GradientDrawable) {
            mRockerBitmap = drawableToBitmap(rockerBackground);
        }


        Drawable unavailableDrawable = typedArray.getDrawable(R.styleable.RockerView_unavailableDrawable);
        if (unavailableDrawable instanceof GradientDrawable) {
            unavailableBitmap = drawableToBitmap(unavailableDrawable);
        }

        mRockerRadius = typedArray.getDimensionPixelSize(R.styleable.RockerView_rockerRadius, DEFAULT_ROCKER_RADIUS);

        typedArray.recycle();

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            //具体值和match_parent
            measureWidth = widthSize;
        } else {
            //wrap_content
            measureWidth = DEFAULT_AREA_SIZE;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            measureHeight = heightSize;
        } else {
            measureHeight = DEFAULT_AREA_SIZE;
        }
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = measureWidth / 2;
        int centerY = measureHeight / 2;

        mCenterPoint.set(centerX, centerY);

        //可移动区域半径
        mAreaRadius = measureWidth <= measureHeight ? centerX : centerY;

        //一开始绘制的时候将摇杆画在中间
        if (0 == mRockerPosition.x || 0 == mRockerPosition.y) {
            mRockerPosition.set(mCenterPoint.x, mCenterPoint.y);
        }
        //画可移动区域
        Rect src = new Rect(0, 0, mAreaBitmap.getWidth(), mAreaBitmap.getHeight());
        Rect dst = new Rect(mCenterPoint.x - mAreaRadius, mCenterPoint.y - mAreaRadius, mCenterPoint.x + mAreaRadius, mCenterPoint.y + mAreaRadius);
        //从原位图src中挖取一块dst
        canvas.drawBitmap(mAreaBitmap, src, dst, mAreaBackgroundPaint);


        if (isAvailable) {
            //画摇杆区域
            Rect srcRocker = new Rect(0, 0, mRockerBitmap.getWidth(), mRockerBitmap.getHeight());
            Rect dstRocker = new Rect(mRockerPosition.x - mRockerRadius, mRockerPosition.y - mRockerRadius, mRockerPosition.x + mRockerRadius, mRockerPosition.y + mRockerRadius);
            canvas.drawBitmap(mRockerBitmap, srcRocker, dstRocker, mRockerPaint);
        } else {
            if (unavailableBitmap == null) {
                Log.e(TAG, "Bitmap is null----------------");
            } else {
                Rect srcRocker = new Rect(0, 0, unavailableBitmap.getWidth(), unavailableBitmap.getHeight());
                Rect dstRocker = new Rect(mRockerPosition.x - mRockerRadius, mRockerPosition.y - mRockerRadius, mRockerPosition.x + mRockerRadius, mRockerPosition.y + mRockerRadius);
                //从原位图src中挖取一块dst
                canvas.drawBitmap(unavailableBitmap, srcRocker, dstRocker, mRockerPaint);
            }

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                callbackStart();
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = motionEvent.getX();
                float moveY = motionEvent.getY();
//                log("当前位置:" + moveX + "," + moveY);
                mRockerPosition = getRockerPositionPoint(mCenterPoint, new Point((int)moveX, (int)moveY), mAreaRadius, mRockerRadius);
                moveRocker(mRockerPosition.x, mRockerPosition.y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                callbackFinish();
                moveRocker(mCenterPoint.x, mCenterPoint.y);
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            default:
                break;

        }
        return true;
    }

    //获取摇杆的实际位置
    //regionRadius  摇杆可活动区域的半径
    //rockerRadius  摇杆的半径
    private Point getRockerPositionPoint(Point centerPoint, Point touchPoint, float regionRadius, float rockerRadius) {
        float lenX = touchPoint.x - centerPoint.x;
        float lenY = touchPoint.y - centerPoint.y;
        float lenXY = (float) Math.sqrt((double) (lenX * lenX+ lenY * lenY));
        //计算弧度,如果触摸点在中心位置以下，则为正弧度，如果在中心位置上面，则为负
        double radian = Math.acos(lenX / lenXY) * (touchPoint.y < centerPoint.y ? -1 : 1);
        //计算角度
        double angle = radianToAngle(radian);
        callback(angle);
//        log("当前角度：" + angle);

        //如果当前触摸位置在可活动范围内，则返回当前位置点
        if (lenXY + rockerRadius < regionRadius) {
            return touchPoint;
        } else {
            //在边界以外
            int showPointX = (int) (centerPoint.x + (regionRadius - rockerRadius) * Math.cos(radian));
            int showPointY = (int) (centerPoint.y + (regionRadius - rockerRadius) * Math.sin(radian));
            return new Point(showPointX, showPointY);
        }
    }

    private void moveRocker(float x, float y) {
        mRockerPosition.set((int)x, (int)y);
//        log("移动位置：(" + x + "," + y + ")");
        invalidate();

    }

    private void callbackStart() {
        if (null != mAngleListener) {
            mAngleListener.onStart();
        }
        if (null != mDirectionListener) {
            mDirectionListener.onStart();
        }
    }

    private void callback(double angle) {
        if (null != mAngleListener) {
            mAngleListener.angle(angle);
        }
        if (null != mDirectionListener) {
            if (0 <= angle && ANGLE_8D_OF_0P > angle || ANGLE_8D_OF_7P <= angle && 360 > angle) {
                //右
                mDirectionListener.onDirectionChange(Direction.DIRECTION_RIGHT);
            } else if (ANGLE_8D_OF_0P <= angle && ANGLE_8D_OF_1P > angle) {
                // 右下
                mDirectionListener.onDirectionChange(Direction.DIRECTION_DOWN_RIGHT);
            } else if (ANGLE_8D_OF_1P <= angle && ANGLE_8D_OF_2P > angle) {
                // 下
                mDirectionListener.onDirectionChange(Direction.DIRECTION_DOWN) ;
            } else if (ANGLE_8D_OF_2P <= angle && ANGLE_8D_OF_3P > angle) {
                // 左下
                mDirectionListener.onDirectionChange(Direction.DIRECTION_DOWN_LEFT);
            } else if (ANGLE_8D_OF_3P <= angle && ANGLE_8D_OF_4P > angle) {
                // 左
                mDirectionListener.onDirectionChange(Direction.DIRECTION_LEFT);
            } else if (ANGLE_8D_OF_4P <= angle && ANGLE_8D_OF_5P > angle) {
                // 左上
                mDirectionListener.onDirectionChange(Direction.DIRECTION_UP_LEFT);
            } else if (ANGLE_8D_OF_5P <= angle && ANGLE_8D_OF_6P > angle) {
                // 上
                mDirectionListener.onDirectionChange(Direction.DIRECTION_UP);
            } else if (ANGLE_8D_OF_6P <= angle && ANGLE_8D_OF_7P > angle) {
                // 右上
                mDirectionListener.onDirectionChange(Direction.DIRECTION_UP_RIGHT);
            }
        }
    }

    private void callbackFinish() {
        if (null != mAngleListener) {
            mAngleListener.onFinish();
        }
        if (null != mDirectionListener) {
            mDirectionListener.onFinish();
        }
    }

    public void setOnDirectionChangeListener(OnDirectionChangeListener listener) {
        this.mDirectionListener = listener;
    }

    public void setAvailable(boolean available) {
        if (isAvailable != available) {
            this.isAvailable = available;
            invalidate();
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;

        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private double radianToAngle(double radian) {
        double tmp = Math.round(radian / Math.PI * 180);
        return tmp >= 0 ? tmp : tmp + 360;
    }

    private void log(String str) {
        Log.i(TAG, str);
    }

    public enum Direction{
        DIRECTION_LEFT,
        DIRECTION_RIGHT,
        DIRECTION_UP,
        DIRECTION_DOWN,
        DIRECTION_UP_LEFT,
        DIRECTION_UP_RIGHT,
        DIRECTION_DOWN_LEFT,
        DIRECTION_DOWN_RIGHT,
        DIRECTION_CENTER
    }

    public interface OnAngleChangeListener {
        void onStart();

        void angle(double angle);

        void onFinish();
    }

    public interface OnDirectionChangeListener{
        void onStart();

        void onDirectionChange(Direction direction);

        void onFinish();
    }

}
