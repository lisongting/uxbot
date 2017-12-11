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

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import cn.iscas.xlab.uxbot.R;


/**
 * 一个用来显示电量多少的水波圆形控件
 * Created by lisongting on 2017/11/14.
 */

public class PercentCircleView extends View {

    private int percent = 66;
    private int radius;
    private int textSize;
    private int textColor;
    private int strokeSize;
    private int strokeColor;
    private Paint strokePaint;
    private Paint textPaint;

    //该圆形控件的圆心点
    private float centerX;
    private float centerY;

    //电量的三种状态颜色
    private int colorBatteryNormal,colorBatteryLow,colorBatteryVeryLow;
    
    private Path bezierPath;
    private Paint bezierPaint;

    //贝塞尔曲线与圆形边界的左交接点位置
    private float bezierStartX , bezierStartY;
    //贝塞尔曲线起始点的偏移量，用于动画效果
    private float bezierStartShift = 0;
    float peakHeight=0;
    float peakWidth =0;
    //用于裁剪的path
    private Path clipPath;
    private String batteryText,batteryPercent;
    private Rect batteryTextBounds,batteryPercentBounds;

    private ValueAnimator valueAnimator;
    public PercentCircleView(Context context) {
        this(context, null);
    }

    public PercentCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PercentCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PercentCircleView, 0, 0);
        for (int i = 0; i < array.getIndexCount(); i++) {
            int index = array.getIndex(i);
            switch (index) {
                case R.styleable.PercentCircleView_radius:
                    radius = array.getDimensionPixelSize(index, 200);
                    break;
                case R.styleable.PercentCircleView_normal_state_color:
                    colorBatteryNormal = array.getColor(index, Color.GREEN);
                    break;
                case R.styleable.PercentCircleView_mid_state_color:
                    colorBatteryLow = array.getColor(index, Color.parseColor("#ff8f00"));
                    break;
                case R.styleable.PercentCircleView_low_state_color:
                    colorBatteryVeryLow = array.getColor(index, Color.RED);
                    break;
                case R.styleable.PercentCircleView_stroke_size:
                    strokeSize = array.getDimensionPixelSize(index, 4);
                    break;
                case R.styleable.PercentCircleView_stroke_color:
                    strokeColor = array.getColor(index, Color.GREEN);
                    break;
                case R.styleable.PercentCircleView_text_size:
                    textSize = array.getDimensionPixelSize(index, 15);
                    break;
                case R.styleable.PercentCircleView_text_color:
                    textColor = array.getColor(index, Color.GREEN);
                    break;
                default:
                    break;
            }
        }
        batteryText = context.getString(R.string.battery_text);

        initializePaint();

        array.recycle();

    }

    private void initializePaint() {

        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint();
        strokePaint.setColor(strokeColor);
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeSize);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);

        bezierPaint = new Paint();
        bezierPaint.setStyle(Paint.Style.FILL);
        bezierPaint.setAntiAlias(true);

        bezierPath = new Path();
        clipPath = new Path();
        batteryTextBounds = new Rect();
        batteryPercentBounds = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0;
        int height = 0;

        if (widthMode == MeasureSpec.AT_MOST) {
//            int wantSize  = (radius + strokeSize) * 2 < widthSize ? (radius + strokeSize) * 2 : widthSize / 2;
//            width = wantSize < widthSize ? wantSize : widthSize;
            width = (radius + strokeSize*5 ) * 2;
        } else if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
//            int wantSize = (radius + strokeSize) * 2 ;
//            height = wantSize < heightSize ? wantSize : heightSize;
            height = (radius + strokeSize*5) * 2 ;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        }

        //将width 和height 统一设置为更小的
//        height = width < height ? width : height;
        setMeasuredDimension(width, height);
        log("onMeasuredDimension:" + width + "x" + height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        centerX = getMeasuredWidth() / 2F;
        centerY = getMeasuredHeight() / 2F;

        canvas.save();
        clipPath.reset();
        canvas.clipPath(clipPath);
        clipPath.addCircle(centerX, centerY, radius+strokeSize/2F, Path.Direction.CCW);
        canvas.clipPath(clipPath, Region.Op.REPLACE);

        if (percent <= 10) {
            bezierPaint.setColor(colorBatteryVeryLow);
        } else if (percent <= 30) {
            bezierPaint.setColor(colorBatteryLow);
        }else{
            bezierPaint.setColor(colorBatteryNormal);
        }

        //使用贝塞尔曲线绘制水波图案
        if (percent>= 0 && percent < 50) {
            float distanceToCenter ;
            if (percent == 0) {
                //如果等于0%则按照1%来显示
                distanceToCenter = 2F*radius*(50 - 1)/100F  ;
            } else {
                distanceToCenter = 2F*radius*(50 - percent)/100F  ;
            }
            double angle = Math.acos(distanceToCenter / radius);
            peakWidth = (float) (radius * Math.sin(angle) / 2);
            peakHeight = (radius-distanceToCenter)/4;
            bezierStartX = centerX - (4F * peakWidth) + bezierStartShift;
            bezierStartY = centerY + distanceToCenter;
        } else if (percent > 50 && percent <= 100) {
            float distanceToCenter;
            //如果等于100则按照99来显示
            if (percent == 100) {
                distanceToCenter = 2 * radius * (99 - 50) / 100F;
            } else {
                distanceToCenter = 2 * radius * (percent - 50) / 100F;
            }
            double angle = Math.acos(distanceToCenter / radius);
            peakWidth = (float) (radius * Math.sin(angle) / 2F);
            peakHeight = (radius-distanceToCenter)/4F;
            bezierStartX = centerX - 4F * peakWidth + bezierStartShift;
            bezierStartY = centerY - distanceToCenter;
        }else{
            //50%
            peakWidth = radius / 2F;
            peakHeight = (radius)/4F;
            bezierStartX = centerX - 4F * peakWidth + bezierStartShift;
            bezierStartY = centerY ;
        }

        //bezierStartX和bezierStartY是贝塞尔曲线绘制的起始坐标
        bezierPath.reset();
        bezierPath.moveTo(bezierStartX, bezierStartY);
        //画n组曲线，一组曲线为一个波峰和一个波谷
        int n = 3;
        for(int i=0;i<n;i++) {
            float pAx = bezierStartX + peakWidth*i*2F;
            float pAy = bezierStartY;

            float cABx = pAx + peakWidth / 2F;
            float cABy = pAy - peakHeight;

            float pBx = pAx + peakWidth;
            float pBy = pAy;

            float cBCx = pBx + peakWidth / 2F;
            float cBCy = pBy + peakHeight;

            float pCx = pAx + peakWidth*2F;
            float pCy = pAy;

            bezierPath.quadTo(cABx, cABy, pBx, pBy);
            bezierPath.quadTo(cBCx,cBCy,pCx,pCy);
            //如果画到最后一组曲线，则将这个曲线的围成的一个图形区域进行封闭
            if (i == n-1 ) {
                bezierPath.lineTo(pCx+getMeasuredWidth(),pCy);
                bezierPath.lineTo(pCx+getMeasuredWidth(),pCy+(float)getMeasuredHeight());
                bezierPath.lineTo(pAx - getMeasuredWidth()-4F*peakWidth, pAy + (float) getMeasuredHeight());
                bezierPath.lineTo(pAx - getMeasuredWidth()-4F*peakWidth, pAy);
            }
        }

        canvas.drawPath(bezierPath,bezierPaint);

        canvas.restore();
        //绘制电量百分比
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        batteryPercent = percent + "%";
        textPaint.getTextBounds(batteryPercent, 0, batteryPercent.length(), batteryPercentBounds);
        canvas.drawText(batteryPercent, centerX - batteryPercentBounds.width() / 2,centerY+batteryPercentBounds.height()/2, textPaint);
        //绘制文字："电量"
        textPaint.setTextSize(42);
        textPaint.setColor(Color.parseColor("#333333"));
        textPaint.getTextBounds(batteryText, 0, batteryText.length(), batteryTextBounds);
        canvas.drawText(batteryText, centerX - batteryTextBounds.width() / 2, centerY + batteryPercentBounds.height() + batteryTextBounds.height() , textPaint);
        canvas.drawCircle(centerX, centerY, radius+strokeSize*2, strokePaint);

    }

    public void startAnim() {
        bezierStartShift = 0F;

        valueAnimator = ValueAnimator.ofFloat(0, peakWidth*2F);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                bezierStartShift = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setDuration(3000);
        valueAnimator.start();
    }

    public void stopAnimation() {
        if (valueAnimator != null) {
            valueAnimator.pause();
            valueAnimator.cancel();
        }
    }

    public void setPercent(int p) {
        if (p < 0) {
            percent = 0;
        } else if (p > 100) {
            percent = 100;
        } else {
            this.percent = p;
        }

        //这里解决了一个隐蔽的问题，当更新了percent后，valueAnimator使用的还是原来的peakWidth
        //动画之间切换的时候就会出现有细微的停顿感和不衔接
        //下面的代码：每次开启动画时重新计算peakWidth，这样就保证每次创建的valueAnimator都会使得动画完整衔接
        if (percent>= 0 && percent < 50) {
            float distanceToCenter ;
            if (percent == 0) {
                //如果等于0%则按照1%来显示
                distanceToCenter = 2F*radius*(50 - 1)/100F  ;
            } else {
                distanceToCenter = 2F*radius*(50 - percent)/100F  ;
            }
            double angle = Math.acos(distanceToCenter / radius);
            peakWidth = (float) (radius * Math.sin(angle) / 2);
        } else if (percent > 50 && percent <= 100) {
            float distanceToCenter;
            //如果等于100则按照99来显示
            if (percent == 100) {
                distanceToCenter = 2 * radius * (99 - 50) / 100F;
            } else {
                distanceToCenter = 2 * radius * (percent - 50) / 100F;
            }
            double angle = Math.acos(distanceToCenter / radius);
            peakWidth = (float) (radius * Math.sin(angle) / 2F);
        }else{
            //50%
            peakWidth = radius / 2F;
        }
        startAnim();
    }

    public int getPercent() {
        return percent;
    }

    private void log(String s) {
        Log.i("tag","PercentCircleView -- "+ s);
    }

}
