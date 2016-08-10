package com.jacky.myrulelayout;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import android.widget.Scroller;

/**
 * Created by lhm05 on 2016/08/10.
 */
public class ScaleBar extends View {
    private Scroller scroller;
    private int tempScale;   //滑動方向
    private int screenMidCountScale; //中間刻度
    private int screenWidth;
    private Context context;
    private Rect rect;
    private int max=100;
    private int totalCountScale;
    private int scaleMargin = 15; //間距
    private int scaleHeight = 20; //高度
    private int mScaleMaxHeight = scaleHeight*3; //整數高度
    private int mMiddleHeight = scaleHeight*2; //整數高度
    private int rectWidth ; //總宽度
    private int rectHeight = 100; //高度
    private int mScrollLastX;
    private OnScrollListener onScrollListener;


    public ScaleBar(Context context, AttributeSet attrs) {
        this(context,attrs,-1);

        int count = attrs.getAttributeCount();
        TypedArray ta=context.obtainStyledAttributes(attrs,R.styleable.ScaleAttrs);
        for(int i=0;i<ta.getIndexCount();i++)
        {
            int attr = ta.getIndex(i);
            String title = "";

            switch (attr)
            {
                case R.styleable.ScaleAttrs_maxvalue:
                    max=ta.getInt(attr,0);
                    break;
            }

        }
        rectWidth= max * scaleMargin;
    }


    //. You can use scrollers  to collect the data you need to produce
    // a scrolling animation—for example, in response to a fling gesture.
    public ScaleBar(Context context,AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        this.context=context;
        screenWidth = getPhoneW(context);
        tempScale = screenWidth/scaleMargin/2; //判断滑动方向
        screenMidCountScale = screenWidth/scaleMargin/2; //中间刻度
        scroller = new Scroller(context);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(rectWidth, rectHeight);
        this.setLayoutParams(lp);
        rect = new Rect(0, 0, rectWidth, rectHeight);

        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(rect, paint);
        onDrawScale(canvas); //畫刻度
        onDrawPointer(canvas); //画指针

    }


    //畫刻度
    private void onDrawScale(Canvas canvas){
        if(canvas == null) return;
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setTextAlign(Paint.Align.CENTER); //文字居中
        paint.setTextSize(20);
        for(int i=0; i<max; i++){
            if(i!=0 && i!=max){
                if(i%10==0){ //整值
                    canvas.drawLine(i*scaleMargin, rectHeight, i*scaleMargin, rectHeight-mScaleMaxHeight, paint);
                    //整值文字
                    canvas.drawText(String.valueOf(i), i*scaleMargin, rectHeight-mScaleMaxHeight-10, paint);
                } else if(i%5==0) {
                    canvas.drawLine(i*scaleMargin, rectHeight, i*scaleMargin,rectHeight-mMiddleHeight, paint);
                    //整值文字
                    canvas.drawText(String.valueOf(i), i*scaleMargin, rectHeight-mMiddleHeight-10, paint);}
                else {
                    canvas.drawLine(i*scaleMargin, rectHeight, i*scaleMargin, rectHeight-scaleHeight, paint);
                }
            }
        }
    }


    /**
     * 指针
     * */
    private void onDrawPointer(Canvas canvas){
        if(canvas == null) return;
        Paint mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(20);
        //每一屏幕刻度的個數/2
        int countScale = screenWidth/scaleMargin/2;
        //根据滑动的距离，计算指针的位置【指针始终位于屏幕中间】
        int finalX =  scroller.getFinalX();
        //滑动的刻度
        int tmpCountScale = (int) Math.rint((double)finalX/(double)scaleMargin); //四舍五入取整
        //总刻度

        totalCountScale = tmpCountScale+countScale;


        if(onScrollListener!=null){ //回呼方法
            onScrollListener.onScrollScale(totalCountScale);
        }
        canvas.drawLine(countScale * scaleMargin + finalX, rectHeight,
                countScale * scaleMargin + finalX, rectHeight - mScaleMaxHeight, mPaint);

        canvas.drawText(String.valueOf(totalCountScale), countScale * scaleMargin + finalX
                , rectHeight - mScaleMaxHeight - 10, mPaint);
    }





    @Override
    public void computeScroll() {
        super.computeScroll();
        if(scroller!=null) {
            if (scroller.computeScrollOffset()) {
                scrollTo(scroller.getCurrX(), 0);
                postInvalidate();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(scroller != null && !scroller.isFinished()){
                    scroller.abortAnimation();
                }
                mScrollLastX = x;
                return true;
            case MotionEvent.ACTION_MOVE:
                int dataX = mScrollLastX - x;
                if(totalCountScale-tempScale<0){ //向右边滑动
                    if(totalCountScale<0) {
                        if(dataX<0) //禁止继续向右滑动
                            return super.onTouchEvent(event);
                    }
                } else if(totalCountScale-tempScale>0){ //向左边滑动
                    if (totalCountScale>max) {
                        if(dataX>0) //禁止继续向左滑动
                            return super.onTouchEvent(event);
                    }
                }
                 smoothScrollBy(dataX, 0);
                mScrollLastX = x;
                postInvalidate();
                tempScale = totalCountScale;
                return true;
            case MotionEvent.ACTION_UP:
                if(totalCountScale<0) totalCountScale=0;
                if(totalCountScale>max) totalCountScale=max;
                int finalX = (totalCountScale-screenMidCountScale) * scaleMargin;
                scroller.setFinalX(finalX); //纠正指针位置
                postInvalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }


    public void smoothScrollBy(int dx, int dy){
        scroller.startScroll(scroller.getFinalX(), scroller.getFinalY(), dx, dy);
    }

    public void smoothScrollTo(int fx, int fy){
        int dx = fx - scroller.getFinalX();
        int dy = fy - scroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }




    public interface OnScrollListener{
        void onScrollScale(int scale);
    }

    // 取得螢幕寬
    private static int  getPhoneW(Context context) {
        DisplayMetrics dm=new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int disW=dm.widthPixels;
        return disW;
    }


}
